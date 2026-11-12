package ua.edu.nupp.kpp.server;

import java.util.List;

/**
 * Service-рівень для маршрутів.
 *
 * <p>Містить правила валідації, які застосовуються перед записом у БД.</p>
 */
public class RouteService {

    private final RouteDAO routeDAO = new RouteDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    /** Повертає всі маршрути. */
    public List<Route> getAll() {
        return routeDAO.getAll();
    }

    /** Повертає маршрут за ідентифікатором або {@code null}. */
    public Route getById(long id) {
        return routeDAO.getById(id);
    }

    /**
     * Додає маршрут.
     *
     * @throws IllegalArgumentException якщо порожні обов'язкові поля
     *                                  або вказано неіснуючий транспортний засіб
     */
    public void add(Route route) {
        validate(route);
        routeDAO.add(route);
    }

    /** Оновлює маршрут. */
    public void update(Route route) {
        validate(route);
        if (route.getId() == null) {
            throw new IllegalArgumentException("Не вказано id маршруту");
        }
        routeDAO.update(route);
    }

    /** Видаляє маршрут. */
    public void delete(long id) {
        routeDAO.delete(id);
    }

    private void validate(Route route) {
        if (route.getRouteNumber() == null || route.getRouteNumber().isBlank()) {
            throw new IllegalArgumentException("Номер маршруту не може бути порожнім");
        }
        if (route.getStopName() == null || route.getStopName().isBlank()) {
            throw new IllegalArgumentException("Назва зупинки не може бути порожньою");
        }
        if (route.getPassengerFlow() < 0) {
            throw new IllegalArgumentException("Пасажиропотік не може бути від'ємним");
        }
        if (route.getRouteImportance() < 0 || route.getRouteImportance() > RiskCalculator.MAX_ROUTE_IMPORTANCE) {
            throw new IllegalArgumentException("Оцінка важливості маршруту має бути в діапазоні 0..10");
        }
        if (vehicleDAO.getById(route.getVehicleId()) == null) {
            throw new IllegalArgumentException(
                    "Транспортний засіб з id=" + route.getVehicleId() + " не знайдено");
        }
    }
}
