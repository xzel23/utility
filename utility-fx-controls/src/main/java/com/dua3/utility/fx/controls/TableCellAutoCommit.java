package com.dua3.utility.fx.controls;

import com.dua3.cabe.annotations.Nullable;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.util.Objects;

/**
 * An editable cell in a {@link TableView} that commits changes upon focus lost.
 * Based on a <a href="https://gist.github.com/james-d/be5bbd6255a4640a5357">Gist</a> by GitHub user james-d.
 *
 * @param <S> The type of the TableView generic type (i.e., S == TableView&lt;S&gt;).
 *            This should also match with the first generic type in TableColumn.
 * @param <T> The type of the item contained within the Cell.
 */
public class TableCellAutoCommit<S, T> extends TableCell<S, T> {

    /**
     * The {@link TextField} used to edit the cell's content.
     */
    private final TextField textField = new TextField();

    /**
     * The {@link StringConverter} used to convert from and to String.
     */
    private final StringConverter<? super T> converter;

    /**
     * Returns a callback that creates an EditCell for a TableColumn with String type.
     * Uses the default String converter provided by DefaultStringConverter.
     *
     * @param <S> the type of the TableView items
     * @return a callback that creates an EditCell for a TableColumn with String type
     */
    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    /**
     * Returns a callback that creates an EditCell for a TableColumn.
     *
     * @param converter The converter used to convert the cell value to String type.
     * @param <S>       The type of the TableView items.
     * @param <T>       The type of the cell value.
     * @return A callback that creates an EditCell for a TableColumn.
     */
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(StringConverter<T> converter) {
        return (list) -> new TableCellAutoCommit<>(converter);
    }

    /**
     * Constructs an EditCell with the given converter.
     *
     * @param converter The converter used to convert the cell value to a string.
     */
    public TableCellAutoCommit(StringConverter<T> converter) {
        this.converter = converter;

        setGraphic(textField);
        setContentDisplay(ContentDisplay.TEXT_ONLY);

        itemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                setText(null);
            } else {
                setText(converter.toString(newValue));
            }
        });

        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case ESCAPE -> {
                    textField.setText(converter.toString(getItem()));
                    cancelEdit();
                    event.consume();
                }
                case RIGHT -> {
                    getTableView().getSelectionModel().selectRightCell();
                    event.consume();
                }
                case LEFT -> {
                    getTableView().getSelectionModel().selectLeftCell();
                    event.consume();
                }
                case UP -> {
                    getTableView().getSelectionModel().selectAboveCell();
                    event.consume();
                }
                case DOWN -> {
                    getTableView().getSelectionModel().selectBelowCell();
                    event.consume();
                }
                default -> {}
            }
        });

        textField.setOnAction(evt -> commitEdit(converter.fromString(textField.getText())));
        textField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue) {
                commitEdit(converter.fromString(textField.getText()));
            }
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        textField.setText(converter.toString(getItem()));
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void commitEdit(@Nullable T newValue) {
        if (!isEditing() && !Objects.equals(newValue, getItem())) {
            TableView<S> table = getTableView();
            if (table != null) {
                TableColumn<S, T> column = getTableColumn();
                CellEditEvent<S, T> event = new CellEditEvent<>(
                        table, new TablePosition<>(table, getIndex(), column), TableColumn.editCommitEvent(), newValue
                );
                Event.fireEvent(column, event);
                Platform.runLater(table::refresh);
            }
        }
        super.commitEdit(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}