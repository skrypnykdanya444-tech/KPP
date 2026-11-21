package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {

    private static final String SQL_FIND_ALL =
            "SELECT id, vehicle_id, route_number, stop_name, passenger_flow, f2 FROM route ORDER BY id";
    private static final String SQL_FIND_BY_ID =
            "SELECT id, vehicle_id, route_number, stop_name, passenger_flow, f2 FROM route WHERE id = ?";
    private static final String SQL_INSERT =
            "INSERT INTO route (vehicle_id, route_number, stop_name, passenger_flow, f2) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE route SET vehicle_id = ?, route_number = ?, stop_name = ?, passenger_flow = ?, f2 = ? WHERE id = ?";
    private static final String SQL_DELETE =
            "DELETE FROM route WHERE id = ?";

    public List<Route> getAll() throws SQLException {
        List<Route> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Route getById(long id) throws SQLException {
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

    public void add(Route route) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, route.getVehicleId());
            ps.setString(2, route.getRouteNumber());
            ps.setString(3, route.getStopName());
            ps.setDouble(4, route.getPassengerFlow());
            ps.setDouble(5, route.getF2());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    route.setId(keys.getLong(1));
                }
            }
        }
    }

    public void update(Route route) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
            ps.setLong(1, route.getVehicleId());
            ps.setString(2, route.getRouteNumber());
            ps.setString(3, route.getStopName());
            ps.setDouble(4, route.getPassengerFlow());
            ps.setDouble(5, route.getF2());
            ps.setLong(6, route.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Route map(ResultSet rs) throws SQLException {
        Route r = new Route();
        r.setId(rs.getLong("id"));
        r.setVehicleId(rs.getLong("vehicle_id"));
        r.setRouteNumber(rs.getString("route_number"));
        r.setStopName(rs.getString("stop_name"));
        r.setPassengerFlow(rs.getDouble("passenger_flow"));
        r.setF2(rs.getDouble("f2"));
        return r;
    }
}
