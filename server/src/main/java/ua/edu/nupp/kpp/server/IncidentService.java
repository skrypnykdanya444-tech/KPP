package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service-рівень для інцидентів.
 *
 * <p>Тут реалізовані предметні правила, які не належать до моделей:</p>
 * <ul>
 *   <li>валідація вхідних даних;</li>
 *   <li>перехід між станами життєвого циклу
 *       ПОВІДОМЛЕНО → ПІДТВЕРДЖЕНО → РЕАГУВАННЯ → ЗАКРИТО;</li>
 *   <li>розрахунок факторів F1, F2, F3 та інтегрального показника
 *       I = 0,40·F1 + 0,35·F2 + 0,25·F3.</li>
 * </ul>
 *
 * <p>SQL тут відсутній: Service звертається лише до DAO.</p>
 */
public class IncidentService {

    private static final Logger LOG = Logger.getLogger(IncidentService.class.getName());

    private final IncidentDAO dao = new IncidentDAO();
    private final RouteDAO routeDAO = new RouteDAO();

    /** Повертає всі інциденти. */
    public List<Incident> getAll() throws SQLException {
        return dao.getAll();
    }

    /** Повертає інцидент за ідентифікатором або {@code null}. */
    public Incident getById(long id) throws SQLException {
        return dao.getById(id);
    }

    /**
     * Додає інцидент.
     *
     * <p>Фактори та інтегральний показник обчислюються сервером
     * за даними маршруту, тому користувач їх не вводить вручну.
     * Новий інцидент автоматично отримує початковий стан NEW.</p>
     *
     * @throws IllegalArgumentException якщо вказано неіснуючий маршрут
     */
    public void add(Incident incident) throws SQLException {
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

    /** Оновлює інцидент, перераховуючи фактори та інтегральний показник. */
    public void update(Incident incident) throws SQLException {
        if (incident.getId() == null) {
            throw new IllegalArgumentException("Не вказано id інциденту");
        }
        validate(incident);
        requireRoute(incident.getRouteId());
        calculateFactors(incident);
        dao.update(incident);
        LOG.info("Оновлено інцидент з id=" + incident.getId()
                + ", I = " + incident.getSeverityIndex());
    }

    /**
     * Виконує перехід інциденту до наступного стану життєвого циклу.
     *
     * @throws IllegalArgumentException якщо інцидент не знайдено
     *                                  або перехід неможливий
     */
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

    /**
     * Змінює стан інциденту на вказаний.
     *
     * @throws IllegalArgumentException якщо інцидент не знайдено
     *                                  або перехід неможливий
     */
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

    /** Видаляє інцидент. */
    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено інцидент з id=" + id);
    }

    /**
     * Перевіряє, чи дозволений перехід між двома станами життєвого циклу.
     *
     * <p>Дозволено лише перехід до наступного стану. Зворотні, «перескокові»
     * та повторні переходи заборонені.</p>
     */
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

    /** Знаходить інцидент або створює предметну помилку. */
    private Incident requireIncident(long id) throws SQLException {
        Incident incident = dao.getById(id);
        if (incident == null) {
            LOG.warning("Інцидент з id=" + id + " не знайдено");
            throw new IllegalArgumentException("Інцидент з id=" + id + " не знайдено");
        }
        return incident;
    }

    /** Перевіряє, що маршрут, на який посилається інцидент, існує. */
    private void requireRoute(long routeId) throws SQLException {
        if (routeDAO.getById(routeId) == null) {
            LOG.warning("Маршрут з id=" + routeId + " не знайдено");
            throw new IllegalArgumentException("Маршрут з id=" + routeId + " не знайдено");
        }
    }

    /**
     * Обчислює фактори F1, F2, F3 та інтегральний показник для інциденту.
     *
     * <p>F1 залежить від пасажиропотоку маршруту, F2 — від важливості маршруту,
     * F3 — від тривалості порушення руху. Усі фактори нормуються до 0–100.</p>
     */
    private void calculateFactors(Incident incident) throws SQLException {
        Route route = routeDAO.getById(incident.getRouteId());
        if (route == null) {
            throw new IllegalArgumentException(
                    "Маршрут з id=" + incident.getRouteId() + " не знайдено");
        }

        double f1 = RiskCalculator.factor1(route.getPassengerFlow());
        double f2 = RiskCalculator.factor2(route.getRouteImportance());
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
