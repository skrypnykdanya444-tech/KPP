package ua.kpp.model;

public class Incident {
    private Long id;              // PK
    private Long routeId;         // FK -> Route[cite: 1]
    private String state;         //[cite: 1]
    private Double f1;            //[cite: 1]
    private Double f2;            //[cite: 1]
    private Double f3;            //[cite: 1]
    private Double severityIndex; //[cite: 1]

    public Incident() {}

    public Incident(Long id, Long routeId, String state, Double f1, Double f2, Double f3, Double severityIndex) {
        this.id = id;
        this.routeId = routeId;
        this.state = state;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.severityIndex = severityIndex;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Double getF1() { return f1; }
    public void setF1(Double f1) { this.f1 = f1; }

    public Double getF2() { return f2; }
    public void setF2(Double f2) { this.f2 = f2; }

    public Double getF3() { return f3; }
    public void setF3(Double f3) { this.f3 = f3; }

    public Double getSeverityIndex() { return severityIndex; }
    public void setSeverityIndex(Double severityIndex) { this.severityIndex = severityIndex; }

    @Override
    public String toString() {
        return "Incident{id=" + id + ", routeId=" + routeId + ", state='" + state + "', severityIndex=" + severityIndex + "}";
    }
}