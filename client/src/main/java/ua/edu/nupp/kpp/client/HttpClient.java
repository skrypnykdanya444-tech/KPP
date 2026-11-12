package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-клієнт для обміну даними із сервером.
 *
 * <p>Клієнт не має прямого доступу до JDBC: усі операції з базою даних
 * виконує сервер (Servlet -> Service -> DAO -> MySQL).</p>
 */
public class HttpClient {

    /** Базова адреса серверного застосунку. */
    private static final String BASE = "http://localhost:8080/server";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // =====================================================================
    // Низькорівневі HTTP-операції
    // =====================================================================

    /** Виконує GET-запит і повертає тіло відповіді. */
    public String get(String path) throws Exception {
        URL url = new URL(BASE + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        int code = conn.getResponseCode();
        if (code >= 400) {
            throw new RuntimeException("HTTP error " + code + ": " + readResponse(conn.getErrorStream()));
        }
        String body = readResponse(conn.getInputStream());
        conn.disconnect();
        return body;
    }

    /** Виконує POST-запит з параметрами форми та повертає тіло відповіді. */
    public String post(String endpoint, Map<String, String> params) throws Exception {
        URL url = new URL(BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (postData.length() > 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
            postData.append('=');
            postData.append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
        }

        byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postDataBytes);
        }

        int code = conn.getResponseCode();
        if (code >= 400) {
            throw new RuntimeException("HTTP error " + code + ": " + readResponse(conn.getErrorStream()));
        }
        String body = readResponse(conn.getInputStream());
        conn.disconnect();
        return body;
    }

    private String readResponse(java.io.InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    // =====================================================================
    // Транспортні засоби
    // =====================================================================

    /** Завантажує список транспортних засобів із сервера. */
    public List<Vehicle> getVehicles() throws Exception {
        String json = get("/vehicle");
        return mapper.readValue(json, new TypeReference<List<Vehicle>>() {
        });
    }

    /** Додає транспортний засіб на сервер і повертає створений запис. */
    public Vehicle addVehicle(Vehicle vehicle) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "add");
        params.put("code", vehicle.getCode());
        params.put("name", vehicle.getName());
        params.put("category", vehicle.getCategory());
        String json = post("/vehicle", params);
        return mapper.readValue(json, Vehicle.class);
    }

    /** Оновлює транспортний засіб на сервері. */
    public Vehicle updateVehicle(Vehicle vehicle) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "update");
        params.put("id", vehicle.getId().toString());
        params.put("code", vehicle.getCode());
        params.put("name", vehicle.getName());
        params.put("category", vehicle.getCategory());
        String json = post("/vehicle", params);
        return mapper.readValue(json, Vehicle.class);
    }

    /** Видаляє транспортний засіб на сервері. */
    public void deleteVehicle(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", id.toString());
        post("/vehicle", params);
    }

    // =====================================================================
    // Маршрути
    // =====================================================================

    /** Завантажує список маршрутів із сервера. */
    public List<Route> getRoutes() throws Exception {
        String json = get("/route");
        return mapper.readValue(json, new TypeReference<List<Route>>() {
        });
    }

    /** Додає маршрут на сервер і повертає створений запис. */
    public Route addRoute(Route route) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "add");
        params.put("vehicleId", String.valueOf(route.getVehicleId()));
        params.put("routeNumber", route.getRouteNumber());
        params.put("stopName", route.getStopName());
        params.put("passengerFlow", String.valueOf(route.getPassengerFlow()));
        params.put("routeImportance", String.valueOf(route.getRouteImportance()));
        String json = post("/route", params);
        return mapper.readValue(json, Route.class);
    }

    /** Оновлює маршрут на сервері. */
    public Route updateRoute(Route route) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "update");
        params.put("id", route.getId().toString());
        params.put("vehicleId", String.valueOf(route.getVehicleId()));
        params.put("routeNumber", route.getRouteNumber());
        params.put("stopName", route.getStopName());
        params.put("passengerFlow", String.valueOf(route.getPassengerFlow()));
        params.put("routeImportance", String.valueOf(route.getRouteImportance()));
        String json = post("/route", params);
        return mapper.readValue(json, Route.class);
    }

    /** Видаляє маршрут на сервері. */
    public void deleteRoute(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", id.toString());
        post("/route", params);
    }

    // =====================================================================
    // Інциденти
    // =====================================================================

    /** Завантажує список інцидентів із сервера. */
    public List<Incident> getIncidents() throws Exception {
        String json = get("/incident");
        return mapper.readValue(json, new TypeReference<List<Incident>>() {
        });
    }

    /** Змінює стан інциденту (перехід життєвого циклу). */
    public Incident changeIncidentState(Long id, IncidentState state) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "transition");
        params.put("id", String.valueOf(id));
        params.put("state", state.name());
        String json = post("/incident", params);
        return mapper.readValue(json, Incident.class);
    }

    /** Додає інцидент на сервер і повертає створений запис. */
    public Incident addIncident(Incident incident) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "add");
        params.put("routeId", String.valueOf(incident.getRouteId()));
        params.put("incidentType", incident.getIncidentType());
        params.put("state", incident.getState().name());
        params.put("disruptionDuration", String.valueOf(incident.getDisruptionDuration()));
        String json = post("/incident", params);
        return mapper.readValue(json, Incident.class);
    }

    /** Оновлює інцидент на сервері. */
    public Incident updateIncident(Incident incident) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "update");
        params.put("id", incident.getId().toString());
        params.put("routeId", String.valueOf(incident.getRouteId()));
        params.put("incidentType", incident.getIncidentType());
        params.put("state", incident.getState().name());
        params.put("disruptionDuration", String.valueOf(incident.getDisruptionDuration()));
        String json = post("/incident", params);
        return mapper.readValue(json, Incident.class);
    }

    /** Переводить інцидент до наступного стану життєвого циклу. */
    public Incident advanceIncidentState(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "transition");
        params.put("id", String.valueOf(id));
        String json = post("/incident", params);
        return mapper.readValue(json, Incident.class);
    }

    /** Видаляє інцидент на сервері. */
    public void deleteIncident(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", id.toString());
        post("/incident", params);
    }

    // =====================================================================
    // Завдання реагування
    // =====================================================================

    /** Завантажує список завдань реагування із сервера. */
    public List<ResponseTask> getResponseTasks() throws Exception {
        String json = get("/response-task");
        return mapper.readValue(json, new TypeReference<List<ResponseTask>>() {
        });
    }

    /** Додає завдання реагування на сервер і повертає створений запис. */
    public ResponseTask addResponseTask(ResponseTask task) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "add");
        params.put("incidentId", String.valueOf(task.getIncidentId()));
        params.put("description", task.getDescription());
        params.put("plannedDate", task.getPlannedDate().toString());
        String json = post("/response-task", params);
        return mapper.readValue(json, ResponseTask.class);
    }

    /** Оновлює завдання реагування на сервері. */
    public ResponseTask updateResponseTask(ResponseTask task) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "update");
        params.put("id", task.getId().toString());
        params.put("incidentId", String.valueOf(task.getIncidentId()));
        params.put("description", task.getDescription());
        params.put("plannedDate", task.getPlannedDate().toString());
        String json = post("/response-task", params);
        return mapper.readValue(json, ResponseTask.class);
    }

    /** Видаляє завдання реагування на сервері. */
    public void deleteResponseTask(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", id.toString());
        post("/response-task", params);
    }
}
