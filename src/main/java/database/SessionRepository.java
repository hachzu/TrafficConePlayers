package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class SessionRepository {
    private SessionRepository() { }

    public static int startSession(int userId) throws SQLException {
        String insertSql = "INSERT INTO usage_session (user_id) VALUES (?)";
        String selectSql = "SELECT LAST_INSERT_ID() AS session_id";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement insert = connection.prepareStatement(insertSql);
                PreparedStatement select = connection.prepareStatement(selectSql)) {
            insert.setInt(1, userId);
            if (insert.executeUpdate() != 1) {
                throw new SQLException("Could not start the usage session.");
            }

            // MySQL returns the ID generated on this connection.
            try (ResultSet rows = select.executeQuery()) {
                if (rows.next()) {
                    return rows.getInt("session_id");
                }
            }
        }
        throw new SQLException("Could not read the usage session ID.");
    }

    public static void endSession(int userId, int sessionId) throws SQLException {
        String sql = "UPDATE usage_session SET ended_at = CURRENT_TIMESTAMP "
                + "WHERE session_id = ? AND user_id = ? AND ended_at IS NULL";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sessionId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }
}
