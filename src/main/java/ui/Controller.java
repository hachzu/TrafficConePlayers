package ui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import database.AchievementRepository;
import database.AchievementRepository.Wallet;
import database.SessionRepository;
import database.StoreRepository;
import database.StoreRepository.Preset;
import database.UserRepository;
import database.UserRepository.User;
import logic.LocalPlayer;

class Controller {
    private final LandingPage landingPage;
    private LocalPlayer localPlayer;
    private DefaultUI playerWindow;
    private Timer timelineTimer;
    private boolean updatingTimeline;
    private long sessionStartedAt;
    private boolean closingPlayer;

    private record ActiveSession(User user, int id) { }

    Controller(LandingPage landingPage) {
        this.landingPage = landingPage;
        landingPage.continueButton.addActionListener(event -> showSignIn());
    }

    private void showSignIn() {
        JTextField username = new JTextField(20);
        JPasswordField password = new JPasswordField(20);
        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 5));
        fields.add(new JLabel("Username"));
        fields.add(username);
        fields.add(new JLabel("Password"));
        fields.add(password);

        int action = JOptionPane.showOptionDialog(landingPage, fields, "Sign in",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[] {"Sign in", "Create account", "Cancel"}, "Sign in");
        if (action != 0 && action != 1) {
            return;
        }

        char[] passwordInput = password.getPassword();
        String enteredPassword = new String(passwordInput);
        Arrays.fill(passwordInput, '\0');
        password.setText("");

        String enteredUsername = username.getText().strip();
        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            showSignInError("Enter your username and password.");
            return;
        }

        boolean creatingAccount = action == 1;
        if (creatingAccount && enteredUsername.codePointCount(0, enteredUsername.length()) > 50) {
            showSignInError("Use a username with at most 50 characters.");
            return;
        }

        authenticate(enteredUsername, enteredPassword, creatingAccount);
    }

    private void authenticate(String username, String password, boolean creatingAccount) {
        landingPage.continueButton.setEnabled(false);
        new SwingWorker<Optional<ActiveSession>, Void>() {
            @Override
            protected Optional<ActiveSession> doInBackground() throws SQLException {
                Optional<User> account = creatingAccount
                        ? UserRepository.createAccount(username, password)
                        : UserRepository.signIn(username, password);
                if (account.isEmpty()) {
                    return Optional.empty();
                }
                User user = account.get();
                int sessionId = SessionRepository.startSession(user.userId());
                return Optional.of(new ActiveSession(user, sessionId));
            }

            @Override
            protected void done() {
                landingPage.continueButton.setEnabled(true);
                try {
                    Optional<ActiveSession> result = get();
                    if (result.isPresent()) {
                        openPlayer(result.get());
                        return;
                    }
                    String message = creatingAccount
                            ? "That username is already taken."
                            : "Incorrect username or password.";
                    showSignInError(message);
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    showSignInError("Sign-in was interrupted. Try again.");
                } catch (ExecutionException error) {
                    showSignInError("Could not access your account. Check that MySQL is running.");
                }
            }
        }.execute();
    }

    private void showSignInError(String message) {
        JOptionPane.showMessageDialog(landingPage, message, "Sign in",
                JOptionPane.ERROR_MESSAGE);
    }

    private void openPlayer(ActiveSession session) {
        User user = session.user();
        sessionStartedAt = System.nanoTime();
        localPlayer = new LocalPlayer(user.userId());
        playerWindow = new DefaultUI(localPlayer.getView());
        playerWindow.accountMenuItem.setText(user.username() + " (ID " + user.userId() + ")");
        updateSessionTime();
        connectControls(session);
        playerWindow.setVisible(true);
        loadCoinBalance(user.userId());
        restorePreset(user.userId());
        landingPage.dispose();
    }

    private void connectControls(ActiveSession session) {
        connectPlaybackControls();
        connectAppButtons(session.user().userId());
        connectWindowControls(session);
    }

    private void connectPlaybackControls() {
        playerWindow.volumeSlider.addChangeListener(event ->
                localPlayer.setVolume(playerWindow.volumeSlider.getValue() / 100.0));
        localPlayer.setVolume(playerWindow.volumeSlider.getValue() / 100.0);

        playerWindow.timelineSlider.addChangeListener(event -> {
            if (!updatingTimeline && playerWindow.timelineSlider.getValueIsAdjusting()) {
                double position = (double) playerWindow.timelineSlider.getValue()
                        / playerWindow.timelineSlider.getMaximum();
                localPlayer.getDuration(duration -> localPlayer.seekTo(duration * position));
            }
        });

        timelineTimer = new Timer(250, event -> {
            updateTimeline();
            if (playerWindow.userMenu.isVisible()) {
                updateSessionTime();
            }
        });
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
        playerWindow.shuffleButton.addActionListener(event ->
                localPlayer.setShuffle(playerWindow.shuffleButton.isSelected()));
        playerWindow.playlistButton.addActionListener(event -> localPlayer.viewPlaylist());
        playerWindow.importButton.addActionListener(event -> localPlayer.importMedia());
        playerWindow.videoToggle.addActionListener(event -> localPlayer.setVideoMode(playerWindow.videoToggle.isSelected()));
        localPlayer.setAudioTrackListener(audioOnly -> SwingUtilities.invokeLater(() -> {
            playerWindow.videoToggle.setEnabled(!audioOnly);
            if (audioOnly) {
                playerWindow.videoToggle.setSelected(false);
                localPlayer.setVideoMode(false);
            }
        }));

    }

    private void connectAppButtons(int userId) {
        Games games = new Games(playerWindow, userId,
                balance -> playerWindow.currencyLabel.setText(Integer.toString(balance)));
        playerWindow.gamesButton.addActionListener(event -> games.show());
        playerWindow.achievementsButton.addActionListener(event ->
                new Achievements(playerWindow, userId).setVisible(true));
        playerWindow.storeButton.addActionListener(event -> new RoadsideStore(playerWindow, userId).show());
        playerWindow.settingsButton.addActionListener(event -> openSettings(userId));
        playerWindow.userButton.addActionListener(event -> {
            updateSessionTime();
            playerWindow.userMenu.show(playerWindow.userButton, 0,
                    playerWindow.userButton.getHeight());
        });
    }

    private void connectWindowControls(ActiveSession session) {
        playerWindow.minimizeButton.addActionListener(event ->
        playerWindow.setState(JFrame.ICONIFIED));
        playerWindow.closeButton.addActionListener(event -> playerWindow.dispatchEvent( new WindowEvent(playerWindow, WindowEvent.WINDOW_CLOSING)));
        playerWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                closePlayer(session);
            }
        });

        addButtonClickFeedback(playerWindow.userButton, playerWindow.videoToggle,
                playerWindow.settingsButton, playerWindow.previousButton,
                playerWindow.playPauseButton, playerWindow.stopButton, playerWindow.nextButton,
                playerWindow.shuffleButton, playerWindow.playlistButton, playerWindow.importButton,
                playerWindow.storeButton, playerWindow.gamesButton, playerWindow.achievementsButton, playerWindow.minimizeButton,
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

    private void loadCoinBalance(int userId) {
        new SwingWorker<Wallet, Void>() {
            @Override
            protected Wallet doInBackground() throws SQLException {
                return AchievementRepository.loadWallet(userId);
            }

            @Override
            protected void done() {
                try {
                    updateCoinBalance(get());
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException error) {
                    playerWindow.currencyLabel.setText("Unavailable");
                }
            }
        }.execute();
    }

    private void updateCoinBalance(Wallet wallet) {
        playerWindow.currencyLabel.setText(String.format("%d", wallet.balance()));
    }

    private void openSettings(int userId) {
        Object[] options = {"Themes", "Customize", "Close"};
        int choice = JOptionPane.showOptionDialog(playerWindow, "Player settings", "Settings",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            openThemes(userId);
        } else if (choice == 1) {
            openCustomization(userId);
        }
    }

    private void restorePreset(int userId) {
        playerWindow.settingsButton.setEnabled(false);
        new SwingWorker<PlayerTheme, Void>() {
            @Override
            protected PlayerTheme doInBackground() throws Exception {
                Preset preset = StoreRepository.equipped(userId);
                return PlayerTheme.preset(preset == null ? null : preset.asset());
            }

            @Override
            protected void done() {
                try {
                    playerWindow.applyTheme(get());
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException error) {
                    System.err.println("Could not restore the equipped theme.");
                } finally {
                    playerWindow.settingsButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void openThemes(int userId) {
        playerWindow.settingsButton.setEnabled(false);
        new SwingWorker<List<Preset>, Void>() {
            @Override
            protected List<Preset> doInBackground() throws SQLException {
                return StoreRepository.owned(userId);
            }

            @Override
            protected void done() {
                playerWindow.settingsButton.setEnabled(true);
                try {
                    List<Object> choices = new ArrayList<>();
                    String defaultTheme = "Original Orange";
                    choices.add(defaultTheme);
                    choices.addAll(get());
                    Object selected = JOptionPane.showInputDialog(playerWindow,
                            "Choose a theme to equip", "Themes", JOptionPane.PLAIN_MESSAGE,
                            null, choices.toArray(), defaultTheme);
                    if (selected != null) {
                        equipTheme(userId, selected instanceof Preset preset ? preset : null);
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException error) {
                    JOptionPane.showMessageDialog(playerWindow, "Could not load owned themes.");
                }
            }
        }.execute();
    }

    private void equipTheme(int userId, Preset preset) {
        playerWindow.settingsButton.setEnabled(false);
        new SwingWorker<PlayerTheme, Void>() {
            @Override
            protected PlayerTheme doInBackground() throws Exception {
                PlayerTheme theme = PlayerTheme.preset(preset == null ? null : preset.asset());
                if (!StoreRepository.equip(userId, preset)) {
                    throw new SQLException("Theme is not owned.");
                }
                return theme;
            }

            @Override
            protected void done() {
                playerWindow.settingsButton.setEnabled(true);
                try {
                    playerWindow.applyTheme(get());
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException error) {
                    JOptionPane.showMessageDialog(playerWindow, "Could not equip the theme.");
                }
            }
        }.execute();
    }

    private void openCustomization(int userId) {
        playerWindow.settingsButton.setEnabled(false);
        new SwingWorker<Wallet, Void>() {
            @Override
            protected Wallet doInBackground() throws SQLException {
                return AchievementRepository.loadWallet(userId);
            }

            @Override
            protected void done() {
                try {
                    Wallet wallet = get();
                    updateCoinBalance(wallet);
                    if (wallet.customizationUnlocked()) {
                        new ThemeEditor(playerWindow).setVisible(true);
                    } else {
                        offerCustomization(userId, wallet.balance());
                        return;
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException error) {
                    JOptionPane.showMessageDialog(playerWindow, "Could not check customization access.");
                }
                playerWindow.settingsButton.setEnabled(true);
            }
        }.execute();
    }

    private void offerCustomization(int userId, int balance) {
        int price = AchievementRepository.CUSTOMIZATION_PRICE;
        if (balance < price) {
            playerWindow.settingsButton.setEnabled(true);
            JOptionPane.showMessageDialog(playerWindow, String.format(
                    "Customization costs %,d Cones. Your balance is %,d Cones.", price, balance));
            return;
        }
        int choice = JOptionPane.showConfirmDialog(playerWindow,
                String.format("Spend %,d Cones to permanently unlock customization?", price),
                "Unlock customization", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            playerWindow.settingsButton.setEnabled(true);
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return AchievementRepository.buyCustomization(userId);
            }

            @Override
            protected void done() {
                playerWindow.settingsButton.setEnabled(true);
                try {
                    if (get()) {
                        loadCoinBalance(userId);
                        new ThemeEditor(playerWindow).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(playerWindow,
                                "The purchase could not be completed. Check your balance.");
                        loadCoinBalance(userId);
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException error) {
                    JOptionPane.showMessageDialog(playerWindow, "Could not unlock customization.");
                }
            }
        }.execute();
    }

    private void updateSessionTime() {
        double seconds = (System.nanoTime() - sessionStartedAt) / 1_000_000_000.0;
        playerWindow.sessionMenuItem.setText("Current session time: " + formatTime(seconds));
    }

    private void closePlayer(ActiveSession session) {
        if (closingPlayer) {
            return;
        }

        closingPlayer = true;
        timelineTimer.stop();
        localPlayer.close();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws SQLException {
                SessionRepository.endSession(session.user().userId(), session.id());
                try {
                    AchievementRepository.refresh(session.user().userId());
                } catch (SQLException error) {
                    System.err.println("Could not check session achievements.");
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException error) {
                    JOptionPane.showMessageDialog(playerWindow,
                            "Could not save the session end time.");
                } finally {
                    playerWindow.dispose();
                }
            }
        }.execute();
    }

    private void updateTimeline() {
        localPlayer.getTrackDetails(details -> SwingUtilities.invokeLater(() -> {
            playerWindow.trackTitle.setText(details.title().isBlank()
                    ? "NO TRACK" : details.title().toUpperCase(Locale.ROOT));
            playerWindow.trackInfo.setText(String.format(Locale.ROOT, "%02d / %02d   |   %s / %s",
                    details.trackNumber(), details.trackCount(),
                    formatTime(details.currentTime()), formatTime(details.duration())));

            if (details.duration() <= 0 || playerWindow.timelineSlider.getValueIsAdjusting()) {
                return;
            }
            updatingTimeline = true;
            playerWindow.timelineSlider.setValue((int) Math.round(
                    Math.max(0.0, Math.min(1.0, details.currentTime() / details.duration()))
                            * playerWindow.timelineSlider.getMaximum()));
            updatingTimeline = false;
        }));
    }

    private String formatTime(double seconds) {
        long totalSeconds = (long) seconds;
        long minutes = totalSeconds / 60;
        long remainingSeconds = totalSeconds % 60;
        if (minutes >= 60) {
            return String.format(Locale.ROOT, "%d:%02d:%02d",
                    minutes / 60, minutes % 60, remainingSeconds);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, remainingSeconds);
    }

    private void addButtonClickFeedback(AbstractButton... buttons) {
        var clickColor = playerWindow.theme.clickColor;

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
