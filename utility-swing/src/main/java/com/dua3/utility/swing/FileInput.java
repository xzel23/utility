package com.dua3.utility.swing;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * A Swing component for file/directory inputs. The component consists of a JTextField that can be edited to enter a file path
 * and a button that opens a file/directory selection dialog, where a file/directory can be selected.
 */
public class FileInput extends JPanel {
    /**
     * Enum for file selection modes.
     */
    public enum SelectionMode {
        SELECT_FILE(JFileChooser.FILES_ONLY), SELECT_DIRECTORY(JFileChooser.DIRECTORIES_ONLY), SELECT_FILE_OR_DIRECTORY(JFileChooser.FILES_AND_DIRECTORIES);
        private final int fileSelectionMode;

        SelectionMode(int fileSelectionMode) {
            this.fileSelectionMode = fileSelectionMode;
        }
    }

    /** Select files only. */
    public static final SelectionMode SELECT_FILE = SelectionMode.SELECT_FILE;
    /** Select directories only. */
    public static final SelectionMode SELECT_DIRECTORY = SelectionMode.SELECT_DIRECTORY;
    /** Select files or directories. */
    public static final SelectionMode SELECT_FILE_OR_DIRECTORY = SelectionMode.SELECT_FILE_OR_DIRECTORY;

    private final JTextField textField;
    private final JButton button;
    private final SelectionMode mode;

    /**
     * Constructor.
     * @param mode the {@link SelectionMode}
     * @param initialPath the initial path
     * @param length the length of the text field
     */
    public FileInput(SelectionMode mode, Path initialPath, int length) {
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        this.textField = new JTextField(length);
        this.button = new JButton(SwingUtil.createAction("...", this::showFileSelectionDialog));
        this.mode = mode;
        add(textField);
        add(button);
    }

    /**
     * Show the selection dialog and update content of text field.
     */
    private void showFileSelectionDialog() {
        Path initialPath = Paths.get(".");
        try {
            Path p = Paths.get(getText());
            while (p != null && !Files.isDirectory(p)) {
                p = p.getParent();
            }
            if (p != null) {
                initialPath = p;
            }
        } catch (InvalidPathException e) {
            // ignored
        }

        JFileChooser jFileChooser = new JFileChooser(initialPath.toFile());
        jFileChooser.setFileSelectionMode(mode.fileSelectionMode);
        int rc = jFileChooser.showOpenDialog(this);

        if (rc == JFileChooser.APPROVE_OPTION) {
            textField.setText(String.valueOf(jFileChooser.getSelectedFile()));
        }
    }

    /**
     * Set path.
     * @param p the path
     */
    public void setPath(Path p) {
        textField.setText(p.toString());
    }

    /**
     * Get path. Note that the file or directory described by the returned path may or may not exist on the file system.
     * @return Optional containing the path; empty, if the content of the text field is not a valid path (valid here
     * does not mean a path to an existing file or directory).
     */
    public Optional<Path> getPath() {
        try {
            return Optional.of(Paths.get(getText()));
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }

    /**
     * Get text. It is not checked whether the text is a valid path.
     * @return the content of the text field
     */
    public String getText() {
        return textField.getText();
    }
}