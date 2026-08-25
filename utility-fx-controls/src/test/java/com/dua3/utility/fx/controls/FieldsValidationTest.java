package com.dua3.utility.fx.controls;

import com.dua3.utility.text.MessageFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class FieldsValidationTest extends FxTestBase {

    private static final String REPORT_FOLDER = "reportFolder";
    private static final String WRITE_REPORT = "writeReport";
    private static final String REPORT_FOLDER_REQUIRED = "Report folder is required.";

    @SuppressWarnings("unchecked")
    @Test
    void revalidatesSiblingControlsAfterADependencyChanges() throws Exception {
        runOnFxThreadAndWait(() -> {
            Fields.Field<Path> reportFolder = Fields.field(REPORT_FOLDER, () -> Path.of("reports"), Fields.alwaysValid());
            Fields.Field<Boolean> writeReport = Fields.field(
                    WRITE_REPORT,
                    () -> true,
                    (write, fields) -> Boolean.TRUE.equals(write) && fields.<Path>getField(REPORT_FOLDER).getValue() == null
                            ? Optional.of(REPORT_FOLDER_REQUIRED)
                            : Optional.empty(),
                    REPORT_FOLDER
            );
            Fields.wireFields(writeReport, reportFolder);

            GridBuilder builder = new GridBuilder(null, MessageFormatter.standard());
            builder.startRow(MessageFormatter.literal("Reporting"))
                    .inputCheckBox(WRITE_REPORT, MessageFormatter.empty(), writeReport.defaultValueAsBoolean(), MessageFormatter.literal("Write report"), writeReport.validate())
                    .inputFolder(REPORT_FOLDER, MessageFormatter.empty(), reportFolder.defaultValue(), false, reportFolder.validate())
                    .endRow();

            Grid grid = builder.build();
            InputControl<Boolean> writeReportControl = (InputControl<Boolean>) control(grid, WRITE_REPORT);
            InputControl<Path> reportFolderControl = (InputControl<Path>) control(grid, REPORT_FOLDER);
            Grid.Meta<?> row = grid.data().stream().filter(meta -> meta.control instanceof InputControlContainer).findFirst().orElseThrow();

            reportFolderControl.set(null);
            assertFalse(reportFolderControl.isValid());
            assertEquals(REPORT_FOLDER_REQUIRED, row.errorMarker.getTooltip().getText());

            writeReportControl.set(false);

            assertTrue(writeReportControl.isValid());
            assertTrue(reportFolderControl.isValid());
            assertTrue(row.control.isValid());
            assertEquals("", row.errorMarker.getText());
            assertNull(row.errorMarker.getTooltip());
        });
    }

    private static InputControl<?> control(Grid grid, String id) {
        return grid.data().stream().filter(meta -> id.equals(meta.id)).findFirst().orElseThrow().control;
    }
}
