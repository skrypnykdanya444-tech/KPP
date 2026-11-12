package ua.edu.nupp.kpp.client;

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
 * Послідовне читання CSV-файлу реєстру транспортних засобів.
 *
 * <p>Це початковий (послідовний) сценарій CSV-імпорту Етапу III:
 * файл читається рядок за рядком, заголовок пропускається, кожен рядок
 * розбивається на поля та перетворюється на клієнтську модель
 * {@link Vehicle}. Подальше модель передається серверу через
 * {@link HttpTransportClient}.</p>
 *
 * <p>На цьому кроці імпорт виконується послідовно, без JavaFX Task.
 * Фоновий імпорт із відображенням прогресу реалізується на наступному
 * етапі ({@link ImportVehiclesTask}).</p>
 *
 * <p>CSV не містить прямого JDBC-доступу: усі записи йдуть через HTTP.</p>
 */
public class VehicleCsvReader {

    /** Відносний шлях до CSV-файлу реєстру транспортних засобів. */
    public static final Path CSV_PATH = Path.of("sample-data", "vehicles.csv");

    /** CSV-файл у ресурсах клієнтського застосунку (резервний варіант). */
    private static final String RESOURCE_PATH = "/sample-data/vehicles.csv";

    /** Роздільник полів у CSV-файлі. */
    private static final String DELIMITER = ";";

    /** Очікувана кількість стовпців: code;name;category. */
    private static final int EXPECTED_COLUMNS = 3;

    /**
     * Читає перший коректний рядок CSV-файлу як клієнтську модель.
     *
     * <p>Заголовок ігнорується. Рядки з неправильною кількістю стовпців
     * та з помилками перетворення типів пропускаються.</p>
     *
     * @param csvPath шлях до CSV-файлу
     * @return модель транспортного засобу або {@code Optional.empty()}
     */
    public Optional<Vehicle> readFirstRow(Path csvPath) throws IOException {
        for (String line : readCsvLines(csvPath)) {
            if (isHeader(line)) {
                continue;
            }
            Optional<Vehicle> parsed = parseRow(line);
            if (parsed.isPresent()) {
                return parsed;
            }
        }
        return Optional.empty();
    }

    /**
     * Читає всі коректні рядки CSV-файлу як клієнтські моделі.
     *
     * @param csvPath шлях до CSV-файлу
     * @return список моделей транспортних засобів
     */
    public List<Vehicle> readAll(Path csvPath) throws IOException {
        List<Vehicle> vehicles = new ArrayList<>();
        boolean first = true;
        for (String line : readCsvLines(csvPath)) {
            if (first) {
                first = false;
                continue; // пропускаємо заголовок
            }
            parseRow(line).ifPresent(vehicles::add);
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

    /**
     * Читає рядки CSV-файлу: спершу з файлової системи, а якщо файл не
     * знайдено — з ресурсів застосунку.
     */
    private List<String> readCsvLines(Path csvPath) throws IOException {
        if (csvPath != null && Files.exists(csvPath)) {
            try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
                return reader.lines().toList();
            }
        }

        try (InputStream in = VehicleCsvReader.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                throw new IOException("CSV-файл не знайдено: " + CSV_PATH.toAbsolutePath());
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().toList();
            }
        }
    }

    /** Перевіряє, чи рядок є заголовком таблиці. */
    private boolean isHeader(String line) {
        return line != null && line.trim().toLowerCase().startsWith("code");
    }
}
