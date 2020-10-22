package com.dua3.utility.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.text.TextUtil;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
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
    private final AbstractTableModel model;
    private final JScrollPane scrollPaneTable;
    private final Function<LogEntry, Color> colorize;
    private final BufferListener bufferListener;
    private final JScrollPane scrollPaneDetails;

    private static Color defaultColorize(LogEntry entry) {
        switch (entry.category()) {
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

    private class LogTableModel extends AbstractTableModel {
        private LogTableModel() {
        }

        @Override
        public int getRowCount() {
            return buffer.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public LogEntry getValueAt(int rowIndex, int columnIndex) {
            return buffer.get(rowIndex);
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column].field.toString();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return LogEntry.Field.class;
        }
    }

    private class BufferListener implements LogBuffer.LogBufferListener {
        @Override
        public void entries(LogEntry[] entries, int removed) {
            onAddEntries(entries, removed);
        }

        @Override
        public void clear() {
            onClear();
        }

        @Override
        public void capacity(int n) {
            onSetCapacity(n);
        }
    }
    
    private void onAddEntries(LogEntry[] entries, int removed) {
        synchronized (model) {
            // inform the model
            int rows = model.getRowCount();
            if (removed!=0) {
                model.fireTableRowsDeleted(0,removed-1);
            }
            model.fireTableRowsInserted(rows - entries.length, rows-1);

            // handle scrolling
            JScrollBar scroll = scrollPaneTable.getVerticalScrollBar();
            int row;
            if (scroll.getValue() >= scroll.getMaximum() - scroll.getVisibleAmount() - table.getRowHeight()) {
                // scroll to last row
                row = rows;
            } else {
                // keep row with current entry visible => scroll up if first row is removed from buffer
                int topRow = getTopRow();
                row = topRow - removed;
            }
            SwingUtilities.invokeLater(() -> scrollRowIntoView(row));
        }
    }
    
    private void onClear() {
        model.fireTableDataChanged();
    }

    private void onSetCapacity(int n) {
        model.fireTableDataChanged();
    }
    
    private class LogEntryFieldCellRenderer extends DefaultTableCellRenderer {
        private final LogEntry.Field f;
        private boolean isSelected = false;

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
        
        Column(LogEntry.Field field, int preferredCharWidth) {
            this.field = field;
            this.preferredCharWidth = preferredCharWidth;
        }
    }
    
    private static final Column[] COLUMNS = {
            new Column(LogEntry.Field.TIME, -"YYYY-MM-DD_HH:MM:SS.mmm".length()),
            new Column(LogEntry.Field.LEVEL, -"WARNING".length()),
            new Column(LogEntry.Field.LOGGER, "com.example.class".length()),
            new Column(LogEntry.Field.MESSAGE, 80),
    };
    
    public SwingLogPane(LogBuffer buffer) {
        this(buffer, SwingLogPane::defaultColorize);
    }
    
    public SwingLogPane(LogBuffer buffer, Function<LogEntry, Color> colorize) {
        super(new BorderLayout());
        
        this.buffer = Objects.requireNonNull(buffer);
        this.colorize = Objects.requireNonNull(colorize);
        this.model = new LogTableModel();
        this.bufferListener = new BufferListener();
        
        // create the table
        table = new JTable(model);
        
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
        
        //
        table.getSelectionModel().addListSelectionListener(evt -> {
            ListSelectionModel lsm = (ListSelectionModel)evt.getSource();
            int firstIndex = evt.getFirstIndex();
            int lastIndex = evt.getLastIndex();
            
            final String text;
            if (lsm.isSelectionEmpty() || evt.getValueIsAdjusting()) {
                text = "";
            } else {
                List<LogEntry> selection;
                synchronized (buffer) {
                    selection = new ArrayList<>(buffer.subList(firstIndex, lastIndex+1));
                }
                StringBuilder sb = new StringBuilder(1024);
                for (int idx=firstIndex; idx<=lastIndex; idx++) {
                    if (lsm.isSelectedIndex(idx)) {
                        sb.append(selection.get(idx-firstIndex)).append("\n");
                    }
                }
                text = sb.toString();
            }
            SwingUtilities.invokeLater(() -> {
                details.setText(text);
                details.setCaretPosition(0);
            });
        });
        
        // prepare the ScrollPanes
        scrollPaneTable = new JScrollPane(table);
        scrollPaneDetails = new JScrollPane(details);
        
        // add
        add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneTable, scrollPaneDetails), BorderLayout.CENTER);
    }

    private int getTopRow() {
        return table.rowAtPoint(scrollPaneTable.getViewport().getViewPosition());
    }

    private void scrollRowIntoView(int row) {
        SwingUtilities.invokeLater(() -> table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true))));
    }

    private void setListeningState(boolean listen) {
        if (listen) {
            buffer.addLogBufferListener(bufferListener);
            model.fireTableDataChanged();
        } else {
            buffer.removeLogBufferListener(bufferListener);            
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        setListeningState(true);
    }

    @Override
    public void removeNotify() {
        setListeningState(false);
        super.removeNotify();
    }

    @Override
    public void setVisible(boolean visible) {
        setListeningState(visible);
        super.setVisible(visible);
    }
}
