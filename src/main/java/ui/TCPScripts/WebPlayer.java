package ui.TCPScripts;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import web.LocalServer;

public class WebPlayer extends JFXPanel implements TCPlayer.VideoBackend {
    private record Video(String title, String id) {}

    private static final List<Video> PLAYLIST = new ArrayList<>(List.of(
            new Video("Featured video", "cY0qV7q9IC4"),
            new Video("Never Gonna Give You Up", "dQw4w9WgXcQ"),
            new Video("Never Fade Away", "P4bKZT_Eg4A"),
            new Video("In love with Myself", "gKoaAow1XLY"),
            new Video("Butchers Vanity" ,"vjBFftpQxxM"),
            new Video("Zako /Akita Neru", "kqj7b59D85Y")));

    private WebView webView;
    private WebEngine engine;
    private LocalServer localServer;
    private volatile int currentIndex;

    /** Starts a local server for the bundled embed page and loads it in WebView. */
    public WebPlayer() throws IOException {
        localServer = new LocalServer(0);
        initialize(localServer.getURL());
    }

    /** Loads a supplied URL. The caller is responsible for hosting it. */
    public WebPlayer(String url) {
        initialize(url);
    }

    private void initialize(String url) {
        Platform.runLater(() -> {
            webView = new WebView();
            engine = webView.getEngine();
            engine.setJavaScriptEnabled(true);
            setScene(new Scene(webView));
            engine.load(url);
        });
    }

    @Override
    public JComponent getView() {
        return this;
    }

    @Override
    public void play() {
        runJavaScript("playYouTubeVideo()");
    }

    @Override
    public void pause() {
        runJavaScript("pauseYouTubeVideo()");
    }

    @Override
    public void setVolume(double volume) {
        double safeVolume = Math.max(0.0, Math.min(1.0, volume));
        setVolume((int) Math.round(safeVolume * 100));
    }

    public void setVolume(int volume) {
        int safeVolume = Math.max(0, Math.min(100, volume));
        runJavaScript("setYouTubeVolume(" + safeVolume + ")");
    }

    public void muteVideo() {
        runJavaScript("muteYouTubeVideo()");
    }

    public void unmuteVideo() {
        runJavaScript("unmuteYouTubeVideo()");
    }

    @Override
    public void seekTo(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0) return;
        runJavaScript("seekYouTubeVideo(" + seconds + ")");
    }

    @Override
    public void getDuration(Consumer<Double> callback) {
        runJavaScriptNumber("getYouTubeDuration()", callback);
    }

    @Override
    public void getCurrentTime(Consumer<Double> callback) {
        runJavaScriptNumber("getYouTubeCurrentTime()", callback);
    }

    private void runJavaScriptNumber(String script, Consumer<Double> callback) {
        Platform.runLater(() -> {
            if (engine == null || engine.getDocument() == null) {
                callback.accept(0.0);
                return;
            }
            Object result = engine.executeScript(script);
            callback.accept(result instanceof Number
                    ? ((Number) result).doubleValue()
                    : 0.0);
        });
    }

    @Override
    public void nextTrack() {
        if (!PLAYLIST.isEmpty()) selectTrack(currentIndex + 1);
    }

    @Override
    public void previousTrack() {
        if (!PLAYLIST.isEmpty()) selectTrack(currentIndex - 1);
    }

    private void selectTrack(int index) {
        if (PLAYLIST.isEmpty()) return;
        currentIndex = Math.floorMod(index, PLAYLIST.size());
        String videoId = PLAYLIST.get(currentIndex).id();
        runJavaScript("loadYouTubeVideo('" + videoId + "')");
    }

    @Override
    public void close() {
        Platform.runLater(() -> {
            if (engine != null) engine.load("about:blank");
        });
        if (localServer != null) {
            localServer.close();
            localServer = null;
        }
    }

    private void runJavaScript(String script) {
        Platform.runLater(() -> {
            if (engine != null && engine.getDocument() != null) {
                engine.executeScript(script);
            }
        });
    }

    @Override 
    public void viewplaylist() {
        SwingUtilities.invokeLater(() -> {
            JFrame playlistFrame = new JFrame("Web Videos");
            playlistFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel rows = new JPanel();
            rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
            JPanel content = new JPanel(new java.awt.BorderLayout());
            content.add(new JScrollPane(rows), java.awt.BorderLayout.CENTER);
            JButton addButton = new JButton("Add YouTube URL or ID");
            addButton.addActionListener(event -> addPlaylistEntry(playlistFrame, rows));
            content.add(addButton, java.awt.BorderLayout.SOUTH);
            playlistFrame.setContentPane(content);
            rebuildPlaylistRows(rows, playlistFrame);
            playlistFrame.setSize(460, 360);
            playlistFrame.setLocationRelativeTo(this);
            playlistFrame.setVisible(true);
        });
    }

    private void rebuildPlaylistRows(JPanel rows, JFrame frame) {
        rows.removeAll();
        for (int i = 0; i < PLAYLIST.size(); i++) {
            final int index = i;
            Video video = PLAYLIST.get(i);
            String reference = "https://www.youtube.com/watch?v=" + video.id();
            JPanel row = new JPanel(new java.awt.BorderLayout(8, 4));
            row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            JPanel labels = new JPanel();
            labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
            labels.add(new JLabel(video.title()));
            labels.add(new JLabel(reference));

            JPanel buttons = new JPanel();
            JButton playButton = new JButton("Play");
            playButton.addActionListener(event -> selectTrack(index));
            JButton urlButton = new JButton("URL");
            urlButton.addActionListener(event -> JOptionPane.showMessageDialog(frame,
                    reference, "YouTube video URL", JOptionPane.INFORMATION_MESSAGE));
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(event -> {
                int answer = JOptionPane.showConfirmDialog(frame,
                        "Delete this video from the playlist?\n" + reference,
                        "Delete video", JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) return;
                boolean deletingCurrent = index == currentIndex;
                PLAYLIST.remove(index);
                if (PLAYLIST.isEmpty()) {
                    currentIndex = 0;
                    runJavaScript("pauseYouTubeVideo()");
                } else {
                    if (index < currentIndex) currentIndex--;
                    else if (deletingCurrent) currentIndex = Math.min(index, PLAYLIST.size() - 1);
                    if (deletingCurrent) selectTrack(currentIndex);
                }
                rebuildPlaylistRows(rows, frame);
            });
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
        String input = JOptionPane.showInputDialog(frame,
                "Enter a YouTube video URL or 11-character video ID:");
        if (input == null || input.isBlank()) return;
        String id = extractVideoId(input.trim());
        if (id == null) {
            JOptionPane.showMessageDialog(frame, "That doesn't look like a YouTube video URL or ID.",
                    "Invalid video", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (Video video : PLAYLIST) {
            if (video.id().equals(id)) {
                JOptionPane.showMessageDialog(frame, "That video is already in the playlist.");
                return;
            }
        }
        PLAYLIST.add(new Video("YouTube video (" + id + ")", id));
        rebuildPlaylistRows(rows, frame);
    }

    private static String extractVideoId(String input) {
        if (input.matches("[A-Za-z0-9_-]{11}")) return input;
        try {
            URI uri = URI.create(input);
            String host = uri.getHost();
            if (host == null) return null;
            String id = null;
            if (host.equals("youtu.be") || host.endsWith(".youtu.be")) {
                id = uri.getPath().replaceFirst("^/", "");
            } else if (host.endsWith("youtube.com") || host.endsWith("youtube-nocookie.com")) {
                if (uri.getPath().equals("/watch")) {
                    String query = uri.getRawQuery();
                    if (query != null) {
                        for (String pair : query.split("&")) {
                            if (pair.startsWith("v=")) id = pair.substring(2);
                        }
                    }
                } else if (uri.getPath().startsWith("/embed/")
                        || uri.getPath().startsWith("/shorts/")) {
                    id = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
                }
            }
            return id != null && id.matches("[A-Za-z0-9_-]{11}") ? id : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
