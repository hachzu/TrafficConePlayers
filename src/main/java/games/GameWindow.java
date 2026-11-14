package games;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ui.DefaultUI;
import ui.PlayerTheme;

public final class GameWindow extends JDialog {
    private Runnable closeHandler = this::dispose;
    public GameWindow(Window parent, DefaultUI player, String title, JComponent body) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        PlayerTheme theme = player.getTheme();
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel frame = new JPanel(new BorderLayout());
        frame.setBorder(BorderFactory.createLineBorder(theme.panelBorder(), 2));
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(theme.panelBorder());
        bar.setPreferredSize(new Dimension(0, 36));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel caption = new JLabel(title, player.getTitleIcon(), JLabel.LEFT);
        caption.setFont(player.getTitleFont());
        caption.setForeground(theme.textColor());
        JButton close = button(theme, "X");
        close.setPreferredSize(new Dimension(34, 28));
        close.setBackground(theme.accent());
        close.setFont(player.getTitleFont());
        close.setToolTipText("Close");
        close.addActionListener(event -> closeHandler.run());
        bar.add(caption, BorderLayout.CENTER);
        bar.add(close, BorderLayout.EAST);

        MouseAdapter drag = new MouseAdapter() {
            private int offsetX;
            private int offsetY;

            @Override
            public void mousePressed(MouseEvent event) {
                offsetX = event.getXOnScreen() - getX();
                offsetY = event.getYOnScreen() - getY();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                setLocation(event.getXOnScreen() - offsetX, event.getYOnScreen() - offsetY);
            }
        };
        caption.addMouseListener(drag);
        caption.addMouseMotionListener(drag);
        frame.add(bar, BorderLayout.NORTH);
        frame.add(body, BorderLayout.CENTER);
        setContentPane(frame);
        pack();
        setLocationRelativeTo(parent);
    }

    public void setCloseHandler(Runnable handler) {
        closeHandler = handler;
    }

    static JPanel gameBody(PlayerTheme theme, JLabel status, JComponent board,
            JButton action) {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(theme.buttonBackground());
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        status.setForeground(theme.buttonTextColor());
        action.setBackground(theme.accent());
        body.add(status, BorderLayout.NORTH);
        body.add(board, BorderLayout.CENTER);
        body.add(action, BorderLayout.SOUTH);
        return body;
    }

    public static JButton button(PlayerTheme theme, String label) {
        JButton button = new JButton(label);
        button.setBackground(theme.buttonBackground());
        button.setForeground(theme.buttonTextColor());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.panelBorder()),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        button.setFocusPainted(false);
        return button;
    }
}
