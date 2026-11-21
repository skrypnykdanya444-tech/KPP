package ua.edu.nupp.kpp.server;

public class Incident {

    private Long id;

    private long routeId;

    private String incidentType;

    private IncidentState state;

    private double disruptionDuration;

    private double f1;

    private double f2;

    private double f3;

    private double severityIndex;

    public Incident() {
    }

    public Incident(Long id,
                    long routeId,
                    String incidentType,
                    IncidentState state,
                    double disruptionDuration,
                    double f1,
                    double f2,
                    double f3,
                    double severityIndex) {
        this.id = id;
        this.routeId = routeId;
        this.incidentType = incidentType;
        this.state = state;
        this.disruptionDuration = disruptionDuration;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.severityIndex = severityIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public IncidentState getState() {
        return state;
    }

    public void setState(IncidentState state) {
        this.state = state;
    }

    public double getDisruptionDuration() {
        return disruptionDuration;
    }

    public void setDisruptionDuration(double disruptionDuration) {
        this.disruptionDuration = disruptionDuration;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getSeverityIndex() {
        return severityIndex;
    }

    public void setSeverityIndex(double severityIndex) {
        this.severityIndex = severityIndex;
    }
}
