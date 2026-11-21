package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class RouteService {

    private static final Logger LOG = Logger.getLogger(RouteService.class.getName());

    private final RouteDAO dao = new RouteDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public List<Route> getAll() throws SQLException {
        return dao.getAll();
    }

    public Route getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Route route) throws SQLException {
        validate(route);
        requireVehicle(route.getVehicleId());
        dao.add(route);
        LOG.info("Додано маршрут №" + route.getRouteNumber());
    }

    public void update(Route route) throws SQLException {
        validate(route);
        if (route.getId() == null) {
            throw new IllegalArgumentException("Не вказано id маршруту");
        }
        requireVehicle(route.getVehicleId());
        dao.update(route);
        LOG.info("Оновлено маршрут №" + route.getRouteNumber());
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено маршрут з id=" + id);
    }

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
        if (route.getF2() < 0 || route.getF2() > 100) {
            throw new IllegalArgumentException(
                    "F2 має бути в діапазоні 0..100");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
