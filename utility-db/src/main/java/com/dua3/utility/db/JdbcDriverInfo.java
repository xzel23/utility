package com.dua3.utility.db;

import com.dua3.utility.options.Option;
import com.dua3.utility.options.OptionSet;
import com.dua3.utility.options.OptionValues;
import com.dua3.utility.text.TextUtil;

import java.util.Objects;

public class JdbcDriverInfo {

    public final String name;
    public final String className;
    public final String urlPrefix;
    public final String urlScheme;
    public final String link;
    public final OptionSet options;

    public JdbcDriverInfo(String name, String className, String urlPrefix, String urlScheme, String link) {
        this.name = name;
        this.className = className;
        this.urlPrefix = urlPrefix;
        this.urlScheme = urlScheme;
        this.link = link;

        this.options = new OptionSet(Option.parseScheme(urlScheme));
    }

    @Override
    public String toString() {
        return name;
    }
    
    public String description() {
        return String.format(
                "%s%n  driver class : %s%n  URL prefix   : %s%n  URL scheme   : %s%n  vendor link  : %s%n%s%n",
                name,
                className,
                urlPrefix,
                urlScheme,
                link,
                options);
    }

    public String getUrl(OptionValues values) {
        return TextUtil.transform(urlScheme, s -> Objects.toString(values.get(options.getOption(s).orElseThrow()).get(), ""));
    }
}
