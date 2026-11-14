package ui;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public final class Icons {
    private Icons() { }

    public static ImageIcon load(String filename, int size) {
        URL image = Icons.class.getResource("/assets/icons/" + filename);
        if (image == null) {
            return null;
        }
        return new ImageIcon(new ImageIcon(image).getImage()
                .getScaledInstance(size, size, Image.SCALE_AREA_AVERAGING));
    }
}
