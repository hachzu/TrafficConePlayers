package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

final class ThemeEditor extends JDialog {
    private static final Map<String, String> COLOR_LABELS = new LinkedHashMap<>();
    private static final Map<String, String> ICON_LABELS = new LinkedHashMap<>();

    static {
        COLOR_LABELS.put("accent", "Canvas accent");
        COLOR_LABELS.put("panelBorder", "Border color");
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

    private final DefaultUI owner;
    private final PlayerTheme originalTheme;
    private final PlayerTheme editedTheme;
    private final JTextField nameField;
    private final JLabel backgroundImageLabel;
    private final Map<String, JLabel> iconPathLabels = new LinkedHashMap<>();

    ThemeEditor(DefaultUI owner) {
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
            iconPathLabels.put(entry.getKey(), pathLabel);
            chooseIcon.addActionListener(event -> chooseIcon(entry.getKey(), pathLabel));
            iconFields.add(new JLabel(entry.getValue()));
            JPanel iconRow = new JPanel(new BorderLayout(6, 0));
            iconRow.add(chooseIcon, BorderLayout.WEST);
            iconRow.add(pathLabel, BorderLayout.CENTER);
            iconFields.add(iconRow);
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Colors and background", new JScrollPane(colorFields));
        JButton importIcons = new JButton("Import icons...");
        importIcons.addActionListener(event -> importIcons());
        JPanel iconTab = new JPanel(new BorderLayout());
        iconTab.add(importIcons, BorderLayout.NORTH);
        iconTab.add(new JScrollPane(iconFields), BorderLayout.CENTER);
        tabs.addTab("Icons", iconTab);

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

    private void importIcons() {
        JFileChooser chooser = imageChooser();
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] files = chooser.getSelectedFiles();
        String[] slots = ICON_LABELS.keySet().toArray(new String[0]);
        String[] choices = new String[slots.length + 1];
        choices[0] = "Skip";
        for (int index = 0; index < slots.length; index++) {
            choices[index + 1] = ICON_LABELS.get(slots[index]);
        }

        JPanel assignments = new JPanel(new GridLayout(0, 2, 10, 10));
        JComboBox<?>[] selectors = new JComboBox<?>[files.length];
        for (int index = 0; index < files.length; index++) {
            assignments.add(new JLabel(files[index].getName()));
            selectors[index] = new JComboBox<>(choices);
            assignments.add(selectors[index]);
        }
        JScrollPane scrollPane = new JScrollPane(assignments);
        scrollPane.setPreferredSize(new Dimension(500, 250));

        while (JOptionPane.showConfirmDialog(this, scrollPane, "Assign imported icons",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Map<String, String> selectedIcons = new LinkedHashMap<>();
            int assignedCount = 0;
            for (int index = 0; index < files.length; index++) {
                int choice = selectors[index].getSelectedIndex();
                if (choice > 0) {
                    selectedIcons.put(slots[choice - 1], files[index].getAbsolutePath());
                    assignedCount++;
                }
            }
            if (selectedIcons.size() != assignedCount) {
                JOptionPane.showMessageDialog(this, "Assign only one image to each icon.");
                continue;
            }

            for (Map.Entry<String, String> entry : selectedIcons.entrySet()) {
                editedTheme.icons.put(entry.getKey(), entry.getValue());
                iconPathLabels.get(entry.getKey()).setText(imageName(entry.getValue()));
            }
            owner.applyTheme(editedTheme);
            return;
        }
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
        Path destination = PlayerTheme.themesDirectory().resolve(safeName + ".properties");
        if (Files.exists(destination)
                && JOptionPane.showConfirmDialog(this, "Replace the existing theme file?",
                "Theme already exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            PlayerTheme themeToSave = editedTheme.copy();
            themeToSave.themeName = name;
            Path assetDirectory = PlayerTheme.themesDirectory().resolve(safeName);
            Path iconDirectory = assetDirectory.resolve("icons");

            themeToSave.backgroundImage = copyCustomAsset(
                    themeToSave.backgroundImage, assetDirectory, "background");
            for (var entry : themeToSave.icons.entrySet()) {
                entry.setValue(copyCustomAsset(entry.getValue(), iconDirectory, entry.getKey()));
            }

            themeToSave.save(destination);
            owner.applyTheme(PlayerTheme.load(destination));
            dispose();
        } catch (IOException | RuntimeException error) {
            JOptionPane.showMessageDialog(this, "Could not save theme:\n" + error.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String copyCustomAsset(String value, Path destinationDirectory, String targetName)
            throws IOException {
        if (value == null || value.isBlank() || getClass().getResource(value) != null) {
            return value;
        }

        Path source = Path.of(value);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Selected image file no longer exists: " + source);
        }

        Files.createDirectories(destinationDirectory);
        String filename = source.getFileName().toString();
        int extensionStart = filename.lastIndexOf('.');
        String extension = extensionStart >= 0 ? filename.substring(extensionStart) : ".img";
        Path destination = destinationDirectory.resolve(targetName + extension);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

        Path themesRoot = PlayerTheme.themesDirectory();
        return themesRoot.relativize(destination).toString().replace('\\', '/');
    }

    private void cancelEditing() {
        owner.applyTheme(originalTheme);
        dispose();
    }
}
