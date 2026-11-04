package player;

import java.awt.CardLayout;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import database.MediaRepository;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class TCPLocalPlayer extends JPanel implements TCPComponents {
    private static final String VIDEO_CARD = "video";
    private static final String VINYL_CARD = "vinyl";

    private final List<String> playlist;
    private final JFXPanel videoSurface = new JFXPanel();
    private final TCPVinyl vinylPanel = new TCPVinyl();
    private volatile int currentIndex;
    private volatile int playlistSize;
    private volatile boolean audioOnly;
    private volatile boolean videoMode;
    private volatile String currentTrackTitle = "";
    private MediaPlayer mediaPlayer;
    private volatile Consumer<Boolean> audioTrackListener = audioOnly -> { };
    private JFrame playlistFrame;
    private JPanel playlistRows;

    public TCPLocalPlayer() {
        this(null);
    }

    public TCPLocalPlayer(String resourcePath) {
        super(new CardLayout());
        add(videoSurface, VIDEO_CARD);
        add(vinylPanel, VINYL_CARD);
        ((CardLayout) getLayout()).show(this, VINYL_CARD);
        vinylPanel.setPlaying(true);

        playlist = new ArrayList<>();
        if (resourcePath != null) playlist.add(resourcePath);
        for (String savedSource : MediaRepository.loadMedia(1)) {
        if (!playlist.contains(savedSource)) playlist.add(savedSource);
        }
        playlistSize = playlist.size();  
        currentIndex = 0;
        Platform.runLater(() -> loadTrack(currentIndex, false));
    }

    private static boolean isVideoResource(String resource) {
        String lower = resource.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".m4v")
                || lower.endsWith(".mov") || lower.endsWith(".m4a")
                || lower.endsWith(".mp3") || lower.endsWith(".wav");
    }

    private static boolean isAudioResource(String resource) {
        String lower = resource.toLowerCase();
        int queryStart = lower.indexOf('?');
        if (queryStart >= 0) lower = lower.substring(0, queryStart);
        return lower.endsWith(".m4a") || lower.endsWith(".mp3") || lower.endsWith(".wav");
    }

    public void setAudioTrackListener(Consumer<Boolean> listener) {
        audioTrackListener = listener;
        if (!playlist.isEmpty()) {
            listener.accept(isAudioResource(playlist.get(currentIndex)));
        }
    }

    private void loadTrack(int index, boolean autoplay) {
        if (playlist.isEmpty()) return;
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        currentIndex = Math.floorMod(index, playlist.size());
        String resourcePath = playlist.get(currentIndex);
        currentTrackTitle = titleFor(resourcePath);
        playlistSize = playlist.size();
        audioOnly = isAudioResource(resourcePath);
        if (audioOnly) setVideoMode(false);
        audioTrackListener.accept(audioOnly);
        URL mediaUrl;
        try {
            if (resourcePath.startsWith("http://") || resourcePath.startsWith("https://")
                    || resourcePath.startsWith("file:")) {
                mediaUrl = URI.create(resourcePath).toURL();
            } else {
                mediaUrl = TCPLocalPlayer.class.getResource(resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Invalid media location: " + resourcePath);
            return;
        }
        if (mediaUrl == null) {
            System.err.println("Media resource not found: " + resourcePath);
            return;
        }

        Media media = new Media(mediaUrl.toExternalForm());
        MediaPlayer newPlayer = new MediaPlayer(media);
        mediaPlayer = newPlayer;
        MediaView mediaView = new MediaView(newPlayer);
        mediaView.setPreserveRatio(true);
        StackPane videoRoot = new StackPane(mediaView);
        videoRoot.setStyle("-fx-background-color: black;");
        mediaView.fitWidthProperty().bind(videoRoot.widthProperty());
        mediaView.fitHeightProperty().bind(videoRoot.heightProperty());
        videoSurface.setScene(new Scene(videoRoot));

        newPlayer.setOnReady(() -> {
            if (autoplay && mediaPlayer == newPlayer) newPlayer.play();
        });
        newPlayer.setOnEndOfMedia(this::nextTrack);
        newPlayer.setOnError(() -> {
            Throwable error = newPlayer.getError();
            if (error != null) error.printStackTrace();
        });
    }

    @Override
    public JComponent getView() {
        return this;
    }

    public void setVideoMode(boolean videoMode) {
        this.videoMode = videoMode && !audioOnly;
        SwingUtilities.invokeLater(() -> {
            CardLayout cards = (CardLayout) getLayout();
            cards.show(this, this.videoMode ? VIDEO_CARD : VINYL_CARD);
            vinylPanel.setPlaying(!this.videoMode);
        });
    }

    @Override
    public void play() {
        Platform.runLater(() -> {
            if (mediaPlayer != null) mediaPlayer.play();
        });
    }

    @Override
    public void pause() {
        Platform.runLater(() -> {
            if (mediaPlayer != null) mediaPlayer.pause();
        });
    }

    @Override
    public void seekTo(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0) return;
        Platform.runLater(() -> {
            if (mediaPlayer != null) mediaPlayer.seek(Duration.seconds(seconds));
        });
    }

    @Override
    public void setVolume(double volume) {
        double safeVolume = Math.max(0.0, Math.min(1.0, volume));
        vinylPanel.setVolume(safeVolume);
        Platform.runLater(() -> {
            if (mediaPlayer != null) mediaPlayer.setVolume(safeVolume);
        });
    }

    @Override
    public void getDuration(Consumer<Double> callback) {
        Platform.runLater(() -> {
            double seconds = mediaPlayer == null
                    ? 0.0
                    : mediaPlayer.getTotalDuration().toSeconds();
            callback.accept(Double.isFinite(seconds) ? seconds : 0.0);
        });
    }

    @Override
    public void getCurrentTime(Consumer<Double> callback) {
        Platform.runLater(() -> {
            double seconds = mediaPlayer == null
                    ? 0.0
                    : mediaPlayer.getCurrentTime().toSeconds();
            callback.accept(Double.isFinite(seconds) ? seconds : 0.0);
        });
    }

    public void getTrackDetails(Consumer<TrackDetails> callback) {
        Platform.runLater(() -> {
            double duration = mediaPlayer == null
                    ? 0.0
                    : mediaPlayer.getTotalDuration().toSeconds();
            double currentTime = mediaPlayer == null
                    ? 0.0
                    : mediaPlayer.getCurrentTime().toSeconds();
            if (!Double.isFinite(duration) || duration < 0) duration = 0.0;
            if (!Double.isFinite(currentTime) || currentTime < 0) currentTime = 0.0;
            int trackNumber = playlistSize == 0 ? 0 : currentIndex + 1;
            callback.accept(new TrackDetails(currentTrackTitle, trackNumber,
                    playlistSize, currentTime, duration));
        });
    }

    public record TrackDetails(String title, int trackNumber, int trackCount,
            double currentTime, double duration) {
    }

    @Override
    public void nextTrack() {
        Platform.runLater(() -> {
            if (!playlist.isEmpty()) loadTrack(currentIndex + 1, true);
        });
    }

    @Override
    public void previousTrack() {
        Platform.runLater(() -> {
            if (!playlist.isEmpty()) loadTrack(currentIndex - 1, true);
        });
    }

    @Override
    public void close() {
        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
        });
        vinylPanel.close();
    }

    @Override 
    public void viewplaylist() {
        SwingUtilities.invokeLater(() -> {
            if (playlistFrame != null && playlistFrame.isDisplayable()) {
                playlistFrame.setVisible(true);
                playlistFrame.toFront();
                return;
            }

            playlistFrame = new JFrame("Videos");
            playlistFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            playlistRows = new JPanel();
            playlistRows.setLayout(new BoxLayout(playlistRows, BoxLayout.Y_AXIS));
            JPanel content = new JPanel(new java.awt.BorderLayout());
            content.add(new JScrollPane(playlistRows), java.awt.BorderLayout.CENTER);
            playlistFrame.setContentPane(content);
            rebuildPlaylistRows(playlistRows, playlistFrame);
            playlistFrame.setSize(360, 420);
            playlistFrame.setLocationRelativeTo(this);
            playlistFrame.setVisible(true);
        });
    }

    public void importMedia() {
        SwingUtilities.invokeLater(() -> {
            JFrame owner = playlistFrame != null && playlistFrame.isDisplayable()
                    ? playlistFrame : null;
            addPlaylistEntry(owner, owner == null ? null : playlistRows);
        });
    }

    private void rebuildPlaylistRows(JPanel rows, JFrame frame) {
        rows.removeAll();
        for (int i = 0; i < playlist.size(); i++) {
            final int index = i;
            String source = playlist.get(i);
            JPanel row = new JPanel(new java.awt.BorderLayout(8, 4));
            row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            JPanel labels = new JPanel();
            labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
            labels.add(new JLabel(titleFor(source)));
            labels.add(new JLabel(source));

            JButton playButton = new JButton("Play");
            playButton.addActionListener(event -> Platform.runLater(() -> loadTrack(index, true)));
            JButton urlButton = new JButton("Reference");
            urlButton.addActionListener(event -> JOptionPane.showMessageDialog(frame,
                    source, "Video reference", JOptionPane.INFORMATION_MESSAGE));
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(event -> {
                int answer = JOptionPane.showConfirmDialog(frame,
                        "Delete this video from the playlist?\n" + source,
                        "Delete video", JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) return;
                int playingIndex = currentIndex;
                playlist.remove(index);
                MediaRepository.deleteMedia(1, source);
                playlistSize = playlist.size();
                if (playlist.isEmpty()) {
                    currentIndex = 0;
                    currentTrackTitle = "";
                    Platform.runLater(() -> {
                        if (mediaPlayer != null) mediaPlayer.dispose();
                        mediaPlayer = null;
                        videoSurface.setScene(new Scene(new StackPane()));
                    });
                } else {
                    if (index < playingIndex) currentIndex = playingIndex - 1;
                    else if (index == playingIndex) currentIndex = Math.min(index, playlist.size() - 1);
                    currentTrackTitle = titleFor(playlist.get(currentIndex));
                    final int nextIndex = currentIndex;
                    if (index == playingIndex) Platform.runLater(() -> loadTrack(nextIndex, true));
                }
                rebuildPlaylistRows(rows, frame);
            });
            JPanel buttons = new JPanel();
            buttons.add(playButton);
            buttons.add(urlButton);
            buttons.add(deleteButton);
            row.add(labels, java.awt.BorderLayout.CENTER);
            row.add(buttons, java.awt.BorderLayout.EAST);
            rows.add(row);
        }
        rows.revalidate();
        rows.repaint();
        frame.revalidate();
    }

    private void addPlaylistEntry(JFrame frame, JPanel rows) {
        Object[] choices = {"Video file", "URL", "Cancel"};
        int choice = JOptionPane.showOptionDialog(frame, "Choose media to import:",
                "Import media", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, choices, choices[0]);
        String source = null;
        if (choice == 0) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Video and audio files", "mp4", "m4v", "mov", "mp3", "m4a", "wav"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                source = chooser.getSelectedFile().toURI().toString();
            }
        } else if (choice == 1) {
            source = JOptionPane.showInputDialog(frame, "Enter a media URL:");
            if (source != null) source = source.trim();
            if (source != null && !(source.startsWith("http://") || source.startsWith("https://"))) {
                JOptionPane.showMessageDialog(frame, "Enter a valid http or https media URL.",
                        "Invalid URL", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (source == null || source.isBlank() || playlist.contains(source)) return;
        playlist.add(source);
        MediaRepository.saveMedia(
        1,
        titleFor(source),
        isAudioResource(source) ? "MP3" : "MP4",
        source
        );
        playlistSize = playlist.size();
        if (rows != null && frame != null && frame.isDisplayable()) {
            rebuildPlaylistRows(rows, frame);
        }
    }

    private static String titleFor(String resource) {
        String path = resource;
        try {
            String uriPath = URI.create(resource).getPath();
            if (uriPath != null && !uriPath.isBlank()) path = uriPath;
        } catch (IllegalArgumentException ignored) {
        }
        String filename = path.substring(path.lastIndexOf('/') + 1);
        int extension = filename.lastIndexOf('.');
        if (extension > 0) filename = filename.substring(0, extension);
        return filename.replaceAll("[-_]+", " ").trim();
    }
}
