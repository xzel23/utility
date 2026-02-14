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
import com.dua3.utility.i18n.I18NInfo;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

/**
 * Sample Application.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class FxDialogSample extends Application {

    private static final Logger LOG = LogManager.getLogger(FxDialogSample.class);

    private static final Locale LOCALE_WHEN_STARTED = Locale.getDefault();

    static {
        ApplicationUtil.initApplicationPreferences(Preferences.userNodeForPackage(FxDialogSample.class));
        ApplicationUtil.addDarkModeListener(FxDialogSample::setDarkMode);
        ApplicationUtil.setUiMode(UiMode.SYSTEM_DEFAULT);
    }

    private static final String ANSWER = "dua3.utility.samples.fx.answer";
    private static final String NO_ANSWER = "dua3.utility.samples.fx.no_answer";
    private static final FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter("all files", "*.*", "*");

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LOG.info("starting FxDialogSample");
        launch(args);
    }

    /**
     * Constructor.
     */
    public FxDialogSample() { /* nothing to do */ }

    private @Nullable ComboBox<UiMode> comboUiMode;
    private @Nullable ComboBox<LabelPlacement> comboLabelPlacement;
    private @Nullable ComboBox<Locale> comboLocale;

    @Override
    public void start(Stage primaryStage) {
        VBox container = new VBox();

        // UiMode
        UiMode currentUiMode = comboUiMode == null ? ApplicationUtil.getUiMode() : comboUiMode.getValue();
        comboUiMode = Controls.comboBox(List.of(UiMode.values()))
                .onChange(ApplicationUtil::setUiMode)
                .initialValue(currentUiMode)
                .maxWidth(Double.MAX_VALUE)
                .build();
        HBox.setHgrow(comboUiMode, Priority.ALWAYS);
        Label lblUiMode = new Label(i18n().get("dua3.utility.samples.fx.ui_mode"));
        lblUiMode.setPrefWidth(120);
        HBox hboxUiMode = new HBox(8, lblUiMode, comboUiMode);
        hboxUiMode.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(hboxUiMode);

        // LabelPlacement
        LabelPlacement currentLabelPlacement = comboLabelPlacement == null ? LabelPlacement.BEFORE : comboLabelPlacement.getValue();
        comboLabelPlacement = new ComboBox<>(FXCollections.observableArrayList(LabelPlacement.values()));
        comboLabelPlacement.setValue(currentLabelPlacement);
        comboLabelPlacement.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboLabelPlacement, Priority.ALWAYS);
        Label lblLabelPlacement = new Label(i18n().get("dua3.utility.samples.fx.label_placement"));
        lblLabelPlacement.setPrefWidth(120);
        HBox hboxLabelPlacement = new HBox(8, lblLabelPlacement, comboLabelPlacement);
        hboxLabelPlacement.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(hboxLabelPlacement);

        // Locale
        try {
            List<Locale> locales = I18NInfo.load(getClass())
                    .stream()
                    .map(I18NInfo::languageTags)
                    .flatMap(Collection::stream)
                    .map(Locale::forLanguageTag)
                    .sorted(Comparator.comparing(locale -> locale.getDisplayName(LOCALE_WHEN_STARTED)))
                    .toList();
            Locale currentLocale = comboLocale == null ? Locale.getDefault() : comboLocale.getValue();
            comboLocale = Controls.comboBox(locales)
                    .onChange(this::setLocale)
                    .initialValue(currentLocale)
                    .maxWidth(Double.MAX_VALUE)
                    .stringRenderer(locale -> locale.getDisplayName(LOCALE_WHEN_STARTED), "")
                    .build();
            HBox.setHgrow(comboLocale, Priority.ALWAYS);
            Label lblLocale = new Label(i18n().get("dua3.utility.samples.fx.locale"));
            lblLocale.setPrefWidth(120);
            HBox hboxLocale = new HBox(8, lblLocale, comboLocale);
            hboxLocale.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().add(hboxLocale);
        } catch (IOException e) {
            LOG.warn("cannot load locales", e);
        }

        // About
        Button btnAbout = createButton(i18n().get("dua3.utility.samples.fx.about"), () -> {
            Dialogs.about(primaryStage)
                    .title(i18n().get("dua3.utility.samples.fx.about.title"))
                    .applicationName(i18n().get("dua3.utility.samples.fx.about.app_name"))
                    .version(i18n().get("dua3.utility.samples.fx.about.version"))
                    .copyright(i18n().get("dua3.utility.samples.fx.about.copyright"))
                    .mail(URI.create("mailto:info@example.com"))
                    .expandableContent(SystemInfo.getSystemInfo().formatted())
                    .modality(Modality.NONE)
                    .build()
                    .show();
            LOG.info("About Dialog shown");
        });
        container.getChildren().add(btnAbout);

        // Confirmation
        Button btnConfirmation = createButton(i18n().get("dua3.utility.samples.fx.confirmation"), () -> {
            var dlg = Dialogs.alert(primaryStage, AlertType.CONFIRMATION, MessageFormatter.standard())
                    .title(i18n().get("dua3.utility.samples.fx.confirmation.title"))
                    .header(i18n().get("dua3.utility.samples.fx.confirmation.header"))
                    .text(i18n().get("dua3.utility.samples.fx.confirmation.text"))
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    LOG.info("{}", i18n().format(ANSWER, newVal));
                }
            });
        });
        container.getChildren().add(btnConfirmation);

        // Information
        Button btnInfo = createButton(i18n().get("dua3.utility.samples.fx.info"), () -> {
            Dialogs.alert(primaryStage, AlertType.INFORMATION, MessageFormatter.standard())
                    .title(i18n().get("dua3.utility.samples.fx.info.title"))
                    .header(i18n().get("dua3.utility.samples.fx.info.header"))
                    .text(i18n().get("dua3.utility.samples.fx.info.text"))
                    .modality(Modality.NONE)
                    .build()
                    .show();
            LOG.info("Info Dialog shown");
        });
        container.getChildren().add(btnInfo);

        // Warning
        Button btnWarning = createButton(i18n().get("dua3.utility.samples.fx.warning"), () -> {
            Dialogs.alert(primaryStage, AlertType.WARNING, MessageFormatter.standard())
                    .title(i18n().get("dua3.utility.samples.fx.warning.title"))
                    .header(i18n().get("dua3.utility.samples.fx.warning.header"))
                    .text(i18n().get("dua3.utility.samples.fx.warning.text"))
                    .modality(Modality.NONE)
                    .build()
                    .show();
            LOG.info("Warning Dialog shown");
        });
        container.getChildren().add(btnWarning);

        // Error
        Button btnError = createButton(i18n().get("dua3.utility.samples.fx.error"), () -> {
            Dialogs.alert(primaryStage, AlertType.ERROR, MessageFormatter.standard())
                    .title(i18n().get("dua3.utility.samples.fx.error.title"))
                    .header(i18n().get("dua3.utility.samples.fx.error.header"))
                    .text(i18n().get("dua3.utility.samples.fx.error.text"))
                    .modality(Modality.NONE)
                    .build()
                    .show();
            LOG.info("Error Dialog shown");
        });
        container.getChildren().add(btnError);

        // Prompt
        Button btnPrompt = createButton(i18n().get("dua3.utility.samples.fx.prompt"), () -> {
            var dlg = Dialogs.prompt(primaryStage, MessageFormatter.standard())
                    .title(i18n().get("dua3.utility.samples.fx.prompt.title"))
                    .header(i18n().get("dua3.utility.samples.fx.prompt.header"))
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    LOG.info("{}", i18n().format(ANSWER, newVal));
                }
            });
        });
        container.getChildren().add(btnPrompt);

        // Password
        Button btnPassword = createButton(i18n().get("dua3.utility.samples.fx.password"), () -> {
            var dlg = Dialogs.prompt(primaryStage, MessageFormatter.standard())
                    .mode(PromptMode.PASSWORD)
                    .title(i18n().get("dua3.utility.samples.fx.password.title"))
                    .header(i18n().get("dua3.utility.samples.fx.password.header"))
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    LOG.info("{}", i18n().format(ANSWER, newVal));
                }
            });
        });
        container.getChildren().add(btnPassword);

        // File selection
        Button btnFileSelection = createButton(i18n().get("dua3.utility.samples.fx.file_selection"), () ->
                Dialogs.chooseFile(primaryStage)
                        .showOpenDialog()
                        .ifPresentOrElse(answer -> LOG.info("{}", i18n().format(ANSWER, answer)), () -> LOG.info("{}", i18n().get(NO_ANSWER)))
        );
        container.getChildren().add(btnFileSelection);

        // Directory selection
        Button btnDirectorySelection = createButton(i18n().get("dua3.utility.samples.fx.directory_selection"), () ->
                Dialogs.chooseDirectory(primaryStage)
                        .showDialog()
                        .ifPresentOrElse(answer -> LOG.info("{}", i18n().format(ANSWER, answer)), () -> LOG.info("{}", i18n().get(NO_ANSWER)))
        );
        container.getChildren().add(btnDirectorySelection);

        // Input
        Button btnInput = createButton(i18n().get("dua3.utility.samples.fx.input"), () -> {
            var dlg = Dialogs.input(primaryStage, MessageFormatter.standard())
                    .title(i18n().get("dua3.utility.samples.fx.input.title"))
                    .header(i18n().get("dua3.utility.samples.fx.input.header"))
                    .labelPlacement(comboLabelPlacement.getValue())
                    .section(1, i18n().get("dua3.utility.samples.fx.input.section.1"))
                    .text(i18n().get("dua3.utility.samples.fx.input.text"))
                    .section(2, i18n().get("dua3.utility.samples.fx.input.section.1.1"))
                    .labeledText(i18n().get("dua3.utility.samples.fx.input.labeled_text"), i18n().get("dua3.utility.samples.fx.input.labeled_text.value"))
                    .section(3, i18n().get("dua3.utility.samples.fx.input.section.1.1.1"))
                    .inputConstant("readonly", i18n().get("dua3.utility.samples.fx.input.readonly"), i18n().get("dua3.utility.samples.fx.input.readonly.value"))
                    .inputConstant("readonly2", i18n().get("dua3.utility.samples.fx.input.readonly2"), LocalDate::now)
                    .section(1, i18n().get("dua3.utility.samples.fx.input.section.2"))
                    .inputString("txt", i18n().get("dua3.utility.samples.fx.input.txt"), () -> "dflt")
                    .inputText("longtext", i18n().get("dua3.utility.samples.fx.input.longtext"), () -> "")
                    .inputHidden("secret1", "A")
                    .inputHidden("secret2", "B")
                    .inputInteger("integer", i18n().get("dua3.utility.samples.fx.input.integer"), () -> 0L)
                    .inputInteger("integer from 4 to 7", i18n().get("dua3.utility.samples.fx.input.integer_range"), () -> null,
                            i -> i != null && i >= 4 && i <= 7 ? Optional.empty() : Optional.of(i18n().format("dua3.utility.samples.fx.input.error.between_4_and_7", i)))
                    .inputDecimal("decimal", i18n().get("dua3.utility.samples.fx.input.decimal"), () -> null)
                    .inputComboBox("list", i18n().get("dua3.utility.samples.fx.input.list"), () -> "Maybe", List.of("Yes", "No", "Maybe"))
                    .verticalSpace(10, LayoutUnit.PIXELS)
                    .inputCheckBox("bool", i18n().get("dua3.utility.samples.fx.input.bool"), () -> false, i18n().get("dua3.utility.samples.fx.input.bool.yes"))
                    .inputFile("file", i18n().get("dua3.utility.samples.fx.input.file"), () -> null, FileDialogMode.OPEN, true, List.of(FILTER_ALL_FILES))
                    .inputFile("directory", i18n().get("dua3.utility.samples.fx.input.directory"), () -> null, FileDialogMode.DIRECTORY, true, List.of(FILTER_ALL_FILES))
                    .inputComboBoxEx(
                            "listEx",
                            i18n().get("dua3.utility.samples.fx.input.list_ex"),
                            s -> Dialogs.prompt(primaryStage, MessageFormatter.standard()).title(i18n().get("dua3.utility.samples.fx.input.edit_item")).defaultValue("%s", s).build().showAndWait().orElse(null),
                            () -> Dialogs.prompt(primaryStage, MessageFormatter.standard()).title(i18n().get("dua3.utility.samples.fx.input.add_item")).build().showAndWait().orElse(null),
                            (cb, item) -> true,
                            Objects::toString,
                            () -> null,
                            List.of("1", "2", "3"),
                            v -> v != null ? Optional.empty() : Optional.of(i18n().get("dua3.utility.samples.fx.input.error.select_item")))
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    LOG.info("{}", i18n().format(ANSWER, newVal));
                }
            });
        });
        container.getChildren().add(btnInput);

        // Options
        Button btnOptions = createButton(i18n().get("dua3.utility.samples.fx.options"), () -> {
            var dlg = Dialogs.options(primaryStage, MessageFormatter.standard())
                    .options(CsvIo.getOptions())
                    .title(i18n().get("dua3.utility.samples.fx.options.title"))
                    .header(i18n().get("dua3.utility.samples.fx.options.header"))
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    LOG.info("{}", i18n().format(ANSWER, newVal));
                }
            });
        });
        container.getChildren().add(btnOptions);

        // Wizard
        Button btnWizard = createButton(i18n().get("dua3.utility.samples.fx.wizard"), () -> {
            var dlg = Dialogs.wizard(primaryStage)
                    .title(i18n().get("dua3.utility.samples.fx.wizard.title"))
                    .page("start",
                            Dialogs.alertPane(AlertType.INFORMATION, MessageFormatter.standard())
                                    .header(i18n().get("dua3.utility.samples.fx.wizard.start.header"))
                                    .text(i18n().get("dua3.utility.samples.fx.wizard.start.text")))
                    .page("dbms",
                            Dialogs.inputDialogPane(MessageFormatter.standard())
                                    .header(i18n().get("dua3.utility.samples.fx.wizard.dbms.header"))
                                    .inputRadioList("rdbms", i18n().get("dua3.utility.samples.fx.wizard.dbms.rdbms"), () -> null, List.of("H2", "PostgreSQL", "MySQL"))
                    )
                    .modality(Modality.NONE)
                    .build();
            dlg.show();
            dlg.resultProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    LOG.info("{}", i18n().format(ANSWER, newVal));
                }
            });
        });
        container.getChildren().add(btnWizard);

        StackPane root = new StackPane(container);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Dialogs");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static I18N i18n() {
        return I18NInstance.get();
    }

    private void setLocale(Locale locale) {
        if (!Objects.equals(locale, Locale.getDefault())) {
            Locale.setDefault(locale);
            I18N.init("", locale);
            PlatformHelper.runLater(() -> start((Stage) comboLocale.getScene().getWindow()));
        }
    }

    private static Button createButton(String text, Runnable action) {
        return Controls.button().text(text).action(action).maxWidth(Double.MAX_VALUE).build();
    }

    private static void setDarkMode(boolean enabled) {
        Supplier<Theme> themeSupplier = enabled ? PrimerDark::new : PrimerLight::new;
        PlatformHelper.runAndWait(() -> setUserAgentStylesheet(themeSupplier.get().getUserAgentStylesheet()));
    }

}
