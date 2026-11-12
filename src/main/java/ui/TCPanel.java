package ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

class TCPanel extends JPanel {
    private Image backgroundImage;

    TCPanel(String resourcePath) {
        super(null);
        setBackgroundImage(resourcePath);
    }

    void setBackgroundImage(String resourcePath) {
        var resource = resourcePath == null ? null : getClass().getResource(resourcePath);
        if (resource != null) {
            backgroundImage = new ImageIcon(resource).getImage();
        } else if (resourcePath != null) {
            ImageIcon fileImage = new ImageIcon(resourcePath);
            backgroundImage = fileImage.getIconWidth() > 0 ? fileImage.getImage() : null;
        } else {
            backgroundImage = null;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (backgroundImage == null) return;
        int originalWidth = backgroundImage.getWidth(this);
        int originalHeight = backgroundImage.getHeight(this);
        if (originalWidth <= 0 || originalHeight <= 0) return;
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
