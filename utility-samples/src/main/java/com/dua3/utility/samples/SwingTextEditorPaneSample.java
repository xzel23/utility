package com.dua3.utility.samples;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.data.Color;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.swing.TextEditorPane;
import com.dua3.utility.swing.TextPane;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Swing sample for {@link TextEditorPane} and {@link TextPane}.
 */
public final class SwingTextEditorPaneSample {
    private static final Float[] DEFAULT_FONT_SIZES = {8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 14.0f, 16.0f, 18.0f, 20.0f, 24.0f, 28.0f, 32.0f, 36.0f, 40.0f, 48.0f, 56.0f, 64.0f};
    private static final Color[] DEFAULT_TEXT_COLORS = {
            Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
            Color.RED.darker(), Color.RED, Color.RED.brighter(),
            Color.GREEN.darker(), Color.GREEN, Color.GREEN.brighter(),
            Color.BLUE.darker(), Color.BLUE, Color.BLUE.brighter(),
            Color.YELLOW.darker(), Color.YELLOW, Color.YELLOW.brighter(),
            Color.DARKCYAN, Color.DARKCYAN.brighter(), Color.LIGHTCYAN,
            Color.DARKMAGENTA, Color.DARKMAGENTA.brighter(), Color.DARKMAGENTA.brighter().brighter()
    };
    private static final Color[] DEFAULT_BACKGROUND_COLORS = {
            Color.TRANSPARENT_WHITE,
            Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
            Color.RED.darker(), Color.RED, Color.RED.brighter(),
            Color.GREEN.darker(), Color.GREEN, Color.GREEN.brighter(),
            Color.BLUE.darker(), Color.BLUE, Color.BLUE.brighter(),
            Color.YELLOW.darker(), Color.YELLOW, Color.YELLOW.brighter(),
            Color.DARKCYAN, Color.DARKCYAN.brighter(), Color.LIGHTCYAN,
            Color.DARKMAGENTA, Color.DARKMAGENTA.brighter(), Color.DARKMAGENTA.brighter().brighter()
    };

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

        JButton copy = new JButton("Copy");
        copy.addActionListener(e -> editor.copy());
        JButton cut = new JButton("Cut");
        cut.addActionListener(e -> editor.cut());
        JButton paste = new JButton("Paste");
        paste.addActionListener(e -> editor.paste());

        JToggleButton bold = new JToggleButton("B");
        bold.addActionListener(e -> editor.markBold(bold.isSelected()));
        JToggleButton italic = new JToggleButton("I");
        italic.addActionListener(e -> editor.markItalic(italic.isSelected()));
        JToggleButton underline = new JToggleButton("U");
        underline.addActionListener(e -> editor.markUnderline(underline.isSelected()));
        JToggleButton strike = new JToggleButton("S");
        strike.addActionListener(e -> editor.markStrikeThrough(strike.isSelected()));
        JButton undo = new JButton("Undo");
        undo.addActionListener(e -> editor.undo());
        JButton redo = new JButton("Redo");
        redo.addActionListener(e -> editor.redo());

        JComboBox<String> fontList = new JComboBox<>(AwtFontUtil.getInstance().getFamilies(FontUtil.FontTypes.ALL).toArray(new String[0]));
        JComboBox<Float> sizeList = new JComboBox<>(DEFAULT_FONT_SIZES);
        JComboBox<Color> textColorList = new JComboBox<>(DEFAULT_TEXT_COLORS);
        JComboBox<Color> backgroundColorList = new JComboBox<>(DEFAULT_BACKGROUND_COLORS);
        textColorList.setRenderer(createColorRenderer());
        backgroundColorList.setRenderer(createColorRenderer());

        AtomicBoolean synchronizingToolbar = new AtomicBoolean(false);
        Runnable syncToolbar = () -> {
            synchronizingToolbar.set(true);
            try {
                bold.setSelected(editor.isBold());
                italic.setSelected(editor.isItalic());
                underline.setSelected(editor.isUnderline());
                strike.setSelected(editor.isStrikeThrough());
                undo.setEnabled(editor.canUndo());
                redo.setEnabled(editor.canRedo());
                fontList.setSelectedItem(editor.getFontFamily());
                sizeList.setSelectedItem((float) editor.getFontSize());
                textColorList.setSelectedItem(editor.getTextColor());
                backgroundColorList.setSelectedItem(editor.getBackgroundColor());
            } finally {
                synchronizingToolbar.set(false);
            }
        };

        editor.addPropertyChangeListener("documentVersion", evt -> {
            syncAll.run();
            syncToolbar.run();
        });
        editor.addPropertyChangeListener("caretPosition", evt -> {
            updateSelectionInfo.run();
            syncToolbar.run();
        });
        editor.addPropertyChangeListener("selectionStart", evt -> {
            updateSelectionInfo.run();
            syncToolbar.run();
        });
        editor.addPropertyChangeListener("selectionEnd", evt -> {
            updateSelectionInfo.run();
            syncToolbar.run();
        });

        fontList.addActionListener(e -> {
            if (synchronizingToolbar.get()) {
                return;
            }
            editor.setFontFamily((String) fontList.getSelectedItem());
            editor.requestFocusInWindow();
            syncToolbar.run();
        });
        sizeList.addActionListener(e -> {
            if (synchronizingToolbar.get()) {
                return;
            }
            Float size = (Float) sizeList.getSelectedItem();
            if (size != null) {
                editor.setFontSize(size);
            }
            editor.requestFocusInWindow();
            syncToolbar.run();
        });
        textColorList.addActionListener(e -> {
            if (synchronizingToolbar.get()) {
                return;
            }
            editor.setTextColor((Color) textColorList.getSelectedItem());
            editor.requestFocusInWindow();
            syncToolbar.run();
        });
        backgroundColorList.addActionListener(e -> {
            if (synchronizingToolbar.get()) {
                return;
            }
            editor.setBackgroundColor((Color) backgroundColorList.getSelectedItem());
            editor.requestFocusInWindow();
            syncToolbar.run();
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
        controlsRow.add(Box.createHorizontalStrut(8));
        controlsRow.add(editable);
        controlsRow.add(Box.createHorizontalStrut(12));
        controlsRow.add(new JLabel("Width:"));
        controlsRow.add(Box.createHorizontalStrut(6));
        controlsRow.add(width);

        JPanel toolbarRow = new JPanel();
        toolbarRow.setLayout(new BoxLayout(toolbarRow, BoxLayout.X_AXIS));
        toolbarRow.add(copy);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(cut);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(paste);
        toolbarRow.add(Box.createHorizontalStrut(12));
        toolbarRow.add(undo);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(redo);
        toolbarRow.add(Box.createHorizontalStrut(12));
        toolbarRow.add(fontList);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(sizeList);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(textColorList);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(backgroundColorList);
        toolbarRow.add(Box.createHorizontalStrut(12));
        toolbarRow.add(bold);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(italic);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(underline);
        toolbarRow.add(Box.createHorizontalStrut(4));
        toolbarRow.add(strike);
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
        syncToolbar.run();

        JFrame frame = new JFrame("TextEditorPane / TextPane Swing Sample");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setContentPane(content);
        frame.setSize(980, 820);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private static String oneLine(RichText text) {
        return text.toString().replace("\n", "\\n");
    }

    private static DefaultListCellRenderer createColorRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Color c) {
                    setText(c.toArgb());
                    setIcon(new ColorIcon(c));
                } else {
                    setText("");
                    setIcon(null);
                }
                return this;
            }
        };
    }

    private record ColorIcon(Color color) implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            java.awt.Color awt = SwingUtil.convert(color);
            g.setColor(awt);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(Objects.equals(awt, java.awt.Color.BLACK) ? java.awt.Color.WHITE : java.awt.Color.BLACK);
            g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
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
