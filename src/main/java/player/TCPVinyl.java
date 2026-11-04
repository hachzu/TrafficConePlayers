package player;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class TCPVinyl extends JPanel {
    private MediaPlayer player;
    private MediaView vinylView;

    public TCPVinyl() {
        super(new BorderLayout());
        setBackground(Color.BLACK);

        JFXPanel videoSurface = new JFXPanel();
        add(videoSurface, BorderLayout.CENTER);

        Platform.runLater(() -> {
            Media media = new Media(getClass().getResource(
                    "/assets/media/vinyl-loop.mp4").toExternalForm());
            player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);

            vinylView = new MediaView(player);
            vinylView.setFitWidth(366);
            vinylView.setFitHeight(226);
            vinylView.setPreserveRatio(true);
            videoSurface.setScene(new Scene(new StackPane(vinylView), 366, 226,
                    javafx.scene.paint.Color.BLACK));
        });
    }

    public void setPlaying(boolean playing) {
        Platform.runLater(() -> {
            if (player == null) {
                return;
            }

            if (playing) {
                player.play();
            } else {
                player.pause();
            }
        });
    }

    public void setVolume(double volume) {
        Platform.runLater(() -> {
            if (player != null) {
                player.setVolume(volume);
            }
        });
    }

    public void setVinylVisible(boolean visible) {
        Platform.runLater(() -> {
            if (vinylView != null) {
                vinylView.setVisible(visible);
            }
        });
    }

    public void close() {
        Platform.runLater(() -> {
            if (player != null) {
                player.dispose();
                player = null;
                vinylView = null;
            }
        });
    }
}
