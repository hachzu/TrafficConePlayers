package ui.TCPScripts;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import web.LocalServer;

public class WebPlayer extends JFXPanel implements TCPlayer.VideoBackend {
    private record Video(String title, String id) {}

    private static final List<Video> PLAYLIST = List.of(
            new Video("Featured video", "cY0qV7q9IC4"),
            new Video("Never Gonna Give You Up", "dQw4w9WgXcQ"),
            new Video("Never Fade Away", "P4bKZT_Eg4A"),
            new Video("In love with Myself", "gKoaAow1XLY"),
            new Video("Butchers Vanity" ,"vjBFftpQxxM"),
            new Video("Zako /Akita Neru", "kqj7b59D85Y"));

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
        selectTrack(currentIndex + 1);
    }

    @Override
    public void previousTrack() {
        selectTrack(currentIndex - 1);
    }

    private void selectTrack(int index) {
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
            DefaultListModel<String> titles = new DefaultListModel<>();
            for (Video video : PLAYLIST) titles.addElement(video.title());

            JList<String> videoList = new JList<>(titles);
            videoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            videoList.setSelectedIndex(currentIndex);
            videoList.addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting() && videoList.getSelectedIndex() >= 0) {
                    selectTrack(videoList.getSelectedIndex());
                }
            });

            JFrame playlistFrame = new JFrame("Web Videos");
            playlistFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            playlistFrame.add(new JScrollPane(videoList));
            playlistFrame.setSize(360, 240);
            playlistFrame.setLocationRelativeTo(this);
            playlistFrame.setVisible(true);
        });
    }
}
