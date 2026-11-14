package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class UserRepository {
    private UserRepository() { }

    public record User(int userId, String username) { }

    public static Optional<User> createAccount(String username, String password)
            throws SQLException {
        if (username.isBlank() || username.codePointCount(0, username.length()) > 50
                || password.isEmpty() || password.codePointCount(0, password.length()) > 255) {
            throw new IllegalArgumentException("Enter a valid username and password.");
        }

        String insertSql = "INSERT INTO `user` (username, password_hash, last_login) "
                + "VALUES (?, ?, CURRENT_TIMESTAMP)";
        String selectSql = "SELECT user_id, username FROM `user` WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement insert = connection.prepareStatement(insertSql);
                PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setString(1, username);
            try (ResultSet rows = select.executeQuery()) {
                if (rows.next()) {
                    return Optional.empty();
                }
            }

            insert.setString(1, username);
            insert.setString(2, password);
            if (insert.executeUpdate() != 1) {
                throw new SQLException("Could not save the account.");
            }

            try (ResultSet rows = select.executeQuery()) {
                if (rows.next()) {
                    return Optional.of(new User(rows.getInt("user_id"),
                            rows.getString("username")));
                }
            }
        }
        throw new SQLException("Could not read the new account.");
    }

    public static Optional<User> signIn(String username, String password) throws SQLException {
        String selectSql = "SELECT user_id, username, password_hash "
                + "FROM `user` WHERE username = ?";
        String updateSql = "UPDATE `user` SET last_login = CURRENT_TIMESTAMP "
                + "WHERE user_id = ? AND password_hash = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement select = connection.prepareStatement(selectSql);
                PreparedStatement update = connection.prepareStatement(updateSql)) {
            select.setString(1, username);

            User user;
            try (ResultSet rows = select.executeQuery()) {
                if (!rows.next() || !password.equals(rows.getString("password_hash"))) {
                    return Optional.empty();
                }
                user = new User(rows.getInt("user_id"), rows.getString("username"));
            }

            update.setInt(1, user.userId());
            update.setString(2, password);
            if (update.executeUpdate() != 1) {
                return Optional.empty();
            }
            return Optional.of(user);
        }
    }
}
