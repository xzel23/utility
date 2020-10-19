package com.dua3.utility.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.RingBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class SwingLogPane extends JPanel implements LogListener {

    private static final int DEFAULT_CAPACITY = 1000;
    
    private final JTable table;
    private final AbstractTableModel model;
    private final JScrollPane scrollPane;
    private final Function<LogEntry, Color> colorize;

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
    
    class LogEntryFieldCellRenderer extends DefaultTableCellRenderer {
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
    
    private final RingBuffer<LogEntry> buffer = new RingBuffer<>(DEFAULT_CAPACITY);

    public SwingLogPane() {
        this(SwingLogPane::defaultColorize);
    }
    
    public SwingLogPane(Function<LogEntry, Color> colorize) {
        super(new BorderLayout());
        
        this.colorize = Objects.requireNonNull(colorize);
        
        model = new AbstractTableModel() {
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
        };
        
        table = new JTable(model);

        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableCellRenderer r = new LogEntryFieldCellRenderer(COLUMNS[i]);
            columnModel.getColumn(i).setCellRenderer(r);
        }
        
        scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    @Override
    public void entry(LogEntry entry) {
        synchronized (buffer) {
            // determine the row to keep on screen
            JScrollBar scroll = scrollPane.getVerticalScrollBar();
            final IntConsumer updateScroll;
            if (scroll.getValue() >= scroll.getMaximum() - scroll.getVisibleAmount() - table.getRowHeight()) {
                // scroll to last row
                updateScroll = added -> scrollRowIntoView(table.getRowCount());
            } else {
                // keep row with current entry visible => scroll up if first row is removed from buffer
                int topRow = getTopRow();
                updateScroll = added -> scrollRowIntoView(topRow - (added == 0?1:0));
            }
            
            // add the new entry
            final int added = buffer.add(Objects.requireNonNull(entry));
            int idx = buffer.size() - 1;
            if (added==0) {
                this.model.fireTableRowsDeleted(0,0);
            }
            this.model.fireTableRowsInserted(idx, idx);

            // handle scrolling
            updateScroll.accept(added);
        }
    }

    private int getTopRow() {
        return table.rowAtPoint(scrollPane.getViewport().getViewPosition());
    }

    private void scrollRowIntoView(int row) {
        SwingUtilities.invokeLater(() -> table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true))));
    }
}
