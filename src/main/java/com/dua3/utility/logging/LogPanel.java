package com.dua3.utility.logging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.dua3.utility.lang.RingBuffer;

public class LogPanel extends JPanel implements LogListener {

	private static final long serialVersionUID = 1L;
	private final JTable table;
	private final LogDispatcher dispatcher;

	private static class Column {
		String name;
		Function<LogRecord, Object> extractor;
		
		Column(String name, Function<LogRecord,Object> extractor) {
			this.name = name;
			this.extractor = extractor;
		}
		
		String name() {
			return name;
		}
		
		Object get(LogRecord r) {
			return extractor.apply(r);
		}
	}
	
	private static final Column[] COLUMNS = {
			new Column("Time", r -> Instant.ofEpochMilli(r.getMillis())),
			new Column("Level", r -> r.getLevel()),
			new Column("Message", r -> r.getMessage())
	};
	
	private static final class LogTableModel extends AbstractTableModel {
		LogTableModel(){
			initStyles();			
		}
		
		private static final long serialVersionUID = 1L;
		
		private RingBuffer<LogRecord> records = new RingBuffer<>(1000);

		private final TreeMap<Integer, MessageStyle> styles = new TreeMap<>();

		@Override
		public int getRowCount() {
			return records.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return COLUMNS[columnIndex].get(getRecordForRow(rowIndex));
		}

		private LogRecord getRecordForRow(int rowIndex) {
			return records.get(rowIndex);
		}
		
		public void publish(LogRecord r) {
			SwingUtilities.invokeLater(() -> addRecord(r));
		}
		
		private void addRecord(LogRecord r) {
			records.add(r);
			fireTableDataChanged();
		}

		private MessageStyle getStyle(Level level) {
			Entry<Integer, MessageStyle> entry = styles.ceilingEntry(level.intValue());
			return entry == null ? styles.lastEntry().getValue() : entry.getValue();
		}

		private MessageStyle createStyle(Level level, Color color) {
			MessageStyle ms = new MessageStyle(color);
			styles.put(level.intValue(), ms);
		
			return ms;
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
		public MessageStyle(Color color) {
			this.color = color;
		}
	
		Color color;
	}

	private LogTableModel dataModel;

	public LogPanel() {
		super();
		setLayout(new BorderLayout());

		table = new JTable();
		add(table);

	    dataModel = new LogTableModel();
		
		table.setModel(dataModel);

		table.getColumnModel().getColumn(1).setCellRenderer(new LogRecordTableCellRenderer());
		dispatcher = new LogDispatcher(this);
	}

	@Override
	public void publish(LogRecord record) {
		dataModel.publish(record);
	}

	@Override
	public void flush() {
		// nop

	}

	@Override
	public void close() throws SecurityException {
		// nop
	}

	public Handler getHandler() {
		return dispatcher;
	}

}
