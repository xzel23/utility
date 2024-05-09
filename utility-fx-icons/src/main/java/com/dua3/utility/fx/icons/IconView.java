package com.dua3.utility.fx.icons;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StringConverter;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A JavaFX control to display icons.
 */
public class IconView extends Control {
    private static final Logger LOG = LogManager.getLogger(IconView.class);

    private static final String DEFAULT_ICON_IDENTIFIER = "";
    private static final int DEFAULT_ICON_SIZE = 10;
    private static final Paint DEFAULT_ICON_COLOR = Paint.valueOf("BLACK");
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    static {
        List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
        styleables.add(StyleableProperties.ICON_IDENTIFIER);
        styleables.add(StyleableProperties.ICON_COLOR);
        styleables.add(StyleableProperties.ICON_SIZE);
        STYLEABLES = Collections.unmodifiableList(styleables);
    }

    private final StackPane pane;
    private final StyleableStringProperty iconIdentifier = new StyleableStringProperty(DEFAULT_ICON_IDENTIFIER) {
        @Override
        public CssMetaData<IconView, String> getCssMetaData() {
            return StyleableProperties.ICON_IDENTIFIER;
        }

        @Override
        public Object getBean() {
            return IconView.this;
        }

        @Override
        public String getName() {
            return "iconIdentifier";
        }

    };
    private final StyleableObjectProperty<Paint> iconColor = new StyleableObjectProperty<>(DEFAULT_ICON_COLOR) {
        @Override
        public CssMetaData<IconView, Paint> getCssMetaData() {
            return StyleableProperties.ICON_COLOR;
        }

        @Override
        public Object getBean() {
            return IconView.this;
        }

        @Override
        public String getName() {
            return "iconColor";
        }

    };
    private final StyleableIntegerProperty iconSize = new StyleableIntegerProperty(DEFAULT_ICON_SIZE) {
        @Override
        public CssMetaData<IconView, Number> getCssMetaData() {
            return StyleableProperties.ICON_SIZE;
        }

        @Override
        public Object getBean() {
            return IconView.this;
        }

        @Override
        public String getName() {
            return "iconSize";
        }
    };

    public IconView() {
        this.pane = new StackPane();
        getChildren().setAll(pane);
        iconIdentifier.addListener((v, o, n) -> setIcon(n));
    }

    public IconView(String iconId, int size, Paint color) {
        this();
        setIcon(iconId);
        setIconSize(size);
        setIconColor(color);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    private void setIcon(String iconId) {
        int size = getIconSize();
        Paint color = getIconColor();

        Optional<Icon> icon = IconUtil.iconFromName(iconId);
        Icon icon1;
        if (icon.isPresent()) {
            icon1 = icon.get();
        } else {
            LOG.warn("icon not found: {}", iconId);
            icon1 = IconUtil.emptyIcon();
        }

        icon1.iconSizeProperty().bind(iconSize);
        icon1.iconColorProperty().bind(iconColor);

        iconSize.set(size);
        iconColor.set(color);

        pane.getChildren().setAll(icon1.node());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new IconViewSkin(this);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return STYLEABLES;
    }

    @Override
    public String toString() {
        return iconIdentifier.get();
    }

    public String getIconIdentifier() {
        return iconIdentifier.getValue();
    }

    public void setIconIdentifier(String icon) {
        iconIdentifier.set(icon);
    }

    public Paint getIconColor() {
        return iconColor.getValue();
    }

    public void setIconColor(Paint color) {
        iconColor.set(color);
    }

    public int getIconSize() {
        return iconSize.get();
    }

    public void setIconSize(int size) {
        iconSize.set(size);
    }

    private static final class StyleableProperties {
        public static final CssMetaData<IconView, Number> ICON_SIZE =
                new CssMetaData<>("-fx-icon-size",
                        SizeConverter.getInstance(), 16.0) {

                    @Override
                    public boolean isSettable(IconView icon) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(IconView iv) {
                        return iv.iconSize;
                    }
                };
        public static final CssMetaData<IconView, Paint> ICON_COLOR =
                new CssMetaData<>("-fx-icon-color",
                        PaintConverter.getInstance(), Color.BLACK) {

                    @Override
                    public boolean isSettable(IconView node) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(IconView iv) {
                        return iv.iconColor;
                    }
                };
        public static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        private static final CssMetaData<IconView, String> ICON_IDENTIFIER =
                new CssMetaData<>("-fx-icon-identifier",
                        StringConverter.getInstance(), "") {

                    @Override
                    public boolean isSettable(IconView iv) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<String> getStyleableProperty(IconView iv) {
                        return iv.iconIdentifier;
                    }
                };

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Node.getClassCssMetaData());
            styleables.add(ICON_SIZE);
            styleables.add(ICON_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    private static class IconViewSkin extends SkinBase<IconView> {
        protected IconViewSkin(IconView control) {
            super(control);
            consumeMouseEvents(false);
        }
    }
}
