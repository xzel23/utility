package com.dua3.utility.samples;

import com.dua3.utility.application.ApplicationUtil;
import com.dua3.utility.application.UiMode;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Minimal Swing application that shows three radio buttons to choose the UiMode
 * and renders in light/dark according to ApplicationUtil's active dark mode.
 */
public final class DarkModeSample {

    private DarkModeSample() {
        // no instances
    }

    /**
     * The main method initializes and displays the application's UI for switching between
     * different UI modes (System Default, Light, and Dark). It sets up the UI components,
     * configures the look and feel, and handles user interactions to toggle the application's
     * UI mode.
     *
     * @param args the command-line arguments passed to the program, not used in this implementation
     */
    public static void main(String[] args) {
        // Use system L&F where possible to keep it minimalistic
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // keep defaults
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dark Mode Sample");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JPanel content = new JPanel();
            content.setLayout(new BorderLayout());

            JPanel buttons = new JPanel();
            buttons.setOpaque(true);

            ButtonGroup group = new ButtonGroup();

            // Create radio buttons for each UiMode
            JRadioButton rbSystem = new JRadioButton("System default");
            JRadioButton rbLight = new JRadioButton("Light");
            JRadioButton rbDark = new JRadioButton("Dark");

            // Minimal, readable font
            Font f = rbSystem.getFont().deriveFont(Font.PLAIN, Math.max(12.0f, rbSystem.getFont().getSize2D()));
            rbSystem.setFont(f);
            rbLight.setFont(f);
            rbDark.setFont(f);

            group.add(rbSystem);
            group.add(rbLight);
            group.add(rbDark);

            buttons.add(rbSystem);
            buttons.add(rbLight);
            buttons.add(rbDark);

            content.add(buttons, BorderLayout.CENTER);
            frame.setContentPane(content);

            // Reflect current UiMode selection
            UiMode currentMode = ApplicationUtil.getUiMode();
            switch (currentMode) {
                case SYSTEM_DEFAULT -> rbSystem.setSelected(true);
                case LIGHT -> rbLight.setSelected(true);
                case DARK -> rbDark.setSelected(true);
            }

            // Apply initial dark/light based on application state
            applyTheme(ApplicationUtil.isDarkMode(), frame.getContentPane());

            // Update when application dark mode changes
            ApplicationUtil.addDarkModeListener(dark -> SwingUtilities.invokeLater(
                    () -> applyTheme(dark, frame.getContentPane())));

            // Change UiMode when user toggles
            rbSystem.addActionListener(e -> ApplicationUtil.setUiMode(UiMode.SYSTEM_DEFAULT));
            rbLight.addActionListener(e -> ApplicationUtil.setUiMode(UiMode.LIGHT));
            rbDark.addActionListener(e -> ApplicationUtil.setUiMode(UiMode.DARK));

            frame.setSize(360, 120);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);

            // Ensure JVM exits when window is closed if this sample is run alone
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {System.exit(0);}
            });
        });
    }

    private static void applyTheme(boolean dark, Container root) {
        // Minimal theming: toggle background/foreground for readability
        Color bg = dark ? new Color(0x1E1E1E) : Color.white;
        Color fg = dark ? new Color(0xE6E6E6) : Color.black;
        setColorsRecursive(root, bg, fg);
        // Optionally tweak panel background to match
        root.repaint();
    }

    private static void setColorsRecursive(Component c, Color bg, Color fg) {
        c.setBackground(bg);
        c.setForeground(fg);
        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) {
                setColorsRecursive(child, bg, fg);
            }
        }
    }
}
