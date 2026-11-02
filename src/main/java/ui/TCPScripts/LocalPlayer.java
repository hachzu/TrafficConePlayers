package ui.TCPScripts;

import java.net.JarURLConnection;
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

import javax.swing.JComponent;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

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
    private static final String DEFAULT_MEDIA = MEDIA_FOLDER
            + "flow-together-venat-instance-with-official-lyrics-final-fantasy-xiv-.mp4";

    private final List<String> playlist;
    private int currentIndex;
    private MediaPlayer mediaPlayer;

    public LocalPlayer() {
        this(DEFAULT_MEDIA);
    }

    /** Pass a classpath resource, for example /assets/media/video.mp4. */
    public LocalPlayer(String resourcePath) {
        playlist = discoverPlaylist(resourcePath);
        currentIndex = playlist.indexOf(resourcePath);
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
                URL sample = LocalPlayer.class.getResource(DEFAULT_MEDIA);
                if (sample != null && "jar".equals(sample.getProtocol())) {
                    JarURLConnection connection = (JarURLConnection) sample.openConnection();
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

        if (resources.isEmpty()) resources.add(DEFAULT_MEDIA);
        if (!resources.contains(selectedResource)) resources.add(selectedResource);

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
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        currentIndex = Math.floorMod(index, playlist.size());
        String resourcePath = playlist.get(currentIndex);
        URL mediaUrl = LocalPlayer.class.getResource(resourcePath);
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
        Platform.runLater(() -> loadTrack(currentIndex + 1, true));
    }

    @Override
    public void previousTrack() {
        Platform.runLater(() -> loadTrack(currentIndex - 1, true));
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
            DefaultListModel<String> titles = new DefaultListModel<>();
            for (String resource : playlist) titles.addElement(titleFor(resource));

            JList<String> videoList = new JList<>(titles);
            videoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            videoList.setSelectedIndex(currentIndex);
            videoList.addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting() && videoList.getSelectedIndex() >= 0) {
                    int selectedIndex = videoList.getSelectedIndex();
                    Platform.runLater(() -> loadTrack(selectedIndex, true));
                }
            });

            JFrame playlistFrame = new JFrame("Videos");
            playlistFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            playlistFrame.add(new JScrollPane(videoList));
            playlistFrame.setSize(360, 420);
            playlistFrame.setLocationRelativeTo(this);
            playlistFrame.setVisible(true);
        });
    }

    private static String titleFor(String resource) {
        String filename = resource.substring(resource.lastIndexOf('/') + 1);
        int extension = filename.lastIndexOf('.');
        if (extension > 0) filename = filename.substring(0, extension);
        return filename.replaceAll("[-_]+", " ").trim();
    }
}
