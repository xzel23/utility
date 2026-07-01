package com.dua3.utility.samples;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.swing.TextEditorPane;
import com.dua3.utility.swing.TextPane;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.DetachableNode;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Swing sample for {@link TextEditorPane} and {@link TextPane}.
 */
public final class SwingTextEditorPaneSample {
    private static final AwtFontUtil FONT_UTIL = AwtFontUtil.getInstance();

    private SwingTextEditorPaneSample() {
        // no instances
    }

    /**
     * Entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SwingUtil.setNativeLookAndFeel("SwingTextEditorPaneSample");
        SwingUtilities.invokeLater(SwingTextEditorPaneSample::createAndShowUi);
    }

    private static void createAndShowUi() {
        RichText defaultText = createSampleText();

        TextEditorPane editor = new TextEditorPane(defaultText);
        editor.setWrapText(true);
        editor.setPreferredSize(new Dimension(640, 220));

        TextPane liveDocumentPane = new TextPane(defaultText);
        liveDocumentPane.setWrapText(true);
        liveDocumentPane.setPreferredSize(new Dimension(640, 200));

        TextPane committedValuePane = new TextPane(defaultText);
        committedValuePane.setWrapText(true);
        committedValuePane.setPreferredSize(new Dimension(640, 200));

        JLabel selectionInfo = new JLabel();
        JLabel committedInfo = new JLabel("Committed value length: " + defaultText.length() + " characters");
        JLabel status = new JLabel("No action yet.");

        final RichText[] committedValue = {defaultText};

        Runnable syncLiveDocument = () -> liveDocumentPane.setText(editor.getText());
        Runnable updateSelectionInfo = () -> selectionInfo.setText(
                "Caret: " + editor.getCaretPosition()
                        + "  Selection: [" + editor.getSelectionStart()
                        + ", " + editor.getSelectionEnd() + ")"
                        + "  Selected: \"" + oneLine(editor.getSelectedText()) + "\""
        );

        Runnable updateCommittedInfo = () -> committedInfo.setText(
                "Committed value length: " + committedValue[0].length() + " characters"
        );

        Runnable syncAll = () -> {
            syncLiveDocument.run();
            updateSelectionInfo.run();
        };

        JCheckBox wrap = new JCheckBox("Wrap text", true);
        wrap.addActionListener(e -> {
            boolean enabled = wrap.isSelected();
            editor.setWrapText(enabled);
            liveDocumentPane.setWrapText(enabled);
            committedValuePane.setWrapText(enabled);
        });

        JCheckBox editable = new JCheckBox("Editable", editor.isEditable());
        editable.addActionListener(e -> editor.setEditable(editable.isSelected()));

        JComboBox<DetachableNode.Location> toolbarLocation = new JComboBox<>(DetachableNode.Location.values());

        editor.addPropertyChangeListener("documentVersion", evt -> {
            syncAll.run();
        });
        editor.addPropertyChangeListener("caretPosition", evt -> {
            updateSelectionInfo.run();
        });
        editor.addPropertyChangeListener("selectionStart", evt -> {
            updateSelectionInfo.run();
        });
        editor.addPropertyChangeListener("selectionEnd", evt -> {
            updateSelectionInfo.run();
        });

        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> {
            committedValue[0] = editor.getText();
            committedValuePane.setText(committedValue[0]);
            updateCommittedInfo.run();
            status.setText("Applied editor value.");
        });

        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> {
            editor.setText(defaultText);
            committedValue[0] = defaultText;
            committedValuePane.setText(defaultText);
            updateCommittedInfo.run();
            syncAll.run();
            status.setText("Reset to default value.");
        });

        JPanel controlsRow = new JPanel();
        controlsRow.setLayout(new BoxLayout(controlsRow, BoxLayout.X_AXIS));
        controlsRow.add(wrap);
        controlsRow.add(Box.createHorizontalStrut(4));
        controlsRow.add(editable);
        controlsRow.add(Box.createHorizontalStrut(12));
        controlsRow.add(new JLabel("Toolbar Location:"));
        controlsRow.add(Box.createHorizontalStrut(4));
        controlsRow.add(toolbarLocation);
        controlsRow.add(Box.createHorizontalStrut(8));
        controlsRow.add(apply);
        controlsRow.add(reset);
        controlsRow.add(Box.createHorizontalGlue());

        JPanel actionRow = new JPanel();
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.add(committedInfo);
        actionRow.add(Box.createHorizontalGlue());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(controlsRow);
        content.add(Box.createVerticalStrut(8));
        content.add(new JLabel("Editor"));
        content.add(editor);
        content.add(Box.createVerticalStrut(6));
        content.add(selectionInfo);
        content.add(Box.createVerticalStrut(8));
        content.add(actionRow);
        content.add(Box.createVerticalStrut(8));
        content.add(new JLabel("Live Document"));
        content.add(liveDocumentPane);
        content.add(Box.createVerticalStrut(8));
        content.add(new JLabel("Committed Value"));
        content.add(committedValuePane);
        content.add(Box.createVerticalStrut(8));
        content.add(status);

        syncAll.run();
        toolbarLocation.setSelectedItem(editor.getToolbarLocation());
        editor.addPropertyChangeListener("toolbarLocation", evt -> toolbarLocation.setSelectedItem(editor.getToolbarLocation()));
        toolbarLocation.addActionListener(e -> {
            DetachableNode.Location location = (DetachableNode.Location) toolbarLocation.getSelectedItem();
            if (location != null) {
                editor.setToolbarLocation(location);
            }
        });

        JFrame frame = new JFrame("TextEditorPane / TextPane Swing Sample");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setContentPane(content);
        editor.setToolbarApplicationParent(frame.getContentPane());

        frame.setSize(980, 820);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private static String oneLine(RichText text) {
        return text.toString().replace("\n", "\\n");
    }


    private static RichText createSampleText() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD).append("Swing TextEditorPane/TextPane demo").pop(Style.BOLD).append('\n');
        builder.append("Edit in the upper pane and press Apply to update committed value.\n");
        builder.append("Reset restores both editor and committed value to this default text.\n\n");
        builder.append("Formatting: select text and use B/I/U/S buttons, then Undo/Redo.\n\n");
        builder.push(Style.ITALIC).append("Long paragraph: ").pop(Style.ITALIC);
        builder.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ");
        builder.append("ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ");
        builder.append("laboris nisi ut aliquip ex ea commodo consequat.");
        return builder.toRichText();
    }
}
