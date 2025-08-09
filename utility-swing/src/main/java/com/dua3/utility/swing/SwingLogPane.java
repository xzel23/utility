package com.dua3.utility.swing;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.data.Color;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Swing component that displays logging messages.
 */
public class SwingLogPane extends JPanel {

    static final Column[] COLUMNS = {
            new Column(LogEntryField.TIME, -"YYYY-MM-DD_HH:MM:SS.mmm".length(), true),
            new Column(LogEntryField.LOGGER, "com.example.class".length(), true),
            new Column(LogEntryField.LEVEL, -"ERROR".length(), true),
            new Column(LogEntryField.MESSAGE, 80, false)
    };
    private static final String ESCAPE = "escape";

    private final LogBuffer buffer;
    private final JTable table;
    private final JTextArea details;
    private final LogTableModel model;
    private final JSplitPane splitPane;
    private final java.util.List<TableColumn> tableColumns = new ArrayList<>();
    private final transient Function<LogEntry, Color> colorize;
    private final transient TableRowSorter<AbstractTableModel> tableRowSorter;
    private transient Function<? super LogEntry, String> format = LogEntry::toString;
    private double dividerLocation = 0.5;
    private final JScrollPane scrollPaneTable;
    private transient List<LogEntry> selectedEntries = Collections.emptyList();

    /**
     * Creates a new instance of SwingLogPane with the default buffer size and connects all known loggers.
     * @see LogBuffer#DEFAULT_CAPACITY
     */
    public SwingLogPane() {
        this(LogBuffer.DEFAULT_CAPACITY);
    }

    /**
     * Creates a new instance of SwingLogPane with the given buffer size and connects all known loggers.
     * @param bufferSize the buffer size
     */
    public SwingLogPane(int bufferSize) {
        this(createBuffer(bufferSize));
    }

    /**
     * Creates a LogBuffer with the given buffer size and adds it to the global log entry handler.
     *
     * @param bufferSize the size of the buffer
     * @return the created LogBuffer
     */
    private static LogBuffer createBuffer(int bufferSize) {
        LogBuffer buffer = new LogBuffer(bufferSize);
        LogUtil.getGlobalDispatcher().addLogEntryHandler(buffer);
        return buffer;
    }

    /**
     * Constructs a new SwingLogPane with the given LogBuffer instance.
     * This constructor calls another constructor with the defaultColorize method as the second argument.
     *
     * @param buffer the LogBuffer instance to be used for log messages
     */
    public SwingLogPane(@Nullable LogBuffer buffer) {
        this(buffer, SwingLogPane::defaultColorize);
    }

    /**
     * Constructs a new SwingLogPane with the given LogBuffer instance and colorize function.
     *
     * @param logBuffer the LogBuffer instance to be used for log messages
     * @param colorize the colorize function that determines the color for each log entry
     */
    public SwingLogPane(@Nullable LogBuffer logBuffer, Function<LogEntry, Color> colorize) {
        super(new BorderLayout());

        this.buffer = logBuffer == null ? createBuffer(LogBuffer.DEFAULT_CAPACITY) : logBuffer;
        this.colorize = colorize;
        this.model = new LogTableModel(buffer);
        this.tableRowSorter = new TableRowSorter<>(model) {
            @Override
            public void sort() {
                model.executeRead(super::sort);
            }
        };

        // create the detail pane
        details = new JTextArea(5, 80);

        this.table = createLogTable();

        // update detail pane when entry is selected
        setupLogTableSelectionListener();

        table.setRowSorter(tableRowSorter);

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        table.getActionMap().put(ESCAPE, SwingUtil.createAction(ESCAPE, this::handleEscapeKey));
        table.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, ESCAPE);

        // prepare the ScrollPanes
        this.scrollPaneTable = new JScrollPane(table);
        JScrollPane scrollPaneDetails = new JScrollPane(details);

        // create SplitPane for table and detail pane
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneTable, scrollPaneDetails);
        splitPane.setDividerLocation(dividerLocation);

        final JScrollBar tableScroller = scrollPaneTable.getVerticalScrollBar();
        model.addTableModelListener(evt ->
                // handle scrolling
                SwingUtilities.invokeLater(() -> {
                    // if scroll position is on the last row and no row is selected, automatically scroll down when rows are inserted
                    if (table.getSelectedRowCount() == 0 && tableScroller.getValue() >= tableScroller.getMaximum() - tableScroller.getVisibleAmount() - 3 * table.getRowHeight()) {
                        // scroll to last row
                        boolean selectionEmpty = table.getSelectedRow() < 0;
                        if (selectionEmpty) {
                            scrollRowIntoView(model.getRowCount());
                        }
                    }
                })
        );

        // create toolbar
        JToolBar toolBar = createToolBar();

        add(toolBar, BorderLayout.PAGE_START);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Sets up a selection listener for the log table to handle user selection events.
     * The method listens to changes in the table's selection, retrieves the selected log entries,
     * and updates the detailed view based on the selected entries.
     * <p>
     * If the selection changes, the method ensures the detailed text view is updated with
     * the formatted content of the newly selected log entries.
     * <p>
     * The listener is executed on the Event Dispatch Thread to ensure thread safety with
     * Swing components.
     */
    private void setupLogTableSelectionListener() {
        table.getSelectionModel().addListSelectionListener(evt -> model.executeRead(() -> {
            ListSelectionModel lsm = (ListSelectionModel) evt.getSource();

            if (evt.getValueIsAdjusting()) {
                return;
            }

            SwingUtilities.invokeLater(() -> {
                final List<LogEntry> newEntries = getSelectedLogEntries(lsm);

                if (!hasChangedLogEntries(newEntries)) {
                    return;
                }

                Function<? super LogEntry, String> fmt = format;
                String text = newEntries.stream().map(entry -> fmt.apply(entry) + "\n").collect(Collectors.joining());

                details.setText(text);
                details.setCaretPosition(0);

                selectedEntries = newEntries;
            });
        }));
    }

    /**
     * Compares a new list of log entries with the currently selected log entries to determine
     * if there are any changes.
     * <p>
     * The method checks if the sizes of the two lists differ, and if their elements are the same
     * in the same order. If either the size or the content differs, it indicates that there have
     * been changes.
     *
     * @param newEntries the list of {@code LogEntry} objects to compare with the currently
     *                   selected entries
     * @return {@code true} if the new list of log entries differs in size or content from
     *         the selected entries; {@code false} otherwise
     */
    private boolean hasChangedLogEntries(List<LogEntry> newEntries) {
        boolean changed = newEntries.size() != selectedEntries.size();
        if (!changed) {
            for (int i = 0; i < newEntries.size(); i++) {
                if (newEntries.get(i) != selectedEntries.get(i)) {
                    changed = true;
                    break;
                }
            }
        }
        return changed;
    }

    /**
     * Retrieves a list of log entries that correspond to the currently selected rows in the table model.
     * The method iterates over the selection indices provided by the {@code ListSelectionModel},
     * converts them to the model's row indices using the table row sorter, and fetches log entries from the model.
     *
     * @param lsm the {@code ListSelectionModel} that provides the selected row indices in the view
     * @return a list of {@code LogEntry} objects corresponding to the selected rows in the model
     */
    private @NonNull List<LogEntry> getSelectedLogEntries(ListSelectionModel lsm) {
        final List<LogEntry> newEntries = new ArrayList<>(lsm.getMaxSelectionIndex() - lsm.getMinSelectionIndex() + 1);
        for (int i = lsm.getMinSelectionIndex(); i <= lsm.getMaxSelectionIndex(); i++) {
            if (lsm.isSelectedIndex(i)) {
                int idxModel = tableRowSorter.convertRowIndexToModel(i);
                newEntries.add(model.getValueAt(idxModel, 0));
            }
        }
        return newEntries;
    }

    /**
     * Creates and configures a new {@code JTable} instance for displaying log entries
     * with custom column settings. The table uses a specific model and ensures thread-safe
     * rendering by executing paint tasks within a read lock. Each column is customized
     * with a specific renderer and width settings based on predefined column attributes.
     *
     * @return a configured {@code JTable} instance for displaying log entries
     */
    private @NonNull JTable createLogTable() {
        JTable t = new JTable(model) {
            @Override
            public void paintComponent(Graphics g) {
                model.executeRead(() -> super.paintComponent(g));
            }
        };

        // column settings
        AwtFontUtil fu = AwtFontUtil.getInstance();

        TableColumnModel columnModel = t.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            Column cd = COLUMNS[i];
            TableColumn column = columnModel.getColumn(i);
            TableCellRenderer r = new LogEntryFieldCellRenderer(cd.field());
            column.setCellRenderer(r);

            java.awt.Font font = t.getFont();
            int chars = cd.preferredCharWidth();
            //noinspection NumericCastThatLosesPrecision
            int width = (int) Math.ceil(fu.getTextWidth("M".repeat(Math.abs(chars)), font));
            if (chars >= 0) {
                column.setPreferredWidth(width);
            } else {
                column.setMinWidth(width);
                column.setMaxWidth(width);
                column.setWidth(width);
            }
        }
        t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        return t;
    }

    /**
     * Creates a new toolbar for log pane functionality. The toolbar includes components for
     * filtering log levels, toggling text-only display, and buttons for clearing and copying
     * the log buffer.
     * <p>
     * The toolbar includes the following components:
     * - A dropdown menu to select the minimum log level for filtering displayed entries.
     * - A checkbox to toggle between showing only text or other log details.
     * - Buttons to clear the log buffer and copy its contents.
     *
     * @return the configured {@code JToolBar} instance with added components for log pane controls
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);

        // add filtering based on level
        toolBar.add(new JLabel("Min Level:"));
        JComboBox<LogLevel> cbLevel = new JComboBox<>(LogLevel.values());
        toolBar.add(cbLevel);
        cbLevel.addItemListener(a -> setFilter((LogLevel) a.getItem()));
        cbLevel.setSelectedItem(LogLevel.INFO);

        // checkbox for text only
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        JCheckBox cbTextOnly = new JCheckBox(SwingUtil.createAction("Show text only", evt -> setTextOnly(((JCheckBox) (evt.getSource())).isSelected())));
        cbTextOnly.setSelected(true);
        toolBar.add(cbTextOnly);

        // buttons "clear" and "copy"
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        toolBar.add(SwingUtil.createAction("Clear", this::clearBuffer));
        toolBar.add(SwingUtil.createAction("Copy", this::copyBuffer));
        return toolBar;
    }

    private static Color defaultColorize(LogEntry entry) {
        return switch (entry.level()) {
            case ERROR -> Color.DARKRED;
            case WARN -> Color.RED;
            case INFO -> Color.DARKBLUE;
            case DEBUG -> Color.BLACK;
            case TRACE -> Color.DARKGRAY;
        };
    }

    private void setTextOnly(boolean textOnly) {
        synchronized (model) {
            TableColumnModel columnModel = table.getColumnModel();

            // keep list of all columns
            if (tableColumns.isEmpty()) {
                for (int i = 0; i < columnModel.getColumnCount(); i++) {
                    tableColumns.add(columnModel.getColumn(i));
                }
            }

            // remove all columns from model
            while (columnModel.getColumnCount() > 0) {
                columnModel.removeColumn(columnModel.getColumn(0));
            }

            // add visible columns again
            for (int i = 0; i < table.getModel().getColumnCount(); i++) {
                if (!textOnly || !COLUMNS[i].hideable()) {
                    table.getColumnModel().addColumn(tableColumns.get(i));
                }
            }
        }
    }

    private void setFilter(LogLevel c) {
        tableRowSorter.setRowFilter(new RowFilter(c));
    }

    private void handleEscapeKey() {
        ListSelectionModel selectionModel = table.getSelectionModel();
        int rows = model.getRowCount();
        if (selectionModel.isSelectionEmpty() && rows > 0) {
            // select last row
            selectionModel.setSelectionInterval(rows - 1, rows - 1);
        } else {
            // clear selection and scroll to bottom
            selectionModel.clearSelection();
            scrollRowIntoView(rows);
        }
    }

    /**
     * Scrolls the specified row into view.
     *
     * @param row the row index to scroll into view
     */
    public void scrollRowIntoView(int row) {
        SwingUtilities.invokeLater(() -> {
            Rectangle rect = new Rectangle(table.getCellRect(row, 0, true));
            table.scrollRectToVisible(rect);
        });
    }

    /**
     * Scrolls the specified number of rows in the table.
     *
     * @param n the number of rows to scroll, positive values scroll down, negative values scroll up
     */
    public void scrollNRows(int n) {
        int rowHeight = table.getRowHeight();
        JScrollBar verticalScrollBar = scrollPaneTable.getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> {
            synchronized (verticalScrollBar) {
                int value = verticalScrollBar.getValue() + rowHeight * n;
                verticalScrollBar.setValue(value);
            }
        });
    }

    /**
     * Set the formatter used to convert log entries to text.
     *
     * @param format the formatting function
     */
    public void setLogFormatter(Function<? super LogEntry, String> format) {
        this.format = format;
    }

    /**
     * Set the divider location. Analog to {@link JSplitPane#setDividerLocation(double)}.
     *
     * @param proportionalLocation the proportional location
     */
    public void setDividerLocation(double proportionalLocation) {
        this.dividerLocation = Math.clamp(proportionalLocation, 0.0, 1.0);
        splitPane.setDividerLocation(dividerLocation);
    }

    /**
     * Retrieves the current location of the divider within the split pane.
     * The location is represented as a proportional value between 0.0 and 1.0,
     * where 0.0 corresponds to the top/leftmost position and 1.0 corresponds
     * to the bottom/rightmost position of the split pane.
     *
     * @return the proportional location of the divider as a double
     */
    public double getDividerLocation() {
        return dividerLocation;
    }

    /**
     * Set the divider location. Analog to {@link JSplitPane#setDividerLocation(int)}.
     *
     * @param location the location
     */
    public void setDividerLocation(int location) {
        setDividerLocation((double) location / (splitPane.getHeight() - splitPane.getDividerSize()));
    }

    /**
     * Clear the log buffer.
     */
    public void clearBuffer() {
        buffer.clear();
    }

    /**
     * Copy contents of log buffer to clipboard.
     */
    public void copyBuffer() {
        try {
            StringBuilder sb = new StringBuilder(16 * 1024);
            buffer.appendTo(sb);
            SwingUtil.setClipboardText(sb.toString());
        } catch (IOException e) {
            // StringBuilder shouldn't throw IOException
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        SwingUtilities.invokeLater(() ->
                splitPane.setDividerLocation(dividerLocation)
        );
    }

    enum LogEntryField {
        LOGGER {
            @Override
            public String get(LogEntry entry) {
                return entry.loggerName();
            }
        },
        TIME {
            @Override
            public String get(LogEntry entry) {
                return entry.time().toString();
            }
        },
        LEVEL {
            @Override
            public String get(LogEntry entry) {
                return entry.level().name();
            }
        },
        MESSAGE {
            @Override
            public String get(LogEntry entry) {
                return entry.message();
            }
        },
        THROWABLE {
            @Override
            public String get(LogEntry entry) {
                return Objects.toString(entry.throwable());
            }
        };

        public abstract String get(LogEntry entry);
    }

    private static class RowFilter extends javax.swing.RowFilter<AbstractTableModel, Integer> {
        private final LogLevel c;

        RowFilter(LogLevel c) {
            this.c = c;
        }

        @Override
        public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
            LogEntry value = (LogEntry) entry.getValue(0);
            return value == null || value.level().compareTo(c) >= 0;
        }
    }

    record Column(LogEntryField field, int preferredCharWidth, boolean hideable) {
    }

    private final class LogEntryFieldCellRenderer extends DefaultTableCellRenderer {
        private final LogEntryField f;

        private LogEntryFieldCellRenderer(LogEntryField f) {
            this.f = f;
        }

        @Override
        public void setValue(Object value) {
            java.awt.Color color;

            Object v;
            if (value instanceof LogEntry entry) {
                color = SwingUtil.convert(colorize.apply(entry));
                v = f.get(entry);
            } else {
                color = java.awt.Color.BLACK;
                v = value;
            }

            setForeground(color);
            setBackground(table.getBackground());

            super.setValue(v);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                java.awt.Color fg = getForeground();
                java.awt.Color bg = getBackground();
                setForeground(bg);
                setBackground(fg);
            }

            return this;
        }
    }
}
