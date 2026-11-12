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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class TCPThemeEditor extends JDialog {
    private static final Map<String, String> COLOR_LABELS = new LinkedHashMap<>();

    static {
        COLOR_LABELS.put("accent", "Canvas accent");
        COLOR_LABELS.put("panelBorder", "Panel and border color");
        COLOR_LABELS.put("buttonBackground", "Button background");
        COLOR_LABELS.put("panelBackground", "Panel background");
        COLOR_LABELS.put("trackText", "Track text");
        COLOR_LABELS.put("timelineBackground", "Timeline background");
        COLOR_LABELS.put("controlBackground", "Control background");
        COLOR_LABELS.put("clickColor", "Pressed button color");
    }

    private final TCPDefaultUI owner;
    private final TCPPlayerTheme originalTheme;
    private final TCPPlayerTheme editedTheme;
    private final JTextField nameField;

    TCPThemeEditor(TCPDefaultUI owner) {
        super(owner, "Theme settings", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        originalTheme = owner.theme.copy();
        editedTheme = owner.theme.copy();
        nameField = new JTextField(editedTheme.themeName, 18);

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 6));
        fields.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        fields.add(new JLabel("Theme name"));
        fields.add(nameField);

        for (var entry : COLOR_LABELS.entrySet()) {
            JButton chooseColor = new JButton("Choose color");
            chooseColor.setBackground(colorFor(entry.getKey()));
            chooseColor.addActionListener(event -> chooseColor(entry.getKey(), chooseColor));
            fields.add(new JLabel(entry.getValue()));
            fields.add(chooseColor);
        }

        JButton saveButton = new JButton("Save as new theme");
        saveButton.addActionListener(event -> saveTheme());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> cancelEditing());
        JPanel actions = new JPanel();
        actions.add(saveButton);
        actions.add(cancelButton);

        setLayout(new BorderLayout());
        add(fields, BorderLayout.CENTER);
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
            case "trackText" -> editedTheme.trackText;
            case "timelineBackground" -> editedTheme.timelineBackground;
            case "controlBackground" -> editedTheme.controlBackground;
            case "clickColor" -> editedTheme.clickColor;
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
            case "trackText" -> editedTheme.trackText = selected;
            case "timelineBackground" -> editedTheme.timelineBackground = selected;
            case "controlBackground" -> editedTheme.controlBackground = selected;
            case "clickColor" -> editedTheme.clickColor = selected;
            default -> throw new IllegalArgumentException("Unknown theme color: " + key);
        }

        preview.setBackground(selected);
        owner.applyTheme(editedTheme);
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
