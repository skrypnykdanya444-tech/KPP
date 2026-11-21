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

public class HttpTransportClient {

    private static final String BASE = "http://localhost:8080/transport-server";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        return send(request);
    }

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

    private String send(HttpRequest request) throws Exception {
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException(extractError(response.body(), response.statusCode()));
        }
        return response.body();
    }

    private String extractError(String body, int statusCode) {
        try {
            Map<String, Object> parsed = mapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
            Object error = parsed.get("error");
            if (error != null) {
                return String.valueOf(error);
            }
        } catch (Exception ignored) {
        }
        return body == null || body.isBlank() ? "HTTP error " + statusCode : body;
    }

    public List<Vehicle> getVehicles() throws Exception {
        String json = get("/vehicles");
        return mapper.readValue(json, new TypeReference<List<Vehicle>>() {
        });
    }

    public Vehicle addVehicle(Vehicle vehicle) throws Exception {
        String json = post("/vehicles", vehicleParams(vehicle, "add"));
        return mapper.readValue(json, Vehicle.class);
    }

    public Vehicle updateVehicle(Vehicle vehicle) throws Exception {
        String json = post("/vehicles", vehicleParams(vehicle, "update"));
        return mapper.readValue(json, Vehicle.class);
    }

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

    public List<Route> getRoutes() throws Exception {
        String json = get("/routes");
        return mapper.readValue(json, new TypeReference<List<Route>>() {
        });
    }

    public Route addRoute(Route route) throws Exception {
        String json = post("/routes", routeParams(route, "add"));
        return mapper.readValue(json, Route.class);
    }

    public Route updateRoute(Route route) throws Exception {
        String json = post("/routes", routeParams(route, "update"));
        return mapper.readValue(json, Route.class);
    }

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
        params.put("f2", String.valueOf(route.getF2()));
        return params;
    }

    public List<Incident> getIncidents() throws Exception {
        String json = get("/incidents");
        return mapper.readValue(json, new TypeReference<List<Incident>>() {
        });
    }

    public Incident addIncident(Incident incident) throws Exception {
        String json = post("/incidents", incidentParams(incident, "add"));
        return mapper.readValue(json, Incident.class);
    }

    public Incident updateIncident(Incident incident) throws Exception {
        String json = post("/incidents", incidentParams(incident, "update"));
        return mapper.readValue(json, Incident.class);
    }

    public void deleteIncident(Long id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "delete");
        params.put("id", String.valueOf(id));
        post("/incidents", params);
    }

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

    public List<ResponseTask> getResponseTasks() throws Exception {
        String json = get("/response-tasks");
        return mapper.readValue(json, new TypeReference<List<ResponseTask>>() {
        });
    }

    public ResponseTask addResponseTask(ResponseTask task) throws Exception {
        String json = post("/response-tasks", taskParams(task, "add"));
        return mapper.readValue(json, ResponseTask.class);
    }

    public ResponseTask updateResponseTask(ResponseTask task) throws Exception {
        String json = post("/response-tasks", taskParams(task, "update"));
        return mapper.readValue(json, ResponseTask.class);
    }

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
