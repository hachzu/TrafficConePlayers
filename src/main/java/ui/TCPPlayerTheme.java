package ui;

import java.awt.Color;

public class TCPPlayerTheme {
    final Color accent;
    final Color panelBorder;
    final Color buttonBackground;
    final Color panelBackground;
    final Color trackText;
    final Color timelineBackground;
    final Color controlBackground;
    final Color clickColor;

    public TCPPlayerTheme copy() {
        return new TCPPlayerTheme(accent, panelBorder, buttonBackground, panelBackground,
                trackText, timelineBackground, controlBackground, clickColor);
    }

    public TCPPlayerTheme(Color accent, Color panelBorder, Color buttonBackground,
            Color panelBackground, Color trackText, Color timelineBackground,
            Color controlBackground, Color clickColor) {
        this.accent = accent;
        this.panelBorder = panelBorder;
        this.buttonBackground = buttonBackground;
        this.panelBackground = panelBackground;
        this.trackText = trackText;
        this.timelineBackground = timelineBackground;
        this.controlBackground = controlBackground;
        this.clickColor = clickColor;
    }

    private TCPPlayerTheme() {
        this(new Color(255, 165, 0), new Color(105, 38, 0), new Color(255, 224, 178),
                new Color(35, 23, 16), new Color(255, 180, 40), new Color(82, 170, 0),
                new Color(255, 128, 0), new Color(255, 102, 0));
    }

    public static TCPPlayerTheme orange() {
        return new TCPPlayerTheme();
    }
}
