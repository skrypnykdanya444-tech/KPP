package ua.edu.nupp.kpp.server;

/**
 * Інцидент (основна сутність №3).
 *
 * <p>Відповідає таблиці {@code Incident}. Інцидент є об'єктом життєвого циклу
 * зі станами {@link IncidentState}.</p>
 *
 * <p>Інцидент несе інтегральний показник та три фактори, з яких він
 * складається:</p>
 * <pre>
 *   I = 0,40·F1 + 0,35·F2 + 0,25·F3
 * </pre>
 * <ul>
 *   <li>{@code f1} — вплив на пасажирів;</li>
 *   <li>{@code f2} — важливість маршруту;</li>
 *   <li>{@code f3} — тривалість порушення руху.</li>
 * </ul>
 * Усі фактори нормовані до діапазону 0–100.
 *
 * <p>Формула розрахунку НЕ зберігається в моделі: розрахунок і предметні
 * правила розміщуються на Service-рівні, модель лише зберігає дані.</p>
 */
public class Incident {

    /** Власний первинний ключ. До запису в БД значення може бути {@code null}. */
    private Long id;

    /** Зовнішній ключ на {@code Route.id}. */
    private long routeId;

    /** Тип інциденту: ДТП, Затримка руху, Технічна несправність тощо. */
    private String incidentType;

    /** Поточний стан життєвого циклу інциденту. */
    private IncidentState state;

    /** Сира тривалість порушення руху, хвилин (джерело фактора F3). */
    private double disruptionDuration;

    /** Фактор F1 — вплив на пасажирів, нормовано 0–100. */
    private double f1;

    /** Фактор F2 — важливість маршруту, нормовано 0–100. */
    private double f2;

    /** Фактор F3 — тривалість порушення руху, нормовано 0–100. */
    private double f3;

    /** Інтегральний показник I = 0,40·F1 + 0,35·F2 + 0,25·F3. */
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
