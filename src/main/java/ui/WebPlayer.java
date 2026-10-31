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

    public void seekTo(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0) {
        return;
    }

    runJavaScript("seekYouTubeVideo(" + seconds + ")");
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
