package ua.kpp.model;

public class Route {
    private Long id;               // PK
    private Long vehicleId;        // FK -> Vehicle[cite: 1]
    private String routeNumber;    //[cite: 1]
    private String stopName;       //[cite: 1]
    private Double addressScore;   //[cite: 1]
    private Double severityScore;  //[cite: 1]

    public Route() {}

    public Route(Long id, Long vehicleId, String routeNumber, String stopName, Double addressScore, Double severityScore) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.routeNumber = routeNumber;
        this.stopName = stopName;
        this.addressScore = addressScore;
        this.severityScore = severityScore;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public Double getAddressScore() { return addressScore; }
    public void setAddressScore(Double addressScore) { this.addressScore = addressScore; }

    public Double getSeverityScore() { return severityScore; }
    public void setSeverityScore(Double severityScore) { this.severityScore = severityScore; }

    @Override
    public String toString() {
        return "Route{id=" + id + ", vehicleId=" + vehicleId + ", routeNumber='" + routeNumber + "', stopName='" + stopName + "'}";
    }
}