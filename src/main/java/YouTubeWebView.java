import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/** Hosts the JavaFX WebView used to display the YouTube player inside Swing. */
public class YouTubeWebView extends JFXPanel {
	public YouTubeWebView(String pageUrl) {
		Platform.runLater(() -> {
			WebView webView = new WebView();
			WebEngine engine = webView.getEngine();

			webView.setContextMenuEnabled(true);
			webView.setZoom(1.0);
			engine.setJavaScriptEnabled(true);
			engine.load(pageUrl);

			setScene(new Scene(webView));
		});
	}
}
