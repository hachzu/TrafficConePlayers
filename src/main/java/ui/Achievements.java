package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import database.AchievementRepository;
import database.AchievementRepository.Achievement;
import database.AchievementRepository.Rewards;

final class Achievements extends JDialog {
    private final DefaultUI owner;
    private final int userId;
    private final JPanel cards = new JPanel(new GridLayout(0, 1, 0, 10));
    private final JLabel status = new JLabel("Loading achievements...");
    private final PlayerTheme theme = PlayerTheme.defaultOrange();
    private final ImageIcon badge = Icons.load("achievement-award.png", 36);
    private final ImageIcon earnedBadge = Icons.load("achievement-badge.png", 40);
    private final ImageIcon lock = Icons.load("achievement-lock.png", 32);
    private final ImageIcon cone = Icons.load("cone.png", 18);

    Achievements(DefaultUI owner, int userId) {
        super(owner, "Achievements", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.userId = userId;

        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(theme.panelBorder, 2));
        status.setOpaque(true);
        status.setIcon(cone);
        status.setBackground(theme.buttonBackground);
        status.setForeground(theme.buttonTextColor);
        status.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cards.setBackground(theme.panelBackground);
        cards.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(cards);
        scroll.setPreferredSize(new Dimension(700, 440));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        JPanel header = new JPanel(new BorderLayout());
        header.add(createTitleBar(), BorderLayout.NORTH);
        header.add(status, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        getRootPane().registerKeyboardAction(event -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        add(scroll, BorderLayout.CENTER);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
        load(null);
    }

    private JPanel createTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(theme.panelBorder);
        bar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel title = new JLabel("ACHIEVEMENTS", badge, JLabel.LEFT);
        title.setForeground(theme.buttonBackground);
        title.setFont(owner.currencyLabel.getFont().deriveFont(Font.BOLD, 16f));
        bar.add(title, BorderLayout.CENTER);

        JButton close = new JButton("X");
        close.setToolTipText("Close");
        close.setBackground(theme.accent);
        close.setForeground(theme.buttonTextColor);
        close.setPreferredSize(new Dimension(34, 28));
        close.setMargin(new java.awt.Insets(0, 0, 0, 0));
        close.setBorder(BorderFactory.createLineBorder(theme.panelBorder));
        close.setFocusPainted(false);
        close.setFont(owner.currencyLabel.getFont().deriveFont(Font.BOLD, 16f));
        close.addActionListener(event -> dispose());
        bar.add(close, BorderLayout.EAST);

        MouseAdapter drag = new MouseAdapter() {
            private Point offset;

            @Override
            public void mousePressed(MouseEvent event) {
                offset = event.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                setLocation(event.getXOnScreen() - offset.x, event.getYOnScreen() - offset.y);
            }
        };
        title.addMouseListener(drag);
        title.addMouseMotionListener(drag);
        return bar;
    }

    private void load(Callable<Boolean> claim) {
        for (var card : cards.getComponents()) {
            if (card instanceof JPanel row && ((BorderLayout) row.getLayout()).getLayoutComponent(BorderLayout.EAST)
                    instanceof JButton button) {
                button.setEnabled(false);
            }
        }
        new SwingWorker<Rewards, Void>() {
            private boolean credited;

            @Override
            protected Rewards doInBackground() throws Exception {
                credited = claim != null && claim.call();
                return AchievementRepository.loadRewards(userId);
            }

            @Override
            protected void done() {
                try {
                    Rewards rewards = get();
                    owner.currencyLabel.setText(String.format("%d", rewards.balance()));
                    if (isDisplayable()) {
                        showRewards(rewards);
                        if (claim != null && !credited) {
                            status.setText(status.getText() + "  |  Reward already claimed or unavailable");
                        }
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    showLoadError();
                } catch (ExecutionException error) {
                    showLoadError();
                }
            }
        }.execute();
    }

    private void showLoadError() {
        if (isDisplayable()) {
            status.setText("Could not load achievements. Close this window and try again.");
        }
    }

    private void showRewards(Rewards rewards) {
        status.setText(String.format("%d", rewards.balance()));
        cards.removeAll();
        for (Achievement achievement : rewards.achievements()) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(theme.buttonBackground);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(theme.panelBorder, 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            JLabel icon = new JLabel(achievement.earned() ? earnedBadge : lock);
            icon.setToolTipText(achievement.earned() ? "Earned" : "Locked");
            icon.setHorizontalAlignment(JLabel.CENTER);
            icon.setPreferredSize(new Dimension(40, 40));
            icon.setForeground(theme.buttonTextColor);
            row.add(icon, BorderLayout.WEST);

            JPanel text = new JPanel(new GridLayout(0, 1, 0, 5));
            text.setOpaque(false);
            JLabel title = new JLabel(achievement.title());
            title.setFont(owner.currencyLabel.getFont().deriveFont(Font.BOLD, 15f));
            title.setForeground(theme.buttonTextColor);
            text.add(title);
            JLabel description = new JLabel(achievement.description());
            description.setFont(description.getFont().deriveFont(Font.PLAIN, 13f));
            description.setForeground(theme.buttonTextColor);
            text.add(description);
            row.add(text, BorderLayout.CENTER);

            String reward = String.format("%d", achievement.coins());
            String label = achievement.claimed() ? "Claimed"
                    : achievement.earned() ? "Claim " + reward : reward;
            JButton claim = new JButton(label, achievement.claimed() ? null : cone);
            claim.setBackground(theme.textColor);
            claim.setPreferredSize(new Dimension(145, 36));
            claim.setForeground(theme.buttonTextColor);
            claim.setBorder(BorderFactory.createLineBorder(theme.panelBorder));
            claim.setFocusPainted(false);
            claim.setDisabledIcon(achievement.claimed() ? null : cone);
            claim.setEnabled(achievement.earned() && !achievement.claimed());
            claim.addActionListener(event ->
                    load(() -> AchievementRepository.claim(userId, achievement.title())));
            row.add(claim, BorderLayout.EAST);
            cards.add(row);
        }
        cards.revalidate();
        cards.repaint();
    }
}
