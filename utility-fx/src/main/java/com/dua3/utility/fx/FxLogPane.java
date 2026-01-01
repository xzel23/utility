package com.dua3.utility.fx;

import com.dua3.utility.application.ApplicationUtil;
import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * FxLogPane is a custom JavaFX component that provides a log viewer with filtering
 * and search capabilities. It extends BorderPane and consists of a TableView for
 * displaying log entries and a TextArea for showing details of the selected log entry.
 * The log entries can be filtered by log level, logger name, and message content.
 * Additionally, it supports text search within the log messages.
 */
public class FxLogPane extends BorderPane {

    private static final double COLUMN_WIDTH_MAX = Double.MAX_VALUE;
    private static final double COLUMN_WIDTH_LARGE = 2000.0;

    /**
     * A CSS class constant representing the style applied to rows in the log table.
     * This class name is used to customize the appearance of log table rows within
     * the FxLogPane.
     */
    public static final String CSS_CLASS_LOGTABLE_ROW = "logtable-row";
    /**
     * A string constant representing the prefix "log-" used for CSS classes
     * associated with log level styling within the {@code FxLogPane} component.
     * This prefix can be appended to specific log level names to create dynamic
     * CSS class names (e.g., "log-info" or "log-error"), allowing customization
     * of the appearance of log entries based on their log level.
     */
    public static final String CSS_PREFIX_LOGLEVEL = "log-";

    private final LogBuffer logBuffer;
    private final TextArea details;
    private final TableView<@Nullable LogEntry> tableView;

    private final AtomicReference<@Nullable LogEntry> selectedItem = new AtomicReference<>();

    private final String darkCss;
    private final String lightCss;

    private boolean autoScroll = true;

    private <T> TableColumn<LogEntry, T> createColumn(String name, Function<? super LogEntry, ? extends T> getter, boolean fixedWidth, String... sampleTexts) {
        TableColumn<LogEntry, T> column = new TableColumn<>(name);
        column.setCellValueFactory(entry -> new SimpleObjectProperty<>(getter.apply(entry.getValue())));
        if (sampleTexts.length == 0) {
            column.setPrefWidth(COLUMN_WIDTH_LARGE);
            column.setMaxWidth(COLUMN_WIDTH_MAX);
        } else {
            double w = 24 + Stream.of(sampleTexts).mapToDouble(FxLogPane::getDisplayWidth).max().orElse(200);
            column.setPrefWidth(w);
            if (fixedWidth) {
                column.setMinWidth(w);
                column.setMaxWidth(w);
            } else {
                column.setMaxWidth(COLUMN_WIDTH_MAX);
            }
        }
        column.setCellFactory(col -> new TableCell<LogEntry, @Nullable T>() {
            @Override
            protected void updateItem(@Nullable T item, boolean empty) {
                super.updateItem(item, empty);
                TableRow<LogEntry> row = getTableRow();
                if (empty || row == null || row.getItem() == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item == null ? "" : item.toString());
                }
                super.updateItem(item, empty);
            }
        });
        return column;
    }

    private static double getDisplayWidth(String s) {
        return new Text(s).getLayoutBounds().getWidth();
    }

    /**
     * Construct a new FxLogPane instance with default buffer capacity.
     */
    public FxLogPane() {
        this(LogBuffer.DEFAULT_CAPACITY);
    }

    /**
     * Construct a new FxLogPane instance with the given buffer capacity.
     *
     * @param bufferSize the buffer size
     */
    public FxLogPane(int bufferSize) {
        this(createBuffer(bufferSize));
    }

    /**
     * Construct a new FxLogPane instance with the given buffer.
     *
     * @param logBuffer the logBuffer to use
     * @throws NullPointerException if logBuffer is null
     */
    public FxLogPane(LogBuffer logBuffer) {
        FilteredList<LogEntry> entries = new FilteredList<>(new LogEntriesObservableList(logBuffer), p -> true);

        this.logBuffer = logBuffer;
        this.darkCss = LangUtil.getResourceURL(getClass(), "dark.css").toExternalForm();
        this.lightCss = LangUtil.getResourceURL(getClass(), "light.css").toExternalForm();
        ToolBar toolBar = new ToolBar();
        this.tableView = new TableView<>(entries);
        this.details = new TextArea();

        // colorize table rows via CSS classes
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(@Nullable LogEntry item, boolean empty) {
                super.updateItem(item, empty);
                // clear previous style classes
                getStyleClass().removeIf(s -> s.startsWith(CSS_PREFIX_LOGLEVEL));
                if (!empty && item != null) {
                    getStyleClass().addAll(CSS_CLASS_LOGTABLE_ROW, cssClassForLogEntry(item.level()));
                }
            }
        });

        // install default stylesheet
        setDarkMode(ApplicationUtil.isDarkMode());
        ApplicationUtil.addDarkModeListener(this::setDarkMode);

        entries.addListener(this::onEntries);

        double tfWidth = getDisplayWidth("X".repeat(32));

        // filtering by log level, logger name, and message content
        ComboBox<LogLevel> cbLogLevel = new ComboBox<>(FXCollections.observableArrayList(LogLevel.values()));
        TextField tfLoggerName = new TextField();
        tfLoggerName.setPrefWidth(tfWidth);
        TextField tfMessageContent = new TextField();
        tfMessageContent.setPrefWidth(tfWidth);

        Runnable updateFilter = () -> updateFilter(cbLogLevel, tfLoggerName, tfMessageContent, entries);

        cbLogLevel.valueProperty().addListener((v, o, n) -> updateFilter.run());
        tfLoggerName.textProperty().addListener((v, o, n) -> updateFilter.run());
        tfMessageContent.textProperty().addListener((v, o, n) -> updateFilter.run());

        cbLogLevel.setValue(LogLevel.INFO);
        tfLoggerName.clear();
        tfMessageContent.clear();

        // search for text
        TextField tfSearchText = new TextField();
        tfSearchText.setPrefWidth(tfWidth);
        Button btnSearchUp = new Button(I18NInstance.get().get("dua3.fx.log.search.up"));
        Button btnSearchDown = new Button(I18NInstance.get().get("dua3.fx.log.search.down"));

        BiConsumer<String, Boolean> searchAction = (text, up) -> searchAction(text, up, entries);

        tfSearchText.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchAction.accept(tfSearchText.getText(), event.isShiftDown());
            }
        });

        btnSearchUp.setOnAction(evt -> searchAction.accept(tfSearchText.getText(), true));
        btnSearchDown.setOnAction(evt -> searchAction.accept(tfSearchText.getText(), false));

        Button btnClear = new Button(I18NInstance.get().get("dua3.fx.log.clear"));
        btnClear.setOnAction(evt -> logBuffer.clear());

        // create toolbar
        toolBar.getItems().setAll(
                new Label(I18NInstance.get().get("dua3.fx.log.level")),
                cbLogLevel,
                new Label(I18NInstance.get().get("dua3.fx.log.logger")),
                tfLoggerName,
                new Label(I18NInstance.get().get("dua3.fx.log.text")),
                tfMessageContent,
                new Separator(Orientation.HORIZONTAL),
                new Label(I18NInstance.get().get("dua3.fx.log.search")),
                tfSearchText,
                btnSearchUp,
                btnSearchDown,
                new Separator(Orientation.HORIZONTAL),
                btnClear
        );

        // define table columns
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        //noinspection unchecked
        tableView.getColumns().setAll(
                createColumn(I18NInstance.get().get("dua3.fx.log.column.time"), LogEntry::time, true, "8888-88-88T88:88:88.8888888"),
                createColumn(I18NInstance.get().get("dua3.fx.log.column.level"), LogEntry::level, true, Arrays.stream(LogLevel.values()).map(Object::toString).toArray(String[]::new)),
                createColumn(I18NInstance.get().get("dua3.fx.log.column.logger"), LogEntry::loggerName, false, "X".repeat(20)),
                createColumn(I18NInstance.get().get("dua3.fx.log.column.message"), LogEntry::message, false)
        );

        // disable autoscroll if the selection is not empty, enable when selection is cleared while scrolled to bottom
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                details.clear();
                autoScroll = autoScroll || isScrolledToBottom();
            } else {
                selectedItem.set(newSelection);
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

    private static final Map<LogLevel, String> CSS_CLASS_BY_LOG_LEVEL = Arrays.stream(LogLevel.values())
            .collect(Collectors.toMap(
                    Function.identity(),
                    level -> CSS_PREFIX_LOGLEVEL + level.name().toLowerCase(Locale.ROOT)
            ));

    private static String cssClassForLogEntry(LogLevel level) {
        return CSS_CLASS_BY_LOG_LEVEL.get(level);
    }

    /**
     * Set dark mode stylesheet for this pane.
     * @param dark true to use dark.css, false to use light.css
     */
    public void setDarkMode(boolean dark) {
        getStylesheets().setAll(dark ? darkCss : lightCss);
    }


    /**
     * Searches within the provided list of log entries for a log entry containing the specified text.
     * The search direction is determined by the `up` parameter and wraps around the list if necessary.
     *
     * @param text    the text to search for in the log entries
     * @param up      a boolean indicating the search direction; true for upward search, false for downward search
     * @param entries the list of log entries to search through; must be a FilteredList of LogEntry objects
     */
    private void searchAction(String text, boolean up, FilteredList<LogEntry> entries) {
        String lowercaseText = text.toLowerCase(Locale.ROOT);
        int step = up ? -1 : 1;
        LogEntry current = selectedItem.get();
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
    }

    /**
     * Updates the filter applied to a list of log entries based on the provided log level, logger name, and message content.
     *
     * @param cbLogLevel     the ComboBox component used to select the log level filter
     * @param tfLoggerName   the TextField used to input the logger name filter
     * @param tfMessageContent the TextField used to input the message content filter
     * @param entries        the FilteredList containing the log entries to be filtered
     */
    private static void updateFilter(ComboBox<LogLevel> cbLogLevel, TextField tfLoggerName, TextField tfMessageContent, FilteredList<LogEntry> entries) {
        LogLevel level = cbLogLevel.getSelectionModel().getSelectedItem();

        BiPredicate<String, LogLevel> predicateLoggerName;
        String loggerText = tfLoggerName.getText().toLowerCase(Locale.ROOT).strip();
        if (loggerText.isEmpty()) {
            predicateLoggerName = (String name, LogLevel lvl) -> true;
        } else {
            predicateLoggerName = (name, lvl) -> name.toLowerCase(Locale.ROOT).contains(loggerText);
        }

        BiPredicate<String, LogLevel> predicateContent;
        String messageContent = tfMessageContent.getText();
        if (messageContent.isEmpty()) {
            predicateContent = (String text, LogLevel lvl) -> true;
        } else {
            predicateContent = (text, lvl) -> text.contains(messageContent);
        }
        entries.setPredicate(new DefaultLogEntryFilter(level, predicateLoggerName, predicateContent));
    }

    /**
     * Selects a specific log entry in the TableView, scrolls to it, and ensures
     * the selection is updated appropriately. This method must be executed on the
     * JavaFX Application thread.
     *
     * @param logEntry the {@link LogEntry} to select and scroll to
     */
    private void selectLogEntry(LogEntry logEntry) {
        PlatformHelper.checkApplicationThread();
        tableView.getSelectionModel().select(logEntry);
        tableView.scrollTo(logEntry);
    }

    /**
     * Handles the scroll event for the log pane. Updates the {@code autoScroll}
     * property based on the current scroll position and selection state.
     *
     * @param evt the {@link ScrollEvent} representing the scroll action
     */
    private void onScrollEvent(ScrollEvent evt) {
        PlatformHelper.checkApplicationThread();
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
        PlatformHelper.checkApplicationThread();
        return selectedItem == null;
    }

    private void clearSelection() {
        PlatformHelper.checkApplicationThread();
        tableView.getSelectionModel().clearSelection();
        selectedItem.set(null);
    }

    private void onEntries(ListChangeListener.Change<? extends LogEntry> change) {
        PlatformHelper.checkApplicationThread();
        if (autoScroll) {
            // scroll to bottom
            Platform.runLater(() -> {
                tableView.scrollTo(tableView.getItems().size() - 1);
                autoScroll = true;
            });
        }
        if (!isSelectionEmpty()) {
            // update selection
            Platform.runLater(() -> tableView.getSelectionModel().select(selectedItem.get()));
        }
    }

    private boolean isScrolledToBottom() {
        PlatformHelper.checkApplicationThread();
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
     * Retrieves the LogBuffer associated with this FxLogPane instance.
     *
     * @return the LogBuffer object
     */
    public LogBuffer getLogBuffer() {
        return logBuffer;
    }
}
