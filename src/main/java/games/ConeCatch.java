package games;

import java.awt.Window;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import ui.DefaultUI;
import ui.PlayerTheme;
import ui.Icons;

public final class ConeCatch {
    private static final int WIDTH = 460;
    private static final int HEIGHT = 280;
    private static final int CONE_SIZE = 24;
    private static final int BASKET_WIDTH = 80;
    private static final int BASKET_Y = HEIGHT - 30;
    private static final int ROUND_SECONDS = 30;

    private final GameWindow window;
    private final JPanel field = new JPanel(null);
    private final JLabel fallingCone = new JLabel(Icons.load("cone.png", CONE_SIZE));
    private final JPanel basket = new JPanel();
    private final JLabel status = new JLabel();
    private final Timer timer = new Timer(30, event -> tick());

    private int score;
    private long endsAt;
    private boolean leftHeld;
    private boolean rightHeld;
    private int reward;

    public static int play(Window parent, DefaultUI player) {
        ConeCatch game = new ConeCatch(parent, player);
        game.window.setVisible(true);
        return game.reward;
    }

    private ConeCatch(Window parent, DefaultUI player) {
        PlayerTheme theme = player.getTheme();
        field.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        field.setBackground(theme.panelBackground());
        basket.setBackground(theme.accent());
        basket.setBorder(BorderFactory.createLineBorder(theme.buttonBackground(), 2));
        basket.setBounds((WIDTH - BASKET_WIDTH) / 2, BASKET_Y, BASKET_WIDTH, 12);
        fallingCone.setSize(CONE_SIZE, CONE_SIZE);
        fallingCone.setVisible(false);
        field.add(basket);
        field.add(fallingCone);

        status.setText("Hold Left or Right to catch falling cones.");
        JButton start = GameWindow.button(theme, "Start");
        start.addActionListener(event -> {
            start.setEnabled(false);
            startRound();
        });
        window = new GameWindow(parent, player, "Cone Catch",
                GameWindow.gameBody(theme, status, field, start));
        bindKey("pressed LEFT", () -> leftHeld = true);
        bindKey("released LEFT", () -> leftHeld = false);
        bindKey("pressed RIGHT", () -> rightHeld = true);
        bindKey("released RIGHT", () -> rightHeld = false);
        WindowAdapter listener = new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent event) {
                leftHeld = false;
                rightHeld = false;
            }

            @Override
            public void windowClosed(WindowEvent event) {
                timer.stop();
            }
        };
        window.addWindowFocusListener(listener);
        window.addWindowListener(listener);
    }

    private void bindKey(String key, Runnable action) {
        window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key), key);
        window.getRootPane().getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    private void startRound() {
        score = 0;
        endsAt = System.nanoTime() + ROUND_SECONDS * 1_000_000_000L;
        basket.setLocation((WIDTH - BASKET_WIDTH) / 2, BASKET_Y);
        dropCone();
        fallingCone.setVisible(true);
        timer.start();
        tick();
    }

    private void dropCone() {
        int x = ThreadLocalRandom.current().nextInt(WIDTH - CONE_SIZE + 1);
        fallingCone.setLocation(x, 0);
    }

    private void tick() {
        long remaining = endsAt - System.nanoTime();
        if (remaining <= 0) {
            timer.stop();
            fallingCone.setVisible(false);
            reward = 5 + score * 5;
            window.dispose();
            return;
        }

        int direction = (rightHeld ? 1 : 0) - (leftHeld ? 1 : 0);
        int x = Math.clamp(basket.getX() + direction * 10, 0, WIDTH - BASKET_WIDTH);
        basket.setLocation(x, BASKET_Y);
        fallingCone.setLocation(fallingCone.getX(), fallingCone.getY() + 5);
        if (fallingCone.getBounds().intersects(basket.getBounds())) {
            score++;
            dropCone();
        } else if (fallingCone.getY() >= HEIGHT) {
            dropCone();
        }
        long seconds = (remaining + 999_999_999L) / 1_000_000_000L;
        status.setText("Caught: " + score + "   |   Time: " + seconds + "s");
    }

}
