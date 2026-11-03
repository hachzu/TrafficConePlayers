package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;

public class TCPUI extends JFrame {
    TCPUI() {
        // WINDOW //
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Traffic Cone Player");

        ImageIcon image = new ImageIcon("src/main/resources/assets/cone.png");
        setIconImage(image.getImage());

        // PANELS & CANVAS //
        Color orange = new Color(255, 165, 0);
        Color panelBorder = new Color(105, 38, 0);
        Color buttonBackground = new Color(255, 224, 178);
        Color buttonBorder = panelBorder;
        // Canvas Background Image
        Image canvasBackground = new ImageIcon(getClass().getResource(
                "/assets/mainui_bg.png")).getImage();
        JPanel canvas = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                double scale = Math.max((double) getWidth() / canvasBackground.getWidth(this),
                        (double) getHeight() / canvasBackground.getHeight(this));
                int imageWidth = (int) (canvasBackground.getWidth(this) * scale);
                int imageHeight = (int) (canvasBackground.getHeight(this) * scale);
                graphics.drawImage(canvasBackground, (getWidth() - imageWidth) / 2,
                        (getHeight() - imageHeight) / 2, imageWidth, imageHeight, this);
            }
        };
        canvas.setBackground(orange);
        canvas.setPreferredSize(new Dimension(920, 360));

        // Video Panel
        VinylVideoPanel videoPanel = new VinylVideoPanel();
        videoPanel.setBounds(10, 10, 370, 230);
        videoPanel.setBorder(BorderFactory.createLineBorder(panelBorder, 2));

        // Video / Audio Display Name Panel
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

        JLabel trackTitle = new JLabel("TRACK TITLE");
        trackTitle.setFont(pixelFont.deriveFont(18f));
        trackTitle.setForeground(new Color(255, 180, 40));
        trackTitle.setHorizontalAlignment(SwingConstants.LEFT);
        trackTitle.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));

        JLabel trackInfo = new JLabel("01 / 05   |   00:00");
        trackInfo.setFont(pixelFont.deriveFont(13f));
        trackInfo.setForeground(new Color(255, 180, 40));
        trackInfo.setHorizontalAlignment(SwingConstants.LEFT);
        trackInfo.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));
        nameDisplayPanel.add(trackTitle);
        nameDisplayPanel.add(trackInfo);

        // Utility Panel
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
        JSlider volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);
        volumeSlider.setToolTipText("Volume");
        volumeSlider.setUI(new ThemedSliderUI(volumeSlider, 25, 10, 6, true));

        JButton userButton = new JButton("USER  v");
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
                .getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JToggleButton videoToggle = new JToggleButton("VIDEO", videoIcon);
        videoToggle.setHorizontalTextPosition(SwingConstants.RIGHT);
        videoToggle.setIconTextGap(8);
        videoToggle.setPreferredSize(new Dimension(110, 34));
        videoToggle.setBackground(buttonBackground);
        videoToggle.setForeground(buttonBorder);
        videoToggle.setFont(pixelFont.deriveFont(14f));
        videoToggle.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        videoToggle.setFocusPainted(false);
        videoToggle.setFocusable(false);
        videoToggle.setSelected(false);
        videoToggle.setToolTipText("Video mode (vinyl plays when off)");

        JButton settingsButton = new JButton(new ImageIcon(
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
        utilityBottom.add(videoToggle, BorderLayout.WEST);
        utilityBottom.add(settingsPosition, BorderLayout.EAST);

        utilityPanel.add(utilityTop, BorderLayout.NORTH);
        utilityPanel.add(utilityBottom, BorderLayout.CENTER);
        volumeSlider.addChangeListener(event ->
                videoPanel.setVolume(volumeSlider.getValue() / 100.0));
        videoPanel.setVolume(volumeSlider.getValue() / 100.0);

        // Timeline Panel (seek position)
        JPanel timelinePanel = new JPanel(new BorderLayout());
        timelinePanel.setBackground(new Color(82, 170, 0));
        timelinePanel.setBounds(0, 260, 920, 40);
        timelinePanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        timelinePanel.setOpaque(false);


        
        // Timeline Slider
        JSlider timelineSlider = new JSlider(0, 1000, 0);
        timelineSlider.setOpaque(false);
        timelineSlider.setToolTipText("Seek position");
        timelineSlider.setUI(new ThemedSliderUI(timelineSlider, 25, 10, 6, true));
        timelinePanel.add(timelineSlider, BorderLayout.CENTER);

        // Control Panel (playback and utility buttons)
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(new Color(255, 128, 0));
        controlPanel.setBounds(0, 310, 920, 50);

        // Playback Buttons (left side)
        JPanel playbackButtons = new JPanel(new GridLayout(1, 5, 8, 0));
        playbackButtons.setOpaque(false);
        playbackButtons.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        // BUTTONS //
        Dimension buttonSize = new Dimension(44, 40);

        JButton previousButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/previous.png")));
        previousButton.setPreferredSize(buttonSize);
        previousButton.setBackground(buttonBackground);
        previousButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        previousButton.setFocusPainted(false);
        previousButton.setToolTipText("Previous track");
        playbackButtons.add(previousButton);

        JToggleButton playPauseButton = new JToggleButton(new ImageIcon(
                getClass().getResource("/assets/icons/play.png")));
        playPauseButton.setSelectedIcon(new ImageIcon(
                getClass().getResource("/assets/icons/pause.png")));
        playPauseButton.setPreferredSize(buttonSize);
        playPauseButton.setBackground(buttonBackground);
        playPauseButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        playPauseButton.setFocusPainted(false);
        playPauseButton.setToolTipText("Play or pause");
        playPauseButton.setSelected(true);
        videoPanel.setPlaying(true);
        playPauseButton.addActionListener(event ->
                videoPanel.setPlaying(!videoToggle.isSelected() && playPauseButton.isSelected()));
        videoToggle.addActionListener(event -> {
            boolean videoMode = videoToggle.isSelected();
            videoPanel.setVinylVisible(!videoMode);
            playPauseButton.setSelected(!videoMode);
            videoPanel.setPlaying(!videoMode);
        });
        playbackButtons.add(playPauseButton);

        JButton stopButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/stop.png")));
        stopButton.setPreferredSize(buttonSize);
        stopButton.setBackground(buttonBackground);
        stopButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        stopButton.setFocusPainted(false);
        stopButton.setToolTipText("Stop");
        playbackButtons.add(stopButton);

        JButton nextButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/next.png")));
        nextButton.setPreferredSize(buttonSize);
        nextButton.setBackground(buttonBackground);
        nextButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        nextButton.setFocusPainted(false);
        nextButton.setToolTipText("Next track");
        playbackButtons.add(nextButton);

        JToggleButton shuffleButton = new JToggleButton(new ImageIcon(
                getClass().getResource("/assets/icons/shuffle-off.png")));
        shuffleButton.setSelectedIcon(new ImageIcon(
                getClass().getResource("/assets/icons/shuffle.png")));
        shuffleButton.setPreferredSize(buttonSize);
        shuffleButton.setBackground(buttonBackground);
        shuffleButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        shuffleButton.setFocusPainted(false);
        shuffleButton.setToolTipText("Toggle shuffle");
        playbackButtons.add(shuffleButton);

        // Utility Buttons (right side)
        JPanel utilityButtons = new JPanel(new BorderLayout(8, 0));
        utilityButtons.setOpaque(false);
        utilityButtons.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        JPanel utilityIconButtons = new JPanel(new GridLayout(1, 3, 8, 0));
        utilityIconButtons.setOpaque(false);

        JButton downloadButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/download.png")));
        downloadButton.setPreferredSize(buttonSize);
        downloadButton.setBackground(buttonBackground);
        downloadButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        downloadButton.setFocusPainted(false);
        downloadButton.setToolTipText("Download");
        utilityIconButtons.add(downloadButton);

        JButton storeButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/store.png")));
        storeButton.setPreferredSize(buttonSize);
        storeButton.setBackground(buttonBackground);
        storeButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        storeButton.setFocusPainted(false);
        storeButton.setToolTipText("Store");
        utilityIconButtons.add(storeButton);

        JButton gamesButton = new JButton(new ImageIcon(
                getClass().getResource("/assets/icons/gamepad.png")));
        gamesButton.setPreferredSize(buttonSize);
        gamesButton.setBackground(buttonBackground);
        gamesButton.setBorder(BorderFactory.createLineBorder(buttonBorder, 1));
        gamesButton.setFocusPainted(false);
        gamesButton.setToolTipText("Games");
        utilityIconButtons.add(gamesButton);

        // Currency Display
        JPanel currencyDisplay = new JPanel(new BorderLayout());
        currencyDisplay.setPreferredSize(new Dimension(112, 40));
        currencyDisplay.setBackground(new Color(255, 224, 178));
        currencyDisplay.setBorder(BorderFactory.createLineBorder(new Color(105, 38, 0), 1));

        ImageIcon coneIcon = new ImageIcon(getClass().getResource("/assets/cone.png"));
        Image smallCone = coneIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JLabel currencyLabel = new JLabel("1,322", new ImageIcon(smallCone), SwingConstants.CENTER);
        currencyLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        currencyLabel.setIconTextGap(6);
        currencyLabel.setForeground(new Color(105, 38, 0));
        currencyLabel.setFont(pixelFont.deriveFont(Font.BOLD, 15f));
        currencyDisplay.add(currencyLabel, BorderLayout.CENTER);

        utilityButtons.add(utilityIconButtons, BorderLayout.CENTER);
        utilityButtons.add(currencyDisplay, BorderLayout.EAST);

        controlPanel.add(playbackButtons, BorderLayout.WEST);
        controlPanel.add(utilityButtons, BorderLayout.EAST);

        // Add Panels to the Canvas
        canvas.add(videoPanel);
        canvas.add(nameDisplayPanel);
        canvas.add(utilityPanel);
        canvas.add(timelinePanel);
        canvas.add(controlPanel);

        // TITLE BAR //
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(920, 36));
        titleBar.setBackground(new Color(105, 38, 0));

        JLabel titleLabel = new JLabel("Traffic Cone Player");
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

        JButton minimizeButton = new JButton("−");
        minimizeButton.setToolTipText("Minimize");
        minimizeButton.addActionListener(event -> setState(JFrame.ICONIFIED));
        windowButtons.add(minimizeButton);

        JButton closeButton = new JButton("×");
        closeButton.setToolTipText("Close");
        closeButton.addActionListener(event -> dispatchEvent(
                new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        windowButtons.add(closeButton);

        for (JButton button : new JButton[] {minimizeButton, closeButton}) {
            button.setPreferredSize(new Dimension(34, 28));
            button.setBackground(new Color(255, 165, 0));
            button.setForeground(new Color(105, 38, 0));
            button.setBorder(BorderFactory.createLineBorder(new Color(105, 38, 0), 1));
            button.setFocusPainted(false);
            button.setFont(pixelFont.deriveFont(Font.BOLD, 16f));
        }

        titleBar.add(windowButtons, BorderLayout.EAST);

        MouseAdapter dragWindow = new MouseAdapter() {
            private Point mouseOffset;

            @Override
            public void mousePressed(MouseEvent event) {
                mouseOffset = new Point(event.getXOnScreen() - getX(),
                        event.getYOnScreen() - getY());
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                setLocation(event.getXOnScreen() - mouseOffset.x,
                        event.getYOnScreen() - mouseOffset.y);
            }
        };

        titleBar.addMouseListener(dragWindow);
        titleBar.addMouseMotionListener(dragWindow);
        titleLabel.addMouseListener(dragWindow);
        titleLabel.addMouseMotionListener(dragWindow);

        JPanel windowContent = new JPanel(new BorderLayout());
        windowContent.setBorder(BorderFactory.createLineBorder(new Color(105, 38, 0), 2));
        windowContent.add(titleBar, BorderLayout.NORTH);
        windowContent.add(canvas, BorderLayout.CENTER);

        setContentPane(windowContent);
        pack();
        setLocationRelativeTo(null);
    }

    private static class ThemedSliderUI extends BasicSliderUI {
        private final int thumbSize;
        private final int trackHeight;
        private final int fillHeight;
        private final boolean showEnds;

        ThemedSliderUI(JSlider slider, int thumbSize, int trackHeight,
                int fillHeight, boolean showEnds) {
            super(slider);
            this.thumbSize = thumbSize;
            this.trackHeight = trackHeight;
            this.fillHeight = fillHeight;
            this.showEnds = showEnds;
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(thumbSize, thumbSize);
        }

        @Override
        public void paintTrack(Graphics graphics) {
            int trackY = trackRect.y + trackRect.height / 2;
            int startX = trackRect.x;
            int endX = trackRect.x + trackRect.width;
            int fillX = xPositionForValue(slider.getValue());

            graphics.setColor(new Color(105, 38, 0));
            graphics.fillRect(startX, trackY - trackHeight / 2,
                    endX - startX, trackHeight);
            graphics.setColor(new Color(255, 224, 178));
            graphics.fillRect(startX, trackY - fillHeight / 2,
                    endX - startX, fillHeight);
            if (fillX > startX) {
                graphics.setColor(new Color(255, 102, 0));
                graphics.fillRect(startX, trackY - fillHeight / 2,
                        fillX - startX, fillHeight);
            }

            if (showEnds) {
                graphics.setColor(new Color(105, 38, 0));
                graphics.fillRect(startX - 1, trackY - trackHeight / 2 - 1,
                        2, trackHeight + 2);
                graphics.fillRect(endX - 1, trackY - trackHeight / 2 - 1,
                        2, trackHeight + 2);
            }
        }

        @Override
        public void paintFocus(Graphics graphics) {
        }

        @Override
        public void paintThumb(Graphics graphics) {
            graphics.setColor(new Color(105, 38, 0));
            graphics.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            graphics.setColor(new Color(255, 102, 0));
            graphics.fillOval(thumbRect.x + 2, thumbRect.y + 2,
                    thumbRect.width - 4, thumbRect.height - 4);
        }
    }
}
