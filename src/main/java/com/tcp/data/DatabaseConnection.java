package com.tcp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens a connection to the app's MySQL database. */
public final class DatabaseConnection {
	private static final String DEFAULT_URL =
			"jdbc:mysql://localhost:3306/trafficplayers_db?connectTimeout=3000&socketTimeout=3000";

	private DatabaseConnection() {}

	public static int checkMediaTable() throws SQLException {
		String url = settingOrDefault("TCP_DB_URL", DEFAULT_URL);
		String username = System.getenv("TCP_DB_USER");
		String password = System.getenv("TCP_DB_PASSWORD");

		if (username == null || username.isBlank() || password == null) {
			throw new SQLException("Set TCP_DB_USER and TCP_DB_PASSWORD to connect to MySQL.");
		}

		try (Connection connection = DriverManager.getConnection(url, username, password);
			 Statement statement = connection.createStatement();
			 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM media")) {
			result.next();
			return result.getInt(1);
		}
	}

	private static String settingOrDefault(String name, String defaultValue) {
		String value = System.getenv(name);
		return value == null || value.isBlank() ? defaultValue : value;
	}
}
