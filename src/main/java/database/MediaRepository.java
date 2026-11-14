package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaRepository {
    public record MediaItem(int mediaId, String title, String sourceLocation) { }

    public static MediaItem saveMedia(int userId, String title, String mediaType,
            String sourceLocation) throws SQLException {
        String insertSql = "INSERT INTO media (user_id, title, media_type, source_location) "
                + "VALUES (?, ?, ?, ?)";
        String selectSql = "SELECT media_id, title, source_location FROM media "
                + "WHERE user_id = ? AND source_location = ? ORDER BY media_id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement insert = connection.prepareStatement(insertSql);
                PreparedStatement select = connection.prepareStatement(selectSql)) {
            insert.setInt(1, userId);
            insert.setString(2, title);
            insert.setString(3, mediaType);
            insert.setString(4, sourceLocation);
            if (insert.executeUpdate() != 1) {
                throw new SQLException("Could not save the track.");
            }

            select.setInt(1, userId);
            select.setString(2, sourceLocation);
            try (ResultSet rows = select.executeQuery()) {
                if (rows.next()) {
                    return readMedia(rows);
                }
            }
        }
        throw new SQLException("Could not read the saved track.");
    }

    public static List<MediaItem> loadMedia(int userId) throws SQLException {
        String sql = "SELECT media_id, title, source_location, media_type FROM media "
                + "WHERE user_id = ? ORDER BY date_added, media_id";
        List<MediaItem> savedMedia = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    String mediaType = rows.getString("media_type");
                    if ("MP3".equals(mediaType) || "MP4".equals(mediaType)) {
                        savedMedia.add(readMedia(rows));
                    }
                }
            }
        }
        return savedMedia;
    }

    public static void deleteMedia(int userId, int mediaId) throws SQLException {
        String sql = "DELETE FROM media WHERE user_id = ? AND media_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, mediaId);
            statement.executeUpdate();
        }
    }

    private static MediaItem readMedia(ResultSet rows) throws SQLException {
        return new MediaItem(rows.getInt("media_id"), rows.getString("title"),
                rows.getString("source_location"));
    }
}
