package ua.kpp.model;

import java.time.LocalDateTime;

public class ResponseTask {
    private Long id;                // PK
    private Long incidentId;        // FK -> Incident[cite: 1]
    private String description;     //[cite: 1]
    private LocalDateTime plannedDate; //[cite: 1]

    public ResponseTask() {}

    public ResponseTask(Long id, Long incidentId, String description, LocalDateTime plannedDate) {
        this.id = id;
        this.incidentId = incidentId;
        this.description = description;
        this.plannedDate = plannedDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDateTime plannedDate) { this.plannedDate = plannedDate; }

    @Override
    public String toString() {
        return "ResponseTask{id=" + id + ", incidentId=" + incidentId + ", description='" + description + "', plannedDate=" + plannedDate + "}";
    }
}