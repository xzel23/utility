package com.dua3.utility.fx.icons;

import javafx.beans.binding.Bindings;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StringConverter;
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
 * Represents a custom JavaFX control that displays an icon.
 * The IconView control can be used to display icons in various JavaFX applications.
 * It provides methods for setting and getting the icon identifier, size, and color.
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

    /**
     * Creates an instance of IconView.
     */
    public IconView() {
        this.pane = new StackPane();
        getChildren().setAll(pane);
        iconIdentifier.addListener((v, o, n) -> setIcon(n));
    }

    /**
     * Constructs an IconView instance with the specified icon identifier, size, and color.
     *
     * @param iconId the identifier of the icon to be displayed
     * @param size the size of the icon
     * @param color the color of the icon
     */
    public IconView(String iconId, int size, Paint color) {
        this();
        setIcon(iconId);
        setIconSize(size);
        setIconColor(color);
    }

    /**
     * Retrieves the CSS metadata for the IconView class.
     *
     * @return a list of CssMetaData objects associated with the IconView class.
     */
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

        iconSize.set(size);
        iconColor.set(color);

        Bindings.bindBidirectional(iconSize, icon1.iconSizeProperty());
        Bindings.bindBidirectional(iconColor, icon1.iconColorProperty());

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

    /**
     * Returns the icon identifier.
     * The icon identifier is a String value that represents the unique identifier of the icon.
     *
     * @return the icon identifier
     */
    public String getIconIdentifier() {
        return iconIdentifier.getValue();
    }

    /**
     * Sets the identifier of the icon to be displayed in the IconView.
     *
     * @param icon the identifier of the icon
     */
    public void setIconIdentifier(String icon) {
        iconIdentifier.set(icon);
    }

    /**
     * Returns the color of the icon.
     *
     * @return the color of the icon
     */
    public Paint getIconColor() {
        return iconColor.getValue();
    }

    /**
     * Sets the color of the icon.
     *
     * @param color the color to set
     */
    public void setIconColor(Paint color) {
        iconColor.set(color);
    }

    /**
     * Get the size of the icon.
     *
     * @return the size of the icon in pixels
     */
    public int getIconSize() {
        return iconSize.get();
    }

    /**
     * Sets the size of the icon.
     *
     * @param size the new size of the icon
     */
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
        public static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = List.of(ICON_SIZE, ICON_COLOR);
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
    }

    private static class IconViewSkin extends SkinBase<IconView> {
        protected IconViewSkin(IconView control) {
            super(control);
            consumeMouseEvents(false);
        }
    }
}
