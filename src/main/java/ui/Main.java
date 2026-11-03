package ui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
       SwingUtilities.invokeLater(() -> {
        TCPUI window = new TCPUI();
        window.setVisible(true);
        });
    }
}