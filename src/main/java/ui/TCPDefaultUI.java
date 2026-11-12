package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

public class TCPDefaultUI extends JFrame {
    final JButton userButton;
    final JPopupMenu userMenu;
    final JButton settingsButton;
    final JButton previousButton;
    final JToggleButton playPauseButton;
    final JButton stopButton;
    final JButton nextButton;
    final JToggleButton shuffleButton;
    final JButton playlistButton;
    final JButton importButton;
    final JButton storeButton;
    final JButton gamesButton;
    final JButton minimizeButton;
    final JButton closeButton;
    final JToggleButton videoToggle;
    final JSlider volumeSlider;
    final JSlider timelineSlider;
    final JLabel trackTitle;
    final JLabel trackInfo;
    final JLabel volumeIcon;
    final JLabel currencyLabel;
    final JPanel titleBar;
    final JLabel titleLabel;
    final TCPanel canvas;
    final TCPPlayerTheme theme;

    static class Builder {
        private JComponent playerDisplay;
        private String title = "Traffic Cone Player";
        private TCPPlayerTheme theme = TCPPlayerTheme.starter();

        Builder playerDisplay(JComponent playerDisplay) {
            this.playerDisplay = playerDisplay;
            return this;
        }

        Builder title(String title) {
            this.title = title;
            return this;
        }

        Builder theme(TCPPlayerTheme theme) {
            this.theme = theme.copy();
            return this;
        }

        TCPDefaultUI build() {
            if (playerDisplay == null) {
                throw new IllegalStateException("A player display is required");
            }
            return new TCPDefaultUI(playerDisplay, title, theme.copy());
        }
    }

    private TCPDefaultUI(JComponent playerDisplay, String title, TCPPlayerTheme theme) {
        this.theme = theme;
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(title);

        ImageIcon image = new ImageIcon(getClass().getResource(getIconFromTheme("window")));
        setIconImage(image.getImage());

        Color accent = theme.accent;
        Color panelBorder = theme.panelBorder;
        Color buttonBackground = theme.buttonBackground;
        Color buttonBorder = panelBorder;

        canvas = new TCPanel(theme.backgroundImage);
        canvas.setBackground(accent);
        canvas.setPreferredSize(new Dimension(920, 360));

        playerDisplay.setBounds(10, 10, 370, 230);
        playerDisplay.setBorder(BorderFactory.createLineBorder(panelBorder, 2));

        Font pixelFont;
        try (var fontStream = getClass().getResourceAsStream(
                "/assets/fonts/LowresPixel-Regular.otf")) {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (Exception error) {
            pixelFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }

        JPanel nameDisplayPanel = new JPanel(new GridLayout(2, 1));
        nameDisplayPanel.setBackground(theme.panelBackground);
        nameDisplayPanel.setBounds(390, 10, 520, 60);
        nameDisplayPanel.setBorder(BorderFactory.createLineBorder(panelBorder, 2));

        trackTitle = new JLabel("TRACK TITLE");
        trackTitle.setFont(pixelFont.deriveFont(18f));
        trackTitle.setForeground(theme.trackText);
        trackTitle.setHorizontalAlignment(SwingConstants.LEFT);
        trackTitle.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));

        trackInfo = new JLabel("01 / 05   |   00:00 / 00:00");
        trackInfo.setFont(pixelFont.deriveFont(13f));
        trackInfo.setForeground(theme.trackText);
        trackInfo.setHorizontalAlignment(SwingConstants.LEFT);
        trackInfo.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));
        nameDisplayPanel.add(trackTitle);
        nameDisplayPanel.add(trackInfo);

        JPanel utilityPanel = new JPanel(new BorderLayout(0, 8));
        utilityPanel.setBackground(theme.panelBackground);
        utilityPanel.setBounds(390, 80, 520, 160);
        utilityPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(panelBorder, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JPanel utilityTop = new JPanel(new BorderLayout(10, 0));
        utilityTop.setOpaque(false);

        volumeIcon = new JLabel(new ImageIcon(getClass().getResource(getIconFromTheme("volume"))));
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);
        volumeSlider.setToolTipText("Volume");
        volumeSlider.setUI(new TCPDefaultSliderUI(volumeSlider, 25, 10, 6, true, buttonBorder, buttonBorder, buttonBorder));

        userButton = new JButton("USER  v");
        userButton.setPreferredSize(new Dimension(122, 38));
        userButton.setBackground(buttonBackground);
        userButton.setForeground(buttonBorder);
        userButton.setFont(pixelFont.deriveFont(14f));
        userButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        userButton.setFocusPainted(false);
        userButton.setFocusable(false);
        userButton.setToolTipText("Open user menu");

        userMenu = new JPopupMenu();
        userMenu.setBackground(buttonBackground);
        userMenu.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        JMenuItem noUsersItem = new JMenuItem("No users available");
        noUsersItem.setBackground(buttonBackground);
        noUsersItem.setForeground(buttonBorder);
        noUsersItem.setFont(pixelFont.deriveFont(12f));
        noUsersItem.setEnabled(false);
        userMenu.add(noUsersItem);

        utilityTop.add(volumeIcon, BorderLayout.WEST);
        utilityTop.add(volumeSlider, BorderLayout.CENTER);
        utilityTop.add(userButton, BorderLayout.EAST);

        JPanel utilityBottom = new JPanel(new BorderLayout());
        utilityBottom.setOpaque(false);

        ImageIcon videoIcon = new ImageIcon(getClass().getResource(getIconFromTheme("video")));
        videoIcon = new ImageIcon(videoIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
        videoToggle = new JToggleButton("VIDEO", videoIcon);
        videoToggle.setHorizontalTextPosition(SwingConstants.RIGHT);
        videoToggle.setIconTextGap(6);
        videoToggle.setPreferredSize(new Dimension(100, 30));
        videoToggle.setBackground(buttonBackground);
        videoToggle.setForeground(buttonBorder);
        videoToggle.setFont(pixelFont.deriveFont(12f));
        videoToggle.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        videoToggle.setFocusPainted(false);
        videoToggle.setFocusable(false);
        videoToggle.setSelected(false);
        videoToggle.setToolTipText("Video mode (vinyl plays when off)");

        JPanel videoPosition = new JPanel(new BorderLayout());
        videoPosition.setOpaque(false);
        videoPosition.add(videoToggle, BorderLayout.NORTH);

        settingsButton = new JButton(new ImageIcon(getClass().getResource(getIconFromTheme("settings"))));
        settingsButton.setPreferredSize(new Dimension(32, 32));
        settingsButton.setMargin(new Insets(0, 0, 0, 0));
        settingsButton.setOpaque(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setFocusable(false);
        settingsButton.setToolTipText("Settings");

        JPanel settingsPosition = new JPanel(new BorderLayout());
        settingsPosition.setOpaque(false);
        settingsPosition.add(settingsButton, BorderLayout.SOUTH);
        utilityBottom.add(videoPosition, BorderLayout.WEST);
        utilityBottom.add(settingsPosition, BorderLayout.EAST);

        utilityPanel.add(utilityTop, BorderLayout.NORTH);
        utilityPanel.add(utilityBottom, BorderLayout.CENTER);

        JPanel timelinePanel = new JPanel(new BorderLayout());
        timelinePanel.setBackground(theme.timelineBackground);
        timelinePanel.setBounds(0, 260, 920, 40);
        timelinePanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        timelinePanel.setOpaque(false);

        timelineSlider = new JSlider(0, 1000, 0);
        timelineSlider.setOpaque(false);
        timelineSlider.setToolTipText("Seek position");
        timelineSlider.setUI(new TCPDefaultSliderUI(timelineSlider, 25, 10, 6, true, buttonBorder, buttonBorder, buttonBorder));
        timelinePanel.add(timelineSlider, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(theme.controlBackground);
        controlPanel.setBounds(0, 310, 920, 50);

        JPanel playbackButtons = new JPanel(new GridLayout(1, 6, 8, 0));
        playbackButtons.setOpaque(false);
        playbackButtons.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        Dimension buttonSize = new Dimension(44, 40);

        previousButton = new JButton(new ImageIcon(getClass().getResource(getIconFromTheme("previous"))));
        previousButton.setPreferredSize(buttonSize);
        previousButton.setBackground(buttonBackground);
        previousButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        previousButton.setFocusPainted(false);
        previousButton.setToolTipText("Previous track");
        playbackButtons.add(previousButton);

        playPauseButton = new JToggleButton(new ImageIcon(getClass().getResource(getIconFromTheme("play"))));
        playPauseButton.setSelectedIcon(new ImageIcon(getClass().getResource(getIconFromTheme("pause"))));
        playPauseButton.setPreferredSize(buttonSize);
        playPauseButton.setBackground(buttonBackground);
        playPauseButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        playPauseButton.setFocusPainted(false);
        playPauseButton.setToolTipText("Play or pause");
        playPauseButton.setSelected(false);
        playbackButtons.add(playPauseButton);

        stopButton = new JButton(new ImageIcon(getClass().getResource(getIconFromTheme("stop"))));
        stopButton.setPreferredSize(buttonSize);
        stopButton.setBackground(buttonBackground);
        stopButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        stopButton.setFocusPainted(false);
        stopButton.setToolTipText("Stop");
        playbackButtons.add(stopButton);

        nextButton = new JButton(new ImageIcon(getClass().getResource(getIconFromTheme("next"))));
        nextButton.setPreferredSize(buttonSize);
        nextButton.setBackground(buttonBackground);
        nextButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        nextButton.setFocusPainted(false);
        nextButton.setToolTipText("Next track");
        playbackButtons.add(nextButton);

        shuffleButton = new JToggleButton(new ImageIcon(getClass().getResource(getIconFromTheme("shuffleOff"))));
        shuffleButton.setSelectedIcon(new ImageIcon(getClass().getResource(getIconFromTheme("shuffle"))));
        shuffleButton.setPreferredSize(buttonSize);
        shuffleButton.setBackground(buttonBackground);
        shuffleButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        shuffleButton.setFocusPainted(false);
        shuffleButton.setToolTipText("Toggle shuffle");
        playbackButtons.add(shuffleButton);

        playlistButton = new JButton(new ImageIcon(getClass().getResource(getIconFromTheme("playlist"))));
        playlistButton.setPreferredSize(buttonSize);
        playlistButton.setBackground(buttonBackground);
        playlistButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        playlistButton.setFocusPainted(false);
        playlistButton.setToolTipText("Current playlist");
        playbackButtons.add(playlistButton);

        JPanel utilityButtons = new JPanel(new BorderLayout(8, 0));
        utilityButtons.setOpaque(false);
        utilityButtons.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        JPanel utilityIconButtons = new JPanel(new GridLayout(1, 3, 8, 0));
        utilityIconButtons.setOpaque(false);

        importButton = new JButton(new ImageIcon(getClass().getResource(getIconFromTheme("import"))));
        importButton.setPreferredSize(buttonSize);
        importButton.setBackground(buttonBackground);
        importButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        importButton.setFocusPainted(false);
        importButton.setToolTipText("Import video or URL to playlist");
        utilityIconButtons.add(importButton);

        storeButton = new JButton(new ImageIcon(getClass().getResource(getIconFromTheme("store"))));

        storeButton.setPreferredSize(buttonSize);
        storeButton.setBackground(buttonBackground);
        storeButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        storeButton.setFocusPainted(false);
        storeButton.setToolTipText("Store");
        utilityIconButtons.add(storeButton);

        gamesButton = new JButton(new ImageIcon(getClass().getResource(getIconFromTheme("games"))));

        gamesButton.setPreferredSize(buttonSize);
        gamesButton.setBackground(buttonBackground);
        gamesButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        gamesButton.setFocusPainted(false);
        gamesButton.setToolTipText("Games");
        utilityIconButtons.add(gamesButton);

        JPanel currencyDisplay = new JPanel(new BorderLayout());
        currencyDisplay.setPreferredSize(new Dimension(112, 40));
        currencyDisplay.setBackground(theme.buttonBackground);
        currencyDisplay.setBorder(BorderFactory.createLineBorder(panelBorder, 1));

        ImageIcon coneIcon = new ImageIcon(getClass().getResource(getIconFromTheme("currency")));
        Image smallCone = coneIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        currencyLabel = new JLabel("1,322", new ImageIcon(smallCone), SwingConstants.CENTER);

        currencyLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        currencyLabel.setIconTextGap(6);
        currencyLabel.setForeground(panelBorder);
        currencyLabel.setFont(pixelFont.deriveFont(Font.BOLD, 15f));
        currencyDisplay.add(currencyLabel, BorderLayout.CENTER);

        utilityButtons.add(utilityIconButtons, BorderLayout.CENTER);
        utilityButtons.add(currencyDisplay, BorderLayout.EAST);

        controlPanel.add(playbackButtons, BorderLayout.WEST);
        controlPanel.add(utilityButtons, BorderLayout.EAST);

        canvas.add(playerDisplay);
        canvas.add(nameDisplayPanel);
        canvas.add(utilityPanel);
        canvas.add(timelinePanel);
        canvas.add(controlPanel);

        titleBar = new JPanel(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(920, 36));
        titleBar.setBackground(panelBorder);

        titleLabel = new JLabel("Traffic Cone Player");
        titleLabel.setIcon(new ImageIcon(image.getImage()
                .getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        titleLabel.setIconTextGap(8);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(pixelFont.deriveFont(Font.BOLD, 14f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        titleBar.add(titleLabel, BorderLayout.CENTER);

        JPanel windowButtons = new JPanel(new GridLayout(1, 2, 6, 0));
        windowButtons.setOpaque(false);
        windowButtons.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        minimizeButton = new JButton("−");
        minimizeButton.setToolTipText("Minimize");
        windowButtons.add(minimizeButton);

        closeButton = new JButton("×");
        closeButton.setToolTipText("Close");
        windowButtons.add(closeButton);

        for (JButton button : new JButton[] {minimizeButton, closeButton}) {
            button.setPreferredSize(new Dimension(34, 28));
            button.setBackground(accent);
            button.setForeground(panelBorder);
            button.setBorder(BorderFactory.createLineBorder(panelBorder, 1));
            button.setFocusPainted(false);
            button.setFont(pixelFont.deriveFont(Font.BOLD, 16f));
        }

        titleBar.add(windowButtons, BorderLayout.EAST);

        JPanel windowContent = new JPanel(new BorderLayout());
        windowContent.setBorder(BorderFactory.createLineBorder(panelBorder, 2));
        windowContent.add(titleBar, BorderLayout.NORTH);
        windowContent.add(canvas, BorderLayout.CENTER);

        setContentPane(windowContent);
        pack();
        setLocationRelativeTo(null);

        try {
            applyTheme(TCPPlayerTheme.load(
                    TCPPlayerTheme.themesDirectory().resolve("teto.properties")));
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Could not load Teto theme", error);
        }
    }

    //misc methods 

    private String getIconFromTheme(String key){
        String path = theme.icons.get(key);

                if (path == null || path.isBlank()) {
                return null;
                }

        return path;
    }

    private void setIconIfPresent(AbstractButton button, String key) {
        ImageIcon icon = themeIcon(key);
        if (icon != null) {
            Dimension size = button.getPreferredSize();
            button.setIcon(scaleIcon(icon, Math.max(1, size.width - 12), Math.max(1, size.height - 12)));
        }
    }

    private void setSelectedIconIfPresent(JToggleButton button, String key) {
        ImageIcon icon = themeIcon(key);
        if (icon != null) {
            Dimension size = button.getPreferredSize();
            button.setSelectedIcon(scaleIcon(icon, Math.max(1, size.width - 12), Math.max(1, size.height - 12)));
        }
    }

    private void setLabelIconIfPresent(JLabel label, String key) {
        ImageIcon icon = themeIcon(key);
        if (icon != null) label.setIcon(scaleIcon(icon, 24, 24));
    }

    private ImageIcon scaleIcon(ImageIcon icon, int maxWidth, int maxHeight) {
        int sourceWidth = icon.getIconWidth();
        int sourceHeight = icon.getIconHeight();
        if (sourceWidth <= 0 || sourceHeight <= 0) return icon;

        double scale = Math.min((double) maxWidth / sourceWidth, (double) maxHeight / sourceHeight);
        int width = Math.max(1, (int) Math.round(sourceWidth * scale));
        int height = Math.max(1, (int) Math.round(sourceHeight * scale));
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    private ImageIcon themeIcon(String key) {
        String path = getIconFromTheme(key);
        if (path == null) return null;
        java.net.URL resource = getClass().getResource(path);
        return resource == null ? null : new ImageIcon(resource);
    }

    private void applyThemeIcons() {
        setIconIfPresent(settingsButton, "settings");
        setIconIfPresent(previousButton, "previous");
        setIconIfPresent(playPauseButton, "play");
        setSelectedIconIfPresent(playPauseButton, "pause");
        setIconIfPresent(stopButton, "stop");
        setIconIfPresent(nextButton, "next");
        setIconIfPresent(shuffleButton, "shuffleOff");
        setSelectedIconIfPresent(shuffleButton, "shuffle");
        setIconIfPresent(playlistButton, "playlist");
        setIconIfPresent(importButton, "import");
        setIconIfPresent(storeButton, "store");
        setIconIfPresent(gamesButton, "games");
        setIconIfPresent(videoToggle, "video");
        setLabelIconIfPresent(volumeIcon, "volume");

        ImageIcon currencyIcon = themeIcon("currency");
        if (currencyIcon != null) {
            currencyLabel.setIcon(scaleIcon(currencyIcon, 24, 24));
        }

        ImageIcon windowIcon = themeIcon("window");
        if (windowIcon != null) {
            setIconImage(windowIcon.getImage());
            titleLabel.setIcon(new ImageIcon(windowIcon.getImage()
                    .getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        }
    }

    

    void applyTheme(TCPPlayerTheme updated){
        if (updated == null){
                throw new IllegalArgumentException("Theme Can Not be Null");
        }

        TCPPlayerTheme previous = theme.copy();

        applyThemeTo(getContentPane(), previous, updated);
        applyThemeTo(userMenu, previous, updated);

        theme.accent = updated.accent;
        theme.panelBorder = updated.panelBorder;
        theme.buttonBackground = updated.buttonBackground;
        theme.panelBackground = updated.panelBackground;
        theme.trackText = updated.trackText;
        theme.timelineBackground = updated.timelineBackground;
        theme.controlBackground = updated.controlBackground;
        theme.clickColor = updated.clickColor;
        theme.backgroundImage = updated.backgroundImage;
        theme.themeName = updated.themeName;
        theme.icons.clear();
        theme.icons.putAll(updated.icons);

        canvas.setBackgroundImage(updated.backgroundImage);
        applyThemeIcons();
        revalidate();
        repaint();

    }

    private void applyThemeTo(java.awt.Container parent, TCPPlayerTheme oldTheme, TCPPlayerTheme newTheme) {
        for (java.awt.Component component : parent.getComponents()) {
            Color background = component.getBackground();
            if (background != null) {
                if (background.equals(oldTheme.accent)) {
                    component.setBackground(newTheme.accent);
                } else if (background.equals(oldTheme.buttonBackground)) {
                    component.setBackground(newTheme.buttonBackground);
                } else if (background.equals(oldTheme.panelBackground)) {
                    component.setBackground(newTheme.panelBackground);
                } else if (background.equals(oldTheme.timelineBackground)) {
                    component.setBackground(newTheme.timelineBackground);
                } else if (background.equals(oldTheme.controlBackground)) {
                    component.setBackground(newTheme.controlBackground);
                } else if (background.equals(oldTheme.panelBorder)) {
                    component.setBackground(newTheme.panelBorder);
                }
            }

            if (component instanceof java.awt.Container child) {
                applyThemeTo(child, oldTheme, newTheme);
            }
        }
    }
}

