// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import com.dua3.utility.data.ImageUtilProvider;
import com.dua3.utility.i18n.I18NProvider;
import com.dua3.utility.io.FileType;
import com.dua3.utility.text.FontUtilProvider;

/**
 * The com.dua3.utility module provides utility classes for various purposes, such as
 * concurrent programming, data manipulation, I/O operations, language utilities,
 * mathematical operations, options handling, text manipulation, and XML processing.
 * <p>
 * This module exports the following packages:
 * <ul>
 * <li>com.dua3.utility.concurrent: Utility classes for concurrent programming.
 * <li>com.dua3.utility.data: Utility classes for data manipulation.
 * <li>com.dua3.utility.io: Utility classes for I/O operations.
 * <li>com.dua3.utility.i18n: Utility classes for internationalization.
 * <li>com.dua3.utility.lang: Utility classes for language utilities.
 * <li>com.dua3.utility.math: Utility classes for mathematical operations.
 * <li>com.dua3.utility.math.geometry: Utility classes for geometric operations.
 * <li>com.dua3.utility.options: Utility classes for handling options.
 * <li>com.dua3.utility.text: Utility classes for text manipulation.
 * <li>com.dua3.utility.xml: Utility classes for XML processing.
 * </ul>
 * This module requires the following modules:
 * <ul>
 * <li>requires static com.dua3.cabe.annotations: A static requirement for the com.dua3.cabe.annotations module.
 * <li>requires java.xml: A requirement for the java.xml module.
 * <li>requires org.apache.logging.log4j: A requirement for the org.apache.logging.log4j module.
 * </ul>
 * This module uses the following services:
 * <ul>
 * <li>com.dua3.utility.text.FontUtil: A service used by the TextUtil class for font handling.
 * <li>com.dua3.utility.io.FileType: A service used for file type handling.
 * <li>com.dua3.utility.data.ImageUtil: A service used for image manipulation.
 * </ul>
 */
open module com.dua3.utility {
    exports com.dua3.utility.concurrent;
    exports com.dua3.utility.data;
    exports com.dua3.utility.io;
    exports com.dua3.utility.i18n;
    exports com.dua3.utility.lang;
    exports com.dua3.utility.math;
    exports com.dua3.utility.math.geometry;
    exports com.dua3.utility.options;
    exports com.dua3.utility.text;
    exports com.dua3.utility.spi;
    exports com.dua3.utility.xml;
    exports com.dua3.utility.awt;

    requires static com.dua3.cabe.annotations;

    requires java.xml;
    requires static java.desktop;
    requires org.apache.logging.log4j;

    uses FileType;
    uses I18NProvider;
    uses FontUtilProvider;
    uses ImageUtilProvider;
}
