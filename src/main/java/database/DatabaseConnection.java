package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/trafficplayers_db";

    public static Connection getConnection() throws SQLException {
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (user == null || user.isBlank() || password == null || password.isEmpty()) {
            throw new SQLException("Set DB_USER and DB_PASSWORD before starting the player.");
        }
        return DriverManager.getConnection(URL, user, password);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Connected to MySQL.");
        } catch (SQLException error) {
            System.out.println("Connection failed. Check MySQL and the database settings.");
        }
    }
}
