package ua.edu.nupp.kpp.server;

/**
 * Розрахунок факторів та інтегрального показника інциденту.
 *
 * <p>Інтегральний показник:</p>
 * <pre>
 *   I = 0,40·F1 + 0,35·F2 + 0,25·F3
 * </pre>
 * <ul>
 *   <li>{@code F1} — вплив на пасажирів (нормалізується з пасажиропотоку маршруту);</li>
 *   <li>{@code F2} — важливість маршруту (нормалізується з оцінки важливості маршруту);</li>
 *   <li>{@code F3} — тривалість порушення руху (нормалізується з тривалості в хвилинах).</li>
 * </ul>
 *
 * <p>Усі фактори нормуються до діапазону 0–100. Значення обмежуються
 * зверху та знизу: від'ємні значення перетворюються на 0, а значення,
 * більші за максимум шкали, — на 100.</p>
 */
public final class RiskCalculator {

    /** Максимальний пасажиропотік, який приймається за 100 балів фактора F1. */
    public static final double MAX_PASSENGER_FLOW = 2500.0;

    /** Максимальна оцінка важливості маршруту, яка приймається за 100 балів фактора F2. */
    public static final double MAX_ROUTE_IMPORTANCE = 10.0;

    /** Максимальна тривалість порушення руху (хв), яка приймається за 100 балів фактора F3. */
    public static final double MAX_DISRUPTION_DURATION = 240.0;

    private RiskCalculator() {
    }

    /**
     * F1 — вплив на пасажирів.
     *
     * @param passengerFlow пасажиропотік маршруту
     * @return нормоване значення фактора в діапазоні 0–100
     */
    public static double factor1(double passengerFlow) {
        if (passengerFlow < 0) {
            throw new IllegalArgumentException("passengerFlow не може бути від'ємним");
        }
        return normalize(passengerFlow / MAX_PASSENGER_FLOW * 100.0);
    }

    /**
     * F2 — важливість маршруту.
     *
     * @param routeImportance оцінка важливості маршруту (шкала 0..10)
     * @return нормоване значення фактора в діапазоні 0–100
     */
    public static double factor2(double routeImportance) {
        if (routeImportance < 0) {
            throw new IllegalArgumentException("routeImportance не може бути від'ємним");
        }
        return normalize(routeImportance / MAX_ROUTE_IMPORTANCE * 100.0);
    }

    /**
     * F3 — тривалість порушення руху.
     *
     * @param disruptionDuration тривалість порушення руху у хвилинах
     * @return нормоване значення фактора в діапазоні 0–100
     */
    public static double factor3(double disruptionDuration) {
        if (disruptionDuration < 0) {
            throw new IllegalArgumentException("disruptionDuration не може бути від'ємним");
        }
        return normalize(disruptionDuration / MAX_DISRUPTION_DURATION * 100.0);
    }

    /**
     * Інтегральний показник I = 0,40·F1 + 0,35·F2 + 0,25·F3.
     *
     * @param f1 фактор «вплив на пасажирів»
     * @param f2 фактор «важливість маршруту»
     * @param f3 фактор «тривалість порушення руху»
     * @return інтегральний показник, округлений до 2 знаків після коми
     */
    public static double calculate(double f1, double f2, double f3) {
        if (f1 < 0 || f1 > 100 || f2 < 0 || f2 > 100 || f3 < 0 || f3 > 100) {
            throw new IllegalArgumentException("Фактори мають бути в діапазоні 0..100");
        }
        double result = 0.40 * f1 + 0.35 * f2 + 0.25 * f3;
        return Math.round(result * 100.0) / 100.0;
    }

    /**
     * Обмежує значення фактора діапазоном 0–100.
     *
     * @param value вихідне значення
     * @return значення в діапазоні 0–100
     */
    private static double normalize(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}
