package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.QuadCurveTo;
import javafx.stage.Stage;
import javafx.scene.shape.Path;

/**
 * The ShapeFx class extends the JavaFX Application class to create and display a window
 * with two different paths drawn on a pane. The paths are created using different methods,
 * demonstrating the use of both JavaFX Path elements and a custom Path2f class with segmented paths.
 */
public class ShapeFx extends Application {

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 700, 400);

        Vector2f c = Vector2f.of(150, 150);
        float rMax = 100;
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            float r = rMax / 2 + rMax / 2 * i / segments;
            float phi = (float) (2 * Math.PI * i / segments);

            Vector2f start = c.add(Vector2f.of(r, 0));
            Vector2f end = c.add(Vector2f.of((float) (r * Math.cos(phi)), (float) (r * Math.sin(phi))));

            Path jfxPath = new Path(
                    new MoveTo(start.x(), start.y()),
                    new javafx.scene.shape.ArcTo(r, r, 0, end.x(), end.y(), i >= segments / 2, i != segments)
            );

            root.getChildren().add(jfxPath);
        }

        Path path1 = createJavaFXPath();
        path1.setStroke(Paint.valueOf("blue"));
        path1.setStrokeWidth(5.0f);
        root.getChildren().add(path1);

        Path path2 = FxUtil.convert(createPath());
        path2.setStroke(Paint.valueOf("lightgrey"));
        path2.setStrokeWidth(3.0f);
        root.getChildren().add(path2);

        c = Vector2f.of(350, 200);
        Vector2f p0 = c.translate(0, -50);
        Vector2f p1 = c.translate(0,  50);
        Vector2f r = Vector2f.of(150, 100);

        float angle = 0;

        Path path0 = new Path(
                new MoveTo(p0.x(), p0.y()),
                new ArcTo(r.x(), r.y(), angle, p1.x(), p1.y(), false, true)
        );
        path0.setStroke(Paint.valueOf("black"));
        path0.setStrokeWidth(1.0f);
        root.getChildren().add(path0);

        path1 = new Path(
                new MoveTo(p0.x(), p0.y()),
                new ArcTo(r.x(), r.y(), angle, p1.x(), p1.y(), true, true)
        );
        path1.setStroke(Paint.valueOf("blue"));
        path1.setStrokeWidth(1.0f);
        root.getChildren().add(path1);

        path2 = new Path(
                new MoveTo(p0.x(), p0.y()),
                new ArcTo(r.x(), r.y(), angle, p1.x(), p1.y(), false, false)
        );
        path2.setStroke(Paint.valueOf("red"));
        path2.setStrokeWidth(1.0f);
        root.getChildren().add(path2);

        Path path3 = new Path(
                new MoveTo(p0.x(), p0.y()),
                new ArcTo(r.x(), r.y(), angle, p1.x(), p1.y(), true, false)
        );
        path3.setStroke(Paint.valueOf("green"));
        path3.setStrokeWidth(1.0f);
        root.getChildren().add(path3);

        stage.setScene(scene);
        stage.setTitle("Shape");
        stage.show();
    }

    private static Path createJavaFXPath() {
        Path path = new Path();

        MoveTo moveTo = new MoveTo();
        moveTo.setX(0.0f);
        moveTo.setY(0.0f);

        HLineTo hLineTo = new HLineTo();
        hLineTo.setX(70.0f);

        QuadCurveTo quadCurveTo = new QuadCurveTo();
        quadCurveTo.setX(120.0f);
        quadCurveTo.setY(60.0f);
        quadCurveTo.setControlX(100.0f);
        quadCurveTo.setControlY(0.0f);

        LineTo lineTo = new LineTo();
        lineTo.setX(175.0f);
        lineTo.setY(55.0f);

        ArcTo arcTo = new ArcTo();
        arcTo.setX(50.0f);
        arcTo.setY(50.0f);
        arcTo.setRadiusX(50.0f);
        arcTo.setRadiusY(50.0f);
        arcTo.setXAxisRotation(0.0f);
        arcTo.setLargeArcFlag(false);
        arcTo.setSweepFlag(false);

        path.getElements().add(moveTo);
        path.getElements().add(hLineTo);
        path.getElements().add(quadCurveTo);
        path.getElements().add(lineTo);
        path.getElements().add(arcTo);
        return path;
    }

    private static Path2f createPath() {
        return Path2f.builder()
                .moveTo(Vector2f.of(0.0f, 0.0f))
                .lineTo(Vector2f.of(70.0f, 0.0f))
                .curveTo(Vector2f.of(100.0f, 0.0f), Vector2f.of(120.0f, 60.0f))
                .lineTo(Vector2f.of(175.0f, 55.0f))
                .arcTo(Vector2f.of(50.0f, 50.0f), Vector2f.of(50.0f, 50.0f), 0.0f, false, false)
                .build();
    }
}