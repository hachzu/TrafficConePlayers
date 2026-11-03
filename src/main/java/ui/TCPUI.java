package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSliderUI;

public class TCPUI extends JFrame {

    // ---------- Theme ----------
    private static final Color ORANGE      = new Color(255, 165, 0);
    private static final Color DEEP_ORANGE = new Color(255, 128, 0);
    private static final Color FILL_ORANGE = new Color(255, 102, 0);
    private static final Color CREAM       = new Color(255, 224, 178);
    private static final Color BROWN       = new Color(105, 38, 0);
    private static final Color DARK_PANEL  = new Color(35, 23, 16);
    private static final Color AMBER       = new Color(255, 180, 40);

    // ---------- Layout ----------
    private static final int WIDTH = 920;
    private static final Dimension ICON_BUTTON_SIZE = new Dimension(44, 40);

    // ---------- State shared between panels ----------
    private final Font pixelFont = loadPixelFont();
    private final ImageIcon appIcon = load("/assets/cone.png");
    private final VinylVideoPanel videoPanel = new VinylVideoPanel();
    private JToggleButton videoToggle;
    private JToggleButton playPauseButton;

    TCPUI() {
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Traffic Cone Player");
        setIconImage(appIcon.getImage());

        JPanel canvas = new JPanel(null);
        canvas.setBackground(ORANGE);
        canvas.setPreferredSize(new Dimension(WIDTH, 360));
        canvas.add(createVideoPanel());
        canvas.add(createNameDisplay());
        canvas.add(createUtilityPanel());
        canvas.add(createTimelinePanel());
        canvas.add(createControlPanel());
        wirePlayback();

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(line(2));
        content.add(createTitleBar(), BorderLayout.NORTH);
        content.add(canvas, BorderLayout.CENTER);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }

    // =====================================================================
    //  Panels
    // =====================================================================

    private JPanel createVideoPanel() {
        videoPanel.setBounds(10, 10, 370, 230);
        videoPanel.setBorder(line(2));
        return videoPanel;
    }

    private JPanel createNameDisplay() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(DARK_PANEL);
        panel.setBounds(390, 10, 520, 60);
        panel.setBorder(line(2));
        panel.add(displayLabel("TRACK TITLE", 18f));
        panel.add(displayLabel("01 / 05   |   00:00", 13f));
        return panel;
    }

    private JPanel createUtilityPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(DARK_PANEL);
        panel.setBounds(390, 80, 520, 160);
        panel.setBorder(BorderFactory.createCompoundBorder(
                line(2), BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        // Top row: volume icon, slider, user menu
        JSlider volumeSlider = createSlider(0, 100, 70, "Volume");
        volumeSlider.addChangeListener(e -> videoPanel.setVolume(volumeSlider.getValue() / 100.0));
        videoPanel.setVolume(volumeSlider.getValue() / 100.0);

        JButton userButton = new JButton("USER  v");
        styleButton(userButton, new Dimension(122, 38), "User menu (empty placeholder)");

        JPanel top = transparentPanel(new BorderLayout(10, 0));
        top.add(new JLabel(load("/assets/icons/volume.png")), BorderLayout.WEST);
        top.add(volumeSlider, BorderLayout.CENTER);
        top.add(userButton, BorderLayout.EAST);

        // Bottom row: video toggle, settings
        videoToggle = new JToggleButton("VIDEO", scaled(load("/assets/icons/video.png"), 24));
        styleButton(videoToggle, new Dimension(110, 34), "Video mode (vinyl plays when off)");
        videoToggle.setHorizontalTextPosition(SwingConstants.RIGHT);
        videoToggle.setIconTextGap(8);

        JButton settingsButton = new JButton(icon("settings"));
        settingsButton.setPreferredSize(new Dimension(32, 32));
        settingsButton.setMargin(new Insets(0, 0, 0, 0));
        settingsButton.setOpaque(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setFocusable(false);
        settingsButton.setToolTipText("Settings");

        JPanel settingsAnchor = transparentPanel(new BorderLayout());
        settingsAnchor.add(settingsButton, BorderLayout.SOUTH);

        JPanel bottom = transparentPanel(new BorderLayout());
        bottom.add(videoToggle, BorderLayout.WEST);
        bottom.add(settingsAnchor, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTimelinePanel() {
        JPanel panel = transparentPanel(new BorderLayout());
        panel.setBounds(0, 260, WIDTH, 40);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        panel.add(createSlider(0, 1000, 0, "Seek position"), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DEEP_ORANGE);
        panel.setBounds(0, 310, WIDTH, 50);

        // Left: playback controls
        playPauseButton = toggleButton("play", "pause", "Play or pause");
        playPauseButton.setSelected(true);

        JPanel playback = transparentPanel(new GridLayout(1, 5, 8, 0));
        playback.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        playback.add(iconButton("previous", "Previous track"));
        playback.add(playPauseButton);
        playback.add(iconButton("stop", "Stop"));
        playback.add(iconButton("next", "Next track"));
        playback.add(toggleButton("shuffle-off", "shuffle", "Toggle shuffle"));

        // Right: download / store / games + currency
        JPanel icons = transparentPanel(new GridLayout(1, 3, 8, 0));
        icons.add(iconButton("download", "Download"));
        icons.add(iconButton("store", "Store"));
        icons.add(iconButton("gamepad", "Games"));

        JPanel utility = transparentPanel(new BorderLayout(8, 0));
        utility.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        utility.add(icons, BorderLayout.CENTER);
        utility.add(createCurrencyDisplay(), BorderLayout.EAST);

        panel.add(playback, BorderLayout.WEST);
        panel.add(utility, BorderLayout.EAST);
        return panel;
    }

    private JPanel createCurrencyDisplay() {
        JLabel label = new JLabel("1,322", scaled(appIcon, 24), SwingConstants.CENTER);
        label.setHorizontalTextPosition(SwingConstants.RIGHT);
        label.setIconTextGap(6);
        label.setForeground(BROWN);
        label.setFont(pixelFont.deriveFont(Font.BOLD, 15f));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(112, 40));
        panel.setBackground(CREAM);
        panel.setBorder(line(1));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTitleBar() {
        JLabel title = new JLabel("Traffic Cone Player", scaled(appIcon, 24), SwingConstants.LEFT);
        title.setIconTextGap(8);
        title.setForeground(Color.WHITE);
        title.setFont(pixelFont.deriveFont(Font.BOLD, 14f));
        title.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel buttons = transparentPanel(new GridLayout(1, 2, 6, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        buttons.add(windowButton("−", "Minimize", () -> setState(JFrame.ICONIFIED)));
        buttons.add(windowButton("×", "Close",
                () -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(WIDTH, 36));
        bar.setBackground(BROWN);
        bar.add(title, BorderLayout.CENTER);
        bar.add(buttons, BorderLayout.EAST);

        makeDraggable(bar, title);
        return bar;
    }

    // =====================================================================
    //  Behavior
    // =====================================================================

    /** Links the play/pause button, the video toggle and the vinyl panel. */
    private void wirePlayback() {
        videoPanel.setPlaying(true);

        playPauseButton.addActionListener(e ->
                videoPanel.setPlaying(!videoToggle.isSelected() && playPauseButton.isSelected()));

        videoToggle.addActionListener(e -> {
            boolean videoMode = videoToggle.isSelected();
            videoPanel.setVinylVisible(!videoMode);
            playPauseButton.setSelected(!videoMode);
            videoPanel.setPlaying(!videoMode);
        });
    }

    private void makeDraggable(Component... targets) {
        MouseAdapter drag = new MouseAdapter() {
            private Point offset;

            @Override
            public void mousePressed(MouseEvent e) {
                offset = new Point(e.getXOnScreen() - getX(), e.getYOnScreen() - getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - offset.x, e.getYOnScreen() - offset.y);
            }
        };
        for (Component target : targets) {
            target.addMouseListener(drag);
            target.addMouseMotionListener(drag);
        }
    }

    // =====================================================================
    //  Component factories
    // =====================================================================

    private JLabel displayLabel(String text, float size) {
        JLabel label = new JLabel(text);
        label.setFont(pixelFont.deriveFont(size));
        label.setForeground(AMBER);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setBorder(BorderFactory.createEmptyBorder(3, 12, 0, 12));
        return label;
    }

    private JButton iconButton(String iconName, String tooltip) {
        JButton button = new JButton(icon(iconName));
        styleButton(button, ICON_BUTTON_SIZE, tooltip);
        return button;
    }

    private JToggleButton toggleButton(String iconName, String selectedIconName, String tooltip) {
        JToggleButton button = new JToggleButton(icon(iconName));
        button.setSelectedIcon(icon(selectedIconName));
        styleButton(button, ICON_BUTTON_SIZE, tooltip);
        return button;
    }

    private JButton windowButton(String text, String tooltip, Runnable action) {
        JButton button = new JButton(text);
        styleButton(button, new Dimension(34, 28), tooltip);
        button.setBackground(ORANGE);
        button.setFont(pixelFont.deriveFont(Font.BOLD, 16f));
        button.addActionListener(e -> action.run());
        return button;
    }

    /** Applies the shared cream-and-brown look to any button. */
    private void styleButton(AbstractButton button, Dimension size, String tooltip) {
        button.setPreferredSize(size);
        button.setBackground(CREAM);
        button.setForeground(BROWN);
        button.setFont(pixelFont.deriveFont(14f));
        button.setBorder(line(1));
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setToolTipText(tooltip);
    }

    private static JSlider createSlider(int min, int max, int value, String tooltip) {
        JSlider slider = new JSlider(min, max, value);
        slider.setOpaque(false);
        slider.setFocusable(false);
        slider.setToolTipText(tooltip);
        slider.setUI(new ThemedSliderUI(slider));
        return slider;
    }

    private static JPanel transparentPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    // =====================================================================
    //  Resources & small helpers
    // =====================================================================

    private static Border line(int thickness) {
        return BorderFactory.createLineBorder(BROWN, thickness);
    }

    private static ImageIcon load(String path) {
        return new ImageIcon(TCPUI.class.getResource(path));
    }

    private static ImageIcon icon(String name) {
        return load("/assets/icons/" + name + ".png");
    }

    private static ImageIcon scaled(ImageIcon source, int size) {
        return new ImageIcon(source.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    private static Font loadPixelFont() {
        try (var stream = TCPUI.class.getResourceAsStream("/assets/fonts/LowresPixel-Regular.otf")) {
            return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception e) {
            return new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }
    }

    // =====================================================================
    //  Custom slider look
    // =====================================================================

    private static class ThemedSliderUI extends BasicSliderUI {
        private static final int THUMB_SIZE = 25;
        private static final int TRACK_HEIGHT = 10;
        private static final int FILL_HEIGHT = 6;

        ThemedSliderUI(JSlider slider) {
            super(slider);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(THUMB_SIZE, THUMB_SIZE);
        }

        @Override
        public void paintTrack(Graphics g) {
            int centerY = trackRect.y + trackRect.height / 2;
            int startX = trackRect.x;
            int endX = trackRect.x + trackRect.width;
            int fillX = xPositionForValue(slider.getValue());
            int trackTop = centerY - TRACK_HEIGHT / 2;
            int fillTop = centerY - FILL_HEIGHT / 2;

            // outline, empty part, filled part
            g.setColor(BROWN);
            g.fillRect(startX, trackTop, endX - startX, TRACK_HEIGHT);
            g.setColor(CREAM);
            g.fillRect(startX, fillTop, endX - startX, FILL_HEIGHT);
            if (fillX > startX) {
                g.setColor(FILL_ORANGE);
                g.fillRect(startX, fillTop, fillX - startX, FILL_HEIGHT);
            }

            // end caps
            g.setColor(BROWN);
            g.fillRect(startX - 1, trackTop - 1, 2, TRACK_HEIGHT + 2);
            g.fillRect(endX - 1, trackTop - 1, 2, TRACK_HEIGHT + 2);
        }

        @Override
        public void paintThumb(Graphics g) {
            g.setColor(BROWN);
            g.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            g.setColor(FILL_ORANGE);
            g.fillOval(thumbRect.x + 2, thumbRect.y + 2, thumbRect.width - 4, thumbRect.height - 4);
        }

        @Override
        public void paintFocus(Graphics g) {
            // no focus ring
        }
    }
}