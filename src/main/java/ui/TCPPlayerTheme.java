package ui;

import java.awt.Color;
import java.nio.file.Path;
import java.util.HashMap;

public class TCPPlayerTheme {
    Color accent;
    Color panelBorder;
    Color buttonBackground;
    Color panelBackground;
    Color trackText;
    Color timelineBackground;
    Color controlBackground;
    Color clickColor;
    String backgroundImage;
    String theme;
    HashMap <String , String> icons = new HashMap<>();

    public TCPPlayerTheme copy() {
        TCPPlayerTheme copy = new TCPPlayerTheme(
            
            accent, 
            panelBorder, 
            buttonBackground, 
            panelBackground,
            trackText, 
            timelineBackground, 
            controlBackground, 
            clickColor, 
            backgroundImage,
            theme);

        return copy;
    }

    public TCPPlayerTheme
        (Color accent, 
        Color panelBorder, 
        Color buttonBackground,
        Color panelBackground, 
        Color trackText, 
        Color timelineBackground,
        Color controlBackground, 
        Color clickColor , 
        String backgroundImage,
        String theme) {

        this.accent = accent;
        this.panelBorder = panelBorder;
        this.buttonBackground = buttonBackground;
        this.panelBackground = panelBackground;
        this.trackText = trackText;
        this.timelineBackground = timelineBackground;
        this.controlBackground = controlBackground;
        this.clickColor = clickColor;
        this.backgroundImage = backgroundImage;
        this.theme = theme;
    }

    private  TCPPlayerTheme() {
           
            // just the default theme 

        this(
            new Color(255, 165, 0),
            new Color(105, 38, 0),
            new Color(255, 224, 178),
            new Color(35, 23, 16),
            new Color(255, 180, 40),
            new Color(82, 170, 0),
            new Color(255, 128, 0),
            new Color(255, 102, 0),
            "/assets/mainui_bg.png", // backgroundImage
            "Orange Theme" // theme name
            );
    }

    public static TCPPlayerTheme orange() {
        return new TCPPlayerTheme();
    }

    static Path themesDirectory() {
        return Path.of("src/main/resources/themes");
    }

}
