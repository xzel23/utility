package com.dua3.utility.samples.fx;

import com.dua3.utility.io.CsvIo;
import com.dua3.utility.fx.controls.Dialogs;
import com.dua3.utility.fx.controls.FileDialogMode;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sample Application.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class DialogSample extends Application {

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox container = new VBox();

        // About
        container.getChildren().add(createButton("About", () -> {
            Dialogs.about(primaryStage)
                    .title("About…")
                    .name("Dialog Sample")
                    .version("v 0.1")
                    .copyright("(c) 2021 Axel Howind")
                    .mail("info@example.com")
                    .showAndWait();
            System.out.println("About Dialog shown");
        }));

        // Confirmation
        container.getChildren().add(createButton("Confirmation", () ->
                Dialogs.confirmation(primaryStage)
                        .title("Elevator cleaning")
                        .header("Good for you!")
                        .text("You've decided to clean the elevator.")
                        .showAndWait()
                        .ifPresentOrElse(answer -> System.out.println("Answer: " + answer), () -> System.out.println("No answer"))
        ));

        // Information
        container.getChildren().add(createButton("Info", () -> {
            Dialogs.information(primaryStage)
                    .title("Info")
                    .header("Elevator cleaning")
                    .text("To clean and service the electromagnetic coils in the bottom, " +
                            "it is necessary to jettison the access plate in the floor.")
                    .showAndWait();
            System.out.println("Info Dialog shown");
        }));

        // Warning
        container.getChildren().add(createButton("Warning", () -> {
            Dialogs.warning(primaryStage)
                    .title("Warning")
                    .header("Attention... danger")
                    .text("Automatic charges will now blow the explosive bolts in the floor plate unit. " +
                            "The plate will disengage from the floor in 5 seconds.")
                    .showAndWait();
            System.out.println("Warning Dialog shown");
        }));

        // Error
        container.getChildren().add(createButton("Error", () -> {
            Dialogs.error(primaryStage)
                    .title("Error")
                    .header("Please leave the elevator immediately")
                    .text("5-4-3-2-1...")
                    .showAndWait();
            System.out.println("Error Dialog shown");
        }));

        // Prompt
        container.getChildren().add(createButton("Prompt", () ->
                Dialogs.prompt(primaryStage)
                        .title("Prompt")
                        .header("This is a prompt dialog.")
                        .showAndWait()
                        .ifPresentOrElse(answer -> System.out.println("Answer: " + answer), () -> System.out.println("No answer"))
        ));

        // Input
        container.getChildren().add(createButton("Input", () ->
                Dialogs.input(primaryStage)
                        .title("Input")
                        .header("This is an input dialog.")
                        .string("txt", "enter text", () -> "dflt")
                        .integer("integer", "enter number", () -> 0)
                        .integer("integer from 4 to 7", "enter number", () -> 0,
                                i -> i >= 4 && i <= 7 ? Optional.empty() : Optional.of(i + " is not between 4 and 7"))
                        .comboBox("list", "choose one", () -> "Maybe", String.class, List.of("Yes", "No", "Maybe"))
                        .checkBox("bool", "Yes or No:", () -> false, "yes")
                        .chooseFile("file", "File", () -> null, FileDialogMode.OPEN, true, List.of(new FileChooser.ExtensionFilter("all files", "*")))
                        .chooseFile("directory", "Directory", () -> null, FileDialogMode.DIRECTORY, true, List.of(new FileChooser.ExtensionFilter("all files", "*")))
                        .comboBoxEx("listEx",
                                "edit items and choose one",
                                s -> Dialogs.prompt(primaryStage).title("Edit item").defaultValue("%s", s).build().showAndWait().orElse(null),
                                () -> Dialogs.prompt(primaryStage).title("Edit item").build().showAndWait().orElse(null),
                                (cb, item) -> true,
                                Objects::toString,
                                () -> null,
                                String.class,
                                List.of("1", "2", "3"))
                        .showAndWait()
                        .ifPresentOrElse(answer -> System.out.println("Answer: " + answer), () -> System.out.println("No answer"))
        ));

        // Options
        container.getChildren().add(createButton("Options", () ->
                Dialogs.options(primaryStage)
                        .options(CsvIo.getOptions())
                        .title("Options")
                        .header("This is an options dialog.")
                        .showAndWait()
                        .ifPresentOrElse(answer -> System.out.println("Answer: " + answer), () -> System.out.println("No answer"))
        ));

        // Wizard
        container.getChildren().add(createButton("Wizard", () ->
                Dialogs.wizard()
                        .title("Database Connection Wizard")
                        .page("start",
                                Dialogs.informationPane()
                                        .header("Create new database connection")
                                        .text("""
                                                This wizard helps you to define a new database connection.
                                                
                                                You will need the following information:
                                                - the vendor or manufacturer name of your database system
                                                - the server name and port
                                                """))
                        .page("dbms",
                                Dialogs.inputPane()
                                        .header("Choose your Database from the list below.")
                                        .radioList("rdbms", "Database", () -> null, String.class, List.of("H2", "PostgreSQL", "MySQL"))
                        )
                        .showAndWait()
                        .ifPresentOrElse(answer -> System.out.println("Answer: " + answer), () -> System.out.println("No answer"))
        ));

        StackPane root = new StackPane(container);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Dialogs");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static Button createButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setOnAction(e -> action.run());
        btn.setPrefWidth(120.0);
        return btn;
    }
}
