package ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

class CanvasBackgroundPanel extends JPanel {
    private final Image backgroundImage;

    CanvasBackgroundPanel(String resourcePath) {
        super(null);
        backgroundImage = new ImageIcon(getClass().getResource(resourcePath)).getImage();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        double scale = Math.max((double) getWidth() / backgroundImage.getWidth(this),
                (double) getHeight() / backgroundImage.getHeight(this));
        int imageWidth = (int) (backgroundImage.getWidth(this) * scale);
        int imageHeight = (int) (backgroundImage.getHeight(this) * scale);
        graphics.drawImage(backgroundImage, (getWidth() - imageWidth) / 2,
                (getHeight() - imageHeight) / 2, imageWidth, imageHeight, this);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(920, 360);
    }
}
