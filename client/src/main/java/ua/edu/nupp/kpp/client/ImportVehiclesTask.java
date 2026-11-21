package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ImportVehiclesTask extends Task<Integer> {

    private final HttpTransportClient client;
    private final VehicleCsvReader reader = new VehicleCsvReader();

    public ImportVehiclesTask(HttpTransportClient client) {
        this.client = client;
    }

    @Override
    protected Integer call() throws Exception {
        List<Vehicle> vehicles = reader.readAll(VehicleCsvReader.CSV_PATH);
        int total = vehicles.size();
        int imported = 0;
        int skipped = 0;

        Set<String> existingCodes = new HashSet<>();
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

    public Vehicle importFirstRow(Path csvPath) throws Exception {
        Vehicle vehicle = reader.readFirstRow(csvPath)
                .orElseThrow(() -> new IllegalStateException(
                        "CSV не містить коректних рядків: " + csvPath));
        return client.addVehicle(vehicle);
    }
}
