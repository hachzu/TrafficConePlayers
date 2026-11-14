package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AchievementRepository {
    public static final int CUSTOMIZATION_PRICE = 5000;
    private static final String CUSTOMIZATION = "Unlock theme customization";

    private record Goal(String title, String description, int target, int coins, String query) { }

    public record Achievement(String title, String description, int coins, boolean earned,
            boolean claimed) { }

    public record Wallet(int balance, boolean customizationUnlocked) { }

    public record Rewards(int balance, boolean customizationUnlocked,
            List<Achievement> achievements) { }

    private static final String IMPORTS = "SELECT COUNT(*) FROM media "
            + "WHERE user_id = ? AND media_type IN ('MP3', 'MP4')";
    private static final String PLAYLISTS = "SELECT COUNT(*) FROM ("
            + "SELECT p.playlist_id FROM playlist p JOIN playlist_media pm "
            + "ON p.playlist_id = pm.playlist_id JOIN media m ON m.media_id = pm.media_id "
            + "WHERE p.user_id = ? AND m.user_id = p.user_id AND m.media_type IN ('MP3', 'MP4') "
            + "GROUP BY p.playlist_id HAVING COUNT(DISTINCT m.media_id) >= 3) qualifying";
    private static final String MIXED = "SELECT COUNT(*) FROM ("
            + "SELECT p.playlist_id FROM playlist p JOIN playlist_media pm "
            + "ON p.playlist_id = pm.playlist_id JOIN media m ON m.media_id = pm.media_id "
            + "WHERE p.user_id = ? AND m.user_id = p.user_id AND m.media_type IN ('MP3', 'MP4') "
            + "GROUP BY p.playlist_id HAVING COUNT(DISTINCT m.media_type) = 2) qualifying";
    private static final String RUNTIME = "SELECT SUM(duration_seconds) FROM usage_session "
            + "WHERE user_id = ? AND ended_at IS NOT NULL";
    private static final String DAYS = "SELECT COUNT(DISTINCT DATE(started_at)) FROM usage_session "
            + "WHERE user_id = ? AND ended_at IS NOT NULL AND duration_seconds >= 300";
    private static final String GAME_ROUNDS = "FROM currency_transaction "
            + "WHERE user_id = ? AND transaction_type = 'EARN' "
            + "AND transaction_reason LIKE 'game-round:%'";
    private static final String GAMES_PLAYED =
            "SELECT COUNT(DISTINCT SUBSTRING_INDEX(transaction_reason, ':', 2)) " + GAME_ROUNDS;
    private static final String DRAWS = "SELECT COUNT(*) FROM gacha_transaction WHERE user_id = ?";
    private static final String RARITY = "SELECT COUNT(*) FROM gacha_transaction g "
            + "JOIN skin s ON s.skin_id = g.skin_id WHERE g.user_id = ? AND ";
    private static final String COLLECTED = "SELECT COUNT(DISTINCT skin_id) "
            + "FROM gacha_transaction WHERE user_id = ?";

    private static final List<Goal> GOALS = List.of(
            new Goal("First on the Deck", "Import your first audio or video file.", 1, 100, IMPORTS),
            new Goal("Local Legend", "Have 10 imported tracks at once.", 10, 300, IMPORTS),
            new Goal("Mixtape Maker", "Add 3 different tracks to one named playlist.", 1, 200, PLAYLISTS),
            new Goal("Different Moods", "Have 3 playlists with at least 3 tracks each.", 3, 400, PLAYLISTS),
            new Goal("Switch the Signal", "Put audio and video in the same playlist.", 1, 200, MIXED),
            new Goal("Green Light", "Use the player for 10 minutes total.", 600, 200, RUNTIME),
            new Goal("The Long Route", "Use the player for 1 hour total.", 3600, 300, RUNTIME),
            new Goal("Cone After Cone", "Use the player for 5 hours total.", 18000, 400, RUNTIME),
            new Goal("Still on the Road", "Use the player for 20 hours total.", 72000, 500, RUNTIME),
            new Goal("Regular on the Route", "Complete a 5-minute session on 3 different days.",
                    3, 300, DAYS),
            new Goal("One More Round", "Finish your first mini-game round.", 1, 100, "SELECT COUNT(*) " + GAME_ROUNDS),
            new Goal("Arcade Regular", "Finish 25 mini-game rounds.", 25, 200, "SELECT COUNT(*) " + GAME_ROUNDS),
            new Goal("Full Circuit", "Finish a round in 3 different mini-games.", 3, 300, GAMES_PLAYED),
            new Goal("First Turn of the Key", "Open your first gacha crate.", 1, 100, DRAWS),
            new Goal("Out of the Ordinary", "Pull your first Rare-or-higher theme.", 1, 200,
                    RARITY + "s.rarity IN ('RARE', 'EPIC', 'LEGENDARY')"),
            new Goal("Traffic Cone Sensei", "Pull your first Legendary theme.", 1, 400,
                    RARITY + "s.rarity = 'LEGENDARY'"),
            new Goal("Collector’s Lane", "Collect 5 different themes through gacha.", 5, 200, COLLECTED)
    );

    private AchievementRepository() { }

    public static void refresh(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // The catalog has no unique name constraint, so serialize catalog creation.
            if (number(connection, "SELECT GET_LOCK('tcp_achievement_catalog', 5)") != 1) {
                throw new SQLException("Could not lock the achievement catalog.");
            }
            try {
                for (Goal goal : GOALS) {
                    int id = number(connection, "SELECT MIN(achievement_id) FROM achievement "
                            + "WHERE achievement_name = ?", goal.title());
                    if (id == 0) {
                        update(connection, "INSERT INTO achievement "
                                + "(achievement_name, description, requirement_value, currency_reward) "
                                + "VALUES (?, ?, ?, ?)",
                                goal.title(), goal.description(), goal.target(), goal.coins());
                        id = number(connection, "SELECT LAST_INSERT_ID()");
                    }
                    if (goal.query() != null && number(connection, goal.query(), userId) >= goal.target()) {
                        update(connection, "INSERT IGNORE INTO user_achievement "
                                + "(user_id, achievement_id) VALUES (?, ?)", userId, id);
                    }
                }
            } finally {
                number(connection, "SELECT RELEASE_LOCK('tcp_achievement_catalog')");
            }
        }
    }

    public static Wallet loadWallet(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return new Wallet(balance(connection, userId),
                    hasTransaction(connection, userId, "SPEND", CUSTOMIZATION));
        }
    }

    public static Rewards loadRewards(int userId) throws SQLException {
        refresh(userId);
        List<Achievement> achievements = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            for (Goal goal : GOALS) {
                boolean earned = earned(connection, userId, goal.title());
                boolean claimed = earned && hasTransaction(connection, userId, "EARN", goal.title());
                achievements.add(new Achievement(goal.title(), goal.description(),
                        goal.coins(), earned, claimed));
            }
            return new Rewards(balance(connection, userId),
                    hasTransaction(connection, userId, "SPEND", CUSTOMIZATION), achievements);
        }
    }

    public static boolean claim(int userId, String title) throws SQLException {
        for (Goal goal : GOALS) {
            if (goal.title().equals(title)) {
                return applyTransaction(userId, "EARN", title, goal.coins());
            }
        }
        return false;
    }

    public static int recordGameRound(int userId, String game, UUID roundId, int reward)
            throws SQLException {
        if (!game.matches("[a-z0-9-]{1,40}") || roundId == null || reward <= 0) {
            throw new IllegalArgumentException("Invalid game round.");
        }
        String reason = "game-round:" + game + ":" + roundId;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (number(connection, "SELECT user_id FROM `user` WHERE user_id = ? FOR UPDATE",
                        userId) == 0) {
                    throw new SQLException("Player account does not exist.");
                }
                if (!hasTransaction(connection, userId, "EARN", reason)) {
                    creditWallet(connection, userId, reward);
                    recordTransaction(connection, userId, reward, "EARN", reason);
                }

                int newBalance = balance(connection, userId);
                connection.commit();
                return newBalance;
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    public static boolean buyCustomization(int userId) throws SQLException {
        return applyTransaction(userId, "SPEND", CUSTOMIZATION, CUSTOMIZATION_PRICE);
    }

    private static boolean applyTransaction(int userId, String type, String reason, int amount)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Lock the account so simultaneous claims cannot pay twice.
                if (number(connection, "SELECT user_id FROM `user` WHERE user_id = ? FOR UPDATE",
                        userId) == 0 || hasTransaction(connection, userId, type, reason)) {
                    connection.rollback();
                    return false;
                }

                if (type.equals("EARN")) {
                    if (!earned(connection, userId, reason)) {
                        connection.rollback();
                        return false;
                    }
                    creditWallet(connection, userId, amount);
                } else if (update(connection, "UPDATE currency_wallet SET balance = balance - ? "
                        + "WHERE user_id = ? AND balance >= ?", amount, userId, amount) != 1) {
                    connection.rollback();
                    return false;
                }

                recordTransaction(connection, userId, amount, type, reason);
                connection.commit();
                return true;
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    static void creditWallet(Connection connection, int userId, int amount)
            throws SQLException {
        update(connection, "INSERT INTO currency_wallet (user_id, balance) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE balance = balance + ?", userId, amount, amount);
    }

    static void recordTransaction(Connection connection, int userId, int amount,
            String type, String reason) throws SQLException {
        update(connection, "INSERT INTO currency_transaction "
                + "(user_id, amount, transaction_type, transaction_reason) VALUES (?, ?, ?, ?)",
                userId, amount, type, reason);
    }

    private static boolean earned(Connection connection, int userId, String title)
            throws SQLException {
        return number(connection, "SELECT COUNT(*) FROM user_achievement ua "
                + "JOIN achievement a ON a.achievement_id = ua.achievement_id "
                + "WHERE ua.user_id = ? AND a.achievement_name = ?", userId, title) > 0;
    }

    private static boolean hasTransaction(Connection connection, int userId, String type,
            String reason) throws SQLException {
        return number(connection, "SELECT COUNT(*) FROM currency_transaction "
                + "WHERE user_id = ? AND transaction_type = ? AND transaction_reason = ?",
                userId, type, reason) > 0;
    }

    private static int balance(Connection connection, int userId) throws SQLException {
        return number(connection, "SELECT balance FROM currency_wallet WHERE user_id = ?", userId);
    }

    static int number(Connection connection, String sql, Object... values)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getInt(1) : 0;
            }
        }
    }

    static int update(Connection connection, String sql, Object... values)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            return statement.executeUpdate();
        }
    }

    private static void bind(PreparedStatement statement, Object[] values) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            if (values[index] instanceof Integer number) {
                statement.setInt(index + 1, number);
            } else {
                statement.setString(index + 1, (String) values[index]);
            }
        }
    }
}
