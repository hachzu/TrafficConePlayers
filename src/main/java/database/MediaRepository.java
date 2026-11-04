package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
    
        
    }
