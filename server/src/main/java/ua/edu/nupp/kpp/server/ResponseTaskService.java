package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service-рівень для завдань реагування.
 *
 * <p>Завдання пов'язане з інцидентом, тому Service використовує також
 * {@link IncidentDAO} для перевірки існування пов'язаного запису.</p>
 */
public class ResponseTaskService {

    private static final Logger LOG = Logger.getLogger(ResponseTaskService.class.getName());

    private final ResponseTaskDAO dao = new ResponseTaskDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();

    /** Повертає всі завдання реагування. */
    public List<ResponseTask> getAll() throws SQLException {
        return dao.getAll();
    }

    /** Повертає завдання реагування за ідентифікатором або {@code null}. */
    public ResponseTask getById(long id) throws SQLException {
        return dao.getById(id);
    }

    /**
     * Додає завдання реагування.
     *
     * @throws IllegalArgumentException якщо порожній опис, не вказано дату
     *                                  або вказано неіснуючий інцидент
     */
    public void add(ResponseTask task) throws SQLException {
        validate(task);
        requireIncident(task.getIncidentId());
        dao.add(task);
        LOG.info("Додано завдання реагування для інциденту id=" + task.getIncidentId());
    }

    /** Оновлює завдання реагування. */
    public void update(ResponseTask task) throws SQLException {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Не вказано id завдання реагування");
        }
        validate(task);
        requireIncident(task.getIncidentId());
        dao.update(task);
        LOG.info("Оновлено завдання реагування з id=" + task.getId());
    }

    /** Видаляє завдання реагування. */
    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено завдання реагування з id=" + id);
    }

    /** Перевіряє, що інцидент, на який посилається завдання, існує. */
    private void requireIncident(long incidentId) throws SQLException {
        if (incidentDAO.getById(incidentId) == null) {
            LOG.warning("Інцидент з id=" + incidentId + " не знайдено");
            throw new IllegalArgumentException(
                    "Інцидент з id=" + incidentId + " не знайдено");
        }
    }

    private void validate(ResponseTask task) {
        if (task == null) {
            throw new IllegalArgumentException("Завдання реагування не може бути null");
        }
        if (blank(task.getDescription())) {
            throw new IllegalArgumentException("Поле description є обов'язковим");
        }
        if (task.getPlannedDate() == null) {
            throw new IllegalArgumentException("Поле plannedDate є обов'язковим");
        }
        if (task.getIncidentId() <= 0) {
            throw new IllegalArgumentException("Поле incidentId має бути додатним");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
