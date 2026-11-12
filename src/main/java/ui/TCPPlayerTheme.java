package ui;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;

public class TCPPlayerTheme {
    Color accent;
    Color panelBorder;
    Color buttonBackground;
    Color panelBackground;
    Color textColor;
    Color buttonTextColor;
    Color timelineBackground;
    Color controlBackground;
    Color clickColor;
    Color sliderTrackColor;
    Color sliderFillColor;
    Color sliderBackgroundColor;
    String backgroundImage;
    String themeName;
    HashMap <String , String> icons = new HashMap<>();

    public TCPPlayerTheme copy() {
        TCPPlayerTheme copy = new TCPPlayerTheme(
            
            accent, 
            panelBorder, 
            buttonBackground, 
            panelBackground,
            textColor, 
            timelineBackground, 
            controlBackground, 
            clickColor, 
            backgroundImage,
            themeName);
        copy.icons.putAll(icons);
        copy.buttonTextColor = buttonTextColor;
        copy.sliderTrackColor = sliderTrackColor;
        copy.sliderFillColor = sliderFillColor;
        copy.sliderBackgroundColor = sliderBackgroundColor;
        return copy;
    }

    public TCPPlayerTheme
        (Color accent, 
        Color panelBorder, 
        Color buttonBackground,
        Color panelBackground, 
        Color textColor, 
        Color timelineBackground,
        Color controlBackground, 
        Color clickColor , 
        String backgroundImage,
        String theme) {

        this.accent = accent;
        this.panelBorder = panelBorder;
        this.buttonBackground = buttonBackground;
        this.panelBackground = panelBackground;
        this.textColor = textColor;
        this.buttonTextColor = panelBorder;
        this.timelineBackground = timelineBackground;
        this.controlBackground = controlBackground;
        this.clickColor = clickColor;
        this.sliderTrackColor = panelBorder;
        this.sliderFillColor = clickColor;
        this.sliderBackgroundColor = buttonBackground;
        this.backgroundImage = backgroundImage;
        this.themeName = theme;
    }
       
    public static TCPPlayerTheme starter() {
        try (var files = Files.list(themesDirectory())) {
            Path firstTheme = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".properties"))
                    .sorted()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No theme properties files found in " + themesDirectory()));
            return load(firstTheme);
        } catch (IOException error) {
            throw new IllegalStateException("Could not load the default theme", error);
        }
    }
    

    static Path themesDirectory() {
        return Path.of("src" , "main" , "resources" , "assets" , "themes" );
    }


    static TCPPlayerTheme load(Path file) throws IOException {
       Properties properties = new Properties();
       try (InputStream input = Files.newInputStream(file)) {
           properties.load(input);
       }
       return fromProperties(properties, null, file.getFileName().toString());
   }

   private static TCPPlayerTheme fromProperties(Properties properties, TCPPlayerTheme theme, String fallbackName) {
       if (theme == null) {
           theme = new TCPPlayerTheme(null, null, null, null, null, null, null, null, null, fallbackName);
       }
       theme.themeName = properties.getProperty("name", fallbackName);
       theme.accent = readColor(properties, "accent", theme.accent);
       theme.panelBorder = readColor(properties, "panelBorder", theme.panelBorder);
       theme.buttonTextColor = readColor(properties, "buttonTextColor", theme.panelBorder);
       theme.buttonBackground = readColor(properties, "buttonBackground", theme.buttonBackground);
       theme.panelBackground = readColor(properties, "panelBackground", theme.panelBackground);
       theme.textColor = readColor(properties, "textColor",
               readColor(properties, "trackText", theme.textColor));
       theme.timelineBackground = readColor(properties, "timelineBackground", theme.timelineBackground);
       theme.controlBackground = readColor(properties, "controlBackground", theme.controlBackground);
       theme.clickColor = readColor(properties, "clickColor", theme.clickColor);
       theme.sliderTrackColor = readColor(properties, "sliderTrackColor", theme.panelBorder);
       theme.sliderFillColor = readColor(properties, "sliderFillColor", theme.clickColor);
       theme.sliderBackgroundColor = readColor(properties, "sliderBackgroundColor", theme.buttonBackground);
       theme.backgroundImage = properties.getProperty("backgroundImage", theme.backgroundImage);

       for (String key : properties.stringPropertyNames()) {
           if (key.startsWith("icon.")) theme.icons.put(key.substring(5), properties.getProperty(key));
       }

       return theme;
   }

   private static Color readColor(Properties properties, String key, Color fallback) {
       try {
           return Color.decode(properties.getProperty(key));
       } catch (RuntimeException ignored) {
           return fallback;
       }
   }

   private static String colorHex(Color color) {
       return String.format("#%06X", color.getRGB() & 0xFFFFFF);
   }

   void save(Path file) throws IOException {
        Properties properties = new Properties();
            properties.setProperty("name", themeName);
            properties.setProperty("accent", colorHex(accent));
            properties.setProperty("panelBorder", colorHex(panelBorder));
            properties.setProperty("buttonBackground", colorHex(buttonBackground));
            properties.setProperty("panelBackground", colorHex(panelBackground));
            properties.setProperty("textColor", colorHex(textColor));
            properties.setProperty("buttonTextColor", colorHex(buttonTextColor));
            properties.setProperty("timelineBackground", colorHex(timelineBackground));
            properties.setProperty("controlBackground", colorHex(controlBackground));
            properties.setProperty("clickColor", colorHex(clickColor));
            properties.setProperty("sliderTrackColor", colorHex(sliderTrackColor));
            properties.setProperty("sliderFillColor", colorHex(sliderFillColor));
            properties.setProperty("sliderBackgroundColor", colorHex(sliderBackgroundColor));
            properties.setProperty("backgroundImage", backgroundImage == null ? "" : backgroundImage);
            icons.forEach((key, value) -> properties.setProperty("icon." + key, value));
            try (OutputStream output = Files.newOutputStream(file)) {
            properties.store(output, "Traffic Cone Player theme");
       }
   }



}
