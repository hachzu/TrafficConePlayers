package com.tcp.ui;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.Map;
import java.util.function.BiConsumer;

/** Displays the YouTube player or a local audio/video file in the Swing window. */
public class MediaPlayerView extends JFXPanel {
	private final BiConsumer<String, String> metadataHandler;
	private WebEngine webEngine;
	private MediaPlayer mediaPlayer;

	public MediaPlayerView(String pageUrl, BiConsumer<String, String> metadataHandler) {
		this.metadataHandler = metadataHandler;
		Platform.runLater(() -> showYouTubePlayer(pageUrl));
	}

	private void showYouTubePlayer(String pageUrl) {
		if (mediaPlayer != null) {
			mediaPlayer.dispose();
			mediaPlayer = null;
		}
		WebView webView = new WebView();
		webEngine = webView.getEngine();
		webView.setContextMenuEnabled(true);
		webView.setZoom(1.0);
		webEngine.setJavaScriptEnabled(true);
		webEngine.titleProperty().addListener((observable, oldTitle, title) -> {
			if (title != null && !title.isBlank() && !title.equals("YouTube video player")) {
				Object pageTitle = webEngine.executeScript("document.body.dataset.mediaTitle || ''");
				Object pageAuthor = webEngine.executeScript("document.body.dataset.mediaAuthor || ''");
				String mediaTitle = pageTitle == null ? "" : pageTitle.toString();
				String channelName = pageAuthor == null ? "" : pageAuthor.toString();
				if (!mediaTitle.isBlank()) {
					publishMetadata(mediaTitle, channelName.isBlank() ? "Loading channel..." : channelName);
				}
			}
		});
		webEngine.load(pageUrl);
		setScene(new Scene(webView));
		publishMetadata("YouTube video", "YouTube");
	}

	public void openLocalMedia(File file) {
		Platform.runLater(() -> {
			if (mediaPlayer != null) {
				mediaPlayer.dispose();
			}
			webEngine = null;
			Media media = new Media(file.toURI().toString());
			media.getMetadata().addListener((MapChangeListener<String, Object>) change ->
				publishLocalMetadata(file, media.getMetadata()));

			MediaPlayer player = new MediaPlayer(media);
			mediaPlayer = player;
			player.setOnReady(() -> {
				publishLocalMetadata(file, media.getMetadata());
				MediaView mediaView = new MediaView(player);
				mediaView.setPreserveRatio(true);
				StackPane mediaPane = new StackPane(mediaView);
				mediaPane.widthProperty().addListener((observable, oldValue, newValue) ->
					mediaView.setFitWidth(newValue.doubleValue()));
				mediaPane.heightProperty().addListener((observable, oldValue, newValue) ->
					mediaView.setFitHeight(newValue.doubleValue()));
				setScene(new Scene(mediaPane));
			});
			player.setOnError(() -> publishMetadata(file.getName(), "Could not load media"));
		});
	}

	public void setPlaying(boolean playing) {
		Platform.runLater(() -> {
			if (mediaPlayer != null) {
				if (playing) {
					mediaPlayer.play();
				} else {
					mediaPlayer.pause();
				}
			} else if (webEngine != null && webEngine.getDocument() != null) {
				String command = playing ? "playVideo" : "pauseVideo";
				webEngine.executeScript("var frame = document.querySelector('iframe'); if (frame) "
						+ "frame.contentWindow.postMessage(JSON.stringify({event:'command',func:'"
						+ command + "',args:[]}), '*');");
			}
		});
	}

	private void publishLocalMetadata(File file, Map<String, Object> metadata) {
		String title = metadataValue(metadata, "title");
		String artist = metadataValue(metadata, "artist");
		if (title.isBlank()) {
			String name = file.getName();
			int extension = name.lastIndexOf('.');
			title = extension > 0 ? name.substring(0, extension) : name;
		}
		if (artist.isBlank()) {
			artist = "Unknown artist";
		}
		publishMetadata(title, artist);
	}

	private String metadataValue(Map<String, Object> metadata, String key) {
		Object value = metadata.get(key);
		return value == null ? "" : value.toString().trim();
	}

	private void publishMetadata(String title, String artist) {
		SwingUtilities.invokeLater(() -> metadataHandler.accept(title, artist));
	}
}
