package ui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LandingPage window = new LandingPage();
            new Controller(window);
            window.setVisible(true);
        });
    }
}
