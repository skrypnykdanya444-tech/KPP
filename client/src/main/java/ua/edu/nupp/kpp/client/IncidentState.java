package ua.edu.nupp.kpp.client;

/**
 * Клієнтський перелік станів життєвого циклу інциденту.
 *
 * <p>Дублює серверний {@code IncidentState}: клієнт не залежить від
 * серверного модуля.</p>
 */
public enum IncidentState {

    NEW("ПОВІДОМЛЕНО"),
    CONFIRMED("ПІДТВЕРДЖЕНО"),
    RESPONSE("РЕАГУВАННЯ"),
    CLOSED("ЗАКРИТО");

    private final String title;

    IncidentState(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
