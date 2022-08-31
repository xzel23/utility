package com.dua3.utility.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.math.MathUtil;
import org.slf4j.event.Level;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.RowFilter;
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A Swing component that displays logging messages.
 */
public class SwingLogPane extends JPanel {

    private final LogBuffer buffer;
    private final JTable table;
    private final JTextArea details;
    private final LogTableModel model;
    private final Function<LogEntry, Color> colorize;
    private final JSplitPane splitPane;
    private TableRowSorter<AbstractTableModel> tableRowSorter;
    private Function<LogEntry, String> format = LogEntry::toString;
    private double dividerLocation = 0.5;

    private static Color defaultColorize(LogEntry entry) {
        return switch (entry.level()) {
            case ERROR -> Color.DARKRED;
            case WARN -> Color.RED;
            case INFO -> Color.DARKBLUE;
            case DEBUG -> Color.BLACK;
            case TRACE -> Color.DARKGRAY;
            default -> Color.BLACK;
        };
    }

    private static final class LogTableModel extends AbstractTableModel implements LogBuffer.LogBufferListener {
        private final LogBuffer buffer;

        private LogTableModel(LogBuffer buffer) {
            this.buffer = Objects.requireNonNull(buffer);
            buffer.addLogBufferListener(this);
        }
        
        private List<LogEntry> data = null;
        private int removed = 0;
        private int added = 0;
        
        private boolean isLocked() {
            return data!=null;
        }

        public synchronized void lock() {
            assert !isLocked() : "internal error: locked";

            synchronized (buffer) {
                data = new ArrayList<>(buffer.entries());
                removed = 0;
                added = 0;
            }
        }

        public synchronized void unlock() {
            assert isLocked() : "internal error: should be locked";
            assert data != null : "internal error, data should have been set in lock()";

            int sz = data.size();
            data = null;
            removed = Math.min(removed, sz);

            if (removed > 0) {
                fireTableRowsDeleted(0, removed);
                sz -= removed;
                removed = 0;
            }

            added = Math.min(added, sz);

            if (added > 0) {
                fireTableRowsInserted(sz - added, sz - 1);
                added = 0;
            }
        }

        @Override
        public synchronized int getRowCount() {
            return data == null ? buffer.size() : data.size();
        }
        
        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public synchronized LogEntry getValueAt(int rowIndex, int columnIndex) {
            return data == null ? buffer.get(rowIndex) : data.get(rowIndex);
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column].field().name();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return LogEntryField.class;
        }

        @Override
        public synchronized void entry(LogEntry entry, boolean replaced) {
            if (isLocked()) {
                if (replaced) {
                    removed++;
                }
                added++;
            } else {
                synchronized (buffer) {
                    int sz = buffer.size();
                    if (replaced) {
                        fireTableRowsUpdated(sz-1, sz-1);
                    } else {
                        fireTableRowsInserted(sz-1, sz-1);                     
                    }
                }
            }
        }

        @Override
        public synchronized void entries(Collection<LogEntry> entries, int replaced) {
            if (isLocked()) {
                if (replaced > 0) {
                    removed += replaced;
                }
                added += entries.size();
            } else {
                synchronized (buffer) {
                    int sz = buffer.size();
                    if (replaced>0) {
                        fireTableRowsUpdated(sz-entries.size(), sz-entries.size()+replaced-1);
                        fireTableRowsInserted(sz-entries.size()+replaced, sz - 1);
                    } else {
                        fireTableRowsInserted(sz - entries.size(), sz - 1);
                    }
                }
            }
        }

        @Override
        public synchronized void clear() {
            if (isLocked()) {
                assert data != null : "internal error, data should have been set in lock()";
                removed = data.size();
                added = 0;
            } else {
                fireTableDataChanged();
            }
        }

    }

    static enum LogEntryField {
        LOGGER {
            @Override
            public String get(LogEntry entry) {
                return entry.logger().getName();
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
                return entry.formatMessage();
            }
        },
        THROWABLE {
            @Override
            public String get(LogEntry entry) {
                return entry.throwable().toString();
            }
        };
        
        public abstract String get(LogEntry entry);
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
                color = SwingUtil.toAwtColor(colorize.apply(entry));
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
    
    private record Column(LogEntryField field, int preferredCharWidth, boolean hideable) {}
    
    private static final Column[] COLUMNS = {
        new Column(LogEntryField.TIME, -"YYYY-MM-DD_HH:MM:SS.mmm".length(), true),
        new Column(LogEntryField.LOGGER, "com.example.class".length(), true),
        new Column(LogEntryField.LEVEL, -"ERROR".length(), true),
        new Column(LogEntryField.MESSAGE, 80, false)
    };
    
    public SwingLogPane(LogBuffer buffer) {
        this(buffer, SwingLogPane::defaultColorize);
    }
    
    public SwingLogPane(LogBuffer buffer, Function<LogEntry, Color> colorize) {
        super(new BorderLayout());
        
        this.buffer = Objects.requireNonNull(buffer);
        this.colorize = Objects.requireNonNull(colorize);
        this.model = new LogTableModel(buffer);
        
        // create the table
        table = new JTable(model) {
            @Override
            public void paint(Graphics g) {
                try {
                    model.lock();
                    super.paint(g);
                } finally {
                    model.unlock();
                }
            }
        };
        
        // create the detail pane
        details = new JTextArea(5, 80);

        // column settings
        SwingFontUtil fu = new SwingFontUtil();
        
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            Column cd = COLUMNS[i];
            TableColumn column = columnModel.getColumn(i);
            TableCellRenderer r = new LogEntryFieldCellRenderer(cd.field());
            column.setCellRenderer(r);
            
            java.awt.Font font = table.getFont();
            int chars = cd.preferredCharWidth();
            //noinspection NumericCastThatLosesPrecision
            int width = (int) Math.ceil(fu.getTextWidth("M".repeat(Math.abs(chars)), font));
            if (chars>=0) {
                column.setPreferredWidth(width);
            } else {
                column.setMinWidth(width);
                column.setMaxWidth(width);
                column.setWidth(width);
            }
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        
        // update detail pane when entry is selected
        table.getSelectionModel().addListSelectionListener(evt -> {
            ListSelectionModel lsm = (ListSelectionModel)evt.getSource();
            final String text;
            if (lsm.isSelectionEmpty() || evt.getValueIsAdjusting()) {
                text = "";
            } else {
                StringBuilder sb = new StringBuilder(1024);
                synchronized (buffer) {
                    for (int idx:lsm.getSelectedIndices()) {
                        if (lsm.isSelectedIndex(idx)) {
                            int idxModel = tableRowSorter.convertRowIndexToModel(idx);
                            LogEntry entry = model.getValueAt(idxModel, 0);
                            sb.append(format.apply(entry)).append("\n");
                        }
                    }
                }
                text = sb.toString();
            }
            
            SwingUtilities.invokeLater(() -> {
                details.setText(text);
                details.setCaretPosition(0);
            });
        });

        tableRowSorter = new TableRowSorter<>(model);
        table.setRowSorter(tableRowSorter);
        
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        table.getActionMap().put("escape", SwingUtil.createAction("escape", this::handleEscapeKey));
        table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "escape");
        
        // prepare the ScrollPanes
        JScrollPane scrollPaneTable = new JScrollPane(table);
        JScrollPane scrollPaneDetails = new JScrollPane(details);
        
        // create SplitPane for table and detail pane
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneTable, scrollPaneDetails);
        splitPane.setDividerLocation(dividerLocation);

        final JScrollBar tableScroller = scrollPaneTable.getVerticalScrollBar();
        model.addTableModelListener(evt -> {
            // handle scrolling
            SwingUtilities.invokeLater( () -> {
                if (table.getSelectedRowCount()==0 && tableScroller.getValue() >= tableScroller.getMaximum() - tableScroller.getVisibleAmount() - table.getRowHeight()) {
                    // scroll to last row
                    boolean selectionEmpty = table.getSelectedRow()<0;
                    if (selectionEmpty) {
                        scrollRowIntoView(model.getRowCount());
                    }
                }
            });
        });
        
        // create toolbar
        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);

        // add filtering based on level
        toolBar.add(new JLabel("Min Level:"));
        JComboBox<Level> cbLevel = new JComboBox<>(Level.values());
        toolBar.add(cbLevel);
        cbLevel.addItemListener(a -> setFilter((Level) a.getItem()));
        cbLevel.setSelectedItem(Level.INFO);

        // checkbox for text only
        toolBar.add(new JSeparator(JSeparator.VERTICAL));
        JCheckBox cbTextOnly = new JCheckBox(SwingUtil.createAction("Show text only", evt -> setTextOnly(((JCheckBox) (evt.getSource())).isSelected())));
        cbTextOnly.setSelected(true);
        toolBar.add(cbTextOnly);
        
        // buttons "clear" and "copy"
        toolBar.add(new JSeparator(JSeparator.VERTICAL));
        toolBar.add(SwingUtil.createAction("Clear", this::clearBuffer));
        toolBar.add(SwingUtil.createAction("Copy", this::copyBuffer));
        
        add(toolBar, BorderLayout.PAGE_START);
        add(splitPane, BorderLayout.CENTER);
        
        setTextOnly(cbTextOnly.isSelected());
    }

    private final java.util.List<TableColumn> tableColumns = new ArrayList<>();
    
    private void setTextOnly(boolean textOnly) {
        synchronized (model) {
            TableColumnModel columnModel = table.getColumnModel();
    
            // keep list of all columns
            if (tableColumns.isEmpty()) {
                for (int i=0; i<columnModel.getColumnCount(); i++) {
                    tableColumns.add(columnModel.getColumn(i));
                }         
            }

            // remove all columns from model
            while (columnModel.getColumnCount()>0) {
                columnModel.removeColumn(columnModel.getColumn(0));
            }

            // add visible columns again
            for (int i=0; i<table.getModel().getColumnCount(); i++) {
                if (!textOnly || !COLUMNS[i].hideable()) {
                    table.getColumnModel().addColumn(tableColumns.get(i));
                }
            }
        }
    }

    private void setFilter(Level c) {
        tableRowSorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
                LogEntry value = (LogEntry) entry.getValue(0);
                return value == null || value.level().compareTo(c) <= 0;
            }
        });
    }

    private void handleEscapeKey() {
        ListSelectionModel selectionModel = table.getSelectionModel();
        int rows = model.getRowCount();
        if (selectionModel.isSelectionEmpty()&&rows>0) {
            // select last row
            selectionModel.setSelectionInterval(rows-1, rows-1);
        } else {
            // clear selection and scroll to bottom
            selectionModel.clearSelection();
            scrollRowIntoView(rows);
        }
    }

    private void scrollRowIntoView(int row) {
        SwingUtilities.invokeLater(() -> table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true))));
    }

    /**
     * Set the formatter used to convert log entries to text.
     * @param format the formatting function
     */
    public void setLogFormatter(Function<LogEntry, String> format) {
        synchronized (buffer) {
            this.format = Objects.requireNonNull(format);
        }
    }
    
    /**
     * Set the divider location. Analog to {@link JSplitPane#setDividerLocation(double)}.
     * @param proportionalLocation the proportional location 
     */
    public void setDividerLocation(double proportionalLocation) {
        this.dividerLocation = MathUtil.clamp(0.0, 1.0, proportionalLocation);
        splitPane.setDividerLocation(dividerLocation);
    }

    /**
     * Set the divider location. Analog to {@link JSplitPane#setDividerLocation(int)}.
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
            StringBuilder sb = new StringBuilder(16*1024);
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
        SwingUtilities.invokeLater( () -> 
            splitPane.setDividerLocation(dividerLocation) 
        );
    }
}
