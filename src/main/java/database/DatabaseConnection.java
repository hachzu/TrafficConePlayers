package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3306/trafficplayers_db";

    private static final String USER = "tcp_java_app";
    private static final String PASSWORD = "TcpLocal#2026!Mango";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Database connected.");
        } catch (SQLException error) {
            error.printStackTrace();
        }
    }
}