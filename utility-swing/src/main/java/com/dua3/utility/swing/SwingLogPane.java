package com.dua3.utility.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * A Swing component that displays logging messages.
 */
public class SwingLogPane extends JPanel {

    private final LogBuffer buffer;
    private final JTable table;
    private final AbstractTableModel model;
    private final JScrollPane scrollPane;
    private final Function<LogEntry, Color> colorize;
    private final BufferListener bufferListener;

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
        public LogTableModel() {
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
            return COLUMNS[column].toString();
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
            JScrollBar scroll = scrollPane.getVerticalScrollBar();
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

        public LogEntryFieldCellRenderer(LogEntry.Field f) {
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
            super.setValue(v);
        }
    }
    
    private static final LogEntry.Field[] COLUMNS = {
            LogEntry.Field.TIME,
            LogEntry.Field.LEVEL,
            LogEntry.Field.CATEGORY,
            LogEntry.Field.TEXT
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
        
        table = new JTable(model);

        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableCellRenderer r = new LogEntryFieldCellRenderer(COLUMNS[i]);
            columnModel.getColumn(i).setCellRenderer(r);
        }
        
        scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    private int getTopRow() {
        return table.rowAtPoint(scrollPane.getViewport().getViewPosition());
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
