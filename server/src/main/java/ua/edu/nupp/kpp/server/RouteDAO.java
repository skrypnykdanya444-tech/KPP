package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) для маршрутів.
 *
 * <p>Виконує безпосередній доступ до таблиці {@code route} через JDBC.</p>
 */
public class RouteDAO {

    private static final String SQL_FIND_ALL =
            "SELECT id, vehicle_id, route_number, stop_name, passenger_flow, route_importance "
                    + "FROM route ORDER BY id";
    private static final String SQL_FIND_BY_ID =
            "SELECT id, vehicle_id, route_number, stop_name, passenger_flow, route_importance "
                    + "FROM route WHERE id = ?";
    private static final String SQL_INSERT =
            "INSERT INTO route (vehicle_id, route_number, stop_name, passenger_flow, route_importance) "
                    + "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE route SET vehicle_id = ?, route_number = ?, stop_name = ?, "
                    + "passenger_flow = ?, route_importance = ? WHERE id = ?";
    private static final String SQL_DELETE =
            "DELETE FROM route WHERE id = ?";

    /** Повертає всі маршрути. */
    public List<Route> getAll() {
        List<Route> list = new ArrayList<>();
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

    /** Повертає маршрут за ідентифікатором або {@code null}. */
    public Route getById(long id) {
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

    /** Додає маршрут і проставляє йому id, згенерований MySQL. */
    public void add(Route route) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, route.getVehicleId());
            ps.setString(2, route.getRouteNumber());
            ps.setString(3, route.getStopName());
            ps.setDouble(4, route.getPassengerFlow());
            ps.setDouble(5, route.getRouteImportance());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    route.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Оновлює маршрут за наявним id. */
    public void update(Route route) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
            ps.setLong(1, route.getVehicleId());
            ps.setString(2, route.getRouteNumber());
            ps.setString(3, route.getStopName());
            ps.setDouble(4, route.getPassengerFlow());
            ps.setDouble(5, route.getRouteImportance());
            ps.setLong(6, route.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Видаляє маршрут за ідентифікатором. */
    public void delete(long id) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Route mapRow(ResultSet rs) throws SQLException {
        Route r = new Route();
        r.setId(rs.getLong("id"));
        r.setVehicleId(rs.getLong("vehicle_id"));
        r.setRouteNumber(rs.getString("route_number"));
        r.setStopName(rs.getString("stop_name"));
        r.setPassengerFlow(rs.getDouble("passenger_flow"));
        r.setRouteImportance(rs.getDouble("route_importance"));
        return r;
    }
}
