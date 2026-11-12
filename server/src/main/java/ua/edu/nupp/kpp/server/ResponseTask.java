package ua.edu.nupp.kpp.server;

import java.time.LocalDateTime;

/**
 * Завдання реагування (основна сутність №4).
 *
 * <p>Відповідає таблиці {@code ResponseTask}. Завдання формується
 * за інцидентом і описує заходи, які має виконати реагувальна бригада.</p>
 */
public class ResponseTask {

    /** Власний первинний ключ. До запису в БД значення може бути {@code null}. */
    private Long id;

    /** Зовнішній ключ на {@code Incident.id}. */
    private long incidentId;

    /** Опис завдання реагування. */
    private String description;

    /** Запланований час виконання завдання. */
    private LocalDateTime plannedDate;

    public ResponseTask() {
    }

    public ResponseTask(Long id,
                        long incidentId,
                        String description,
                        LocalDateTime plannedDate) {
        this.id = id;
        this.incidentId = incidentId;
        this.description = description;
        this.plannedDate = plannedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(long incidentId) {
        this.incidentId = incidentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDateTime plannedDate) {
        this.plannedDate = plannedDate;
    }
}
