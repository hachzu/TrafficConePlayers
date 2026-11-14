package games;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import ui.DefaultUI;
import ui.PlayerTheme;

public final class SignalMemory {
    private static final int MAX_LEVEL = 8;
    private final PlayerTheme theme;
    private final List<Integer> sequence = new ArrayList<>();
    private final JButton[] pads = new JButton[4];
    private final JLabel status = new JLabel("Watch the pads flash, then repeat the sequence.");
    private final JButton start;
    private final Timer timer = new Timer(330, event -> flashNext());
    private final GameWindow window;
    private int flashStep;
    private int entered;
    private int levels;
    private int reward;

    public static int play(Window parent, DefaultUI player) {
        SignalMemory game = new SignalMemory(parent, player);
        game.window.setVisible(true);
        return game.reward;
    }

    private SignalMemory(Window parent, DefaultUI player) {
        theme = player.getTheme();
        JPanel board = new JPanel(new GridLayout(2, 2, 10, 10));
        board.setBackground(theme.panelBackground());
        board.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        board.setPreferredSize(new Dimension(360, 220));
        for (int index = 0; index < pads.length; index++) {
            int pad = index;
            pads[index] = GameWindow.button(theme, Integer.toString(index + 1));
            pads[index].setEnabled(false);
            pads[index].addActionListener(event -> selectPad(pad));
            board.add(pads[index]);
        }
        start = GameWindow.button(theme, "Start");
        start.addActionListener(event -> startOrFinish());
        JPanel body = GameWindow.gameBody(theme, status, board, start);
        window = new GameWindow(parent, player, "Signal Memory", body);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                timer.stop();
            }
        });
    }

    private void startOrFinish() {
        if (reward > 0) {
            window.dispose();
            return;
        }
        start.setEnabled(false);
        nextLevel();
    }

    private void nextLevel() {
        entered = 0;
        flashStep = 0;
        sequence.add(ThreadLocalRandom.current().nextInt(pads.length));
        setPadsEnabled(false);
        status.setText("Level " + sequence.size() + "   |   Watch the sequence");
        timer.start();
    }

    private void flashNext() {
        for (JButton pad : pads) {
            pad.setBackground(theme.buttonBackground());
        }
        if (flashStep == sequence.size() * 2) {
            timer.stop();
            status.setText("Level " + sequence.size() + "   |   Your turn");
            setPadsEnabled(true);
            return;
        }
        if (flashStep % 2 == 0) {
            pads[sequence.get(flashStep / 2)].setBackground(theme.accent());
        }
        flashStep++;
    }

    private void selectPad(int pad) {
        if (pad != sequence.get(entered)) {
            finish();
            return;
        }
        entered++;
        if (entered == sequence.size()) {
            levels++;
            if (levels == MAX_LEVEL) {
                finish();
            } else {
                nextLevel();
            }
        }
    }

    private void finish() {
        setPadsEnabled(false);
        status.setText("Levels cleared: " + levels);
        start.setText("Finish");
        start.setEnabled(true);
        reward = 5 + levels * 10;
    }

    private void setPadsEnabled(boolean enabled) {
        for (JButton pad : pads) {
            pad.setEnabled(enabled);
        }
    }
}
