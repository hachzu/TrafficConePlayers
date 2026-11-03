package ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import ui.player.LocalPlayer;

class TCPController {
    private final LandingPage landingPage;
    private LocalPlayer localPlayer;
    private TCPUI playerWindow;
    private Timer timelineTimer;

    TCPController(LandingPage landingPage) {
        this.landingPage = landingPage;
        landingPage.continueButton.addActionListener(event -> openPlayer());
    }

    private void openPlayer() {
        localPlayer = new LocalPlayer();
        playerWindow = new TCPUI(localPlayer.getView());
        connectControls();
        playerWindow.setVisible(true);
        landingPage.dispose();
    }

    private void connectControls() {
        playerWindow.volumeSlider.addChangeListener(event ->
                localPlayer.setVolume(playerWindow.volumeSlider.getValue() / 100.0));
        localPlayer.setVolume(playerWindow.volumeSlider.getValue() / 100.0);

        boolean[] updatingTimeline = {false};
        playerWindow.timelineSlider.addChangeListener(event -> {
            if (!updatingTimeline[0] && playerWindow.timelineSlider.getValueIsAdjusting()) {
                double position = (double) playerWindow.timelineSlider.getValue()
                        / playerWindow.timelineSlider.getMaximum();
                localPlayer.getDuration(duration -> localPlayer.seekTo(duration * position));
            }
        });

        timelineTimer = new Timer(250, event -> updateTimeline(updatingTimeline));
        timelineTimer.start();

        playerWindow.playPauseButton.addActionListener(event -> {
            if (playerWindow.playPauseButton.isSelected()) localPlayer.play();
            else localPlayer.pause();
        });
        playerWindow.stopButton.addActionListener(event -> {
            localPlayer.pause();
            localPlayer.seekTo(0);
            playerWindow.playPauseButton.setSelected(false);
        });
        playerWindow.nextButton.addActionListener(event -> {
            localPlayer.nextTrack();
            playerWindow.playPauseButton.setSelected(true);
        });
        playerWindow.previousButton.addActionListener(event -> {
            localPlayer.previousTrack();
            playerWindow.playPauseButton.setSelected(true);
        });
        playerWindow.playlistButton.addActionListener(event -> localPlayer.viewplaylist());
        playerWindow.importButton.addActionListener(event -> localPlayer.importMedia());
        playerWindow.videoToggle.addActionListener(event ->
                localPlayer.setVideoMode(playerWindow.videoToggle.isSelected()));

        localPlayer.setAudioTrackListener(audioOnly -> SwingUtilities.invokeLater(() -> {
            playerWindow.videoToggle.setEnabled(!audioOnly);
            if (audioOnly) {
                playerWindow.videoToggle.setSelected(false);
                localPlayer.setVideoMode(false);
            }
        }));

        playerWindow.minimizeButton.addActionListener(event ->
                playerWindow.setState(JFrame.ICONIFIED));
        playerWindow.closeButton.addActionListener(event -> playerWindow.dispatchEvent(
                new WindowEvent(playerWindow, WindowEvent.WINDOW_CLOSING)));
        playerWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                timelineTimer.stop();
                localPlayer.close();
            }
        });

        addOrangeClickFeedback(playerWindow.userButton, playerWindow.videoToggle,
                playerWindow.settingsButton, playerWindow.previousButton,
                playerWindow.playPauseButton, playerWindow.stopButton, playerWindow.nextButton,
                playerWindow.shuffleButton, playerWindow.playlistButton, playerWindow.importButton,
                playerWindow.storeButton, playerWindow.gamesButton, playerWindow.minimizeButton,
                playerWindow.closeButton);

        MouseAdapter dragWindow = new MouseAdapter() {
            private Point mouseOffset;

            @Override
            public void mousePressed(MouseEvent event) {
                mouseOffset = new Point(event.getXOnScreen() - playerWindow.getX(),
                        event.getYOnScreen() - playerWindow.getY());
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                playerWindow.setLocation(event.getXOnScreen() - mouseOffset.x,
                        event.getYOnScreen() - mouseOffset.y);
            }
        };
        playerWindow.titleBar.addMouseListener(dragWindow);
        playerWindow.titleBar.addMouseMotionListener(dragWindow);
        playerWindow.titleLabel.addMouseListener(dragWindow);
        playerWindow.titleLabel.addMouseMotionListener(dragWindow);
    }

    private void updateTimeline(boolean[] updatingTimeline) {
        localPlayer.getDuration(duration -> {
            if (duration <= 0 || !Double.isFinite(duration)) return;
            localPlayer.getCurrentTime(current -> SwingUtilities.invokeLater(() -> {
                if (playerWindow.timelineSlider.getValueIsAdjusting()
                        || !Double.isFinite(current)) return;
                updatingTimeline[0] = true;
                playerWindow.timelineSlider.setValue((int) Math.round(
                        Math.max(0.0, Math.min(1.0, current / duration))
                                * playerWindow.timelineSlider.getMaximum()));
                updatingTimeline[0] = false;
            }));
        });
    }

    private void addOrangeClickFeedback(AbstractButton... buttons) {
        Color clickColor = new Color(255, 102, 0);

        for (AbstractButton button : buttons) {
            Color normalColor = button.getBackground();
            boolean wasOpaque = button.isOpaque();
            boolean filledNormally = button.isContentAreaFilled();

            if (button instanceof JToggleButton) button.setUI(new BasicToggleButtonUI());
            else button.setUI(new BasicButtonUI());

            button.getModel().addChangeListener(event -> {
                boolean pressed = button.getModel().isArmed() && button.getModel().isPressed();
                button.setBackground(pressed ? clickColor : normalColor);
                button.setOpaque(pressed || wasOpaque);
                button.setContentAreaFilled(pressed || filledNormally);
            });
        }
    }
}
