package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
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
import javax.swing.border.Border;

public class DefaultUI extends JFrame {
    final JButton userButton;
    final JPopupMenu userMenu;
    final JMenuItem accountMenuItem;
    final JButton achievementsButton;
    final JMenuItem sessionMenuItem;
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
    final CanvasPanel canvas;
    final PlayerTheme theme;
    private final Map<JComponent, BorderStyle> themedBorders = new IdentityHashMap<>();
    private final Map<JComponent, String> themedBackgrounds = new IdentityHashMap<>();
    private final Map<JComponent, String> themedForegrounds = new IdentityHashMap<>();
    private final Map<AbstractButton, String> themedButtonIcons = new IdentityHashMap<>();
    private final Map<JToggleButton, String> themedSelectedIcons = new IdentityHashMap<>();

    DefaultUI(JComponent playerDisplay) {
        String title = "Traffic Cone Player";
        this.theme = PlayerTheme.starter().copy();
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle(title);

        ImageIcon image = themeIconOrEmpty("window");
        setIconImage(image.getImage());

        canvas = new CanvasPanel(theme.backgroundImage);
        setThemedBackground(canvas, "accent");
        canvas.setPreferredSize(new Dimension(920, 360));

        playerDisplay.setBounds(10, 10, 370, 230);
        setThemedBorder(playerDisplay, 2);

        Font pixelFont;
        try (var fontStream = getClass().getResourceAsStream(
                "/assets/fonts/LowresPixel-Regular.otf")) {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (Exception error) {
            pixelFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }

        JPanel nameDisplayPanel = new JPanel(new GridLayout(2, 1));
        setThemedBackground(nameDisplayPanel, "panelBackground");
        nameDisplayPanel.setBounds(390, 10, 520, 60);
        setThemedBorder(nameDisplayPanel, 2);

        trackTitle = new JLabel("TRACK TITLE");
        trackTitle.setFont(pixelFont.deriveFont(18f));
        setThemedForeground(trackTitle, "textColor");
        trackTitle.setHorizontalAlignment(SwingConstants.LEFT);
        trackTitle.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));

        trackInfo = new JLabel("01 / 05   |   00:00 / 00:00");
        trackInfo.setFont(pixelFont.deriveFont(13f));
        setThemedForeground(trackInfo, "textColor");
        trackInfo.setHorizontalAlignment(SwingConstants.LEFT);
        trackInfo.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));
        nameDisplayPanel.add(trackTitle);
        nameDisplayPanel.add(trackInfo);

        JPanel utilityPanel = new JPanel(new BorderLayout(0, 8));
        setThemedBackground(utilityPanel, "panelBackground");
        utilityPanel.setBounds(390, 80, 520, 160);
        setThemedBorder(utilityPanel, 2, BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel utilityTop = new JPanel(new BorderLayout(10, 0));
        utilityTop.setOpaque(false);

        volumeIcon = new JLabel(themeIconOrEmpty("volume"));
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);
        volumeSlider.setToolTipText("Volume");
        volumeSlider.setUI(new DefaultSliderUI(volumeSlider, 25, 10, 6, true,
                theme.sliderTrackColor, theme.sliderFillColor, theme.sliderBackgroundColor));

        userButton = new JButton("USER  v");
        userButton.setPreferredSize(new Dimension(122, 38));
        setThemedBackground(userButton, "buttonBackground");
        setThemedForeground(userButton, "buttonTextColor");
        userButton.setFont(pixelFont.deriveFont(14f));
        setThemedBorder(userButton, 1);
        userButton.setFocusPainted(false);
        userButton.setFocusable(false);
        userButton.setToolTipText("Open user menu");

        userMenu = new JPopupMenu();
        setThemedBackground(userMenu, "buttonBackground");
        setThemedBorder(userMenu, 1);
        accountMenuItem = new JMenuItem();
        setThemedBackground(accountMenuItem, "buttonBackground");
        setThemedForeground(accountMenuItem, "buttonTextColor");
        accountMenuItem.setFont(pixelFont.deriveFont(12f));
        accountMenuItem.setEnabled(false);
        userMenu.add(accountMenuItem);

        sessionMenuItem = new JMenuItem();
        setThemedBackground(sessionMenuItem, "buttonBackground");
        setThemedForeground(sessionMenuItem, "buttonTextColor");
        sessionMenuItem.setFont(pixelFont.deriveFont(12f));
        sessionMenuItem.setEnabled(false);
        userMenu.add(sessionMenuItem);


        utilityTop.add(volumeIcon, BorderLayout.WEST);
        utilityTop.add(volumeSlider, BorderLayout.CENTER);
        utilityTop.add(userButton, BorderLayout.EAST);

        JPanel utilityBottom = new JPanel(new BorderLayout());
        utilityBottom.setOpaque(false);

        videoToggle = new JToggleButton("VIDEO");
        videoToggle.setHorizontalTextPosition(SwingConstants.RIGHT);
        videoToggle.setIconTextGap(6);
        videoToggle.setPreferredSize(new Dimension(100, 30));
        registerThemedButtonIcon(videoToggle, "video");
        setThemedBackground(videoToggle, "buttonBackground");
        setThemedForeground(videoToggle, "buttonTextColor");
        videoToggle.setFont(pixelFont.deriveFont(12f));
        setThemedBorder(videoToggle, 1);
        videoToggle.setFocusPainted(false);
        videoToggle.setFocusable(false);
        videoToggle.setSelected(false);
        videoToggle.setToolTipText("Video mode (vinyl plays when off)");

        JPanel videoPosition = new JPanel(new BorderLayout());
        videoPosition.setOpaque(false);
        videoPosition.add(videoToggle, BorderLayout.NORTH);

        settingsButton = new JButton();
        settingsButton.setPreferredSize(new Dimension(32, 32));
        registerThemedButtonIcon(settingsButton, "settings");
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
        setThemedBackground(timelinePanel, "timelineBackground");
        timelinePanel.setBounds(0, 260, 920, 40);
        timelinePanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        timelinePanel.setOpaque(false);

        timelineSlider = new JSlider(0, 1000, 0);
        timelineSlider.setOpaque(false);
        timelineSlider.setToolTipText("Seek position");
        timelineSlider.setUI(new DefaultSliderUI(timelineSlider, 25, 10, 6, true,
                theme.sliderTrackColor, theme.sliderFillColor, theme.sliderBackgroundColor));
        timelinePanel.add(timelineSlider, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        setThemedBackground(controlPanel, "controlBackground");
        controlPanel.setBounds(0, 310, 920, 50);

        JPanel playbackButtons = new JPanel(new GridLayout(1, 6, 8, 0));
        playbackButtons.setOpaque(false);
        playbackButtons.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        Dimension buttonSize = new Dimension(44, 40);

        previousButton = new JButton();
        previousButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(previousButton, "previous");
        setThemedBackground(previousButton, "buttonBackground");
        setThemedBorder(previousButton, 1);
        previousButton.setFocusPainted(false);
        previousButton.setToolTipText("Previous track");
        playbackButtons.add(previousButton);

        playPauseButton = new JToggleButton();
        playPauseButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(playPauseButton, "play");
        registerThemedSelectedIcon(playPauseButton, "pause");
        setThemedBackground(playPauseButton, "buttonBackground");
        setThemedBorder(playPauseButton, 1);
        playPauseButton.setFocusPainted(false);
        playPauseButton.setToolTipText("Play or pause");
        playPauseButton.setSelected(false);
        playbackButtons.add(playPauseButton);

        stopButton = new JButton();
        stopButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(stopButton, "stop");
        setThemedBackground(stopButton, "buttonBackground");
        setThemedBorder(stopButton, 1);
        stopButton.setFocusPainted(false);
        stopButton.setToolTipText("Stop");
        playbackButtons.add(stopButton);

        nextButton = new JButton();
        nextButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(nextButton, "next");
        setThemedBackground(nextButton, "buttonBackground");
        setThemedBorder(nextButton, 1);
        nextButton.setFocusPainted(false);
        nextButton.setToolTipText("Next track");
        playbackButtons.add(nextButton);

        shuffleButton = new JToggleButton();
        shuffleButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(shuffleButton, "shuffleOff");
        registerThemedSelectedIcon(shuffleButton, "shuffle");
        setThemedBackground(shuffleButton, "buttonBackground");
        setThemedBorder(shuffleButton, 1);
        shuffleButton.setFocusPainted(false);
        shuffleButton.setToolTipText("Toggle shuffle");
        playbackButtons.add(shuffleButton);

        playlistButton = new JButton();
        playlistButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(playlistButton, "playlist");
        setThemedBackground(playlistButton, "buttonBackground");
        setThemedBorder(playlistButton, 1);
        playlistButton.setFocusPainted(false);
        playlistButton.setToolTipText("Current playlist");
        playbackButtons.add(playlistButton);

        JPanel utilityButtons = new JPanel(new BorderLayout(8, 0));
        utilityButtons.setOpaque(false);
        utilityButtons.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        JPanel utilityIconButtons = new JPanel(new GridLayout(1, 4, 8, 0));
        utilityIconButtons.setOpaque(false);

        importButton = new JButton();
        importButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(importButton, "import");
        setThemedBackground(importButton, "buttonBackground");
        setThemedBorder(importButton, 1);
        importButton.setFocusPainted(false);
        importButton.setToolTipText("Import video or URL to playlist");
        utilityIconButtons.add(importButton);

        storeButton = new JButton();

        storeButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(storeButton, "store");
        setThemedBackground(storeButton, "buttonBackground");
        setThemedBorder(storeButton, 1);
        storeButton.setFocusPainted(false);
        storeButton.setToolTipText("Store");
        utilityIconButtons.add(storeButton);

        gamesButton = new JButton();

        gamesButton.setPreferredSize(buttonSize);
        registerThemedButtonIcon(gamesButton, "games");
        setThemedBackground(gamesButton, "buttonBackground");
        setThemedBorder(gamesButton, 1);
        gamesButton.setFocusPainted(false);
        gamesButton.setToolTipText("Games");
        utilityIconButtons.add(gamesButton);

        achievementsButton = new JButton();
        achievementsButton.setPreferredSize(buttonSize);
        achievementsButton.setIcon(Icons.load("achievement-award.png", 28));
        setThemedBackground(achievementsButton, "buttonBackground");
        setThemedBorder(achievementsButton, 1);
        achievementsButton.setFocusPainted(false);
        achievementsButton.setToolTipText("Achievements");
        achievementsButton.getAccessibleContext().setAccessibleName("Achievements");
        utilityIconButtons.add(achievementsButton);

        JPanel currencyDisplay = new JPanel(new BorderLayout());
        currencyDisplay.setPreferredSize(new Dimension(150, 40));
        setThemedBackground(currencyDisplay, "buttonBackground");
        setThemedBorder(currencyDisplay, 1);

        ImageIcon coneIcon = themeIconOrEmpty("currency");
        Image smallCone = coneIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        currencyLabel = new JLabel("...", new ImageIcon(smallCone), SwingConstants.CENTER);

        currencyLabel.setToolTipText("Cones balance");
        currencyLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        currencyLabel.setIconTextGap(6);
        setThemedForeground(currencyLabel, "buttonTextColor");
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
        setThemedBackground(titleBar, "panelBorder");

        titleLabel = new JLabel("Traffic Cone Player");
        titleLabel.setIcon(new ImageIcon(image.getImage()
                .getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        titleLabel.setIconTextGap(8);
        setThemedForeground(titleLabel, "textColor");
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
            setThemedBackground(button, "accent");
            setThemedForeground(button, "buttonTextColor");
            setThemedBorder(button, 1);
            button.setFocusPainted(false);
            button.setFont(pixelFont.deriveFont(Font.BOLD, 16f));
        }

        titleBar.add(windowButtons, BorderLayout.EAST);

        JPanel windowContent = new JPanel(new BorderLayout());
        setThemedBorder(windowContent, 2);
        windowContent.add(titleBar, BorderLayout.NORTH);
        windowContent.add(canvas, BorderLayout.CENTER);

        setContentPane(windowContent);
        pack();
        setLocationRelativeTo(null);

    }

    //misc methods 

    public PlayerTheme getTheme() {
        return theme;
    }

    public Icon getTitleIcon() {
        return titleLabel.getIcon();
    }

    public Font getTitleFont() {
        return titleLabel.getFont();
    }

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

    private void registerThemedButtonIcon(AbstractButton button, String key) {
        themedButtonIcons.put(button, key);
        setIconIfPresent(button, key);
    }

    private void registerThemedSelectedIcon(JToggleButton button, String key) {
        themedSelectedIcons.put(button, key);
        setSelectedIconIfPresent(button, key);
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
        if (resource != null) return new ImageIcon(resource);
        ImageIcon fileIcon = new ImageIcon(path);
        return fileIcon.getIconWidth() > 0 ? fileIcon : null;
    }

    private ImageIcon themeIconOrEmpty(String key) {
        ImageIcon icon = themeIcon(key);
        return icon == null ? new ImageIcon() : icon;
    }

    private void applyThemeIcons() {
        themedButtonIcons.forEach(this::setIconIfPresent);
        themedSelectedIcons.forEach(this::setSelectedIconIfPresent);
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

    

    private record BorderStyle(int thickness, Border padding) {
        Border withColor(Color color) {
            Border outline = BorderFactory.createLineBorder(color, thickness);
            return padding == null ? outline : BorderFactory.createCompoundBorder(outline, padding);
        }
    }

    private void setThemedBorder(JComponent component, int thickness) {
        setThemedBorder(component, thickness, null);
    }

    private void setThemedBorder(JComponent component, int thickness, Border padding) {
        BorderStyle style = new BorderStyle(thickness, padding);
        themedBorders.put(component, style);
        component.setBorder(style.withColor(theme.panelBorder));
    }

    private void setThemedBackground(JComponent component, String role) {
        themedBackgrounds.put(component, role);
        component.setBackground(colorForRole(theme, role));
    }

    private void setThemedForeground(JComponent component, String role) {
        themedForegrounds.put(component, role);
        component.setForeground(colorForRole(theme, role));
    }

    private Color colorForRole(PlayerTheme source, String role) {
        return switch (role) {
            case "accent" -> source.accent;
            case "panelBorder" -> source.panelBorder;
            case "buttonBackground" -> source.buttonBackground;
            case "panelBackground" -> source.panelBackground;
            case "textColor" -> source.textColor;
            case "buttonTextColor" -> source.buttonTextColor;
            case "timelineBackground" -> source.timelineBackground;
            case "controlBackground" -> source.controlBackground;
            default -> throw new IllegalArgumentException("Unknown theme color role: " + role);
        };
    }

    void applyTheme(PlayerTheme updated) {
        if (updated == null) {
            throw new IllegalArgumentException("Theme cannot be null");
        }

        themedBackgrounds.forEach((component, role) ->
                component.setBackground(colorForRole(updated, role)));
        themedForegrounds.forEach((component, role) ->
                component.setForeground(colorForRole(updated, role)));

        themedBorders.forEach((component, style) ->
                component.setBorder(style.withColor(updated.panelBorder)));

        theme.accent = updated.accent;
        theme.panelBorder = updated.panelBorder;
        theme.buttonBackground = updated.buttonBackground;
        theme.panelBackground = updated.panelBackground;
        theme.textColor = updated.textColor;
        theme.buttonTextColor = updated.buttonTextColor;
        theme.timelineBackground = updated.timelineBackground;
        theme.controlBackground = updated.controlBackground;
        theme.clickColor = updated.clickColor;
        theme.sliderTrackColor = updated.sliderTrackColor;
        theme.sliderFillColor = updated.sliderFillColor;
        theme.sliderBackgroundColor = updated.sliderBackgroundColor;
        theme.backgroundImage = updated.backgroundImage;
        theme.themeName = updated.themeName;
        theme.icons.clear();
        theme.icons.putAll(updated.icons);

        canvas.setBackgroundImage(updated.backgroundImage);
        volumeSlider.setUI(new DefaultSliderUI(volumeSlider, 25, 10, 6, true,
                theme.sliderTrackColor, theme.sliderFillColor, theme.sliderBackgroundColor));
        timelineSlider.setUI(new DefaultSliderUI(timelineSlider, 25, 10, 6, true,
                theme.sliderTrackColor, theme.sliderFillColor, theme.sliderBackgroundColor));
        applyThemeIcons();
        revalidate();
        repaint();

    }

}

