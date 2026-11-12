package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Фоновий CSV-імпорт реєстру транспортних засобів.
 *
 * <p>Імпорт реалізовано у вигляді JavaFX {@link Task}, щоб довгий час
 * обробки файлу не блокував інтерфейс. Прогрес імпорту відображається
 * у {@code ProgressBar}: значення {@code progress} — частка оброблених рядків
 * (0..1), значення {@code message} — поточний стан імпорту.</p>
 *
 * <p>Розбір рядків CSV виконує {@link VehicleCsvReader} — послідовний
 * читач, який не залежить від JavaFX. Фоновий режим додає лише потік
 * виконання та відображення прогресу.</p>
 *
 * <p>Послідовність одного рядка:</p>
 * <pre>
 *   CSV -> парсинг -> клієнтська модель -> HttpTransportClient -> POST -> Servlet
 *        -> Service -> DAO -> MySQL
 * </pre>
 *
 * <p>Клієнт не має прямого JDBC-доступу: усі записи йдуть через HTTP.</p>
 */
public class ImportVehiclesTask extends Task<Integer> {

    private final HttpTransportClient client;
    private final VehicleCsvReader reader = new VehicleCsvReader();

    public ImportVehiclesTask(HttpTransportClient client) {
        this.client = client;
    }

    /**
     * Виконує імпорт у фоновому потоці.
     *
     * <p>Імпорт є ідемпотентним: транспортні засоби, код яких уже є
     * в реєстрі на сервері, пропускаються (їх кількість враховується
     * окремо). Такий підхід дозволяє безпечно повторювати імпорт
     * одного й того самого CSV-файлу.</p>
     *
     * @return кількість успішно імпортованих транспортних засобів
     */
    @Override
    protected Integer call() throws Exception {
        List<Vehicle> vehicles = reader.readAll(VehicleCsvReader.CSV_PATH);
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
     * Послідовний (без JavaFX Task) імпорт одного рядка CSV-файлу.
     *
     * <p>Використовується для початкового сценарію імпорту та ручної
     * перевірки: рядок читається з файлу, перетворюється на модель
     * і передається серверу через HTTP.</p>
     *
     * @param csvPath шлях до CSV-файлу
     * @return створений на сервері транспортний засіб
     */
    public Vehicle importFirstRow(Path csvPath) throws Exception {
        Vehicle vehicle = reader.readFirstRow(csvPath)
                .orElseThrow(() -> new IllegalStateException(
                        "CSV не містить коректних рядків: " + csvPath));
        return client.addVehicle(vehicle);
    }
}
