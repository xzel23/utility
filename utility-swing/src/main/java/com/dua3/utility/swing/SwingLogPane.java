package com.dua3.utility.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.logging.Category;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.math.MathUtil;
import com.dua3.utility.text.TextUtil;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.List;
import java.util.function.Function;

/**
 * A Swing component that displays logging messages.
 */
public class SwingLogPane extends JPanel {

    private final LogBuffer buffer;
    private final JTable table;
    private final JTextArea details;
    private final LogTableModel model;
    private final JScrollPane scrollPaneTable;
    private final Function<LogEntry, Color> colorize;
    private final JScrollPane scrollPaneDetails;
    private final JSplitPane splitPane;
    private TableRowSorter<AbstractTableModel> tableRowSorter;
    private Function<LogEntry, String> format = LogEntry::format;
    private double dividerLocation = 0.5;

    private static Color defaultColorize(LogEntry entry) {
        switch (entry.category()) {
            case FATAL:
                return Color.DARKRED;
            case SEVERE:
                return Color.RED;
            case WARNING:
                return Color.DARKORANGE;
            case INFO:
                return Color.DARKBLUE;
            case DEBUG:
                return Color.BLACK;
            case TRACE:
                return  Color.DARKGRAY;
            default:
                return Color.BLACK;
        }
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
            LangUtil.check(!isLocked(), "internal error: locked");

            synchronized (buffer) {
                data = new ArrayList<>(buffer.entries());
                removed = 0;
                added = 0;
            }
        }
        
        public synchronized void unlock() {
            int sz = data.size();
            data = null;
            removed = Math.min(removed, sz);
            if (removed>0) {
                fireTableRowsDeleted(0, removed);
                sz-=removed;
                removed=0;
            }
            added = Math.min(added, sz);
            if (added>0) {
                fireTableRowsInserted(sz - added, sz - 1);
                added=0;
            }
        }
        
        @Override
        public synchronized int getRowCount() {
            return data==null ? buffer.size() : data.size();
        }
        
        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public synchronized LogEntry getValueAt(int rowIndex, int columnIndex) {
            return data==null ? buffer.get(rowIndex) : data.get(rowIndex);
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column].field.toString();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return LogEntry.Field.class;
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
                    if (replaced) {
                        fireTableRowsDeleted(0, removed);
                    }
                    int sz = buffer.size();
                    fireTableRowsInserted(sz-1, sz-1);
                }
            }
        }

        @Override
        public synchronized void entries(Collection<LogEntry> entries, int replaced) {
            if (isLocked()) {
                if (replaced>0) {
                    removed+=replaced;
                }
                added+=entries.size();
            } else {
                synchronized (buffer) {
                    if (replaced > 0) {
                        fireTableRowsDeleted(0, replaced);
                    }
                    int sz = buffer.size();
                    fireTableRowsInserted(sz - entries.size(), sz - 1);
                }
            }
        }

        @Override
        public synchronized void clear() {
            if (isLocked()) {
                removed=data.size();
                added=0;
            } else {
                fireTableDataChanged();
            }
        }

        @Override
        public synchronized void capacity(int n) {
            fireTableDataChanged();
        }
    }

    private void onAddEntries(Collection<LogEntry> entries, int removed) {
        synchronized (model) {
            // handle scrolling
            SwingUtilities.invokeLater( () -> {
                JScrollBar scroll = scrollPaneTable.getVerticalScrollBar();
                if (table.getSelectedRowCount()==0 && scroll.getValue() >= scroll.getMaximum() - scroll.getVisibleAmount() - table.getRowHeight()) {
                    // scroll to last row
                    boolean selectionEmpty = table.getSelectedRow()<0;
                    if (selectionEmpty) {
                        scrollRowIntoView(model.getRowCount());
                    }
                }
            });
        }
    }
    
    private final class LogEntryFieldCellRenderer extends DefaultTableCellRenderer {
        private final LogEntry.Field f;

        private LogEntryFieldCellRenderer(LogEntry.Field f) {
            this.f = f;
        }

        public void setValue(Object value) {
            java.awt.Color color; 
            
            Object v;
            if (value instanceof LogEntry) {
                LogEntry entry = (LogEntry) value;
                color = SwingUtil.toAwtColor(colorize.apply(entry));
                v = entry.get(f);
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
    
    private static class Column {
        final LogEntry.Field field;
        final int preferredCharWidth;
        final boolean hideable;
        
        Column(LogEntry.Field field, int preferredCharWidth, boolean hideable) {
            this.field = field;
            this.preferredCharWidth = preferredCharWidth;
            this.hideable = hideable;
        }
    }
    
    private static final Column[] COLUMNS = {
            new Column(LogEntry.Field.TIME, -"YYYY-MM-DD_HH:MM:SS.mmm".length(), true),
            new Column(LogEntry.Field.LEVEL, -"WARNING".length(), true),
            new Column(LogEntry.Field.LOGGER, "com.example.class".length(), true),
            new Column(LogEntry.Field.MESSAGE, 80, false)
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
            TableCellRenderer r = new LogEntryFieldCellRenderer(cd.field);
            column.setCellRenderer(r);
            
            java.awt.Font font = table.getFont();
            int chars = cd.preferredCharWidth;
            int width = (int) Math.ceil(fu.getTextWidth(TextUtil.repeat("M", Math.abs(chars)), font));
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
                    int first = lsm.getMinSelectionIndex();
                    int last = lsm.getMaxSelectionIndex();
                    for (int idx = first; idx <= last; idx++) {
                        if (lsm.isSelectedIndex(idx)) {
                            int idxModel = tableRowSorter.convertRowIndexToModel(idx);
                            LogEntry entry = (LogEntry) model.getValueAt(idxModel, 0);
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
        scrollPaneTable = new JScrollPane(table);
        scrollPaneDetails = new JScrollPane(details);
        
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
        JComboBox<Category> cbCategory = new JComboBox<>(Category.values());
        toolBar.add(cbCategory);
        cbCategory.addItemListener(a -> setFilter((Category) a.getItem()));
        cbCategory.setSelectedItem(Category.INFO);

        // checkbox for text only
        toolBar.add(new JSeparator(JSeparator.VERTICAL));
        JCheckBox cbTextOnly = new JCheckBox(SwingUtil.createAction("Show text only", evt -> setTextOnly(((JCheckBox) (evt.getSource())).isSelected())));
        cbTextOnly.setSelected(true);
        toolBar.add(cbTextOnly);
        
        // buttons "clear" and "copy"
        toolBar.add(new JSeparator(JSeparator.VERTICAL));
        toolBar.add(SwingUtil.createAction("Clear", this::clearBuffer));
        toolBar.add(SwingUtil.createAction("Copy", this::copyBuffer));
        
        add(toolBar, BorderLayout.NORTH);
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

            // remove all columns crom model
            while (columnModel.getColumnCount()>0) {
                columnModel.removeColumn(columnModel.getColumn(0));
            }

            // add visible columns again
            for (int i=0; i<table.getModel().getColumnCount(); i++) {
                if (!textOnly || !COLUMNS[i].hideable) {
                    table.getColumnModel().addColumn(tableColumns.get(i));
                }
            }
        }
    }

    private void setFilter(Category c) {
        tableRowSorter.setRowFilter(new RowFilter<AbstractTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
                LogEntry value = (LogEntry) entry.getValue(0);
                return value == null || value.category().compareTo(c) <= 0;
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

    private int getTopRow() {
        return table.rowAtPoint(scrollPaneTable.getViewport().getViewPosition());
    }

    private void scrollRowIntoView(int row) {
        SwingUtilities.invokeLater(() -> table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true))));
    }

    /**
     * Set the formatter used to convert log entries to text.
     * @param format the formatting function
     */
    public void setLogFormatter(Function<LogEntry, String> format) {
        this.format = Objects.requireNonNull(format);
    }
    
    /**
     * Set the diivider location. Analog to {@link JSplitPane#setDividerLocation(double)}.
     * @param propertionalLocation the proportional location 
     */
    public void setDividerLocation(double propertionalLocation) {
        this.dividerLocation = MathUtil.clamp(0.0, 1.0, propertionalLocation);
        splitPane.setDividerLocation(dividerLocation);
    }

    /**
     * Set the diivider location. Analog to {@link JSplitPane#setDividerLocation(int)}.
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
            /* nop */
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
