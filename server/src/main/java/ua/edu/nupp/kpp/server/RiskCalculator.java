package ua.edu.nupp.kpp.server;

public final class RiskCalculator {

    public static final double MAX_PASSENGER_FLOW = 2500.0;

    public static final double MAX_ROUTE_IMPORTANCE = 100.0;

    public static final double MAX_DISRUPTION_DURATION = 240.0;

    private RiskCalculator() {
    }

    public static double factor1(double passengerFlow) {
        if (passengerFlow < 0) {
            throw new IllegalArgumentException("passengerFlow не може бути від'ємним");
        }
        return normalize(passengerFlow / MAX_PASSENGER_FLOW * 100.0);
    }

    public static double factor2(double routeImportance) {
        if (routeImportance < 0) {
            throw new IllegalArgumentException("routeImportance не може бути від'ємним");
        }
        return normalize(routeImportance);
    }

    public static double factor3(double disruptionDuration) {
        if (disruptionDuration < 0) {
            throw new IllegalArgumentException("disruptionDuration не може бути від'ємним");
        }
        return normalize(disruptionDuration / MAX_DISRUPTION_DURATION * 100.0);
    }

    public static double calculate(double f1, double f2, double f3) {
        if (f1 < 0 || f1 > 100 || f2 < 0 || f2 > 100 || f3 < 0 || f3 > 100) {
            throw new IllegalArgumentException("Фактори мають бути в діапазоні 0..100");
        }
        double result = 0.40 * f1 + 0.35 * f2 + 0.25 * f3;
        return Math.round(result * 100.0) / 100.0;
    }

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
