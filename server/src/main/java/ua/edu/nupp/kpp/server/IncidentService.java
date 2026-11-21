package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class IncidentService {

    private static final Logger LOG = Logger.getLogger(IncidentService.class.getName());

    private final IncidentDAO dao = new IncidentDAO();
    private final RouteDAO routeDAO = new RouteDAO();

    public List<Incident> getAll() throws SQLException {
        return dao.getAll();
    }

    public Incident getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Incident incident) throws SQLException {
        if (incident == null) {
            throw new IllegalArgumentException("Інцидент не може бути null");
        }
        if (incident.getState() == null) {
            incident.setState(IncidentState.NEW);
        }
        validate(incident);
        requireRoute(incident.getRouteId());
        calculateFactors(incident);
        dao.add(incident);
        LOG.info("Створено інцидент: " + incident.getIncidentType()
                + ", I = " + incident.getSeverityIndex());
    }

    public void update(Incident incident) throws SQLException {
        if (incident == null) {
            throw new IllegalArgumentException("Інцидент не може бути null");
        }
        if (incident.getId() == null) {
            throw new IllegalArgumentException("Не вказано id інциденту");
        }
        validate(incident);
        requireRoute(incident.getRouteId());
        Incident existing = requireIncident(incident.getId());
        IncidentState currentState = existing.getState() == null ? IncidentState.NEW : existing.getState();
        IncidentState targetState = incident.getState() == null ? currentState : incident.getState();
        if (targetState != currentState && !isAllowedTransition(currentState, targetState)) {
            throw new IllegalArgumentException(
                    "Недопустимий перехід: " + currentState.getTitle() + " → " + targetState.getTitle());
        }
        incident.setState(targetState);
        calculateFactors(incident);
        dao.update(incident);
        LOG.info("Оновлено інцидент з id=" + incident.getId()
                + ", I = " + incident.getSeverityIndex());
    }

    public void advanceState(long id) throws SQLException {
        Incident incident = requireIncident(id);

        IncidentState current = incident.getState() == null ? IncidentState.NEW : incident.getState();
        if (current.isFinal()) {
            LOG.warning("Інцидент уже є в кінцевому стані: " + current.getTitle());
            throw new IllegalArgumentException(
                    "Інцидент уже є в кінцевому стані: " + current.getTitle());
        }

        IncidentState next = switch (current) {
            case NEW -> IncidentState.CONFIRMED;
            case CONFIRMED -> IncidentState.RESPONSE;
            case RESPONSE -> IncidentState.CLOSED;
            case CLOSED -> throw new IllegalArgumentException("Неможливий перехід із стану CLOSED");
        };

        dao.updateState(id, next);
        LOG.info("Змінено стан інциденту: " + current.name() + " -> " + next.name());
    }

    public void changeState(long id, IncidentState target) throws SQLException {
        Incident incident = requireIncident(id);
        if (target == null) {
            throw new IllegalArgumentException("Не вказано стан інциденту");
        }

        IncidentState current = incident.getState() == null ? IncidentState.NEW : incident.getState();
        if (current == target) {
            return;
        }
        if (current.isFinal()) {
            LOG.warning("Недопустимий перехід стану: " + current.name() + " -> " + target.name());
            throw new IllegalArgumentException(
                    "Інцидент уже є в кінцевому стані: " + current.getTitle());
        }
        if (!isAllowedTransition(current, target)) {
            LOG.warning("Недопустимий перехід стану: " + current.name() + " -> " + target.name());
            throw new IllegalArgumentException(
                    "Недопустимий перехід: " + current.getTitle() + " → " + target.getTitle());
        }

        dao.updateState(id, target);
        LOG.info("Змінено стан інциденту: " + current.name() + " -> " + target.name());
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено інцидент з id=" + id);
    }

    public static boolean isAllowedTransition(IncidentState from, IncidentState to) {
        if (from == null || to == null) {
            return false;
        }
        return switch (from) {
            case NEW -> to == IncidentState.CONFIRMED;
            case CONFIRMED -> to == IncidentState.RESPONSE;
            case RESPONSE -> to == IncidentState.CLOSED;
            case CLOSED -> false;
        };
    }

    private Incident requireIncident(long id) throws SQLException {
        Incident incident = dao.getById(id);
        if (incident == null) {
            LOG.warning("Інцидент з id=" + id + " не знайдено");
            throw new IllegalArgumentException("Інцидент з id=" + id + " не знайдено");
        }
        return incident;
    }

    private void requireRoute(long routeId) throws SQLException {
        if (routeDAO.getById(routeId) == null) {
            LOG.warning("Маршрут з id=" + routeId + " не знайдено");
            throw new IllegalArgumentException("Маршрут з id=" + routeId + " не знайдено");
        }
    }

    private void calculateFactors(Incident incident) throws SQLException {
        Route route = routeDAO.getById(incident.getRouteId());
        if (route == null) {
            throw new IllegalArgumentException(
                    "Маршрут з id=" + incident.getRouteId() + " не знайдено");
        }

        double f1 = RiskCalculator.factor1(route.getPassengerFlow());
        double f2 = RiskCalculator.factor2(route.getF2());
        double f3 = RiskCalculator.factor3(incident.getDisruptionDuration());

        incident.setF1(f1);
        incident.setF2(f2);
        incident.setF3(f3);
        incident.setSeverityIndex(RiskCalculator.calculate(f1, f2, f3));
    }

    private void validate(Incident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("Інцидент не може бути null");
        }
        if (blank(incident.getIncidentType())) {
            throw new IllegalArgumentException("Поле incidentType є обов'язковим");
        }
        if (incident.getRouteId() <= 0) {
            throw new IllegalArgumentException("Поле routeId має бути додатним");
        }
        if (incident.getDisruptionDuration() < 0) {
            throw new IllegalArgumentException("Тривалість порушення руху не може бути від'ємною");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
