package com.dua3.utility.swing;

import com.dua3.utility.lang.RingBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class SwingLogPane extends JPanel implements LogListener {

    private static final int DEFAULT_CAPACITY = 1000;
    
    private final JTable table;
    private final AbstractTableModel model;
    private final JScrollPane scrollPane;

    private static class Column {
        final String name;
        final Class<?> type;
        final Function<LogEntry,?> get;
        
        public <T> Column(String name, Class<T> type, Function<LogEntry,T> get) {
            this.name = name;
            this.type = type;
            this.get = get;
        }
    }
    
    private static final Column[] COLUMNS = {
      new Column("Time", LocalDateTime.class, LogEntry::time),
      new Column("Level", String.class, LogEntry::level),
      new Column("Message", String.class, LogEntry::text)      
    };
    
    private final RingBuffer<LogEntry> buffer = new RingBuffer<>(DEFAULT_CAPACITY);
    
    public SwingLogPane() {
        super(new BorderLayout());
        
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
            public Object getValueAt(int rowIndex, int columnIndex) {
                return COLUMNS[columnIndex].get.apply(buffer.get(rowIndex));
            }

            @Override
            public String getColumnName(int column) {
                return COLUMNS[column].name;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return COLUMNS[columnIndex].type;
            }
        };
        
        table = new JTable(model);
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
