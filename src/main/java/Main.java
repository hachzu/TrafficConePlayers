import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {                         // Builds the interface on Swing's UI thread.
            JFrame frame = new JFrame("Traffic Cone Player");      // Creates and names the window.
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exits the program when the window closes.
            frame.setContentPane(new landingpage());               // Adds the landing screen.
            frame.pack();                                         // Fits the window to the landing image's dimensions.
            frame.setResizable(false);                            // Keeps the image's proportions.
            frame.setLocationRelativeTo(null);                     // Centers the window on your screen.
            frame.setVisible(true);                                // Displays the completed window.
        });
    }
}
