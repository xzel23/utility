package com.dua3.utility.samples;

import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.swing.TextEditorPane;
import com.dua3.utility.swing.TextPane;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Swing sample for {@link TextEditorPane} and {@link TextPane}.
 */
public final class SwingTextEditorPaneSample {

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
                        + "  Selection: [" + editor.getTextComponent().getSelectionStart()
                        + ", " + editor.getTextComponent().getSelectionEnd() + ")"
                        + "  Selected: \"" + oneLine(editor.getSelectedText()) + "\""
        );

        Runnable updateCommittedInfo = () -> committedInfo.setText(
                "Committed value length: " + committedValue[0].length() + " characters"
        );

        Runnable syncAll = () -> {
            syncLiveDocument.run();
            updateSelectionInfo.run();
        };

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                syncAll.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                syncAll.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                syncAll.run();
            }
        };

        attachDocumentListener(editor, documentListener, syncAll);
        editor.getTextComponent().addCaretListener(evt -> updateSelectionInfo.run());

        JCheckBox wrap = new JCheckBox("Wrap text", true);
        wrap.addActionListener(e -> {
            boolean enabled = wrap.isSelected();
            editor.setWrapText(enabled);
            liveDocumentPane.setWrapText(enabled);
            committedValuePane.setWrapText(enabled);
        });

        JCheckBox editable = new JCheckBox("Editable", editor.getTextComponent().isEditable());
        editable.addActionListener(e -> editor.getTextComponent().setEditable(editable.isSelected()));

        JSlider width = new JSlider(220, 780, 640);
        width.setPaintTicks(true);
        width.setPaintLabels(true);
        width.setMajorTickSpacing(140);
        width.setMinorTickSpacing(20);
        width.addChangeListener(e -> {
            int w = width.getValue();
            editor.setPreferredSize(new Dimension(w, 220));
            liveDocumentPane.setPreferredSize(new Dimension(w, 200));
            committedValuePane.setPreferredSize(new Dimension(w, 200));
            editor.revalidate();
            liveDocumentPane.revalidate();
            committedValuePane.revalidate();
        });

        JButton bold = new JButton("B");
        bold.addActionListener(e -> editor.markBold());
        JButton italic = new JButton("I");
        italic.addActionListener(e -> editor.markItalic());
        JButton underline = new JButton("U");
        underline.addActionListener(e -> editor.markUnderline());
        JButton strike = new JButton("S");
        strike.addActionListener(e -> editor.markStrikeThrough());
        JButton undo = new JButton("Undo");
        undo.addActionListener(e -> editor.undo());
        JButton redo = new JButton("Redo");
        redo.addActionListener(e -> editor.redo());

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
        controlsRow.add(Box.createHorizontalStrut(8));
        controlsRow.add(editable);
        controlsRow.add(Box.createHorizontalStrut(12));
        controlsRow.add(new JLabel("Width:"));
        controlsRow.add(Box.createHorizontalStrut(6));
        controlsRow.add(width);

        JPanel toolbarRow = new JPanel();
        toolbarRow.setLayout(new BoxLayout(toolbarRow, BoxLayout.X_AXIS));
        toolbarRow.add(bold);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(italic);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(underline);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(strike);
        toolbarRow.add(Box.createHorizontalStrut(12));
        toolbarRow.add(undo);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(redo);
        toolbarRow.add(Box.createHorizontalGlue());
        toolbarRow.add(apply);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(reset);

        JPanel actionRow = new JPanel();
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.add(committedInfo);
        actionRow.add(Box.createHorizontalGlue());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(controlsRow);
        content.add(Box.createVerticalStrut(8));
        content.add(toolbarRow);
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

        JFrame frame = new JFrame("TextEditorPane / TextPane Swing Sample");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setContentPane(content);
        frame.setSize(980, 820);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private static void attachDocumentListener(TextEditorPane editor, DocumentListener listener, Runnable syncAction) {
        Document initial = editor.getTextComponent().getDocument();
        initial.addDocumentListener(listener);
        editor.getTextComponent().addPropertyChangeListener("document", evt -> {
            if (evt.getOldValue() instanceof Document oldDocument) {
                oldDocument.removeDocumentListener(listener);
            }
            if (evt.getNewValue() instanceof Document newDocument) {
                newDocument.addDocumentListener(listener);
            }
            syncAction.run();
        });
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
