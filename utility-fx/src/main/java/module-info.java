import com.dua3.utility.data.ImageUtilProvider;
import com.dua3.utility.fx.FxFontUtilProvider;
import com.dua3.utility.fx.FxImageUtilProvider;
import com.dua3.utility.text.FontUtilProvider;
import org.jspecify.annotations.NullMarked;

/**
 * Utility module for JavaFX.
 * <p>
 * This module provides utility classes for working with fonts and images in JavaFX.
 * <p>
 * The module exports the package {@code com.dua3.fx.util} and opens it for reflection.
 * <p>
 * The module provides the following services:
 * <ul>
 *   <li>{@code FontUtil} implementation: {@code FxFontUtil}</li>
 *   <li>{@code ImageUtil} implementation: {@code FxImageUtil}</li>
 * </ul>
 * <p>
 * The module requires the following modules:
 * <ul>
 *   <li>{@code com.dua3.utility}</li>
 *   <li>{@code java.prefs}</li>
 *   <li>{@code javafx.controls}</li>
 *   <li>{@code org.apache.logging.log4j}</li>
 *   <li>{@code com.dua3.cabe.annotations}</li>
 * </ul>
 */
@NullMarked
open module com.dua3.utility.fx {
    exports com.dua3.utility.fx;

    provides FontUtilProvider
            with FxFontUtilProvider;

    provides ImageUtilProvider
            with FxImageUtilProvider;

    requires com.dua3.utility;
    requires static com.dua3.utility.logging;
    requires static javafx.controls;
    requires static javafx.graphics;
    requires org.apache.logging.log4j;
    requires static org.jspecify;    requires java.desktop;
}
