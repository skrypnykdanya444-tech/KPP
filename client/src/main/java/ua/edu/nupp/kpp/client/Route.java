package ua.edu.nupp.kpp.client;

public class Route {

    private Long id;
    private long vehicleId;
    private String routeNumber;
    private String stopName;
    private double passengerFlow;
    private double f2;

    public Route() {
    }

    public Route(Long id,
                 long vehicleId,
                 String routeNumber,
                 String stopName,
                 double passengerFlow,
                 double f2) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.routeNumber = routeNumber;
        this.stopName = stopName;
        this.passengerFlow = passengerFlow;
        this.f2 = f2;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public double getPassengerFlow() {
        return passengerFlow;
    }

    public void setPassengerFlow(double passengerFlow) {
        this.passengerFlow = passengerFlow;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }
}
