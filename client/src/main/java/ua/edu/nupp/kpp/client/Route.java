package ua.edu.nupp.kpp.client;

/**
 * Клієнтська модель маршруту (DTO).
 *
 * <p>Дублює серверну сутність {@code Route}. Поля відповідають колонкам
 * таблиці {@code route}.</p>
 */
public class Route {

    private Long id;
    private long vehicleId;
    private String routeNumber;
    private String stopName;
    private double passengerFlow;
    private double routeImportance;

    public Route() {
    }

    public Route(Long id,
                 long vehicleId,
                 String routeNumber,
                 String stopName,
                 double passengerFlow,
                 double routeImportance) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.routeNumber = routeNumber;
        this.stopName = stopName;
        this.passengerFlow = passengerFlow;
        this.routeImportance = routeImportance;
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

    public double getRouteImportance() {
        return routeImportance;
    }

    public void setRouteImportance(double routeImportance) {
        this.routeImportance = routeImportance;
    }
}
