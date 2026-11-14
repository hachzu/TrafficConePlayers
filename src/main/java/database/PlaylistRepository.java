package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.MediaRepository.MediaItem;

public final class PlaylistRepository {
    private PlaylistRepository() { }

    public record SavedPlaylist(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    public static void createPlaylist(int userId, String name) throws SQLException {
        if (name.isBlank() || name.codePointCount(0, name.length()) > 100) {
            throw new IllegalArgumentException("Enter a playlist name of 1 to 100 characters.");
        }

        String sql = "INSERT INTO playlist (user_id, playlist_name) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }

    public static List<SavedPlaylist> loadPlaylists(int userId) throws SQLException {
        String sql = "SELECT playlist_id, playlist_name FROM playlist "
                + "WHERE user_id = ? ORDER BY playlist_name, playlist_id";
        List<SavedPlaylist> playlists = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    playlists.add(new SavedPlaylist(rows.getInt("playlist_id"),
                            rows.getString("playlist_name")));
                }
            }
        }
        return playlists;
    }

    public static List<MediaItem> loadTracks(int userId, int playlistId) throws SQLException {
        String sql = "SELECT media.media_id, media.title, media.source_location "
                + "FROM playlist_media "
                + "JOIN playlist ON playlist.playlist_id = playlist_media.playlist_id "
                + "JOIN media ON media.media_id = playlist_media.media_id "
                + "WHERE playlist.user_id = ? AND playlist.playlist_id = ? "
                + "AND media.user_id = playlist.user_id "
                + "ORDER BY playlist_media.position, playlist_media.playlist_media_id";
        List<MediaItem> tracks = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, playlistId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    tracks.add(new MediaItem(rows.getInt("media_id"), rows.getString("title"),
                            rows.getString("source_location")));
                }
            }
        }
        return tracks;
    }

    public static boolean addTrack(int userId, int playlistId, int mediaId)
            throws SQLException {
        String sql = "INSERT INTO playlist_media (playlist_id, media_id, position) "
                + "SELECT playlist.playlist_id, media.media_id, "
                + "COALESCE(MAX(existing.position), 0) + 1 "
                + "FROM playlist "
                + "JOIN media ON media.user_id = playlist.user_id "
                + "LEFT JOIN playlist_media AS existing "
                + "ON existing.playlist_id = playlist.playlist_id "
                + "WHERE playlist.user_id = ? AND playlist.playlist_id = ? AND media.media_id = ? "
                + "GROUP BY playlist.playlist_id, media.media_id "
                + "HAVING COUNT(CASE WHEN existing.media_id = media.media_id THEN 1 END) = 0";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, playlistId);
            statement.setInt(3, mediaId);
            return statement.executeUpdate() == 1;
        }
    }

    public static void removeTrack(int userId, int playlistId, int mediaId)
            throws SQLException {
        String sql = "DELETE FROM playlist_media WHERE playlist_id = ? AND media_id = ? "
                + "AND playlist_id IN (SELECT playlist_id FROM playlist WHERE user_id = ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, playlistId);
            statement.setInt(2, mediaId);
            statement.setInt(3, userId);
            statement.executeUpdate();
        }
    }
}
