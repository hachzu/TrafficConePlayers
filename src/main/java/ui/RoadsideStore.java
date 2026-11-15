package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import database.StoreRepository;
import database.StoreRepository.Crate;
import database.StoreRepository.Inventory;
import database.StoreRepository.Pull;
import games.GameWindow;

final class RoadsideStore {
    private final DefaultUI player;
    private final int userId;
    private final GameWindow window;
    private final JButton[] buttons = new JButton[5];
    private final JLabel pity = new JLabel(" ");
    private final JLabel wallet = new JLabel("…");
    private final JLabel result = new JLabel(" ");
    private boolean busy;

    RoadsideStore(DefaultUI player, int userId) {
        this.player = player;
        this.userId = userId;
        PlayerTheme theme = player.theme;
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(theme.controlBackground);
        body.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        body.setPreferredSize(new Dimension(920, 580));
        wallet.setIcon(Icons.load("cone.png", 24));
        wallet.setFont(player.getTitleFont());
        pity.setFont(player.getTitleFont());
        result.setFont(player.getTitleFont().deriveFont(18f));
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(player.getTitleFont());
        JPanel summary = new JPanel(new BorderLayout());
        summary.setBackground(theme.buttonBackground);
        summary.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        wallet.setForeground(theme.buttonTextColor);
        pity.setForeground(theme.buttonTextColor);
        pity.setToolTipText("Shared by all crates. Legendary or Mythic resets pity.");
        summary.add(wallet, BorderLayout.WEST);
        summary.add(pity, BorderLayout.EAST);
        body.add(summary, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 5, 8, 0));
        cards.setPreferredSize(new Dimension(0, 215));
        cards.setOpaque(false);
        Color[] headers = {new Color(0xDFD4BF), new Color(0xA8DD90),
                new Color(0xD5A5EE), new Color(0xFADB75), new Color(0xF89B9B)};
        for (Crate crate : Crate.values()) {
            int index = crate.ordinal();
            JPanel card = new JPanel(new BorderLayout(0, 6));
            card.setBackground(theme.buttonBackground);
            card.setBorder(BorderFactory.createLineBorder(theme.panelBorder, 2));
            JLabel title = new JLabel(crate.title.toUpperCase(Locale.ROOT), JLabel.CENTER);
            title.setFont(player.getTitleFont());
            title.setOpaque(true);
            title.setBackground(headers[index]);
            title.setForeground(new Color(0x261914));
            title.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            card.add(title, BorderLayout.NORTH);
            ImageIcon crateIcon = Icons.load("crate-" + crate.name().toLowerCase(Locale.ROOT)
                    + ".png", 112);
            if (crateIcon == null) crateIcon = Icons.load("roadside-crate.png", 112);
            card.add(new JLabel(crateIcon, JLabel.CENTER), BorderLayout.CENTER);

            JButton button = GameWindow.button(theme, Integer.toString(crate.price));
            button.setFont(player.getTitleFont());
            button.setIcon(Icons.load("cone.png", 18));
            button.setDisabledIcon(button.getIcon());
            button.setToolTipText("Spend Cones and open this crate");
            button.addActionListener(event -> load(crate));
            buttons[index] = button;
            card.add(button, BorderLayout.SOUTH);
            cards.add(card);
        }
        result.setForeground(new Color(0xF5E8D8));
        result.setOpaque(true);
        result.setBackground(new Color(0x221D2D));
        result.setHorizontalAlignment(JLabel.CENTER);
        result.setVerticalAlignment(JLabel.CENTER);
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.panelBorder, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        result.setText("CHOOSE A CRATE");
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.add(cards, BorderLayout.NORTH);
        content.add(result, BorderLayout.CENTER);
        body.add(content, BorderLayout.CENTER);

        window = new GameWindow(player, player, "Roadside Gacha", body);
        window.setCloseHandler(() -> {
            if (!busy) window.dispose();
        });
    }

    void show() {
        load(null);
        window.setVisible(true);
    }

    private void load(Crate crate) {
        if (crate != null) {
            Timer previous = (Timer) result.getClientProperty("revealTimer");
            if (previous != null) previous.stop();
            result.setIcon(null);
            result.setText("Opening crate...");
        }
        busy = true;
        for (JButton button : buttons) button.setEnabled(false);

        new SwingWorker<Inventory, Void>() {
            private Pull pull;

            @Override
            protected Inventory doInBackground() throws Exception {
                if (crate != null) pull = StoreRepository.open(userId, crate);
                return StoreRepository.loadInventory(userId);
            }

            @Override
            protected void done() {
                busy = false;
                try {
                    Inventory inventory = get();
                    wallet.setText(Integer.toString(inventory.balance()));
                    pity.setText("Pity: " + inventory.untilGuarantee());
                    player.currencyLabel.setText(Integer.toString(inventory.balance()));
                    for (Crate option : Crate.values()) {
                        int index = option.ordinal();


                        buttons[index].setEnabled(inventory.balance() >= option.price);
                    }
                    if (crate != null) showPull(pull);
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException error) {
                    result.setText("Could not load the gacha store. Reopen it to refresh.");
                }
            }
        }.execute();
    }

    private void showPull(Pull pull) {
        Timer previous = (Timer) result.getClientProperty("revealTimer");
        if (previous != null) previous.stop();
        if (pull == null) {
            result.setText("Insufficient Cones.");
            return;
        }
        result.setIcon(Icons.load("roadside-open.png", 64));
        result.setHorizontalTextPosition(JLabel.CENTER);
        result.setVerticalTextPosition(JLabel.BOTTOM);
        Color[] rarityColors = {new Color(0xDFD4BF), new Color(0xA8DD90),
                new Color(0xD5A5EE), new Color(0xFADB75), new Color(0xF89B9B)};
        result.setForeground(rarityColors[pull.preset().rarity().ordinal()]);
        result.setText("OPENING CRATE...");
        Timer timer = new Timer(1000, event -> {
            String font = player.getTitleFont().getFamily();
            result.setText("<html><center style='font-family: " + font + "'>"
                    + "<span style='font-size:14px'>"
                    + pull.preset().rarity().title.toUpperCase(Locale.ROOT)
                    + "</span><br><span style='font-size:23px'>" + pull.preset().title().toUpperCase(Locale.ROOT)
                    + "</span><br><br><span style='font-size:16px'>"
                    + (pull.refund() > 0 ? "DUPLICATE" : "UNLOCKED")
                    + "</span><br><span style='font-size:12px'>"
                    + (pull.refund() > 0 ? pull.refund() + " Cones refunded"
                            : "Equip in Settings &gt; Themes")
                    + "</span></center></html>");
        });
        timer.setRepeats(false);
        result.putClientProperty("revealTimer", timer);
        timer.start();
    }
}
