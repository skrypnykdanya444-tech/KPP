package ua.edu.nupp.kpp.server;

import java.util.List;

/**
 * Service-рівень для транспортних засобів.
 *
 * <p>Містить правила валідації, які застосовуються перед записом у БД.</p>
 */
public class VehicleService {

    private final VehicleDAO vehicleDAO = new VehicleDAO();

    /** Повертає всі транспортні засоби. */
    public List<Vehicle> getAll() {
        return vehicleDAO.getAll();
    }

    /** Повертає транспортний засіб за ідентифікатором або {@code null}. */
    public Vehicle getById(long id) {
        return vehicleDAO.getById(id);
    }

    /**
     * Додає транспортний засіб.
     *
     * @throws IllegalArgumentException якщо код, назва або категорія порожні
     *                                  або код уже використовується
     */
    public void add(Vehicle vehicle) {
        validate(vehicle);
        if (vehicleDAO.existsByCode(vehicle.getCode())) {
            throw new IllegalArgumentException(
                    "Транспортний засіб із кодом " + vehicle.getCode() + " вже існує");
        }
        vehicleDAO.add(vehicle);
    }

    /** Оновлює транспортний засіб. */
    public void update(Vehicle vehicle) {
        validate(vehicle);
        if (vehicle.getId() == null) {
            throw new IllegalArgumentException("Не вказано id транспортного засобу");
        }
        vehicleDAO.update(vehicle);
    }

    /** Видаляє транспортний засіб. */
    public void delete(long id) {
        vehicleDAO.delete(id);
    }

    private void validate(Vehicle vehicle) {
        if (vehicle.getCode() == null || vehicle.getCode().isBlank()) {
            throw new IllegalArgumentException("Код транспортного засобу не може бути порожнім");
        }
        if (vehicle.getName() == null || vehicle.getName().isBlank()) {
            throw new IllegalArgumentException("Назва транспортного засобу не може бути порожньою");
        }
        if (vehicle.getCategory() == null || vehicle.getCategory().isBlank()) {
            throw new IllegalArgumentException("Категорія транспортного засобу не може бути порожньою");
        }
    }
}
