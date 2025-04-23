package com.dua3.utility.swing;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.io.File;
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
    private static final Logger LOG = LogManager.getLogger(FileInput.class);

    /**
     * Select files only.
     */
    public static final SelectionMode SELECT_FILE = SelectionMode.SELECT_FILE;
    /**
     * Select directories only.
     */
    public static final SelectionMode SELECT_DIRECTORY = SelectionMode.SELECT_DIRECTORY;
    /**
     * Select files or directories.
     */
    public static final SelectionMode SELECT_FILE_OR_DIRECTORY = SelectionMode.SELECT_FILE_OR_DIRECTORY;
    /**
     * The text input for the file path.
     */
    private final JTextField textField;
    /**
     * The {@link SelectionMode} for the file selection dialog.
     */
    private final SelectionMode mode;

    /**
     * Constructor.
     *
     * @param mode        the {@link SelectionMode}
     * @param initialPath the initial path
     * @param length      the length of the text field
     */
    public FileInput(SelectionMode mode, Path initialPath, int length) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.textField = new JTextField(initialPath.toString(), length);
        JButton button = new JButton(SwingUtil.createAction("…", this::showFileSelectionDialog));
        this.mode = mode;
        add(textField);
        add(button);

        SwingUtil.addDropFilesSupport(textField, files -> files.stream().findFirst().map(File::toPath).ifPresent(this::setPath));
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
     * Get path. Note that the file or directory described by the returned path may or may not exist on the file system.
     *
     * @return Optional containing the path; empty, if the content of the text field is not a valid path (valid here
     * does not mean a path to an existing file or directory).
     */
    public Optional<Path> getPath() {
        String t = getText();
        try {
            return Optional.of(Paths.get(t));
        } catch (InvalidPathException e) {
            LOG.warn("invalid path: {}", t, e);
            return Optional.empty();
        }
    }

    /**
     * Set path.
     *
     * @param p the path
     */
    public void setPath(Path p) {
        textField.setText(p.toString());
    }

    /**
     * Get text. It is not checked whether the text is a valid path.
     *
     * @return the content of the text field
     */
    public String getText() {
        return textField.getText();
    }

    /**
     * Enum for file selection modes.
     * <p>
     * The file selection mode determines whether the user can select only files,
     * only directories, or both files and directories when browsing for a file.
     */
    public enum SelectionMode {
        /**
         * The SELECT_FILE constant indicates that only files should be selectable.
         */
        SELECT_FILE(JFileChooser.FILES_ONLY),
        /**
         * The SELECT_DIRECTORY constant indicates that only directories should be selectable.
         */
        SELECT_DIRECTORY(JFileChooser.DIRECTORIES_ONLY),
        /**
         * The SELECT_DIRECTORY constant indicates that both files and directories should be selectable.
         */
        SELECT_FILE_OR_DIRECTORY(JFileChooser.FILES_AND_DIRECTORIES);

        private final int fileSelectionMode;

        SelectionMode(int fileSelectionMode) {
            this.fileSelectionMode = fileSelectionMode;
        }
    }
}
