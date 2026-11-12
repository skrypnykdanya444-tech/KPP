package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Фоновий CSV-імпорт реєстру транспортних засобів.
 *
 * <p>Імпорт реалізовано у вигляді JavaFX {@link Task}, щоб довгий час
 * обробки файлу не блокував інтерфейс. Прогрес імпорту відображається
 * у {@code ProgressBar}: значення {@code progress} — частка оброблених рядків
 * (0..1), значення {@code message} — поточний стан імпорту.</p>
 *
 * <p>Послідовність одного рядка:</p>
 * <pre>
 *   CSV -> парсинг -> клієнтська модель -> HttpClient -> POST -> Servlet
 *        -> Service -> DAO -> MySQL
 * </pre>
 *
 * <p>Клієнт не має прямого JDBC-доступу: усі записи йдуть через HTTP.</p>
 */
public class ImportVehiclesTask extends Task<Integer> {

    /** Шлях до CSV-файлу реєстру транспортних засобів відносно каталогу клієнта. */
    public static final Path CSV_PATH = Path.of("sample-data", "vehicles.csv");

    /** CSV-файл у ресурсах клієнтського застосунку (резервний варіант). */
    private static final String RESOURCE_PATH = "/sample-data/vehicles.csv";

    /** Роздільник полів у CSV-файлі. */
    private static final String DELIMITER = ";";

    /** Очікувана кількість стовпців: code;name;category. */
    private static final int EXPECTED_COLUMNS = 3;

    private final HttpClient client;

    public ImportVehiclesTask(HttpClient client) {
        this.client = client;
    }

    // =====================================================================
    // Основна логіка Task
    // =====================================================================

    /**
     * Виконує імпорт у фоновому потоці.
     *
     * <p>Імпорт є ідемпотентним: транспортні засоби, код яких уже є
     * у реєстрі на сервері, пропускаються (їх кількість враховується
     * окремо). Такий підхід дозволяє безпечно повторювати імпорт
     * одного й того самого CSV-файлу.</p>
     *
     * @return кількість успішно імпортованих транспортних засобів
     */
    @Override
    protected Integer call() throws Exception {
        List<Vehicle> vehicles = parseAll(readCsvLines());
        int total = vehicles.size();
        int imported = 0;
        int skipped = 0;

        // Коди, які вже є в реєстрі на сервері
        java.util.Set<String> existingCodes = new java.util.HashSet<>();
        for (Vehicle v : client.getVehicles()) {
            if (v.getCode() != null) {
                existingCodes.add(v.getCode());
            }
        }

        int processed = 0;
        updateMessage("Імпортовано 0 з " + total);
        for (Vehicle vehicle : vehicles) {
            processed++;
            updateMessage("Імпорт: " + vehicle.getCode() + " (" + processed + " з " + total + ")");

            if (existingCodes.contains(vehicle.getCode())) {
                skipped++;
            } else {
                client.addVehicle(vehicle);
                existingCodes.add(vehicle.getCode());
                imported++;
            }
            updateProgress(processed, total);
        }

        updateMessage("Імпорт завершено: додано " + imported + ", пропущено " + skipped
                + " (уже існували) з " + total);
        return imported;
    }

    /**
     * Читає рядки CSV-файлу: спершу з файлової системи, а якщо файл не
     * знайдено — з ресурсів застосунку.
     */
    private List<String> readCsvLines() throws IOException {
        if (Files.exists(CSV_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CSV_PATH, StandardCharsets.UTF_8)) {
                return reader.lines().toList();
            }
        }

        try (InputStream in = ImportVehiclesTask.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                throw new IOException("CSV-файл не знайдено: " + CSV_PATH.toAbsolutePath());
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().toList();
            }
        }
    }

    // =====================================================================
    // Розбір рядків CSV
    // =====================================================================

    /**
     * Перетворює всі рядки CSV (без заголовка) на клієнтські моделі.
     * Рядки з неправильною кількістю стовпців пропускаються.
     */
    private List<Vehicle> parseAll(List<String> lines) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            parseRow(lines.get(i)).ifPresent(vehicles::add);
        }
        return vehicles;
    }

    /**
     * Розділяє один рядок на поля та створює модель {@link Vehicle}.
     *
     * @return модель транспортного засобу або {@code Optional.empty()},
     * якщо рядок некоректний
     */
    public Optional<Vehicle> parseRow(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        String[] parts = line.split(DELIMITER, -1);

        if (parts.length != EXPECTED_COLUMNS) {
            System.err.println("CSV: пропущено рядок — очікувалося " + EXPECTED_COLUMNS
                    + " стовпців, отримано " + parts.length + ": " + line);
            return Optional.empty();
        }

        String code = parts[0].trim();
        String name = parts[1].trim();
        String category = parts[2].trim();

        if (code.isEmpty() || name.isEmpty() || category.isEmpty()) {
            System.err.println("CSV: пропущено рядок — порожнє обов'язкове поле: " + line);
            return Optional.empty();
        }

        // id не імпортується: його формує MySQL через AUTO_INCREMENT
        return Optional.of(new Vehicle(null, code, name, category));
    }
}
