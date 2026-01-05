package com.dua3.utility.samples.fx;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.dua3.utility.application.ApplicationUtil;
import com.dua3.utility.application.UiMode;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.fx.controls.Controls;
import com.dua3.utility.fx.controls.LabelPlacement;
import com.dua3.utility.fx.controls.PromptMode;
import com.dua3.utility.i18n.I18N;
import com.dua3.utility.io.CsvIo;
import com.dua3.utility.fx.controls.Dialogs;
import com.dua3.utility.fx.controls.FileDialogMode;
import com.dua3.utility.fx.controls.LayoutUnit;
import com.dua3.utility.lang.SystemInfo;
import com.dua3.utility.text.MessageFormatter;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URI;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sample Application.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class FxDialogSample extends Application {

    static {
        ApplicationUtil.initApplicationPreferences(Preferences.userNodeForPackage(FxDialogSample.class));
        ApplicationUtil.addDarkModeListener(FxDialogSample::setDarkMode);
        ApplicationUtil.setUiMode(UiMode.SYSTEM_DEFAULT);
    }

    private static final String ANSWER = "Answer: ";
    private static final String NO_ANSWER = "No answer";
    private static final FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter("all files", "*.*", "*");

    private static void println(Object o) {
        System.out.println(o);
    }

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Constructor.
     */
    public FxDialogSample() { /* nothing to do */ }

    @Override
    public void start(Stage primaryStage) {
        VBox container = new VBox();

        // UiMode
        ComboBox<UiMode> comboUiMode = Controls.comboBox(List.of(UiMode.values()))
                .onChange(ApplicationUtil::setUiMode)
                .initialValue(ApplicationUtil.getUiMode())
                .maxWidth(Double.MAX_VALUE)
                .build();
        HBox.setHgrow(comboUiMode, Priority.ALWAYS);
        Label lblUiMode = new Label("UI Mode");
        lblUiMode.setPrefWidth(120);
        HBox hboxUiMode = new HBox(8, lblUiMode, comboUiMode);
        hboxUiMode.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(hboxUiMode);

        // LabelPlacement
        ComboBox<LabelPlacement> comboLabelPlacement = new ComboBox<>(FXCollections.observableArrayList(LabelPlacement.values()));
        comboLabelPlacement.setValue(LabelPlacement.BEFORE);
        comboLabelPlacement.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboLabelPlacement, Priority.ALWAYS);
        Label lblLabelPlacement = new Label("Label Placement");
        lblLabelPlacement.setPrefWidth(120);
        HBox hboxLabelPlacement = new HBox(8, lblLabelPlacement, comboLabelPlacement);
        hboxLabelPlacement.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(hboxLabelPlacement);

        // Locale
        List<Locale> locales = Stream.of(
                        "ar", "bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "hi", "hr", "hu", "id", "it", "ja", "ko", "lt", "lv", "mt", "nl", "pl", "pt", "ro", "ru", "sk", "sl", "sv", "th", "tr", "zh", "zh_Hant"
                )
                .map(Locale::forLanguageTag)
                .sorted(Comparator.comparing(Locale::getDisplayName))
                .collect(Collectors.toList());
        ComboBox<Locale> comboLocale = Controls.comboBox(locales)
                .onChange(this::setLocale)
                .initialValue(Locale.getDefault())
                .maxWidth(Double.MAX_VALUE)
                .build();
        HBox.setHgrow(comboLocale, Priority.ALWAYS);
        Label lblLocale = new Label("Locale");
        lblLocale.setPrefWidth(120);
        HBox hboxLocale = new HBox(8, lblLocale, comboLocale);
        hboxLocale.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(hboxLocale);

        // About
        container.getChildren().add(createButton("About", () -> {
            Dialogs.about(primaryStage)
                    .title("About…")
                    .applicationName("Dialog Sample")
                    .version("v 0.1")
                    .copyright("(c) 2021 Axel Howind")
                    .mail(URI.create("mailto:info@example.com"))
                    .expandableContent(SystemInfo.getSystemInfo().formatted())
                    .modality(Modality.NONE)
                    .build()
                    .show();
            println("About Dialog shown");
        }));

        // Confirmation
        container.getChildren().add(createButton("Confirmation", () -> {
            var dlg = Dialogs.alert(primaryStage, AlertType.CONFIRMATION, MessageFormatter.standard())
                    .title("Elevator cleaning")
                    .header("Good for you!")
                    .text("You've decided to clean the elevator.")
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    println(ANSWER + newVal);
                }
            });
        }));

        // Information
        container.getChildren().add(createButton("Info", () -> {
            Dialogs.alert(primaryStage, AlertType.INFORMATION, MessageFormatter.standard())
                    .title("Info")
                    .header("Elevator cleaning")
                    .text("To clean and service the electromagnetic coils in the bottom, " +
                            "it is necessary to jettison the access plate in the floor.")
                    .modality(Modality.NONE)
                    .build()
                    .show();
            println("Info Dialog shown");
        }));

        // Warning
        container.getChildren().add(createButton("Warning", () -> {
            Dialogs.alert(primaryStage, AlertType.WARNING, MessageFormatter.standard())
                    .title("Warning")
                    .header("Attention... danger")
                    .text("Automatic charges will now blow the explosive bolts in the floor plate unit. " +
                            "The plate will disengage from the floor in 5 seconds.")
                    .modality(Modality.NONE)
                    .build()
                    .show();
            println("Warning Dialog shown");
        }));

        // Error
        container.getChildren().add(createButton("Error", () -> {
            Dialogs.alert(primaryStage, AlertType.ERROR, MessageFormatter.standard())
                    .title("Error")
                    .header("Please leave the elevator immediately")
                    .text("5-4-3-2-1...")
                    .modality(Modality.NONE)
                    .build()
                    .show();
            println("Error Dialog shown");
        }));

        // Prompt
        container.getChildren().add(createButton("Prompt", () -> {
            var dlg = Dialogs.prompt(primaryStage, MessageFormatter.standard())
                    .title("Prompt")
                    .header("This is a prompt dialog.")
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    println(ANSWER + newVal);
                }
            });
        }));

        // Password
        container.getChildren().add(createButton("Password", () -> {
            var dlg = Dialogs.prompt(primaryStage, MessageFormatter.standard())
                    .mode(PromptMode.PASSWORD)
                    .title("Password Prompt")
                    .header("This is a password prompt dialog.")
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    println(ANSWER + newVal);
                }
            });
        }));

        // File selection
        container.getChildren().add(createButton("File selection", () ->
                Dialogs.chooseFile(primaryStage)
                        .showOpenDialog()
                        .ifPresentOrElse(answer -> println(ANSWER + answer), () -> println(NO_ANSWER))
        ));

        // Directory selection
        container.getChildren().add(createButton("Directory selection", () ->
                Dialogs.chooseDirectory(primaryStage)
                        .showDialog()
                        .ifPresentOrElse(answer -> println(ANSWER + answer), () -> println(NO_ANSWER))
        ));

        // Input
        container.getChildren().add(createButton("Input", () -> {
            var dlg = Dialogs.input(primaryStage, MessageFormatter.standard())
                    .title("Input")
                    .header("This is an input dialog.")
                    .labelPlacement(comboLabelPlacement.getValue())
                    .text("This is some text without label.")
                    .labeledText("static text", "This is some labeled text.")
                    .inputConstant("readonly", "readonly", "This is the value of the readonly field.")
                    .inputConstant("readonly2", "readonly date", LocalDate::now, LocalDate.class)
                    .inputString("txt", "enter text", () -> "dflt")
                    .inputText("longtext", "long text", () -> "")
                    .inputHidden("secret1", "A")
                    .inputHidden("secret2", "B")
                    .inputInteger("integer", "enter number", () -> 0L)
                    .inputInteger("integer from 4 to 7", "enter number [4-7]", () -> null,
                            i -> i != null && i >= 4 && i <= 7 ? Optional.empty() : Optional.of(i + " is not between 4 and 7"))
                    .inputDecimal("decimal", "decimal", () -> null)
                    .inputComboBox("list", "choose one", () -> "Maybe", String.class, List.of("Yes", "No", "Maybe"))
                    .verticalSpace(10, LayoutUnit.PIXELS)
                    .inputCheckBox("bool", "Yes or No:", () -> false, "yes")
                    .inputFile("file", "File", () -> null, FileDialogMode.OPEN, true, List.of(FILTER_ALL_FILES))
                    .inputFile("directory", "Directory", () -> null, FileDialogMode.DIRECTORY, true, List.of(FILTER_ALL_FILES))
                    .inputComboBoxEx(
                            "listEx",
                            "edit items and choose one",
                            s -> Dialogs.prompt(primaryStage, MessageFormatter.standard()).title("Edit item").defaultValue("%s", s).build().showAndWait().orElse(null),
                            () -> Dialogs.prompt(primaryStage, MessageFormatter.standard()).title("Add item").build().showAndWait().orElse(null),
                            (cb, item) -> true,
                            Objects::toString,
                            () -> null,
                            String.class,
                            List.of("1", "2", "3"),
                            v -> v != null ? Optional.empty() : Optional.of("Select an item or enter a new one"))
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    println(ANSWER + newVal);
                }
            });
        }));

        // Options
        container.getChildren().add(createButton("Options", () -> {
            var dlg = Dialogs.options(primaryStage, MessageFormatter.standard())
                    .options(CsvIo.getOptions())
                    .title("Options")
                    .header("This is an options dialog.")
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    println(ANSWER + newVal);
                }
            });
        }));

        // Wizard
        container.getChildren().add(createButton("Wizard", () -> {
            var dlg = Dialogs.wizard(primaryStage)
                    .title("Database Connection Wizard")
                    .page("start",
                            Dialogs.alertPane(AlertType.INFORMATION, MessageFormatter.standard())
                                    .header("Create new database connection")
                                    .text("""
                                            This wizard helps you to define a new database connection.
                                            
                                            You will need the following information:
                                            - the vendor or manufacturer name of your database system
                                            - the server name and port
                                            """))
                    .page("dbms",
                            Dialogs.inputDialogPane(MessageFormatter.standard())
                                    .header("Choose your Database from the list below.")
                                    .inputRadioList("rdbms", "Database", () -> null, String.class, List.of("H2", "PostgreSQL", "MySQL"))
                    )
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    println(ANSWER + newVal);
                }
            });
        }));

        StackPane root = new StackPane(container);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Dialogs");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setLocale(Locale locale) {
        Locale.setDefault(locale);
        I18N.resetInstance(locale);
    }

    private static Button createButton(String text, Runnable action) {
        return Controls.button().text(text).action(action).maxWidth(Double.MAX_VALUE).build();
    }

    private static void setDarkMode(boolean enabled) {
        Supplier<Theme> themeSupplier = enabled ? PrimerDark::new : PrimerLight::new;
        PlatformHelper.runAndWait(() -> setUserAgentStylesheet(themeSupplier.get().getUserAgentStylesheet()));
    }

}
