package ua.edu.nupp.kpp.client;

import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SequentialCsvImport {

    public static final Path CSV_PATH = VehicleCsvReader.CSV_PATH;

    private final HttpTransportClient client;
    private final VehicleCsvReader reader = new VehicleCsvReader();

    public SequentialCsvImport(HttpTransportClient client) {
        this.client = client;
    }

    public Vehicle importFirstRow(Path csvPath) throws Exception {
        Set<String> existingCodes = existingCodes();

        for (Vehicle vehicle : reader.readAll(csvPath)) {
            if (existingCodes.contains(vehicle.getCode())) {
                continue;
            }
            return client.addVehicle(vehicle);
        }
        throw new IllegalStateException(
                "CSV не містить рядків, яких немає в реєстрі на сервері: " + csvPath);
    }

    public List<Vehicle> importAll(Path csvPath) throws Exception {
        List<Vehicle> imported = new ArrayList<>();
        Set<String> existingCodes = existingCodes();

        for (Vehicle vehicle : reader.readAll(csvPath)) {
            if (existingCodes.contains(vehicle.getCode())) {
                continue;
            }
            imported.add(client.addVehicle(vehicle));
            existingCodes.add(vehicle.getCode());
        }
        return imported;
    }

    private Set<String> existingCodes() throws Exception {
        Set<String> codes = new HashSet<>();
        for (Vehicle v : client.getVehicles()) {
            if (v.getCode() != null) {
                codes.add(v.getCode());
            }
        }
        return codes;
    }

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
