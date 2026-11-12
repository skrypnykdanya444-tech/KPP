package ua.edu.nupp.kpp.client;

import java.time.LocalDateTime;

/**
 * Клієнтська модель завдання реагування (DTO).
 *
 * <p>Дублює серверну сутність {@code ResponseTask}.</p>
 */
public class ResponseTask {

    private Long id;
    private long incidentId;
    private String description;
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
