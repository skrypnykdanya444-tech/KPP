package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) для завдань реагування.
 *
 * <p>Виконує безпосередній доступ до таблиці {@code response_task} через JDBC.
 * Усі SQL-запити зосереджені саме тут.</p>
 */
public class ResponseTaskDAO {

    private static final String SQL_FIND_ALL =
            "SELECT id, incident_id, description, planned_date FROM response_task ORDER BY id";
    private static final String SQL_FIND_BY_ID =
            "SELECT id, incident_id, description, planned_date FROM response_task WHERE id = ?";
    private static final String SQL_INSERT =
            "INSERT INTO response_task (incident_id, description, planned_date) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE response_task SET incident_id = ?, description = ?, planned_date = ? WHERE id = ?";
    private static final String SQL_DELETE =
            "DELETE FROM response_task WHERE id = ?";

    /** Повертає всі завдання реагування. */
    public List<ResponseTask> getAll() throws SQLException {
        List<ResponseTask> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /** Повертає завдання реагування за ідентифікатором або {@code null}. */
    public ResponseTask getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    /** Додає завдання реагування і проставляє йому id, згенерований MySQL. */
    public void add(ResponseTask task) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bind(ps, task);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getLong(1));
                }
            }
        }
    }

    /** Оновлює завдання реагування за наявним id. */
    public void update(ResponseTask task) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
            bind(ps, task);
            ps.setLong(4, task.getId());
            ps.executeUpdate();
        }
    }

    /** Видаляє завдання реагування за ідентифікатором. */
    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Переносить значення полів моделі в параметри PreparedStatement. */
    private void bind(PreparedStatement ps, ResponseTask t) throws SQLException {
        ps.setLong(1, t.getIncidentId());
        ps.setString(2, t.getDescription());
        ps.setTimestamp(3, Timestamp.valueOf(t.getPlannedDate()));
    }

    /** Перетворює рядок ResultSet на модель {@link ResponseTask}. */
    private ResponseTask map(ResultSet rs) throws SQLException {
        ResponseTask t = new ResponseTask();
        t.setId(rs.getLong("id"));
        t.setIncidentId(rs.getLong("incident_id"));
        t.setDescription(rs.getString("description"));
        Timestamp planned = rs.getTimestamp("planned_date");
        t.setPlannedDate(planned.toLocalDateTime());
        return t;
    }
}
