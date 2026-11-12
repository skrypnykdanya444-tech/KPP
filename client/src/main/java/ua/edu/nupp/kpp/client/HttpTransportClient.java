package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-клієнт JavaFX для обміну даними із сервером.
 *
 * <p>Ізолює графічний інтерфейс від деталей HTTP-протоколу: JavaFX
 * викликає зрозумілі методи ({@code getVehicles()}, {@code addVehicle()}…),
 * а цей клас сам формує запити, кодує параметри та перетворює JSON
 * у Java-об'єкти.</p>
 *
 * <p>Клієнт не має прямого доступу до JDBC: усі операції з базою даних
 * виконує сервер (Servlet -> Service -> DAO -> MySQL).</p>
 */
public class HttpTransportClient {

    /**
     * Базова адреса серверного застосунку.
     *
     * <p>Має відповідати {@code finalName} у {@code server/pom.xml}:
     * finalName = transport-server -> context path = /transport-server.</p>
     */
    private static final String BASE = "http://localhost:8080/transport-server";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // =====================================================================
    // Базові механізми GET / POST
    // =====================================================================

    /**
     * Виконує GET-запит і повертає тіло відповіді у вигляді рядка JSON.
     *
     * @param path шлях до серверного маршруту, наприклад {@code /vehicles}
     * @return JSON у вигляді рядка
     */
    public String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        return send(request);
    }

    /**
     * Виконує POST-запит з параметрами форми та повертає JSON-відповідь.
     *
     * <p>Параметри передаються у форматі
     * {@code application/x-www-form-urlencoded}, тому Servlet читає їх
     * через {@code req.getParameter(...)}.</p>
     *
     * @param endpoint серверний маршрут, наприклад {@code /vehicles}
     * @param params   параметри запиту
     * @return JSON у вигляді рядка
     */
    public String post(String endpoint, Map<String, String> params) throws Exception {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
            body.append('=');
            body.append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + endpoint))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    /**
     * Надсилає запит і повертає тіло відповіді.
     *
     * <p>Якщо сервер повернув статус {@code >= 400}, створюється виняток
     * із текстом повідомлення сервера, щоб JavaFX показав користувачеві
     * зрозумілу причину помилки, а не HTTP-код.</p>
     */
    private String send(HttpRequest request) throws Exception {
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException(extractError(response.body(), response.statusCode()));
        }
        return response.body();
    }

    /**
     * Витягує текст помилки з JSON-відповіді сервера.
     *
     * <p>Сервер повертає помилки у вигляді {@code {"error":"..."}}.
     * Якщо JSON некоректний, повертається сирий текст відповіді —
     * помилка не губиться мовчки.</p>
     */
    private String extractError(String body, int statusCode) {
        try {
            Map<String, Object> parsed = mapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
            Object error = parsed.get("error");
            if (error != null) {
                return String.valueOf(error);
            }
        } catch (Exception ignored) {
            // відповідь не є JSON — повертаємо сирий текст нижче
        }
        return body == null || body.isBlank() ? "HTTP error " + statusCode : body;
    }

    // =====================================================================
    // Транспортні засоби
    // =====================================================================

    /** Завантажує список транспортних засобів із сервера. */
    public List<Vehicle> getVehicles() throws Exception {
        String json = get("/vehicles");
        return mapper.readValue(json, new TypeReference<List<Vehicle>>() {
        });
    }

    /** Додає транспортний засіб на сервер і повертає створений запис. */
    public Vehicle addVehicle(Vehicle vehicle) throws Exception {
        String json = post("/vehicles", vehicleParams(vehicle, "add"));
        return mapper.readValue(json, Vehicle.class);
    }

    /** Оновлює транспортний засіб на сервері. */
    public Vehicle updateVehicle(Vehicle vehicle) throws Exception {
        String json = post("/vehicles", vehicleParams(vehicle, "update"));
        return mapper.readValue(json, Vehicle.class);
    }

    /** Видаляє транспортний засіб на сервері (POST action=delete). */
    public void deleteVehicle(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", String.valueOf(id));
        post("/vehicles", params);
    }

    private Map<String, String> vehicleParams(Vehicle vehicle, String action) {
        Map<String, String> params = new HashMap<>();
        params.put("action", action);
        if (vehicle.getId() != null) {
            params.put("id", String.valueOf(vehicle.getId()));
        }
        params.put("code", vehicle.getCode());
        params.put("name", vehicle.getName());
        params.put("category", vehicle.getCategory());
        return params;
    }

    // =====================================================================
    // Маршрути
    // =====================================================================

    /** Завантажує список маршрутів із сервера. */
    public List<Route> getRoutes() throws Exception {
        String json = get("/routes");
        return mapper.readValue(json, new TypeReference<List<Route>>() {
        });
    }

    /** Додає маршрут на сервер і повертає створений запис. */
    public Route addRoute(Route route) throws Exception {
        String json = post("/routes", routeParams(route, "add"));
        return mapper.readValue(json, Route.class);
    }

    /** Оновлює маршрут на сервері. */
    public Route updateRoute(Route route) throws Exception {
        String json = post("/routes", routeParams(route, "update"));
        return mapper.readValue(json, Route.class);
    }

    /** Видаляє маршрут на сервері (POST action=delete). */
    public void deleteRoute(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", String.valueOf(id));
        post("/routes", params);
    }

    private Map<String, String> routeParams(Route route, String action) {
        Map<String, String> params = new HashMap<>();
        params.put("action", action);
        if (route.getId() != null) {
            params.put("id", String.valueOf(route.getId()));
        }
        params.put("vehicleId", String.valueOf(route.getVehicleId()));
        params.put("routeNumber", route.getRouteNumber());
        params.put("stopName", route.getStopName());
        params.put("passengerFlow", String.valueOf(route.getPassengerFlow()));
        params.put("routeImportance", String.valueOf(route.getRouteImportance()));
        return params;
    }

    // =====================================================================
    // Інциденти
    // =====================================================================

    /** Завантажує список інцидентів із сервера. */
    public List<Incident> getIncidents() throws Exception {
        String json = get("/incidents");
        return mapper.readValue(json, new TypeReference<List<Incident>>() {
        });
    }

    /** Додає інцидент на сервер і повертає створений запис. */
    public Incident addIncident(Incident incident) throws Exception {
        String json = post("/incidents", incidentParams(incident, "add"));
        return mapper.readValue(json, Incident.class);
    }

    /** Оновлює інцидент на сервері. */
    public Incident updateIncident(Incident incident) throws Exception {
        String json = post("/incidents", incidentParams(incident, "update"));
        return mapper.readValue(json, Incident.class);
    }

    /** Видаляє інцидент на сервері (POST action=delete). */
    public void deleteIncident(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", String.valueOf(id));
        post("/incidents", params);
    }

    /**
     * Змінює стан інциденту — перехід життєвого циклу.
     *
     * <p>JavaFX не перевіряє допустимість переходу: він лише передає
     * вибране значення серверу, а рішення приймає Service.</p>
     */
    public Incident changeIncidentState(Long id, IncidentState state) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "transition");
        params.put("id", String.valueOf(id));
        params.put("state", state.name());
        String json = post("/incidents", params);
        return mapper.readValue(json, Incident.class);
    }

    private Map<String, String> incidentParams(Incident incident, String action) {
        Map<String, String> params = new HashMap<>();
        params.put("action", action);
        if (incident.getId() != null) {
            params.put("id", String.valueOf(incident.getId()));
        }
        params.put("routeId", String.valueOf(incident.getRouteId()));
        params.put("incidentType", incident.getIncidentType());
        params.put("state", incident.getState().name());
        params.put("disruptionDuration", String.valueOf(incident.getDisruptionDuration()));
        return params;
    }

    // =====================================================================
    // Завдання реагування
    // =====================================================================

    /** Завантажує список завдань реагування із сервера. */
    public List<ResponseTask> getResponseTasks() throws Exception {
        String json = get("/response-tasks");
        return mapper.readValue(json, new TypeReference<List<ResponseTask>>() {
        });
    }

    /** Додає завдання реагування на сервер і повертає створений запис. */
    public ResponseTask addResponseTask(ResponseTask task) throws Exception {
        String json = post("/response-tasks", taskParams(task, "add"));
        return mapper.readValue(json, ResponseTask.class);
    }

    /** Оновлює завдання реагування на сервері. */
    public ResponseTask updateResponseTask(ResponseTask task) throws Exception {
        String json = post("/response-tasks", taskParams(task, "update"));
        return mapper.readValue(json, ResponseTask.class);
    }

    /** Видаляє завдання реагування на сервері (POST action=delete). */
    public void deleteResponseTask(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", String.valueOf(id));
        post("/response-tasks", params);
    }

    private Map<String, String> taskParams(ResponseTask task, String action) {
        Map<String, String> params = new HashMap<>();
        params.put("action", action);
        if (task.getId() != null) {
            params.put("id", String.valueOf(task.getId()));
        }
        params.put("incidentId", String.valueOf(task.getIncidentId()));
        params.put("description", task.getDescription());
        LocalDateTime planned = task.getPlannedDate();
        params.put("plannedDate", planned == null ? "" : planned.toString());
        return params;
    }
}
