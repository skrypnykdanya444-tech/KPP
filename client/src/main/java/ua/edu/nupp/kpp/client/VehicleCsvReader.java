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

public class VehicleCsvReader {

    public static final Path CSV_PATH = Path.of("sample-data", "vehicles.csv");

    private static final String RESOURCE_PATH = "/sample-data/vehicles.csv";

    private static final String DELIMITER = ";";

    private static final int EXPECTED_COLUMNS = 3;

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

    public List<Vehicle> readAll(Path csvPath) throws IOException {
        List<Vehicle> vehicles = new ArrayList<>();
        boolean first = true;
        for (String line : readCsvLines(csvPath)) {
            if (first) {
                first = false;
                continue;
            }
            parseRow(line).ifPresent(vehicles::add);
        }
        return vehicles;
    }

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

        return Optional.of(new Vehicle(null, code, name, category));
    }

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

    private boolean isHeader(String line) {
        return line != null && line.trim().toLowerCase().startsWith("code");
    }
}
