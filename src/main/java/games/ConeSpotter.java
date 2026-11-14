package games;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import ui.DefaultUI;
import ui.PlayerTheme;
import ui.Icons;

public final class ConeSpotter {
    private static final int ROUND_SECONDS = 20;
    private final JButton[] tiles = new JButton[16];
    private final JLabel status = new JLabel("Click the cone as quickly as you can.");
    private final JButton start;
    private final GameWindow window;
    private final Timer timer = new Timer(100, event -> tick());
    private int target;
    private int score;
    private int reward;
    private long endsAt;

    public static int play(Window parent, DefaultUI player) {
        ConeSpotter game = new ConeSpotter(parent, player);
        game.window.setVisible(true);
        return game.reward;
    }

    private ConeSpotter(Window parent, DefaultUI player) {
        PlayerTheme theme = player.getTheme();
        JPanel board = new JPanel(new GridLayout(4, 4, 6, 6));
        board.setBackground(theme.panelBackground());
        board.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        board.setPreferredSize(new Dimension(360, 260));
        var cone = Icons.load("cone.png", 24);
        var sign = Icons.load("stop.png", 24);

        for (int index = 0; index < tiles.length; index++) {
            int tile = index;
            tiles[index] = GameWindow.button(theme, "");
            tiles[index].setIcon(sign);
            tiles[index].setEnabled(false);
            tiles[index].addActionListener(event -> {
                if (tile == target && System.nanoTime() < endsAt) {
                    score++;
                    tiles[target].setIcon(sign);
                    target = (target + ThreadLocalRandom.current().nextInt(1, tiles.length))
                            % tiles.length;
                    tiles[target].setIcon(cone);
                    tick();
                }
            });
            board.add(tiles[index]);
        }

        start = GameWindow.button(theme, "Start");
        start.addActionListener(event -> {
            start.setEnabled(false);
            for (JButton tile : tiles) {
                tile.setEnabled(true);
            }
            target = ThreadLocalRandom.current().nextInt(tiles.length);
            tiles[target].setIcon(cone);
            endsAt = System.nanoTime() + ROUND_SECONDS * 1_000_000_000L;
            timer.start();
            tick();
        });
        window = new GameWindow(parent, player, "Cone Spotter",
                GameWindow.gameBody(theme, status, board, start));
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                timer.stop();
            }
        });
    }

    private void tick() {
        long remaining = endsAt - System.nanoTime();
        if (remaining > 0) {
            long seconds = (remaining + 999_999_999L) / 1_000_000_000L;
            status.setText("Found: " + score + "   |   Time: " + seconds + "s");
            return;
        }

        timer.stop();
        for (JButton tile : tiles) {
            tile.setEnabled(false);
        }
        reward = 5 + score * 3;
        window.dispose();
    }
}
