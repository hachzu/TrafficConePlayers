package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

class ThemedSliderUI extends BasicSliderUI {
    private final int thumbSize;
    private final int trackHeight;
    private final int fillHeight;
    private final boolean showEnds;

    ThemedSliderUI(JSlider slider, int thumbSize, int trackHeight,
            int fillHeight, boolean showEnds) {
        super(slider);
        this.thumbSize = thumbSize;
        this.trackHeight = trackHeight;
        this.fillHeight = fillHeight;
        this.showEnds = showEnds;
    }

    @Override
    protected Dimension getThumbSize() {
        return new Dimension(thumbSize, thumbSize);
    }

    @Override
    public void paintTrack(Graphics graphics) {
        int trackY = trackRect.y + trackRect.height / 2;
        int startX = trackRect.x;
        int endX = trackRect.x + trackRect.width;
        int fillX = xPositionForValue(slider.getValue());

        graphics.setColor(new Color(105, 38, 0));
        graphics.fillRect(startX, trackY - trackHeight / 2,
                endX - startX, trackHeight);
        graphics.setColor(new Color(255, 224, 178));
        graphics.fillRect(startX, trackY - fillHeight / 2,
                endX - startX, fillHeight);
        if (fillX > startX) {
            graphics.setColor(new Color(255, 102, 0));
            graphics.fillRect(startX, trackY - fillHeight / 2,
                    fillX - startX, fillHeight);
        }

        if (showEnds) {
            graphics.setColor(new Color(105, 38, 0));
            graphics.fillRect(startX - 1, trackY - trackHeight / 2 - 1,
                    2, trackHeight + 2);
            graphics.fillRect(endX - 1, trackY - trackHeight / 2 - 1,
                    2, trackHeight + 2);
        }
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
}
