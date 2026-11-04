package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

class TCPLandingPage extends JFrame {
    final JButton continueButton;

    TCPLandingPage() {
        setTitle("Traffic Cone Player");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        Image image = new ImageIcon(getClass().getResource(
                "/assets/landingpage.png")).getImage();
        JLabel background = new JLabel(new ImageIcon(
                image.getScaledInstance(920, 518, Image.SCALE_SMOOTH)));
        background.setLayout(null);

        continueButton = new JButton("CONTINUE");
        continueButton.setBounds(380, 462, 160, 42);
        continueButton.setBackground(new Color(255, 224, 178));
        continueButton.setForeground(new Color(105, 38, 0));
        continueButton.setBorder(BorderFactory.createLineBorder(new Color(105, 38, 0), 1));
        continueButton.setFocusPainted(false);
        try (var fontStream = getClass().getResourceAsStream(
                "/assets/fonts/LowresPixel-Regular.otf")) {
            continueButton.setFont(Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(18f));
        } catch (Exception ignored) {
        }
        background.add(continueButton);
        setContentPane(background);
        pack();
        setLocationRelativeTo(null);
    }
}
