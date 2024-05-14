package com.dua3.utility.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.data.Color;
import com.dua3.utility.logging.DefaultLogEntryFilter;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

public class FxLogPane extends BorderPane {

    public static final double COLUMN_WIDTH_MAX = Double.MAX_VALUE;
    public static final double COLUMN_WIDTH_LARGE = 10000.0;
    private final Function<? super LogEntry, Color> colorize;
    private final ToolBar toolBar;
    private final TextArea details;
    private final TableView<LogEntry> tableView;

    private volatile LogEntry selectedItem;

    private boolean autoScroll = true;

    private <T> TableColumn<LogEntry, T> createColumn(String name, Function<? super LogEntry, ? extends T> getter, boolean fixedWidth, String... sampleTexts) {
        TableColumn<LogEntry, T> column = new TableColumn<>(name);
        column.setCellValueFactory(entry -> new SimpleObjectProperty<>(getter.apply(entry.getValue())));
        if (sampleTexts.length == 0) {
            column.setPrefWidth(COLUMN_WIDTH_LARGE);
            column.setMaxWidth(COLUMN_WIDTH_MAX);
        }else {
            double w = 8 + Stream.of(sampleTexts).mapToDouble(FxLogPane::getDisplayWidth).max().orElse(80);
            column.setPrefWidth(w);
            if (fixedWidth) {
                column.setMinWidth(w);
                column.setMaxWidth(w);
            } else {
                column.setMaxWidth(COLUMN_WIDTH_MAX);
            }
        }
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(@Nullable T item, boolean empty) {
                super.updateItem(item, empty);
                TableRow<LogEntry> row = getTableRow();
                if (empty || row == null || row.getItem() == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item == null ? "" : item.toString());
                    Color textColor = colorize.apply(row.getItem());
                    setTextFill(FxUtil.convert(textColor));
                }
                super.updateItem(item, empty);
            }
        });
        return column;
    }

    private static double getDisplayWidth(String s) {
        return new Text(s).getLayoutBounds().getWidth();
    }

    public FxLogPane() {
        this(LogBuffer.DEFAULT_CAPACITY);
    }

    public FxLogPane(int bufferSize) {
        this(createBuffer(bufferSize));
    }

    public FxLogPane(LogBuffer buffer) {
        this(buffer, FxLogPane::defaultColorize);
    }

    public FxLogPane(LogBuffer buffer, Function<? super LogEntry, Color> colorize) {
        FilteredList<LogEntry> entries = new FilteredList<>(new LogEntriesObservableList(buffer), p -> true);

        this.colorize = colorize;
        this.toolBar = new ToolBar();
        this.tableView = new TableView<>(entries);
        this.details = new TextArea();

        entries.addListener(this::onEntries);

        double tfWidth = getDisplayWidth("X".repeat(32));

        // filtering by log level and logger name
        ComboBox<LogLevel> cbLogLevel = new ComboBox<>(FXCollections.observableArrayList(LogLevel.values()));
        TextField tfLoggerName = new TextField();
        tfLoggerName.setPrefWidth(tfWidth);

        Runnable updateFilter = () -> {
            LogLevel level = cbLogLevel.getSelectionModel().getSelectedItem();
            BiPredicate<String, LogLevel> predicate;
            String loggerText = tfLoggerName.getText().toLowerCase(Locale.ROOT);
            if (loggerText.isEmpty()) {
                predicate = (String name, LogLevel lvl) -> true;
            } else {
                predicate = (name, lvl) -> name.toLowerCase(Locale.ROOT).contains(loggerText);
            }
            entries.setPredicate(new DefaultLogEntryFilter(level, predicate));
        };

        cbLogLevel.valueProperty().addListener((v,o,n) -> updateFilter.run());
        tfLoggerName.textProperty().addListener((v,o,n) -> updateFilter.run());

        cbLogLevel.setValue(LogLevel.INFO);
        tfLoggerName.clear();

        // search for text
        TextField tfSearchText = new TextField();
        tfSearchText.setPrefWidth(tfWidth);
        Button btnSearchUp = new Button("▲");
        Button btnSearchDown = new Button("▼");

        BiConsumer<String, Boolean> searchAction = (text, up) -> {
            String lowercaseText = text.toLowerCase(Locale.ROOT);
            int step = up ? -1 : 1;
            LogEntry current = selectedItem;
            List<LogEntry> items = List.copyOf(entries);
            int n = items.size();
            int pos = current != null ? Math.max(0, items.indexOf(current)) : 0;
            for (int i = 0; i < n; i++) {
                int j = Math.floorMod(pos + step * i, n);
                LogEntry logEntry = items.get(j);
                if (logEntry.message().toLowerCase(Locale.ROOT).contains(lowercaseText) && (i != 0 || current == null)) { // skip current entry if selected
                    selectLogEntry(logEntry);
                    break;
                }
            }
        };

        tfSearchText.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) {
                    searchAction.accept(tfSearchText.getText(), true);
                } else {
                    searchAction.accept(tfSearchText.getText(), false);
                }
            }
        });

        btnSearchUp.setOnAction(evt -> searchAction.accept(tfSearchText.getText(), true));
        btnSearchDown.setOnAction(evt -> searchAction.accept(tfSearchText.getText(), false));

        Button btnClear = new Button("🗑️");
        btnClear.setOnAction(evt -> buffer.clear());

        // create toolbar
        toolBar.getItems().setAll(
                new Label("Level:"),
                cbLogLevel,
                new Label("Logger:"),
                tfLoggerName,
                new Separator(Orientation.HORIZONTAL),
                new Label("Search by Message:"),
                tfSearchText,
                btnSearchUp,
                btnSearchDown,
                new Separator(Orientation.HORIZONTAL),
                btnClear
        );

        // define table columns
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);
        tableView.getColumns().setAll(
                createColumn("Time", LogEntry::time, true, "8888-88-88T88:88:88.8888888"),
                createColumn("Level", LogEntry::level, true, Arrays.stream(LogLevel.values()).map(Object::toString).toArray(String[]::new)),
                createColumn("Logger", LogEntry::loggerName, false, "X".repeat(20)),
                createColumn("Message", LogEntry::message, false, "X".repeat(60))
        );

        // disable autoscroll if the selection is not empty, enable when selection is cleared while scrolled to bottom
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                details.clear();
                autoScroll = autoScroll || isScrolledToBottom();
            } else {
                this.selectedItem = newSelection;
                autoScroll = false;
                details.setText(newSelection.toString());
            }
        });

        //  ESC clears the selection
        tableView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                clearSelection();
                autoScroll = true;
                event.consume();
            }
        });

        // automatically enable/disable autoscroll when user scrolls
        tableView.addEventFilter(ScrollEvent.ANY, this::onScrollEvent);

        SplitPane splitPane = new SplitPane(tableView, details);
        splitPane.setOrientation(Orientation.VERTICAL);

        setTop(toolBar);
        setCenter(splitPane);
    }

    private void selectLogEntry(LogEntry logEntry) {
        assert Platform.isFxApplicationThread() : "not on FX Application Thread";
        tableView.getSelectionModel().select(logEntry);
        tableView.scrollTo(logEntry);
    }

    private void onScrollEvent(ScrollEvent evt) {
        assert Platform.isFxApplicationThread() : "not on FX Application Thread";
        if (autoScroll) {
            // disable autoscroll when manually scrolling
            autoScroll = false;
        } else {
            // enable autoscroll when scrolling ends at end of input and selection is empty
            autoScroll = isSelectionEmpty() && isScrolledToBottom();
        }
    }

    private Optional<ScrollBar> getScrollBar(Orientation orientation) {
        for (Node node : tableView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar sb && sb.getOrientation() == orientation) {
                return Optional.of(sb);
            }
        }
        return Optional.empty();
    }

    private boolean isSelectionEmpty() {
        assert Platform.isFxApplicationThread() : "not on FX Application Thread";
        return selectedItem == null;
    }

    private void clearSelection() {
        assert Platform.isFxApplicationThread() : "not on FX Application Thread";
        tableView.getSelectionModel().clearSelection();
        selectedItem = null;
    }

    private void onEntries(ListChangeListener.Change<? extends LogEntry> change) {
        assert Platform.isFxApplicationThread() : "not on FX Application Thread";
        if (autoScroll) {
            // scroll to bottom
            Platform.runLater(() -> {
                tableView.scrollTo(tableView.getItems().size()-1);
                autoScroll = true;
            });
        }
        if (!isSelectionEmpty()) {
            // update selection
            Platform.runLater(() -> tableView.getSelectionModel().select(selectedItem));
        }
    }

    private boolean isScrolledToBottom() {
        assert Platform.isFxApplicationThread() : "not on FX Application Thread";
        return getScrollBar(Orientation.VERTICAL).map(sb -> {
                    double max = sb.getMax();
                    double current = sb.getValue();
                    double step = max / (1.0 + tableView.getItems().size());
                    return current >= max - step;
                })
                .orElse(true);
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
     * Default colorize method used in the FxLogPane class to determine the color of log entries.
     *
     * @param entry the log entry to be colorized
     * @return the Color object representing the color for the given log entry
     */
    private static Color defaultColorize(LogEntry entry) {
        return switch (entry.level()) {
            case ERROR -> Color.DARKRED;
            case WARN -> Color.RED;
            case INFO -> Color.DARKBLUE;
            case DEBUG -> Color.BLACK;
            case TRACE -> Color.DARKGRAY;
        };
    }


}
