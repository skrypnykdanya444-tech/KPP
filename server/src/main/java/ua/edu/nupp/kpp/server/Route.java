package ua.edu.nupp.kpp.server;

/**
 * Маршрут (основна сутність №2).
 *
 * <p>Відповідає таблиці {@code Route}. Маршрут прив'язаний до транспортного
 * засобу та несе дві сирі оцінки, з яких Service-рівень формує фактори
 * інтегрального показника:</p>
 * <ul>
 *   <li>{@code passengerFlow} — сирий пасажиропотік маршруту (джерело фактора F1
 *       «вплив на пасажирів»);</li>
 *   <li>{@code routeImportance} — сира оцінка важливості маршруту
 *       (джерело фактора F2 «важливість маршруту»).</li>
 * </ul>
 */
public class Route {

    /** Власний первинний ключ. До запису в БД значення може бути {@code null}. */
    private Long id;

    /** Зовнішній ключ на {@code Vehicle.id}. */
    private long vehicleId;

    /** Номер маршруту, наприклад {@code 22}. */
    private String routeNumber;

    /** Назва зупинки маршруту. */
    private String stopName;

    /** Сирий пасажиропотік маршруту (джерело фактора F1). */
    private double passengerFlow;

    /** Сира оцінка важливості маршруту, шкала 0..10 (джерело фактора F2). */
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
