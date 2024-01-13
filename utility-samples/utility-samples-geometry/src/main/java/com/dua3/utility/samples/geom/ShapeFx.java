package com.dua3.utility.samples.geom;

import com.dua3.utility.math.geometry.Arc2f;
import com.dua3.utility.math.geometry.ClosePath2f;
import com.dua3.utility.math.geometry.Curve2f;
import com.dua3.utility.math.geometry.Line2f;
import com.dua3.utility.math.geometry.MoveTo2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.stage.Stage;

public class ShapeFx extends Application {

    @Override
    public void start(Stage stage) {
        Pane root = new StackPane();
        Scene scene = new Scene(root, 300, 200);

        Path path1 = createJavaFXPath();
        path1.setStroke(Paint.valueOf("blue"));
        path1.setStrokeWidth(5.0f);
        root.getChildren().add(path1);

        Path path2 = createPath();
        path2.setStroke(Paint.valueOf("lightgrey"));
        path2.setStrokeWidth(3.0f);
        root.getChildren().add(path2);

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

    private static Path createPath() {
        Path2f path = Path2f.builder()
                .moveTo(Vector2f.of(0.0f, 0.0f))
                .lineTo(Vector2f.of(70.0f, 0.0f))
                .curveTo(Vector2f.of(100.0f, 0.0f), Vector2f.of(120.0f, 60.0f))
                .lineTo(Vector2f.of(175.0f, 55.0f))
                .arcTo(Vector2f.of(50.0f, 50.0f), Vector2f.of(50.0f, 50.0f), 0.0f, false, false)
                .build();
        return convertToJavaFxPath(path);
    }

    private static Path convertToJavaFxPath(Path2f path) {
        Path jfxPath = new Path();
        path.segments().forEach(segment -> {
            if (segment instanceof MoveTo2f s) {
                jfxPath.getElements().add(new MoveTo(s.end().x(), s.end().y()));
            } else if (segment instanceof Line2f s) {
                jfxPath.getElements().add(new LineTo(s.end().x(), s.end().y()));
            } else if (segment instanceof Curve2f s) {
                int n = s.numberOfControls();
                jfxPath.getElements().add(switch (n) {
                    case 3 -> new QuadCurveTo(
                            s.control(1).x(), s.control(1).y(),
                            s.control(2).x(), s.control(2).y()
                    );
                    case 4 -> new CubicCurveTo(
                            s.control(1).x(), s.control(1).y(),
                            s.control(2).x(), s.control(2).y(),
                            s.control(3).x(), s.control(3).y()
                    );
                    default -> throw new IllegalArgumentException("Unsupported number of control points: " + n);
                });
            } else if (segment instanceof Arc2f s) {
                jfxPath.getElements().add(new ArcTo(s.rx(), s.ry(), s.angle(), s.control(1).x(), s.control(1).y(), false, false));
            } else if (segment instanceof ClosePath2f c) {
                jfxPath.getElements().add(new ClosePath());
            } else {
                throw new IllegalArgumentException("Unsupported segment type: " + segment.getClass().getName());
            }
        });
        return jfxPath;
    }

    public static void main(String[] args) {
        launch(args);
    }
}