package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import database.AchievementRepository;
import games.ConeCatch;
import games.ConeSpotter;
import games.GameWindow;
import games.MiniGame;
import games.SignalMemory;

public final class Games {
    private record Game(String id, String title, String instructions, MiniGame play) {
        @Override
        public String toString() {
            return title;
        }
    }

    private final DefaultUI player;
    private final int userId;
    private final IntConsumer updateBalance;
    private final JLabel status = new JLabel(" ");
    private final JButton play;
    private final GameWindow window;
    private boolean saving;
    private UUID pendingRound;
    private Game pendingGame;
    private int pendingReward;

    public Games(DefaultUI player, int userId, IntConsumer updateBalance) {
        this.player = player;
        this.userId = userId;
        this.updateBalance = updateBalance;
        PlayerTheme theme = player.getTheme();

        DefaultListModel<Game> games = new DefaultListModel<>();
        games.addElement(new Game("cone-catch", "Cone Catch",
                "Hold Left or Right to catch falling cones.", ConeCatch::play));
        games.addElement(new Game("signal-memory", "Signal Memory",
                "Watch the flashing pads, then repeat the sequence.", SignalMemory::play));
        games.addElement(new Game("cone-spotter", "Cone Spotter",
                "Find the single cone among the signs before time runs out.", ConeSpotter::play));
        JList<Game> list = new JList<>(games);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setFixedCellHeight(36);
        list.setBackground(theme.panelBackground());
        list.setForeground(theme.buttonBackground());
        list.setSelectionBackground(theme.accent());
        list.setSelectionForeground(theme.buttonTextColor());

        JLabel instructions = new JLabel(list.getSelectedValue().instructions());
        instructions.setForeground(theme.buttonTextColor());
        list.addListSelectionListener(event -> {
            if (list.getSelectedValue() != null) {
                instructions.setText(list.getSelectedValue().instructions());
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(theme.buttonBackground());
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        body.setPreferredSize(new Dimension(460, 240));
        body.add(instructions, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(theme.panelBorder(), 2));
        body.add(scroll, BorderLayout.CENTER);

        play = GameWindow.button(theme, "Play");
        play.setBackground(theme.accent());
        play.addActionListener(event -> {
            if (pendingRound != null) {
                saveRound();
                return;
            }
            Game game = list.getSelectedValue();
            if (game != null) {
                playGame(game);
            }
        });
        status.setForeground(theme.buttonTextColor());
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.add(status, BorderLayout.CENTER);
        footer.add(play, BorderLayout.EAST);
        body.add(footer, BorderLayout.SOUTH);
        window = new GameWindow(player, player, "Games", body);
        window.getRootPane().setDefaultButton(play);
        window.setCloseHandler(() -> {
            if (!saving) {
                window.setVisible(false);
            }
        });
    }

    public void show() {
        window.setVisible(true);
    }

    private void playGame(Game game) {
        int reward = game.play().play(window, player);
        if (reward == 0) {
            return;
        }
        pendingGame = game;
        pendingRound = UUID.randomUUID();
        pendingReward = reward;
        saveRound();
    }

    private void saveRound() {
        saving = true;
        play.setEnabled(false);
        status.setText("Saving reward...");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return AchievementRepository.recordGameRound(
                        userId, pendingGame.id(), pendingRound, pendingReward);
            }

            @Override
            protected void done() {
                saving = false;
                play.setEnabled(true);
                try {
                    updateBalance.accept(get());
                    status.setText(pendingGame.title() + " earned " + pendingReward + " Cones.");
                    pendingRound = null;
                    play.setText("Play");
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    showRetry();
                } catch (ExecutionException error) {
                    showRetry();
                }
            }
        }.execute();
    }

    private void showRetry() {
        status.setText("Could not save the reward.");
        play.setText("Retry save");
    }
}
