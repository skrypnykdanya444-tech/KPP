package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class VehicleService {

    private static final Logger LOG = Logger.getLogger(VehicleService.class.getName());

    private final VehicleDAO dao = new VehicleDAO();

    public List<Vehicle> getAll() throws SQLException {
        return dao.getAll();
    }

    public Vehicle getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Vehicle vehicle) throws SQLException {
        validate(vehicle);
        if (dao.existsByCode(vehicle.getCode())) {
            LOG.warning("Транспортний засіб із кодом " + vehicle.getCode() + " вже існує");
            throw new IllegalArgumentException(
                    "Транспортний засіб із кодом " + vehicle.getCode() + " вже існує");
        }
        dao.add(vehicle);
        LOG.info("Додано транспортний засіб: " + vehicle.getCode());
    }

    public void update(Vehicle vehicle) throws SQLException {
        validate(vehicle);
        if (vehicle.getId() == null) {
            throw new IllegalArgumentException("Не вказано id транспортного засобу");
        }
        dao.update(vehicle);
        LOG.info("Оновлено транспортний засіб: " + vehicle.getCode());
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено транспортний засіб з id=" + id);
    }

    private void validate(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Транспортний засіб не може бути null");
        }
        if (blank(vehicle.getCode())) {
            throw new IllegalArgumentException("Поле code є обов'язковим");
        }
        if (blank(vehicle.getName())) {
            throw new IllegalArgumentException("Поле name є обов'язковим");
        }
        if (blank(vehicle.getCategory())) {
            throw new IllegalArgumentException("Поле category є обов'язковим");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
