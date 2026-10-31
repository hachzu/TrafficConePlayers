package ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WebPlayer extends JFXPanel{
    private  WebView webView;
    private  WebEngine engine;

    public WebPlayer(String url) {
        Platform.runLater(() -> {
        webView = new WebView();
        engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
            Scene scene = new Scene(webView);
            this.setScene(scene);
            loadURL(url);
        });
    }

    public void loadURL(String url) {
        Platform.runLater(() -> engine.load(url));
    }

    public void playVideo() {
        runJavaScript("playYouTubeVideo()");
    }

    public void pauseVideo() {
        runJavaScript("pauseYouTubeVideo()");
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

    public void getDuration(java.util.function.Consumer<Double> callback) {
    Platform.runLater(() -> {
        if (engine == null) {
            callback.accept(0.0);
            return;
        }

        Object result = engine.executeScript("getYouTubeDuration()");

        double duration = result instanceof Number
                ? ((Number) result).doubleValue()
                : 0.0;

        callback.accept(duration);
        });
    }

    public void setTimeline(double percentage) {
        if (!Double.isFinite(percentage)
            || percentage < 0
            || percentage > 1) {
        return;
    }

    getDuration(duration -> {
        if (duration <= 0) {
            return;
        }
         runJavaScript("setTimeline(" + percentage + ")");
    });

    }

    /* literally just calls the execution thread and executes the script's in the 
       html
    */
    private void runJavaScript(String script) {
        Platform.runLater(() -> {
            if (engine != null) {
                engine.executeScript(script);
            }
         });
    }
}
