package ua.edu.nupp.kpp.client;

import java.nio.file.Path;
import java.util.List;

/**
 * Початковий послідовний сценарій CSV-імпорту реєстру транспортних засобів.
 *
 * <p>Етап III реалізує саме послідовний варіант: файл читається рядок
 * за рядком, заголовок пропускається, кожен коректний рядок перетворюється
 * на клієктську модель {@link Vehicle} і передається серверу через HTTP.</p>
 *
 * <pre>
 *   CSV -> VehicleCsvReader (парсинг) -> Vehicle (клієнтська модель)
 *        -> HttpTransportClient -> POST -> Servlet -> Service -> DAO -> MySQL
 * </pre>
 *
 * <p>CSV не містить прямого JDBC-доступу.</p>
 *
 * <p>Фоновий варіант із JavaFX Task і відображенням прогресу реалізовано
 * в {@link ImportVehiclesTask} (наступний етап).</p>
 */
public class SequentialCsvImport {

    /** Відносний шлях до CSV-файлу реєстру транспортних засобів. */
    public static final Path CSV_PATH = VehicleCsvReader.CSV_PATH;

    private final HttpTransportClient client;
    private final VehicleCsvReader reader = new VehicleCsvReader();

    public SequentialCsvImport(HttpTransportClient client) {
        this.client = client;
    }

    /**
     * Читає перший коректний рядок CSV-файлу та передає його серверу.
     *
     * <p>Якщо код транспортного засобу вже є в реєстрі на сервері,
     * береться наступний рядок: інакше Service відхилив би запит
     * через дублікат коду.</p>
     *
     * @param csvPath шлях до CSV-файлу
     * @return транспортний засіб, створений на сервері (з id, згенерованим MySQL)
     */
    public Vehicle importFirstRow(Path csvPath) throws Exception {
        java.util.Set<String> existingCodes = existingCodes();

        for (Vehicle vehicle : reader.readAll(csvPath)) {
            if (existingCodes.contains(vehicle.getCode())) {
                continue;
            }
            return client.addVehicle(vehicle);
        }
        throw new IllegalStateException(
                "CSV не містить рядків, яких немає в реєстрі на сервері: " + csvPath);
    }

    /**
     * Читає весь CSV-файл і передає кожен коректний рядок серверу.
     *
     * <p>Рядки, код яких уже є в реєстрі на сервері, пропускаються,
     * щоб імпорт можна було безпечно повторювати.</p>
     *
     * @param csvPath шлях до CSV-файлу
     * @return список транспортних засобів, створених на сервері
     */
    public List<Vehicle> importAll(Path csvPath) throws Exception {
        List<Vehicle> imported = new java.util.ArrayList<>();
        java.util.Set<String> existingCodes = existingCodes();

        for (Vehicle vehicle : reader.readAll(csvPath)) {
            if (existingCodes.contains(vehicle.getCode())) {
                continue;
            }
            imported.add(client.addVehicle(vehicle));
            existingCodes.add(vehicle.getCode());
        }
        return imported;
    }

    /** Коди транспортних засобів, які вже є в реєстрі на сервері. */
    private java.util.Set<String> existingCodes() throws Exception {
        java.util.Set<String> codes = new java.util.HashSet<>();
        for (Vehicle v : client.getVehicles()) {
            if (v.getCode() != null) {
                codes.add(v.getCode());
            }
        }
        return codes;
    }

    /**
     * Точка входу для ручної перевірки послідовного імпорту.
     *
     * @param args аргументи командного рядка (не використовуються)
     */
    public static void main(String[] args) {
        try {
            HttpTransportClient client = new HttpTransportClient();
            SequentialCsvImport importer = new SequentialCsvImport(client);

            System.out.println("CSV-файл: " + CSV_PATH.toAbsolutePath());

            System.out.println("--- Читання першого коректного рядка ---");
            Vehicle created = importer.importFirstRow(CSV_PATH);
            System.out.println("  Створено на сервері: id=" + created.getId()
                    + ", code=" + created.getCode()
                    + ", name=" + created.getName()
                    + ", category=" + created.getCategory());

            System.out.println("--- Імпорт решти рядків файлу ---");
            List<Vehicle> all = importer.importAll(CSV_PATH);
            System.out.println("  Додано записів: " + all.size());
        } catch (Exception e) {
            System.err.println("Помилка імпорту: " + e.getMessage());
        }
    }
}
