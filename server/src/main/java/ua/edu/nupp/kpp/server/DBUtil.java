package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Утиліта для встановлення JDBC-з'єднання з MySQL.
 *
 * <p>Створена на першому етапі курсової роботи та надалі використовується
 * DAO-класами. Метод {@link #getConnection()} повертає готове з'єднання
 * з базою даних {@code transport_db}.</p>
 */
public class DBUtil {

    /** URL з'єднання з локальним сервером MySQL. */
    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/transport_db"
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                    + "&connectTimeout=5000&socketTimeout=5000";

    /** Користувач MySQL. */
    private static final String USER = "root";

    /** Пароль MySQL. */
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found", e);
        }
    }

    /**
     * Відкриває нове JDBC-з'єднання з базою даних.
     *
     * @return з'єднання з MySQL
     * @throws SQLException якщо з'єднання встановити не вдалося
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
