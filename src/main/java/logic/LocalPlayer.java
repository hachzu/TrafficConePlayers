package logic;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import database.AchievementRepository;
import database.MediaRepository;
import database.MediaRepository.MediaItem;
import database.PlaylistRepository;
import database.PlaylistRepository.SavedPlaylist;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class LocalPlayer extends JPanel {
    private static final String VIDEO_CARD = "video";
    private static final String VINYL_CARD = "vinyl";
    private static final Path TRACKS_FOLDER = Path.of("src", "main", "resources", "Tracks");
    private static final String[] MEDIA_EXTENSIONS = {"mp4", "m4v", "mov", "mp3", "m4a", "wav"};

    private static final Path IMPORTS_FOLDER = TRACKS_FOLDER.resolve("Imported");

    private final int userId;
    private final List<MediaItem> libraryTracks = new ArrayList<>();
    private final List<MediaItem> orderedTracks = new ArrayList<>();
    private final List<MediaItem> playlist = new ArrayList<>();
    private boolean shuffleEnabled;
    private boolean playingSavedPlaylist;
    private final ExecutorService mediaTasks = Executors.newSingleThreadExecutor();
    private final JFXPanel videoSurface = new JFXPanel();
    private final Vinyl vinylPanel = new Vinyl();

    private int currentIndex;
    private volatile boolean audioOnly;
    private volatile boolean videoMode;
    private volatile String currentTrackTitle = "";
    private MediaPlayer mediaPlayer;
    private volatile boolean closed;

    private volatile Consumer<Boolean> audioTrackListener = audioOnly -> { };

    public LocalPlayer(int userId) {
        super(new CardLayout());
        this.userId = userId;

        add(videoSurface, VIDEO_CARD);
        add(vinylPanel, VINYL_CARD);
        ((CardLayout) getLayout()).show(this, VINYL_CARD);
        vinylPanel.setPlaying(true);

        mediaTasks.execute(this::loadPlaylist);
    }

    public JComponent getView() {
        return this;
    }

    private void loadPlaylist() {
        List<MediaItem> tracks = new ArrayList<>();
        addBundledTracks(tracks);
        addSavedTracks(tracks);

        Platform.runLater(() -> {
            if (!closed) {
                libraryTracks.addAll(tracks);
                orderedTracks.addAll(tracks);
                arrangePlayback(null);
                loadTrack(0, false);
            }
        });
    }

    private void addBundledTracks(List<MediaItem> tracks) {
        File[] files = TRACKS_FOLDER.toFile().listFiles();
        if (files == null) {
            System.err.println("Could not load the local Tracks folder: " + TRACKS_FOLDER);
            return;
        }

        Arrays.sort(files);
        for (File file : files) {
            Path path = file.toPath();
            if (file.isFile() && isSupportedMedia(path)) {
                tracks.add(new MediaItem(0, file.getName(),
                        path.toAbsolutePath().normalize().toString()));
            }
        }
    }

    private void addSavedTracks(List<MediaItem> tracks) {
        try {
            for (MediaItem item : MediaRepository.loadMedia(userId)) {
                if (hasSupportedPath(item)) {
                    tracks.add(item);
                }
            }
        } catch (SQLException error) {
            showMediaError("Could not load saved media");
        }
    }

    private static boolean hasSupportedPath(MediaItem item) {
        try {
            return isSupportedMedia(Path.of(item.sourceLocation()));
        } catch (InvalidPathException error) {
            return false;
        }
    }

    private void loadTrack(int index, boolean autoplay) {
        if (playlist.isEmpty()) {
            return;
        }

        disposeMediaPlayer();

        currentIndex = Math.floorMod(index, playlist.size());
        MediaItem track = playlist.get(currentIndex);
        Path mediaPath = Path.of(track.sourceLocation()).toAbsolutePath().normalize();
        currentTrackTitle = track.title();
        audioOnly = isAudioResource(track.sourceLocation());
        if (audioOnly) {
            setVideoMode(false);
        }
        audioTrackListener.accept(audioOnly);

        if (!Files.isRegularFile(mediaPath)) {
            System.err.println("Media file is missing: " + mediaPath);
            return;
        }

        Media media = new Media(mediaPath.toUri().toString());
        MediaPlayer newPlayer = new MediaPlayer(media);
        mediaPlayer = newPlayer;
        showVideo(newPlayer);

        newPlayer.setOnReady(() -> {
            // The ready callback can arrive after another track has been selected.
            if (autoplay && mediaPlayer == newPlayer) {
                newPlayer.play();
            }
        });
        newPlayer.setOnEndOfMedia(this::nextTrack);

        newPlayer.setOnError(() -> {
            Throwable error = newPlayer.getError();
            if (error != null) {
                error.printStackTrace();
            }
        });
    }

    private void showVideo(MediaPlayer player) {
        MediaView mediaView = new MediaView(player);
        mediaView.setPreserveRatio(true);

        StackPane videoRoot = new StackPane(mediaView);
        videoRoot.setStyle("-fx-background-color: black;");
        mediaView.fitWidthProperty().bind(videoRoot.widthProperty());
        mediaView.fitHeightProperty().bind(videoRoot.heightProperty());

        videoSurface.setScene(new Scene(videoRoot));
    }

    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public void importMedia() {
        SwingUtilities.invokeLater(this::chooseAndImportMedia);
    }

    private void chooseAndImportMedia() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(
                new FileNameExtensionFilter("Video and audio files", MEDIA_EXTENSIONS));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION || closed) {
            return;
        }

        File[] selectedFiles = chooser.getSelectedFiles();
        if (selectedFiles.length > 0) {
            mediaTasks.submit(() -> {
                importTracks(selectedFiles);
                return null;
            });
        }
    }

    private void importTracks(File[] selectedFiles) throws IOException, SQLException {
        for (File file : selectedFiles) {
            Path selectedFile = file.toPath();
            if (isSupportedMedia(selectedFile)) {
                MediaItem track = importTrack(selectedFile);
                Platform.runLater(() -> addImportedTrack(track));
                refreshAchievements();
            }
        }
    }

    private MediaItem importTrack(Path selectedFile) throws IOException, SQLException {
        Files.createDirectories(IMPORTS_FOLDER);
        String title = selectedFile.getFileName().toString();
        String extension = title.substring(title.lastIndexOf('.'));
        Path destination = IMPORTS_FOLDER.resolve(UUID.randomUUID() + extension)
                .toAbsolutePath().normalize();
        Files.copy(selectedFile, destination, StandardCopyOption.COPY_ATTRIBUTES);

        // The existing schema has one category for audio and one for video.
        String mediaType = isAudioResource(title) ? "MP3" : "MP4";
        return MediaRepository.saveMedia(userId, title, mediaType, destination.toString());
    }

    private void addImportedTrack(MediaItem track) {
        if (closed) {
            return;
        }
        libraryTracks.add(track);
        if (playingSavedPlaylist) {
            return;
        }
        orderedTracks.add(track);
        playlist.add(track);
        if (playlist.size() == 1) {
            loadTrack(0, false);
        }
    }

    private void refreshAchievements() {
        try {
            AchievementRepository.refresh(userId);
        } catch (SQLException error) {
            System.err.println("Could not check achievements.");
        }
    }

    private void showMediaError(String message) {
        SwingUtilities.invokeLater(() -> {
            if (!closed) {
                JOptionPane.showMessageDialog(this, message,
                        "Media", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void setVideoMode(boolean videoMode) {
        this.videoMode = videoMode && !audioOnly;
        SwingUtilities.invokeLater(() -> {
            CardLayout cards = (CardLayout) getLayout();
            cards.show(this, this.videoMode ? VIDEO_CARD : VINYL_CARD);
            vinylPanel.setPlaying(!this.videoMode);
        });
    }

    private static boolean isSupportedMedia(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        String filename = fileName.toString().toLowerCase(Locale.ROOT);
        for (String extension : MEDIA_EXTENSIONS) {
            if (filename.endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAudioResource(String resource) {
        String filename = resource.toLowerCase(Locale.ROOT);
        return filename.endsWith(".m4a") || filename.endsWith(".mp3")
                || filename.endsWith(".wav");
    }

    public void setAudioTrackListener(Consumer<Boolean> listener) {
        audioTrackListener = listener;
        listener.accept(audioOnly);
    }

    public void play() {
        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            }
        });
    }

    public void pause() {
        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });
    }

    public void getDuration(Consumer<Double> callback) {
        Platform.runLater(() -> callback.accept(secondsOrZero(totalDuration())));
    }

    public void getCurrentTime(Consumer<Double> callback) {
        Platform.runLater(() -> callback.accept(secondsOrZero(currentTime())));
    }

    public void getTrackDetails(Consumer<TrackDetails> callback) {
        Platform.runLater(() -> {
            double duration = Math.max(0.0, secondsOrZero(totalDuration()));
            double currentTime = Math.max(0.0, secondsOrZero(currentTime()));
            int trackNumber = playlist.isEmpty() ? 0 : currentIndex + 1;
            callback.accept(new TrackDetails(currentTrackTitle, trackNumber,
                    playlist.size(), currentTime, duration));
        });
    }

    public record TrackDetails(String title, int trackNumber, int trackCount,
            double currentTime, double duration) { }

    private Duration totalDuration() {
        return mediaPlayer == null ? null : mediaPlayer.getTotalDuration();
    }

    private Duration currentTime() {
        return mediaPlayer == null ? null : mediaPlayer.getCurrentTime();
    }

    private static double secondsOrZero(Duration duration) {
        double seconds = duration == null ? 0.0 : duration.toSeconds();
        return Double.isFinite(seconds) ? seconds : 0.0;
    }

    public void setShuffle(boolean enabled) {
        Platform.runLater(() -> {
            if (closed || shuffleEnabled == enabled) {
                return;
            }
            MediaItem currentTrack = playlist.isEmpty() ? null : playlist.get(currentIndex);
            shuffleEnabled = enabled;
            arrangePlayback(currentTrack);
        });
    }

    private void arrangePlayback(MediaItem currentTrack) {
        playlist.clear();
        playlist.addAll(orderedTracks);
        if (shuffleEnabled) {
            playlist.remove(currentTrack);
            Collections.shuffle(playlist);
            if (currentTrack != null) {
                playlist.add(0, currentTrack);
            }
        }
        currentIndex = Math.max(0, playlist.indexOf(currentTrack));
    }

    public void nextTrack() {
        Platform.runLater(() -> loadTrack(currentIndex + 1, true));
    }

    public void previousTrack() {
        Platform.runLater(() -> loadTrack(currentIndex - 1, true));
    }

    public void close() {
        closed = true;
        mediaTasks.shutdown();
        Platform.runLater(this::disposeMediaPlayer);
        vinylPanel.close();
    }

    public void viewPlaylist() {
        if (closed) {
            return;
        }
        mediaTasks.execute(() -> {
            try {
                List<SavedPlaylist> saved = PlaylistRepository.loadPlaylists(userId);
                SwingUtilities.invokeLater(() -> showPlaylistMenu(saved));
            } catch (SQLException error) {
                showMediaError("Could not load playlists.");
            }
        });
    }

    private void showPlaylistMenu(List<SavedPlaylist> saved) {
        if (closed) {
            return;
        }
        JList<SavedPlaylist> list = new JList<>(saved.toArray(new SavedPlaylist[0]));
        int action = showTrackOptions(list, "Playlists",
                new String[] {"Open / play playlists", "Create playlist", "All tracks", "Close"});

        if (closed) {
            return;
        }
        List<SavedPlaylist> selected = list.getSelectedValuesList();
        if (action == 0 && selected.size() == 1) {
            openSavedPlaylist(selected.getFirst());
        } else if (action == 0 && selected.size() > 1) {
            playSavedPlaylists(selected);
        } else if (action == 1) {
            createPlaylist();
        } else if (action == 2) {
            Platform.runLater(() -> {
                List<MediaItem> tracks = List.copyOf(libraryTracks);
                SwingUtilities.invokeLater(() -> showPlaylist(tracks));
            });
        }
    }

    private void playSavedPlaylists(List<SavedPlaylist> selected) {
        mediaTasks.execute(() -> {
            try {
                List<MediaItem> tracks = new ArrayList<>();
                for (SavedPlaylist saved : selected) {
                    for (MediaItem track : PlaylistRepository.loadTracks(userId, saved.id())) {
                        if (hasSupportedPath(track) && !tracks.contains(track)) {
                            tracks.add(track);
                        }
                    }
                }
                if (tracks.isEmpty()) {
                    showMediaError("The selected playlists have no playable tracks.");
                    return;
                }
                Platform.runLater(() -> playQueue(tracks, 0, true));
            } catch (SQLException error) {
                showMediaError("Could not open the selected playlists.");
            }
        });
    }

    private void createPlaylist() {
        String name = JOptionPane.showInputDialog(this, "Playlist name");
        if (name == null || closed) {
            return;
        }
        String playlistName = name.strip();
        if (playlistName.isEmpty() || playlistName.codePointCount(0, playlistName.length()) > 100) {
            showMediaError("Enter a playlist name of 1 to 100 characters.");
            return;
        }
        mediaTasks.execute(() -> {
            try {
                PlaylistRepository.createPlaylist(userId, playlistName);
                SwingUtilities.invokeLater(this::viewPlaylist);
            } catch (SQLException error) {
                showMediaError("Could not create the playlist.");
            }
        });
    }

    private void openSavedPlaylist(SavedPlaylist saved) {
        if (closed) {
            return;
        }
        mediaTasks.execute(() -> {
            try {
                List<MediaItem> tracks = PlaylistRepository.loadTracks(userId, saved.id());
                tracks.removeIf(track -> !hasSupportedPath(track));
                SwingUtilities.invokeLater(() -> showSavedPlaylist(saved, tracks));
            } catch (SQLException error) {
                showMediaError("Could not open the playlist.");
            }
        });
    }

    private void showSavedPlaylist(SavedPlaylist saved, List<MediaItem> tracks) {
        if (closed) {
            return;
        }
        JList<String> list = trackList(tracks);
        int action = showTrackOptions(list, saved.name(),
                new String[] {"Play selection", "Add tracks", "Remove selected", "Back"});
        int index = list.getSelectedIndex();

        if (action == 0 && index >= 0) {
            playSelectedTracks(tracks, list.getSelectedIndices(), true);
        } else if (action == 1) {
            Platform.runLater(() -> {
                List<MediaItem> available = new ArrayList<>();
                for (MediaItem track : libraryTracks) {
                    if (track.mediaId() > 0 && !tracks.contains(track)) {
                        available.add(track);
                    }
                }
                SwingUtilities.invokeLater(() -> choosePlaylistTrack(saved, available));
            });
        } else if (action == 2 && index >= 0) {
            changePlaylistTracks(saved, selectedTracks(tracks, list.getSelectedIndices()), false);
        } else if (action >= 0) {
            viewPlaylist();
        }
    }

    private void choosePlaylistTrack(SavedPlaylist saved, List<MediaItem> tracks) {
        if (closed) {
            return;
        }
        if (tracks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No more imported tracks to add.");
            openSavedPlaylist(saved);
            return;
        }
        JList<String> list = trackList(tracks);
        int action = showTrackOptions(list, "Add to " + saved.name(),
                new String[] {"Add selected tracks", "Back"});
        if (action == 0 && list.getSelectedIndex() >= 0) {
            changePlaylistTracks(saved, selectedTracks(tracks, list.getSelectedIndices()), true);
        } else {
            openSavedPlaylist(saved);
        }
    }

    private void changePlaylistTracks(SavedPlaylist saved, List<MediaItem> tracks,
            boolean adding) {
        if (closed) {
            return;
        }
        mediaTasks.execute(() -> {
            try {
                for (MediaItem track : tracks) {
                    if (adding) {
                        if (PlaylistRepository.addTrack(userId, saved.id(), track.mediaId())) {
                            refreshAchievements();
                        }
                    } else {
                        PlaylistRepository.removeTrack(userId, saved.id(), track.mediaId());
                    }
                }
            } catch (SQLException error) {
                showMediaError("Could not update every selected track.");
            }
            SwingUtilities.invokeLater(() -> openSavedPlaylist(saved));
        });
    }

    private JList<String> trackList(List<MediaItem> tracks) {
        String[] titles = new String[tracks.size()];
        for (int index = 0; index < tracks.size(); index++) {
            titles[index] = tracks.get(index).title();
        }
        return new JList<>(titles);
    }

    private int showTrackOptions(JList<?> list, String title, String[] actions) {
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (list.getModel().getSize() > 0) {
            list.setSelectedIndex(0);
        }
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        Object[] content = {new JLabel("Ctrl or Shift to select multiple items."),
                scrollPane};
        return JOptionPane.showOptionDialog(this, content, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                actions, actions[0]);
    }

    private void showPlaylist(List<MediaItem> tracks) {
        if (tracks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The playlist is empty.");
            return;
        }

        JList<String> list = trackList(tracks);
        int action = showTrackOptions(list, "All tracks",
                new String[] {"Play selection", "Remove selected imports", "Back"});
        if (action == 2) {
            viewPlaylist();
            return;
        }
        int index = list.getSelectedIndex();
        if (index < 0 || closed) {
            return;
        }

        int[] selection = list.getSelectedIndices();
        if (action == 0) {
            playSelectedTracks(tracks, selection, false);
        } else if (action == 1) {
            removeSavedTracks(selectedTracks(tracks, selection));
        }
    }

    private List<MediaItem> selectedTracks(List<MediaItem> tracks, int[] selection) {
        List<MediaItem> selected = new ArrayList<>();
        for (int index : selection) {
            selected.add(tracks.get(index));
        }
        return selected;
    }

    private void playSelectedTracks(List<MediaItem> tracks, int[] selection,
            boolean saved) {
        if (selection.length == 1) {
            MediaItem track = tracks.get(selection[0]);
            Platform.runLater(() -> {
                List<MediaItem> queue = saved ? tracks : libraryTracks;
                int index = queue.indexOf(track);
                if (index >= 0) {
                    playQueue(queue, index, saved);
                }
            });
        } else {
            List<MediaItem> selected = selectedTracks(tracks, selection);
            Platform.runLater(() -> playQueue(selected, 0, true));
        }
    }

    private void playQueue(List<MediaItem> tracks, int index, boolean saved) {
        if (closed) {
            return;
        }
        playingSavedPlaylist = saved;
        orderedTracks.clear();
        orderedTracks.addAll(tracks);
        arrangePlayback(tracks.get(index));
        loadTrack(currentIndex, true);
    }

    private void removeSavedTracks(List<MediaItem> tracks) {
        List<MediaItem> imports = new ArrayList<>();
        for (MediaItem track : tracks) {
            if (track.mediaId() > 0) {
                imports.add(track);
            }
        }
        if (imports.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least one imported track to remove.");
            return;
        }

        mediaTasks.execute(() -> {
            try {
                for (MediaItem track : imports) {
                    MediaRepository.deleteMedia(userId, track.mediaId());
                    Platform.runLater(() -> removeFromPlaylist(track));
                }
            } catch (SQLException error) {
                showMediaError("Could not remove every selected import.");
            }
        });
    }

    private void removeFromPlaylist(MediaItem track) {
        libraryTracks.remove(track);
        orderedTracks.remove(track);
        int index = playlist.indexOf(track);
        if (index < 0 || closed) {
            return;
        }

        playlist.remove(index);
        if (index < currentIndex) {
            currentIndex--;
        } else if (index == currentIndex && !playlist.isEmpty()) {
            loadTrack(Math.min(index, playlist.size() - 1), false);
        } else if (playlist.isEmpty()) {
            disposeMediaPlayer();
            currentIndex = 0;
            currentTrackTitle = "";
        }
    }

    public void seekTo(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0) {
            return;
        }

        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(seconds));
            }
        });
    }

    public void setVolume(double volume) {
        double safeVolume = Math.max(0.0, Math.min(1.0, volume));
        vinylPanel.setVolume(safeVolume);
        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(safeVolume);
            }
        });
    }
}
