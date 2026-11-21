package ua.edu.nupp.kpp.server;

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

    public boolean isFinal() {
        return this == CLOSED;
    }
}
