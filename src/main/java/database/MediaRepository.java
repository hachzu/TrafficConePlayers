package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaRepository {
    public static void saveMedia(int userId, String title,
            String mediaType, String sourceLocation) {

        String sql = """
                INSERT INTO media (user_id, title, media_type, source_location)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, title);
            statement.setString(3, mediaType);
            statement.setString(4, sourceLocation);

            statement.executeUpdate();
            System.out.println("Media saved.");

        } catch (SQLException error) {
            error.printStackTrace();
        }
    }

    public static List<String> loadMedia(int userId) {
        List<String> savedMedia = new ArrayList<>();

        String sql = """
                SELECT source_location
                FROM media
                WHERE user_id = ?
                ORDER BY date_added
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                savedMedia.add(result.getString("source_location"));
            }

        } catch (SQLException error) {
            error.printStackTrace();
        }

        return savedMedia;
    }
        public static void deleteMedia(int userId, String sourceLocation) {
        String sql = """
                DELETE FROM media
                WHERE user_id = ? AND source_location = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, sourceLocation);
            statement.executeUpdate();

        } catch (SQLException error) {
            error.printStackTrace();
        }
    }
}