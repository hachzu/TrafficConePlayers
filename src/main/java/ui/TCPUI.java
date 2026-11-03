package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;

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
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Traffic Cone Player");

        ImageIcon image = new ImageIcon("src/main/resources/assets/cone.png");
        setIconImage(image.getImage());

        // PANELS & CANVAS //
        Color orange = new Color(255, 165, 0);
        JPanel canvas = new JPanel(null);
        canvas.setBackground(orange);
        canvas.setPreferredSize(new Dimension(920, 360));

        // Video Panel
        JPanel videoPanel = new JPanel();
        videoPanel.setBackground(new Color(255, 82, 82));
        videoPanel.setBounds(10, 10, 370, 230);

        // Video / Audio Display Name Panel
        JPanel nameDisplayPanel = new JPanel();
        nameDisplayPanel.setBackground(new Color(153, 146, 198));
        nameDisplayPanel.setBounds(390, 10, 520, 60);

        // Utility Panel (where miscellaneous buttons go)
        JPanel utilityPanel = new JPanel();
        utilityPanel.setBackground(new Color(255, 0, 200));
        utilityPanel.setBounds(390, 80, 520, 160);

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
        timelineSlider.setUI(new BasicSliderUI(timelineSlider) {
            @Override
            protected Dimension getThumbSize() {
                return new Dimension(25, 25);
            }

            @Override
            public void paintTrack(Graphics graphics) {
                int trackY = trackRect.y + trackRect.height / 2;
                int startX = trackRect.x;
                int endX = trackRect.x + trackRect.width;
                int progressX = xPositionForValue(slider.getValue());

                graphics.setColor(new Color(105, 38, 0));
                graphics.fillRect(startX, trackY - 5, endX - startX, 10);

                graphics.setColor(new Color(255, 224, 178));
                graphics.fillRect(startX, trackY - 3, endX - startX, 6);

                if (progressX > startX) {
                    graphics.setColor(new Color(255, 102, 0));
                    graphics.fillRect(startX, trackY - 3, progressX - startX, 6);
                }

                graphics.setColor(new Color(105, 38, 0));
                graphics.fillRect(startX - 1, trackY - 6, 2, 12);
                graphics.fillRect(endX - 1, trackY - 6, 2, 12);
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
        });
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
        Color buttonBackground = new Color(255, 224, 178);
        Color buttonBorder = new Color(105, 38, 0);
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
        currencyLabel.setFont(currencyLabel.getFont().deriveFont(Font.BOLD, 15f));
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

        setContentPane(canvas);
        pack();
    }
}
