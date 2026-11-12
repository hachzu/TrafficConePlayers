package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

final class TCPThemeEditor extends JDialog {
    private static final Map<String, String> COLOR_LABELS = new LinkedHashMap<>();
    private static final Map<String, String> ICON_LABELS = new LinkedHashMap<>();

    static {
        COLOR_LABELS.put("accent", "Canvas accent");
        COLOR_LABELS.put("panelBorder", "Panel and border color");
        COLOR_LABELS.put("buttonBackground", "Button background");
        COLOR_LABELS.put("panelBackground", "Panel background");
        COLOR_LABELS.put("textColor", "Text color");
        COLOR_LABELS.put("buttonTextColor", "Button text color");
        COLOR_LABELS.put("timelineBackground", "Timeline background");
        COLOR_LABELS.put("controlBackground", "Control background");
        COLOR_LABELS.put("clickColor", "Pressed button color");
        COLOR_LABELS.put("sliderTrackColor", "Slider track");
        COLOR_LABELS.put("sliderFillColor", "Slider fill");
        COLOR_LABELS.put("sliderBackgroundColor", "Slider background");

        ICON_LABELS.put("settings", "Settings");
        ICON_LABELS.put("previous", "Previous");
        ICON_LABELS.put("play", "Play");
        ICON_LABELS.put("pause", "Pause");
        ICON_LABELS.put("stop", "Stop");
        ICON_LABELS.put("next", "Next");
        ICON_LABELS.put("shuffleOff", "Shuffle off");
        ICON_LABELS.put("shuffle", "Shuffle on");
        ICON_LABELS.put("playlist", "Playlist");
        ICON_LABELS.put("import", "Import");
        ICON_LABELS.put("store", "Store");
        ICON_LABELS.put("games", "Games");
        ICON_LABELS.put("video", "Video");
        ICON_LABELS.put("volume", "Volume");
        ICON_LABELS.put("window", "Window and title");
        ICON_LABELS.put("currency", "Currency");
    }

    private final TCPDefaultUI owner;
    private final TCPPlayerTheme originalTheme;
    private final TCPPlayerTheme editedTheme;
    private final JTextField nameField;
    private final JLabel backgroundImageLabel;

    TCPThemeEditor(TCPDefaultUI owner) {
        super(owner, "Theme settings", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        originalTheme = owner.theme.copy();
        editedTheme = owner.theme.copy();
        nameField = new JTextField(editedTheme.themeName, 18);

        JPanel colorFields = new JPanel(new GridLayout(0, 2, 8, 6));
        colorFields.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        colorFields.add(new JLabel("Theme name"));
        colorFields.add(nameField);

        JButton chooseBackground = new JButton("Choose image...");
        chooseBackground.addActionListener(event -> chooseBackgroundImage());
        backgroundImageLabel = new JLabel(imageName(editedTheme.backgroundImage));
        colorFields.add(new JLabel("Background image"));
        JPanel backgroundRow = new JPanel(new BorderLayout(6, 0));
        backgroundRow.add(chooseBackground, BorderLayout.WEST);
        backgroundRow.add(backgroundImageLabel, BorderLayout.CENTER);
        colorFields.add(backgroundRow);

        for (var entry : COLOR_LABELS.entrySet()) {
            JButton chooseColor = new JButton("Choose color");
            chooseColor.setBackground(colorFor(entry.getKey()));
            chooseColor.addActionListener(event -> chooseColor(entry.getKey(), chooseColor));
            colorFields.add(new JLabel(entry.getValue()));
            colorFields.add(chooseColor);
        }

        JPanel iconFields = new JPanel(new GridLayout(0, 2, 8, 6));
        iconFields.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        for (var entry : ICON_LABELS.entrySet()) {
            JButton chooseIcon = new JButton("Choose image...");
            JLabel pathLabel = new JLabel(imageName(editedTheme.icons.get(entry.getKey())));
            chooseIcon.addActionListener(event -> chooseIcon(entry.getKey(), pathLabel));
            iconFields.add(new JLabel(entry.getValue()));
            JPanel iconRow = new JPanel(new BorderLayout(6, 0));
            iconRow.add(chooseIcon, BorderLayout.WEST);
            iconRow.add(pathLabel, BorderLayout.CENTER);
            iconFields.add(iconRow);
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Colors and background", new JScrollPane(colorFields));
        tabs.addTab("Icons", new JScrollPane(iconFields));

        JButton saveButton = new JButton("Save as new theme");
        saveButton.addActionListener(event -> saveTheme());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> cancelEditing());
        JPanel actions = new JPanel();
        actions.add(saveButton);
        actions.add(cancelButton);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
    }

    private Color colorFor(String key) {
        return switch (key) {
            case "accent" -> editedTheme.accent;
            case "panelBorder" -> editedTheme.panelBorder;
            case "buttonBackground" -> editedTheme.buttonBackground;
            case "panelBackground" -> editedTheme.panelBackground;
            case "textColor" -> editedTheme.textColor;
            case "buttonTextColor" -> editedTheme.buttonTextColor;
            case "timelineBackground" -> editedTheme.timelineBackground;
            case "controlBackground" -> editedTheme.controlBackground;
            case "clickColor" -> editedTheme.clickColor;
            case "sliderTrackColor" -> editedTheme.sliderTrackColor;
            case "sliderFillColor" -> editedTheme.sliderFillColor;
            case "sliderBackgroundColor" -> editedTheme.sliderBackgroundColor;
            default -> throw new IllegalArgumentException("Unknown theme color: " + key);
        };
    }

    private void chooseColor(String key, JButton preview) {
        Color selected = JColorChooser.showDialog(this, COLOR_LABELS.get(key), colorFor(key));
        if (selected == null) return;

        switch (key) {
            case "accent" -> editedTheme.accent = selected;
            case "panelBorder" -> editedTheme.panelBorder = selected;
            case "buttonBackground" -> editedTheme.buttonBackground = selected;
            case "panelBackground" -> editedTheme.panelBackground = selected;
            case "textColor" -> editedTheme.textColor = selected;
            case "buttonTextColor" -> editedTheme.buttonTextColor = selected;
            case "timelineBackground" -> editedTheme.timelineBackground = selected;
            case "controlBackground" -> editedTheme.controlBackground = selected;
            case "clickColor" -> editedTheme.clickColor = selected;
            case "sliderTrackColor" -> editedTheme.sliderTrackColor = selected;
            case "sliderFillColor" -> editedTheme.sliderFillColor = selected;
            case "sliderBackgroundColor" -> editedTheme.sliderBackgroundColor = selected;
            default -> throw new IllegalArgumentException("Unknown theme color: " + key);
        }

        preview.setBackground(selected);
        owner.applyTheme(editedTheme);
    }

    private void chooseBackgroundImage() {
        JFileChooser chooser = imageChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        editedTheme.backgroundImage = chooser.getSelectedFile().getAbsolutePath();
        backgroundImageLabel.setText(imageName(editedTheme.backgroundImage));
        owner.applyTheme(editedTheme);
    }

    private void chooseIcon(String key, JLabel pathLabel) {
        JFileChooser chooser = imageChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getAbsolutePath();
        editedTheme.icons.put(key, path);
        pathLabel.setText(imageName(path));
        owner.applyTheme(editedTheme);
    }

    private JFileChooser imageChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", "png", "jpg", "jpeg", "gif", "bmp"));
        return chooser;
    }

    private String imageName(String path) {
        if (path == null || path.isBlank()) return "No image selected";
        try {
            return Path.of(path).getFileName().toString();
        } catch (RuntimeException ignored) {
            return path;
        }
    }

    private void saveTheme() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a theme name.", "Missing name",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String safeName = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (safeName.equals(".") || safeName.equals("..")) safeName = "custom_theme";
        Path destination = TCPPlayerTheme.themesDirectory().resolve(safeName + ".properties");
        if (Files.exists(destination)
                && JOptionPane.showConfirmDialog(this, "Replace the existing theme file?",
                "Theme already exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            editedTheme.themeName = name;
            editedTheme.save(destination);
            owner.applyTheme(editedTheme);
            dispose();
        } catch (IOException | RuntimeException error) {
            JOptionPane.showMessageDialog(this, "Could not save theme:\n" + error.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelEditing() {
        owner.applyTheme(originalTheme);
        dispose();
    }
}
