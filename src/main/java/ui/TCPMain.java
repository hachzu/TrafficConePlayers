package ui;

import javax.swing.SwingUtilities;

public class TCPMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TCPLandingPage window = new TCPLandingPage();
            new TCPController(window);
            window.setVisible(true);
        });
    }
}
