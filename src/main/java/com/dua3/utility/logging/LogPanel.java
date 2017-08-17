package com.dua3.utility.logging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.dua3.utility.lang.RingBuffer;
import com.dua3.utility.swing.SwingUtil;

/**
 * A panel that displays log messages.
 * <p>
 * <b>Attention:</b>
 * Only works with java.util.logging (or jdk14 binding for SLF4J).
 * </p>
 */
public class LogPanel extends JPanel implements LogListener {

    private static class Column {
        String name;
        Function<LogRecord, Object> extractor;

        Column(String name, Function<LogRecord, Object> extractor) {
            this.name = name;
            this.extractor = extractor;
        }

        Object get(LogRecord r) {
            return extractor.apply(r);
        }

        String name() {
            return name;
        }
    }

    private static final class LogTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        private final RingBuffer<LogRecord> records = new RingBuffer<>(1000);

        private final TreeMap<Integer, MessageStyle> styles = new TreeMap<>();

        LogTableModel() {
            initStyles();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column].name();
        }

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return COLUMNS[columnIndex].get(getRecordForRow(rowIndex));
        }

        public void publish(LogRecord r) {
            SwingUtilities.invokeLater(() -> addRecord(r));
        }

        private void addRecord(LogRecord r) {
            records.add(r);
            fireTableDataChanged();
        }

        private MessageStyle createStyle(Level level, Color color) {
            MessageStyle ms = new MessageStyle(color);
            styles.put(level.intValue(), ms);

            return ms;
        }

        private LogRecord getRecordForRow(int rowIndex) {
            return records.get(rowIndex);
        }

        private MessageStyle getStyle(Level level) {
            Entry<Integer, MessageStyle> entry = styles.ceilingEntry(level.intValue());
            return entry == null ? styles.lastEntry().getValue() : entry.getValue();
        }

        private void initStyles() {
            createStyle(Level.SEVERE, Color.RED);
            createStyle(Level.WARNING, Color.RED);
            createStyle(Level.INFO, Color.BLUE);
            createStyle(Level.CONFIG, Color.BLACK);
            createStyle(Level.FINE, Color.BLACK);
            createStyle(Level.FINER, Color.BLACK);
            createStyle(Level.FINEST, Color.BLACK);
        }

    }

    static class LogRecordTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            LogTableModel model = (LogTableModel) table.getModel();
            LogRecord r = model.getRecordForRow(row);
            MessageStyle style = model.getStyle(r.getLevel());
            comp.setForeground(style.color);
            return comp;
        }
    }
    
    static class MessageStyle {
        Color color;

        public MessageStyle(Color color) {
            this.color = color;
        }
    }
    
    private static final long serialVersionUID = 1L;

    private static final Formatter formatter = new SimpleFormatter();

    private static final Column[] COLUMNS = {
            new Column("Time", r -> Instant.ofEpochMilli(r.getMillis())),
            new Column("Logger", r -> r.getLoggerName()),
            new Column("Level", r -> r.getLevel()),
            new Column("Message", formatter::formatMessage)
    };

    private final JTable table;

    private JScrollPane scrollPane;

    private final LogDispatcher dispatcher;

    private LogTableModel dataModel;

    public LogPanel(Logger... loggers) {
        super();
        setLayout(new BorderLayout());

        table = new JTable();
        scrollPane = new JScrollPane(table);
        add(scrollPane);

        dataModel = new LogTableModel();

        table.setModel(dataModel);

        table.getColumnModel().getColumn(2).setCellRenderer(new LogRecordTableCellRenderer());
        dispatcher = new LogDispatcher(this);

        for (Logger logger : loggers) {
            addLogger(logger);
        }
    }

    public void addAllKnowLoggers() {
        LogManager logManager = LogManager.getLogManager();
        Enumeration<String> loggerNames = logManager.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            String loggerName = loggerNames.nextElement();
            addLogger(logManager.getLogger(loggerName));
        }
    }

    public void addLogger(Logger logger) {
        Handler handler = getHandler();
        if (!Arrays.asList(logger.getHandlers()).contains(handler)) {
            logger.addHandler(handler);
        }
    }

    @Override
    public void close() {
        // nop
    }

    @Override
    public void flush() {
        // nop

    }

    public Handler getHandler() {
        return dispatcher;
    }

    @Override
    public void publish(LogRecord record) {
        SwingUtil.updateAndScrollToBottom(scrollPane, () -> dataModel.publish(record));
    }

}
