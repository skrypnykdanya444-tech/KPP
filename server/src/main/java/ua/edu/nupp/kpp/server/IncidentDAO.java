package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) для інцидентів.
 *
 * <p>Виконує безпосередній доступ до таблиці {@code incident} через JDBC.</p>
 */
public class IncidentDAO {

    private static final String SQL_FIND_ALL =
            "SELECT id, route_id, incident_type, state, disruption_duration, f1, f2, f3, severity_index "
                    + "FROM incident ORDER BY id";
    private static final String SQL_FIND_BY_ID =
            "SELECT id, route_id, incident_type, state, disruption_duration, f1, f2, f3, severity_index "
                    + "FROM incident WHERE id = ?";
    private static final String SQL_INSERT =
            "INSERT INTO incident (route_id, incident_type, state, disruption_duration, f1, f2, f3, severity_index) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE incident SET route_id = ?, incident_type = ?, state = ?, disruption_duration = ?, "
                    + "f1 = ?, f2 = ?, f3 = ?, severity_index = ? WHERE id = ?";
    private static final String SQL_UPDATE_STATE =
            "UPDATE incident SET state = ? WHERE id = ?";
    private static final String SQL_DELETE =
            "DELETE FROM incident WHERE id = ?";

    /** Повертає всі інциденти. */
    public List<Incident> getAll() {
        List<Incident> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /** Повертає інцидент за ідентифікатором або {@code null}. */
    public Incident getById(long id) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /** Додає інцидент і проставляє йому id, згенерований MySQL. */
    public void add(Incident incident) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, incident.getRouteId());
            ps.setString(2, incident.getIncidentType());
            ps.setString(3, incident.getState().name());
            ps.setDouble(4, incident.getDisruptionDuration());
            ps.setDouble(5, incident.getF1());
            ps.setDouble(6, incident.getF2());
            ps.setDouble(7, incident.getF3());
            ps.setDouble(8, incident.getSeverityIndex());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    incident.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Оновлює інцидент за наявним id. */
    public void update(Incident incident) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
            ps.setLong(1, incident.getRouteId());
            ps.setString(2, incident.getIncidentType());
            ps.setString(3, incident.getState().name());
            ps.setDouble(4, incident.getDisruptionDuration());
            ps.setDouble(5, incident.getF1());
            ps.setDouble(6, incident.getF2());
            ps.setDouble(7, incident.getF3());
            ps.setDouble(8, incident.getSeverityIndex());
            ps.setLong(9, incident.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Змінює лише стан життєвого циклу інциденту. */
    public void updateState(long id, IncidentState state) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE_STATE)) {
            ps.setString(1, state.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Видаляє інцидент за ідентифікатором. */
    public void delete(long id) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Incident mapRow(ResultSet rs) throws SQLException {
        Incident i = new Incident();
        i.setId(rs.getLong("id"));
        i.setRouteId(rs.getLong("route_id"));
        i.setIncidentType(rs.getString("incident_type"));
        i.setState(IncidentState.valueOf(rs.getString("state")));
        i.setDisruptionDuration(rs.getDouble("disruption_duration"));
        i.setF1(rs.getDouble("f1"));
        i.setF2(rs.getDouble("f2"));
        i.setF3(rs.getDouble("f3"));
        i.setSeverityIndex(rs.getDouble("severity_index"));
        return i;
    }
}
