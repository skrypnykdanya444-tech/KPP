package ua.edu.nupp.kpp.server;

import java.util.List;

/**
 * Service-рівень для інцидентів.
 *
 * <p>Тут реалізовані предметні правила, які не належать до моделей:</p>
 * <ul>
 *   <li>перехід між станами життєвого циклу
 *       ПОВІДОМЛЕНО → ПІДТВЕРДЖЕНО → РЕАГУВАННЯ → ЗАКРИТО;</li>
 *   <li>розрахунок факторів F1, F2, F3 та інтегрального показника
 *       I = 0,40·F1 + 0,35·F2 + 0,25·F3.</li>
 * </ul>
 */
public class IncidentService {

    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final RouteDAO routeDAO = new RouteDAO();

    /** Повертає всі інциденти. */
    public List<Incident> getAll() {
        return incidentDAO.getAll();
    }

    /** Повертає інцидент за ідентифікатором або {@code null}. */
    public Incident getById(long id) {
        return incidentDAO.getById(id);
    }

    /**
     * Додає інцидент.
     *
     * <p>Фактори та інтегральний показник обчислюються сервером
     * за даними маршруту, тому користувач їх не вводить вручну.</p>
     *
     * @throws IllegalArgumentException якщо вказано неіснуючий маршрут
     */
    public void add(Incident incident) {
        if (incident.getState() == null) {
            incident.setState(IncidentState.NEW);
        }
        calculateFactors(incident);
        incidentDAO.add(incident);
    }

    /** Оновлює інцидент, перераховуючи фактори та інтегральний показник. */
    public void update(Incident incident) {
        if (incident.getId() == null) {
            throw new IllegalArgumentException("Не вказано id інциденту");
        }
        calculateFactors(incident);
        incidentDAO.update(incident);
    }

    /**
     * Виконує перехід інциденту до наступного стану життєвого циклу.
     *
     * @throws IllegalArgumentException якщо інцидент не знайдено
     *                                  або перехід неможливий
     */
    public void advanceState(long id) {
        Incident incident = incidentDAO.getById(id);
        if (incident == null) {
            throw new IllegalArgumentException("Інцидент з id=" + id + " не знайдено");
        }

        IncidentState current = incident.getState();
        if (current == null) {
            current = IncidentState.NEW;
        }
        if (current.isFinal()) {
            throw new IllegalArgumentException(
                    "Інцидент уже є в кінцевому стані: " + current.getTitle());
        }

        IncidentState next = switch (current) {
            case NEW -> IncidentState.CONFIRMED;
            case CONFIRMED -> IncidentState.RESPONSE;
            case RESPONSE -> IncidentState.CLOSED;
            case CLOSED -> throw new IllegalArgumentException("Неможливий перехід із стану CLOSED");
        };

        incidentDAO.updateState(id, next);
    }

    /**
     * Змінює стан інциденту на вказаний.
     *
     * @throws IllegalArgumentException якщо інцидент не знайдено
     *                                  або перехід неможливий
     */
    public void changeState(long id, IncidentState target) {
        Incident incident = incidentDAO.getById(id);
        if (incident == null) {
            throw new IllegalArgumentException("Інцидент з id=" + id + " не знайдено");
        }
        if (target == null) {
            throw new IllegalArgumentException("Не вказано стан інциденту");
        }

        IncidentState current = incident.getState() == null ? IncidentState.NEW : incident.getState();
        if (current == target) {
            return;
        }
        if (current.isFinal()) {
            throw new IllegalArgumentException(
                    "Інцидент уже є в кінцевому стані: " + current.getTitle());
        }
        if (!isAllowedTransition(current, target)) {
            throw new IllegalArgumentException(
                    "Недопустимий перехід: " + current.getTitle() + " → " + target.getTitle());
        }

        incidentDAO.updateState(id, target);
    }

    /** Видаляє інцидент. */
    public void delete(long id) {
        incidentDAO.delete(id);
    }

    /**
     * Перевіряє, чи дозволений перехід між двома станами життєвого циклу.
     *
     * <p>Дозволено лише перехід до наступного стану. Зворотні та «перескокові»
     * переходи заборонені.</p>
     */
    public boolean isAllowedTransition(IncidentState from, IncidentState to) {
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

    /**
     * Обчислює фактори F1, F2, F3 та інтегральний показник для інциденту.
     *
     * <p>F1 залежить від пасажиропотоку маршруту, F2 — від важливості маршруту,
     * F3 — від тривалості порушення руху. Усі фактори нормуються до 0–100.</p>
     */
    private void calculateFactors(Incident incident) {
        Route route = routeDAO.getById(incident.getRouteId());
        if (route == null) {
            throw new IllegalArgumentException(
                    "Маршрут з id=" + incident.getRouteId() + " не знайдено");
        }
        if (incident.getIncidentType() == null || incident.getIncidentType().isBlank()) {
            throw new IllegalArgumentException("Тип інциденту не може бути порожнім");
        }
        if (incident.getDisruptionDuration() < 0) {
            throw new IllegalArgumentException("Тривалість порушення руху не може бути від'ємною");
        }

        double f1 = RiskCalculator.factor1(route.getPassengerFlow());
        double f2 = RiskCalculator.factor2(route.getRouteImportance());
        double f3 = RiskCalculator.factor3(incident.getDisruptionDuration());

        incident.setF1(f1);
        incident.setF2(f2);
        incident.setF3(f3);
        incident.setSeverityIndex(RiskCalculator.calculate(f1, f2, f3));
    }
}
