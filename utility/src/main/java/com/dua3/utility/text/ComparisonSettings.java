package com.dua3.utility.text;

import java.util.function.Function;

/**
 * Settings for comparing {@link RichText} instances.
 *
 * @param fontMapper mapping of font family names, for example "ArialMT" -> "Arial
 * @param ignoreCase whether to ignore difference in character case
 * @param ignoreFontFamily whether to ignore differences in font family name
 * @param ignoreFontSize whether to ignore font sizes
 * @param ignoreTextColor whether to ignore text color
 * @param ignoreUnderline whether to ignore underline decorations
 * @param ignoreStrikeThrough whether to ignore strikethrough decorations
 * @param ignoreFontWeight where to ignore font weight
 * @param ignoreItalic whether to ignore difference in italics
 */
public record ComparisonSettings(
        Function<String, String> fontMapper,
        boolean ignoreCase,
        boolean ignoreFontFamily,
        boolean ignoreFontSize,
        boolean ignoreTextColor,
        boolean ignoreUnderline,
        boolean ignoreStrikeThrough,
        boolean ignoreFontWeight,
        boolean ignoreItalic
) {
    /**
     * Default comparison settings; all ignore... flags are set to false and no font mapping is used.
     * @return the default comparison settings as described above
     */
    public static ComparisonSettings defaultSettings() {
        return new Builder().build();
    }

    /**
     * Returns a new instance of the Builder class, allowing the creation of custom comparison settings.
     *
     * @return a new instance of the Builder class
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder class for creating instances of ComparisonSettings.
     */
    public static class Builder {
        Function<String, String> fontMapper = Function.identity();
        boolean ignoreCase;
        boolean ignoreFontFamily;
        boolean ignoreFontSize;
        boolean ignoreTextColor;
        boolean ignoreUnderline;
        boolean ignoreStrikeThrough;
        boolean ignoreBold;
        boolean ignoreItalic;

        /**
         * This class represents a Builder used to construct objects.
         *
         * <p>Provides a default constructor to create a new Builder object.
         *
         * <p>Example usage:
         * <pre>{@code
         * Builder builder = new Builder();
         * }</pre>
         */
        Builder() {
        }

        /**
         * Sets the font mapper function used to map font names.
         *
         * @param fontMapper the font mapper function to be set
         * @return the Builder object for method chaining
         */
        public Builder setFontMapper(Function<String, String> fontMapper) {
            this.fontMapper = fontMapper;
            return this;
        }

        /**
         * Sets whether the method should ignore case when comparing texts.
         *
         * @param ignoreCase a boolean value indicating whether to ignore case (true) or not (false)
         * @return the Builder object for method chaining
         */
        public Builder setIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        /**
         * Sets whether the method should ignore the font family when comparing texts.
         *
         * @param ignoreFontFamily a boolean value indicating whether to ignore the font family (true) or not (false)
         * @return the Builder object for method chaining
         */
        public Builder setIgnoreFontFamily(boolean ignoreFontFamily) {
            this.ignoreFontFamily = ignoreFontFamily;
            return this;
        }

        /**
         * Sets whether the method should ignore the font size when comparing texts.
         *
         * @param ignoreFontSize a boolean value indicating whether to ignore the font size (true) or not (false)
         * @return the Builder object for method chaining
         */
        public Builder setIgnoreFontSize(boolean ignoreFontSize) {
            this.ignoreFontSize = ignoreFontSize;
            return this;
        }

        /**
         * Sets whether the method should ignore the text color when comparing texts.
         *
         * @param ignoreTextColor a boolean value indicating whether to ignore the text color (true) or not (false)
         * @return the Builder object for method chaining
         */
        public Builder setIgnoreTextColor(boolean ignoreTextColor) {
            this.ignoreTextColor = ignoreTextColor;
            return this;
        }

        public Builder setIgnoreUnderline(boolean ignoreUnderline) {
            this.ignoreUnderline = ignoreUnderline;
            return this;
        }

        /**
         * Sets whether the method should ignore the strike-through style when comparing texts.
         *
         * @param ignoreStrikeThrough a boolean value indicating whether to ignore the strike-through style (true) or not (false)
         * @return the Builder object for method chaining
         */
        public Builder setIgnoreStrikeThrough(boolean ignoreStrikeThrough) {
            this.ignoreStrikeThrough = ignoreStrikeThrough;
            return this;
        }

        /**
         * Sets whether the method should ignore the bold style when comparing texts.
         *
         * @param ignoreBold a boolean value indicating whether to ignore the bold style (true) or not (false)
         * @return the Builder object for method chaining
         */
        public Builder setIgnoreBold(boolean ignoreBold) {
            this.ignoreBold = ignoreBold;
            return this;
        }

        /**
         * Sets whether the method should ignore the italic style when comparing texts.
         *
         * @param ignoreItalic a boolean value indicating whether to ignore the italic style (true) or not (false)
         * @return the Builder object for method chaining
         */
        public Builder setIgnoreItalic(boolean ignoreItalic) {
            this.ignoreItalic = ignoreItalic;
            return this;
        }

        /**
         * Builds the ComparisonSettings object with the specified fontMapper and ignore settings.
         *
         * @return the built ComparisonSettings object
         */
        public ComparisonSettings build() {
            return new ComparisonSettings(
                    fontMapper,
                    ignoreCase,
                    ignoreFontFamily,
                    ignoreFontSize,
                    ignoreTextColor,
                    ignoreUnderline,
                    ignoreStrikeThrough,
                    ignoreBold,
                    ignoreItalic
            );
        }
    }

}
