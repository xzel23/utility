# com.dua3.utility

Some libraries with utility classes.

| library       | description               | exported module    | required modules                                         |
|---------------|---------------------------|--------------------|----------------------------------------------------------|
| utility       | general purpose utilities | dua3_utility       | java.logging                                             |
| utility-db    | database utilities        | dua3_utility.db    | dua3_utility java.logging java.sql dua3_utility          |
| utility-swing | swing utilities           | dua3_utility.swing | dua3_utility java.datatransfer java.desktop java.logging |

## License

This library is developed by Axel Howind and available under the MIT License. Refer to the accompanying file [LICENSE](LICENSE) for details.

## Source

Source code is available at https://gitlab.com/com.dua3/lib/utility.git.

## Requirements

 - building: JDK 11 or later 
 - using: JDK 8 or later

## Including the library

Binaries are available on Jcenter and Maven Central Repository.

### Maven

Replace `${utility_version}` with the current version.

        <dependency>
            <groupId>com.dua3.utility</groupId>
            <artifactId>utility</artifactId>
            <version>${utility_version}</version>
        </dependency>
        <dependency>
            <groupId>com.dua3.utility</groupId>
            <artifactId>utility-db</artifactId>
            <version>${utility_version}</version>
        </dependency>
        ...

### Gradle

Replace `${utility_version}` with the current version.

    dependencies {
        compile        "com.dua3.utility:utility:${utility_version}"
        compile        "com.dua3.utility:utility-db:${utility_version}"
        ...        
    }

## Logging

Logging is done through JUL (java.util.logging).
 
 - If you use a logging framework such as logback in your __application__, please use that framework's JUL bridge to reroute logging messages. 
 - If your project is a library, don't try to reroute log messages - you cannot tell which framework the user of your library will prefer using, so please don't make his (and your own) life harder by forcing the user of your library to use the framework of *your* choice.
 
IMHO, using a logging framework in *libraries* is in most cases not necessary anymore in Java 8+ since log messages can now be formatted using lambdas that are only called when logging on that level is enabled. I have had more than enough trouble with trying to put libraries using different versions of log4j, SLF4J, logback, commopns.logging, and more into a single project that I will not integrate any logging framework into my libraries, so please don't even ask for it. Most advanced logging framework now have some sort of JUL bridge, so that there shouldn't be any issues with this. 

## Changes

### Version 6.5

 - promote ProgressTracker from incubator to concurrent package 
 - SwingProcessView: more informative exception messages

### Version 6.4.2

 - Font: fix fontspec parsing
 
### Version 6.4.1

 - Font: fix fontspec parsing when font size does not containing a fractional part; do not output fractional part in fontspec if unneeded
 
### Version 6.4

 - fontspec color part changed to CSS format (might break app that rely on the fontspec format)
 - new constructor Font(String fontspec)
 - Font valueOf(String) expects arg in CSS format (might be breaking)

### Version 6.3.1

 - Color: new methods isOpaque(), toRgba(), toArgb(), toCss()

### Version 6.3

 - mark Font.FontDef final
 - cache Font.fontspec
 - Font.similar()
 - fix invalid CSS for Fontdef if underline or strikethrough is set explicitly to false
 - fix exception and debug messages using default locale instead if Locale.ROOT

### Version 6.2.4

 - interface ProgressTracker, incubating class SwingProgressView

### Version 6.2.3

 - fix IOUtil.replaceExtension(), IOUtil.stripExtension() when path contains multiple dots

### Version 6.2.2

 - IOUtil.lines(InputStream, Charset)
 - always use "\r\n" as line delimiter in CSV files (as per RFC 4180)
 - minor improvements and code cleanups
 - change Font.isUnderlined() to isUnderline()
 
### Version 6.2.1

 - new Utility class StreamUtil
 - TextUtil.indent()
 
### Version 6.2

 - DomUtil and BatchCollector moved out of incubator
 - new method FontDef.getCssStyle() returning CSS-compatible font definition
 
### Version 6.1.1

 - new method IOUtil.openStream(URI)
 - new overload IOUtil.read(URI, Charset)
 
### Version 6.1

 - now included in Maven Central Repository
 - add instructions for setting up Maven
 - fix instructions for setting up Gradle
 - many code cleanups
 - test code compatible with JDK 8 (but JDK 11+ is still required for the build)
 - utility method TextUtil.prettyPrint(...) for pretty-printing org.w3c.dom.Document 
 - utility methods IOUtil.format(...) for output of org.w3c.dom.Document 
 - Font.FontDef: added equals(), hashCode(), toString()
 - added new package com.dua3.utility.incubator for incubating features
 - new Utility class DomUtil (incubating)
 - new class BatchCollector (incubating)
 
### Version 6.0.3

 - fix missing root in getUnixPath()
 
### Version 6.0.2

 - support Path as argument to FileType.read()/write() methods
 
### Version 6.0.1

 - new method FileType.isCompound() is used to excldue file types from lookup by extension (default implementation returns false; see javadoc for details)
 
### Version 6.0

 - LangUtil.uncheckedXXX() methods don't wrap uncheckedExceptions
 - relaxed some method parameters from String to CharSequence
 - code cleanup
 - improve unit test coverage

### Version 5.3.4.1, 5.3.4.2, 5.3.4.3

 - Java 8 compatibility fixes: remove usage of API not available in Java 8
 
### Version 5.3.4

 - restore Java 8 compatibility (needed for my customer's project)
 - update gradle to 6.3 (for JDK 14 support)
 - updatze SpotBugs and SpotBugs plugin
 
### Version 5.3.3

 - Pair.of(Map.Entry)
 - add split support to stopwatch class
 
### Version 5.3.2

 - IOUtil.toUnixPath()
 
### Version 5.3.1

 - TextUtil.containsAnyOf()
 - TextUtil.containsNoneOf()
 
### Version 5.3

 - IOUtil.toUri()
 - IOUtil.toPath()
 
### Version 5.2

 - Removal of utility-json
 
### Version 5.1.3

 - TextUtil.transform(String, Pair<String,String>...)
 
### Version 5.1.2

 - added methods in LangUtil to resolve localised resources
 - minor cleanups
 
### Version 5.1.1

 - Methods in IOUtil that take a filename as a String argument have been changed to return correct results when instead of a filename a path is given.
  
### Version 5.1

 - add Zip class
 - add TextUtil.generateMailToLink()
 
### Version 5.0

 - update to gradle 6.0.1
 - add Platform class

### Version 5-BETA6

 - update to gradle 6.0; fix gradle 7 compatibility warnings
 
### Version 5-BETA5

 - fix DataUtil.convert() to LocalDateTime conversion
 - added some unit tests
 
### Version 5-BETA4

 - Option.fileOption(): added OpenMode

### Version 5-BETA3

 - code cleanup
 - LangUtil.enumSet()
 
### Version 5-BETA2

 - Update spotbugs to 4.0.0-beta4 to be able to compile using JDK 13.
 - `DataUtil.convert()`: be more strict with boolean conversions. Converting a `String s` to `Boolean` will yield `null` if `s` is `null`, `Boolean.TRUE` if and only if `s` equals ignoring case `"true"`, `Boolean.FALSE` if and only if `s` equals ignoring case `"false"`. In all other cases `IllegalArgumentException` is thrown.
 
### Version 5-BETA1

 - fixed many small issues, typos (also in method names), so that I decided to also bump the major version
 - module names changed to dua3_util*

### Version 4.1.0-BETA12

 - `DataUtil.convert()` overload for converting collections to list
 - `DataUtil.convertCollection()` for converting collections to collection of arbitrary type
 - (4.1.0-BETA12a): fix for `DataUtil.convert()` - don't bail out early if source type is `Double` or `Float`

### Version 4.1.0-BETA11

 - `LangUtil.uncheckedSupplier()`

### Version 4.1.0-BETA10

 - new: `ÌOUtil.getInputStream(Object)` and `ÌOUtil.getOutputStream(Object)`
 
### Version 4.1.0-BETA9

 - new: `MathUtil.roundingOperation(int n, RoundingMode mode)` for bulk 
rounding, with support for all types of `java.math.RoundingMode`
 - Better documentation for `MathUtil.round()` and `MathUtil.roundToPrecision()`
 - fixed some compilation warnings

### Version 4.1.0-BETA8

 - `DataUtil.collect()` and `DataUtil.collectArray()`
 
### Version 4.1.0-BETA7

 - LangUtil: `public static <E extends Exception> void check(boolean condition, Supplier<E> exceptionSupplier) throws E`
 
### Version 4.1.0-BETA6

 - new class `MappingIterator` and method `DataUtil.map(Iterator<T>, Function<T,U>)`

### Version 4.1.0-BETA5

 - new class `FilterIterator` and method `DataUtil.filter(Iterator<T>, Predicate<T>)`

### Version 4.1.0-BETA4

 - Support converting Long, Integer to Double, Float in DataUtil.convert...()-methods 

### Version 4.1.0-BETA3

 - Support converting String to LocalDate in the DataUtil.convert...()-methods
 
### Version 4.1.0-BETA2

 - `DataUtil.convert()` and `DataUtil.convertToArray()` for converting objects to other types. Conversion is done as follows:
    * if value is {@code null}, {@code null} is returned;
    * if the target class is assignment compatible, a simple cast is performed;
    * if the target class is `String`, `Object.toString()` is used;
    * if the target class is an integer type and the value is of type double, a conversion without loss of precision is tried;
    * if the target class provides a method `public static T valueOf(T)` and `value instanceof U`, that method is invoked;
    * if `useConstructor` is `true` and the target class provides a constructor taking a single argument of value's type, that constructor is used;
    * otherwise an exception is thrown.

 
### Version 4.0.8

- new class `DataUtil`, methods for data conversion 

### Version 4.0.7

- new helper method `TextUtil.align()` 

### Version 4.0.6

- __BREAKING:__ default separator for CSV changed from semicolon to comma to make it compliant with RFCc4180. To change the separator, use `CsvIo.getOptionValues(CsvIo.OPTION_SEPARATOR, ';')` when creating the `CsvReader`/`CsvWriter` instance.

### Version 4.0.5

- FileType: new static `read(path, class)` method

### Version 4.0.4

- IOUtil: add more conversion methods between URI, URL, and Path

### Version 4.0.3

- remove version file
- update spotbugs plugin
- cleanup build file
- IOUtil.getFileExtension(URI)

### Version 4.0.2

- new class `JsonUtil`:  loading JSON from URL/path

### Version 4.0.1

- `JdbcDataSource.setUser()` and `JdbcDataSource.setPassword()` support `null` argument to unset value.

### Version 4.0.0

- Change default date format to use a 4-digit year.
- utility-json.

__BETA 4__

- changed the build system to make life easier for me as developer :) - version information is stored in a file named `version` in the project root. This makes it possible to consistently update dependencies information automatically using a script.

__BETA 3__

- set dependency version information in gradle.properties file
- JdbcDriverInfo: create connection URL from option values

__BETA 2__

- don't use decimal grouping in CSV output
- `ValueChangeListener` support in `OptionValues`
- classes `FileType` and `OpenMode`
- fix CSV date formatting issues

__BETA 1__

- update gradle wrapper
- `CsvReader` and `CsvWriter` classes
- new classes `Option` (an option to control object behavior), `OptionSet` (a set of options to control object behavior), and `Options` (a set of options and corresponding values)
- new package `com.dua3.utility.data` with classes `TreeNode` (to build tree data structures) and `FileTreeNode` to create a tree of files and subdirectories of a directory
- moved the `Pair` and `Color` classes to the `com.dua3.utility.data` package
- renamed `Options` to `OptionValues`

### Version 3.1.4

- add `uses` declarations in `module-info.java` files (fix ServiceLoader issues when run from eclipse)
- spotbugs: use exclude filter for false positives; don't ignore failures

### Version 3.1.3

- Added methods to measure text in TextUtil. The concrete implementation is using either AWT or JavaFX and is loaded via a ServiceLoader. The AWT implementation is included in utility.swing whereas a JavaFX implementation is included in the fx.util subproject of the fx project.

### Version 3.1.2

- skipped

### Version 3.1.1

- Fix NamedParameterStatement throwing exception when using ojdbc8.jar with certain statements.

### Version 3.1.0

- The git repository moved to https://gitlab.com/com.dua3/lib/utility.git
- Added `NamedParameterStatement.getParameterInfo()` and `NamedParameterStatement.getParameterInfo(String)` to query parameter meta data (i.e. the corresponding SQL type)
- Added `NamedParameterStatement.getResultSet()`
- Added `NamedParameterStatement.getUpdateCount()`
- Added `DbUtil.toLocalTime()`
 
### VERSION 3.0.1

- changed license to MIT to make it possible to use in pre-V3 GPL projects

### VERSION 3.0

- BREAKING: package `com.dua3.utility.db` was moved to a separate library `utility.db`. You have to update dependencies and module-info.
- REMOVED: `Font.getAwtFont()` and `Font.getTextWidth()`; these introduced a dependency on java.desktop and can be easily replaced where needed.
- The utility.db library now include methods to dynamically load JDBC drivers. This allows to use jlink on applications that use non-modular JDBC-drivers.
- dependencies on these modules were removed from the main library, allowing for smaller `jlink` images: 
    - java.xml
    - java.desktop
    - java.sql
 
### version 2.2

- New class CryptUtil: encryption and decryption utility.
- TextUtil.base64Encode()
- TextUtil.base64Decode()
- *breaking change*: TextUtil.byteArrayToHex() has been renamed to byteArrayToHexString()
- TextUtil.hexStringToByteArrayToHex()
 
### version 2.1

- IOUtil.loadText(): helper method to load text files where the character encoding is not known.
 
### version 2.0

- Requires Java 11+
- Removed Swing and JavaFX.
