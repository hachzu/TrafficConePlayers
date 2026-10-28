

import java.awt.*;
import javax.swing.*;

public class landingpage extends JPanel {

    private final Image background = new ImageIcon(
        "src/main/resources/assets/landingpage.png"
    ).getImage();                                                         // Update this path if you move the project.

    public landingpage() {
        setPreferredSize(new Dimension(960, 540));                        // Matches image's 16:9 proportions.
        setLayout(null);

        JButton continueButton = new JButton("CONTINUE");
        continueButton.setBounds(390, 479, 180, 44);                      // Button position (x, y) and size (width, height).
        continueButton.setFont(new Font("Lexend", Font.BOLD, 16));
        continueButton.setBackground(new Color(255, 102, 0));
        continueButton.setForeground(Color.WHITE);
        continueButton.setContentAreaFilled(false);
        continueButton.setOpaque(true);
        continueButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true));
        continueButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        continueButton.addActionListener(e -> {
            Tcplayer.main(new String[0]);                                 // Add or change app-opening code here.
            SwingUtilities.getWindowAncestor(this).dispose();             // Closes the landing window.
        });

        add(continueButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
