package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service-рівень для маршрутів.
 *
 * <p>Містить правила валідації, які застосовуються перед записом у БД.
 * Маршрут пов'язаний із транспортним засобом, тому Service використовує
 * також {@link VehicleDAO} для перевірки існування пов'язаного запису.</p>
 */
public class RouteService {

    private static final Logger LOG = Logger.getLogger(RouteService.class.getName());

    private final RouteDAO dao = new RouteDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    /** Повертає всі маршрути. */
    public List<Route> getAll() throws SQLException {
        return dao.getAll();
    }

    /** Повертає маршрут за ідентифікатором або {@code null}. */
    public Route getById(long id) throws SQLException {
        return dao.getById(id);
    }

    /**
     * Додає маршрут.
     *
     * @throws IllegalArgumentException якщо порожні обов'язкові поля
     *                                  або вказано неіснуючий транспортний засіб
     */
    public void add(Route route) throws SQLException {
        validate(route);
        requireVehicle(route.getVehicleId());
        dao.add(route);
        LOG.info("Додано маршрут №" + route.getRouteNumber());
    }

    /** Оновлює маршрут. */
    public void update(Route route) throws SQLException {
        validate(route);
        if (route.getId() == null) {
            throw new IllegalArgumentException("Не вказано id маршруту");
        }
        requireVehicle(route.getVehicleId());
        dao.update(route);
        LOG.info("Оновлено маршрут №" + route.getRouteNumber());
    }

    /** Видаляє маршрут. */
    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено маршрут з id=" + id);
    }

    /** Перевіряє, що транспортний засіб, на який посилається маршрут, існує. */
    private void requireVehicle(long vehicleId) throws SQLException {
        if (vehicleDAO.getById(vehicleId) == null) {
            LOG.warning("Транспортний засіб з id=" + vehicleId + " не знайдено");
            throw new IllegalArgumentException(
                    "Транспортний засіб з id=" + vehicleId + " не знайдено");
        }
    }

    private void validate(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("Маршрут не може бути null");
        }
        if (blank(route.getRouteNumber())) {
            throw new IllegalArgumentException("Поле routeNumber є обов'язковим");
        }
        if (blank(route.getStopName())) {
            throw new IllegalArgumentException("Поле stopName є обов'язковим");
        }
        if (route.getVehicleId() <= 0) {
            throw new IllegalArgumentException("Поле vehicleId має бути додатним");
        }
        if (route.getPassengerFlow() < 0) {
            throw new IllegalArgumentException("Пасажиропотік не може бути від'ємним");
        }
        if (route.getRouteImportance() < 0
                || route.getRouteImportance() > RiskCalculator.MAX_ROUTE_IMPORTANCE) {
            throw new IllegalArgumentException(
                    "Оцінка важливості маршруту має бути в діапазоні 0..10");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
