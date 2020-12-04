package com.dua3.utility.db;

import com.dua3.utility.data.Pair;
import com.dua3.utility.options.Option;
import com.dua3.utility.options.OptionSet;
import com.dua3.utility.options.OptionValues;
import com.dua3.utility.text.TextUtil;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * JDBC driver information.
 */
public class JdbcDriverInfo {

    /** The driver name. */
    public final String name;
    /** The driver's class name. */
    public final String className;
    /** URL prefix used by this driver (used to identify the correct driver to use when accessing a database URL). */
    public final String urlPrefix;
    /** The URL scheme for JDBC connections used by this driver. */
    public final String urlScheme;
    /** Linke to the vendor's driver webpage. */
    public final String link;
    /** This driver's options. */
    public final OptionSet options;

    /** constructor. */
    public JdbcDriverInfo(String name, String className, String urlPrefix, String urlScheme, String link) {
        this.name = name;
        this.className = className;
        this.urlPrefix = urlPrefix;
        this.link = link;

        Pair<String, List<Option<?>>> parsed = Option.parseScheme(urlScheme);
        this.urlScheme = parsed.first;
        this.options = new OptionSet(parsed.second);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Get driver description text.
     * @return the driver description
     */
    public String description() {
        return String.format(Locale.ROOT,
                "%s%n  driver class : %s%n  URL prefix   : %s%n  URL scheme   : %s%n  vendor link  : %s%n%s%n",
                name,
                className,
                urlPrefix,
                urlScheme,
                link,
                options);
    }

    public String getUrl(OptionValues values) {
        return TextUtil.transform(urlScheme, 
                s -> Objects.toString(
                        values.get(options.getOption(s).orElseThrow(() -> new NoSuchElementException("No value present"))).get(), ""));
    }
}
