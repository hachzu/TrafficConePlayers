package ui.TCPScripts;

import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class LocalPlayer extends JFXPanel implements TCPlayer.VideoBackend {
    private static final String MEDIA_FOLDER = "/assets/media/";

    private final List<String> playlist;
    private volatile int currentIndex;
    private MediaPlayer mediaPlayer;

    public LocalPlayer() {
        this(null);
    }

    /** Pass a classpath resource, for example /assets/media/video.mp4. */
    public LocalPlayer(String resourcePath) {
        playlist = discoverPlaylist(resourcePath);
        currentIndex = resourcePath == null ? 0 : playlist.indexOf(resourcePath);
        if (currentIndex < 0) currentIndex = 0;
        Platform.runLater(() -> loadTrack(currentIndex, false));
    }

    private static List<String> discoverPlaylist(String selectedResource) {
        Set<String> resources = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        try {
            URL directory = LocalPlayer.class.getResource(MEDIA_FOLDER);
            if (directory != null && "file".equals(directory.getProtocol())) {
                try (var paths = Files.list(Path.of(directory.toURI()))) {
                    paths.filter(Files::isRegularFile)
                            .map(path -> MEDIA_FOLDER + path.getFileName())
                            .filter(LocalPlayer::isVideoResource)
                            .forEach(resources::add);
                }
            } else {
                if (directory != null && "jar".equals(directory.getProtocol())) {
                    JarURLConnection connection = (JarURLConnection) directory.openConnection();
                    JarFile jar = connection.getJarFile();
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        String folderPrefix = MEDIA_FOLDER.substring(1);
                        if (name.startsWith(folderPrefix)
                                && !name.substring(folderPrefix.length()).contains("/")
                                && isVideoResource("/" + name)) {
                            resources.add("/" + name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not list " + MEDIA_FOLDER + ": " + e.getMessage());
        }

        if (selectedResource != null && !resources.contains(selectedResource)) {
            resources.add(selectedResource);
        }

        List<String> ordered = new ArrayList<>(resources);
        ordered.sort(Comparator.comparing(String::toLowerCase));
        return ordered;
    }

    private static boolean isVideoResource(String resource) {
        String lower = resource.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".m4v")
                || lower.endsWith(".mov") || lower.endsWith(".m4a")
                || lower.endsWith(".mp3") || lower.endsWith(".wav");
    }

    private void loadTrack(int index, boolean autoplay) {
        if (playlist.isEmpty()) return;
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        currentIndex = Math.floorMod(index, playlist.size());
        String resourcePath = playlist.get(currentIndex);
        URL mediaUrl;
        try {
            if (resourcePath.startsWith("http://") || resourcePath.startsWith("https://")
                    || resourcePath.startsWith("file:")) {
                mediaUrl = URI.create(resourcePath).toURL();
            } else {
                mediaUrl = LocalPlayer.class.getResource(resourcePath);
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
        mediaView.setFitWidth(800);
        mediaView.setFitHeight(400);
        setScene(new Scene(new StackPane(mediaView)));

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
    }

    @Override 
    public void viewplaylist() {
        SwingUtilities.invokeLater(() -> {
            JFrame playlistFrame = new JFrame("Videos");
            playlistFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel rows = new JPanel();
            rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
            JPanel content = new JPanel(new java.awt.BorderLayout());
            content.add(new JScrollPane(rows), java.awt.BorderLayout.CENTER);
            JButton addButton = new JButton("Add video or URL");
            addButton.addActionListener(event -> addPlaylistEntry(playlistFrame, rows));
            content.add(addButton, java.awt.BorderLayout.SOUTH);
            playlistFrame.setContentPane(content);
            rebuildPlaylistRows(rows, playlistFrame);
            playlistFrame.setSize(360, 420);
            playlistFrame.setLocationRelativeTo(this);
            playlistFrame.setVisible(true);
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
                if (playlist.isEmpty()) {
                    currentIndex = 0;
                    Platform.runLater(() -> {
                        if (mediaPlayer != null) mediaPlayer.dispose();
                        mediaPlayer = null;
                        setScene(new Scene(new StackPane()));
                    });
                } else {
                    if (index < playingIndex) currentIndex = playingIndex - 1;
                    else if (index == playingIndex) currentIndex = Math.min(index, playlist.size() - 1);
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
        int choice = JOptionPane.showOptionDialog(frame, "Choose a video source to add:",
                "Add video", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
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
        rebuildPlaylistRows(rows, frame);
    }

    private static String titleFor(String resource) {
        String filename = resource.substring(resource.lastIndexOf('/') + 1);
        int extension = filename.lastIndexOf('.');
        if (extension > 0) filename = filename.substring(0, extension);
        return filename.replaceAll("[-_]+", " ").trim();
    }
}
