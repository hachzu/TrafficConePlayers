package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

public class TCPUI extends JFrame {
    final JButton userButton;
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
    final JPanel titleBar;
    final JLabel titleLabel;

    TCPUI(JComponent playerDisplay) {
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Traffic Cone Player");

        ImageIcon image = new ImageIcon("src/main/resources/assets/cone.png");
        setIconImage(image.getImage());

        Color orange = new Color(255, 165, 0);
        Color panelBorder = new Color(105, 38, 0);
        Color buttonBackground = new Color(255, 224, 178);
        Color buttonBorder = panelBorder;

        JPanel canvas = new CanvasBackgroundPanel("/assets/mainui_bg.png");
        canvas.setBackground(orange);
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
        nameDisplayPanel.setBackground(new Color(35, 23, 16));
        nameDisplayPanel.setBounds(390, 10, 520, 60);
        nameDisplayPanel.setBorder(BorderFactory.createLineBorder(panelBorder, 2));

        trackTitle = new JLabel("TRACK TITLE");
        trackTitle.setFont(pixelFont.deriveFont(18f));
        trackTitle.setForeground(new Color(255, 180, 40));
        trackTitle.setHorizontalAlignment(SwingConstants.LEFT);
        trackTitle.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));

        trackInfo = new JLabel("01 / 05   |   00:00 / 00:00");
        trackInfo.setFont(pixelFont.deriveFont(13f));
        trackInfo.setForeground(new Color(255, 180, 40));
        trackInfo.setHorizontalAlignment(SwingConstants.LEFT);
        trackInfo.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));
        nameDisplayPanel.add(trackTitle);
        nameDisplayPanel.add(trackInfo);

        JPanel utilityPanel = new JPanel(new BorderLayout(0, 8));
        utilityPanel.setBackground(new Color(35, 23, 16));
        utilityPanel.setBounds(390, 80, 520, 160);
        utilityPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(panelBorder, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JPanel utilityTop = new JPanel(new BorderLayout(10, 0));
        utilityTop.setOpaque(false);

        JLabel volumeIcon = new JLabel(new ImageIcon(getClass().getResource(
                "/assets/icons/volume.png")));
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);
        volumeSlider.setToolTipText("Volume");
        volumeSlider.setUI(new ThemedSliderUI(volumeSlider, 25, 10, 6, true));

        userButton = new JButton("USER  v");
        userButton.setPreferredSize(new Dimension(122, 38));
        userButton.setBackground(buttonBackground);
        userButton.setForeground(buttonBorder);
        userButton.setFont(pixelFont.deriveFont(14f));
        userButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        userButton.setFocusPainted(false);
        userButton.setFocusable(false);
        userButton.setToolTipText("User menu (empty placeholder)");

        utilityTop.add(volumeIcon, BorderLayout.WEST);
        utilityTop.add(volumeSlider, BorderLayout.CENTER);
        utilityTop.add(userButton, BorderLayout.EAST);

        JPanel utilityBottom = new JPanel(new BorderLayout());
        utilityBottom.setOpaque(false);

        ImageIcon videoIcon = new ImageIcon(getClass().getResource("/assets/icons/video.png"));
        videoIcon = new ImageIcon(videoIcon.getImage()
                .getScaledInstance(18, 18, Image.SCALE_SMOOTH));
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

        settingsButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/settings.png")));
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
        timelinePanel.setBackground(new Color(82, 170, 0));
        timelinePanel.setBounds(0, 260, 920, 40);
        timelinePanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        timelinePanel.setOpaque(false);

        timelineSlider = new JSlider(0, 1000, 0);
        timelineSlider.setOpaque(false);
        timelineSlider.setToolTipText("Seek position");
        timelineSlider.setUI(new ThemedSliderUI(timelineSlider, 25, 10, 6, true));
        timelinePanel.add(timelineSlider, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(new Color(255, 128, 0));
        controlPanel.setBounds(0, 310, 920, 50);

        JPanel playbackButtons = new JPanel(new GridLayout(1, 6, 8, 0));
        playbackButtons.setOpaque(false);
        playbackButtons.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        Dimension buttonSize = new Dimension(44, 40);

        previousButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/previous.png")));
        previousButton.setPreferredSize(buttonSize);
        previousButton.setBackground(buttonBackground);
        previousButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        previousButton.setFocusPainted(false);
        previousButton.setToolTipText("Previous track");
        playbackButtons.add(previousButton);

        playPauseButton = new JToggleButton(new ImageIcon(
                getClass().getResource("/assets/icons/play.png")));
        playPauseButton.setSelectedIcon(new ImageIcon(
                getClass().getResource("/assets/icons/pause.png")));
        playPauseButton.setPreferredSize(buttonSize);
        playPauseButton.setBackground(buttonBackground);
        playPauseButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        playPauseButton.setFocusPainted(false);
        playPauseButton.setToolTipText("Play or pause");
        playPauseButton.setSelected(false);
        playbackButtons.add(playPauseButton);

        stopButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/stop.png")));
        stopButton.setPreferredSize(buttonSize);
        stopButton.setBackground(buttonBackground);
        stopButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        stopButton.setFocusPainted(false);
        stopButton.setToolTipText("Stop");
        playbackButtons.add(stopButton);

        nextButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/next.png")));
        nextButton.setPreferredSize(buttonSize);
        nextButton.setBackground(buttonBackground);
        nextButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        nextButton.setFocusPainted(false);
        nextButton.setToolTipText("Next track");
        playbackButtons.add(nextButton);

        shuffleButton = new JToggleButton(new ImageIcon(
                getClass().getResource("/assets/icons/shuffle-off.png")));
        shuffleButton.setSelectedIcon(new ImageIcon(
                getClass().getResource("/assets/icons/shuffle.png")));
        shuffleButton.setPreferredSize(buttonSize);
        shuffleButton.setBackground(buttonBackground);
        shuffleButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        shuffleButton.setFocusPainted(false);
        shuffleButton.setToolTipText("Toggle shuffle");
        playbackButtons.add(shuffleButton);

        playlistButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/playlist.png")));
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

        importButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/download.png")));
        importButton.setPreferredSize(buttonSize);
        importButton.setBackground(buttonBackground);
        importButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        importButton.setFocusPainted(false);
        importButton.setToolTipText("Import video or URL to playlist");
        utilityIconButtons.add(importButton);

        storeButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/store.png")));
        storeButton.setPreferredSize(buttonSize);
        storeButton.setBackground(buttonBackground);
        storeButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        storeButton.setFocusPainted(false);
        storeButton.setToolTipText("Store");
        utilityIconButtons.add(storeButton);

        gamesButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/gamepad.png")));
        gamesButton.setPreferredSize(buttonSize);
        gamesButton.setBackground(buttonBackground);
        gamesButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        gamesButton.setFocusPainted(false);
        gamesButton.setToolTipText("Games");
        utilityIconButtons.add(gamesButton);

        JPanel currencyDisplay = new JPanel(new BorderLayout());
        currencyDisplay.setPreferredSize(new Dimension(112, 40));
        currencyDisplay.setBackground(new Color(255, 224, 178));
        currencyDisplay.setBorder(BorderFactory.createLineBorder(panelBorder, 1));

        ImageIcon coneIcon = new ImageIcon(getClass().getResource("/assets/cone.png"));
        Image smallCone = coneIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JLabel currencyLabel = new JLabel("1,322", new ImageIcon(smallCone), SwingConstants.CENTER);
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
            button.setBackground(orange);
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
    }
}
