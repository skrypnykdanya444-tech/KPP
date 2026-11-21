package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    private static final String SQL_FIND_ALL =
            "SELECT id, code, name, category FROM vehicle ORDER BY id";
    private static final String SQL_FIND_BY_ID =
            "SELECT id, code, name, category FROM vehicle WHERE id = ?";
    private static final String SQL_INSERT =
            "INSERT INTO vehicle (code, name, category) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE vehicle SET code = ?, name = ?, category = ? WHERE id = ?";
    private static final String SQL_DELETE =
            "DELETE FROM vehicle WHERE id = ?";
    private static final String SQL_EXISTS_BY_CODE =
            "SELECT id FROM vehicle WHERE code = ?";

    public List<Vehicle> getAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Vehicle getById(long id) throws SQLException {
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

    public void add(Vehicle vehicle) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, vehicle.getCode());
            ps.setString(2, vehicle.getName());
            ps.setString(3, vehicle.getCategory());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    vehicle.setId(keys.getLong(1));
                }
            }
        }
    }

    public void update(Vehicle vehicle) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, vehicle.getCode());
            ps.setString(2, vehicle.getName());
            ps.setString(3, vehicle.getCategory());
            ps.setLong(4, vehicle.getId());
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

    public boolean existsByCode(String code) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_EXISTS_BY_CODE)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getLong("id"));
        v.setCode(rs.getString("code"));
        v.setName(rs.getString("name"));
        v.setCategory(rs.getString("category"));
        return v;
    }
}
