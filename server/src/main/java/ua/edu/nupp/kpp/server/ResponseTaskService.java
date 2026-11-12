package ua.edu.nupp.kpp.server;

import java.util.List;

/**
 * Service-рівень для завдань реагування.
 *
 * <p>Містить правила валідації, які застосовуються перед записом у БД.</p>
 */
public class ResponseTaskService {

    private final ResponseTaskDAO taskDAO = new ResponseTaskDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();

    /** Повертає всі завдання реагування. */
    public List<ResponseTask> getAll() {
        return taskDAO.getAll();
    }

    /** Повертає завдання реагування за ідентифікатором або {@code null}. */
    public ResponseTask getById(long id) {
        return taskDAO.getById(id);
    }

    /**
     * Додає завдання реагування.
     *
     * @throws IllegalArgumentException якщо порожній опис, не вказано дату
     *                                  або вказано неіснуючий інцидент
     */
    public void add(ResponseTask task) {
        validate(task);
        taskDAO.add(task);
    }

    /** Оновлює завдання реагування. */
    public void update(ResponseTask task) {
        validate(task);
        if (task.getId() == null) {
            throw new IllegalArgumentException("Не вказано id завдання реагування");
        }
        taskDAO.update(task);
    }

    /** Видаляє завдання реагування. */
    public void delete(long id) {
        taskDAO.delete(id);
    }

    private void validate(ResponseTask task) {
        if (task.getDescription() == null || task.getDescription().isBlank()) {
            throw new IllegalArgumentException("Опис завдання реагування не може бути порожнім");
        }
        if (task.getPlannedDate() == null) {
            throw new IllegalArgumentException("Не вказано заплановану дату виконання");
        }
        if (incidentDAO.getById(task.getIncidentId()) == null) {
            throw new IllegalArgumentException(
                    "Інцидент з id=" + task.getIncidentId() + " не знайдено");
        }
    }
}
