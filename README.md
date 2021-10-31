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

 - JDK 17 or later 

## Including the library

Binaries are available on Maven Central Repository.

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

## Using the BuildInfo class in Gradle builds

Add this to your `build.gradle` to include a `build.properties` file in your JAR:

    // === generate BuildInfo ===
    def generateBuildInfo = tasks.register('generateBuildInfo') {
        // do not cache the buildinfo
        outputs.upToDateWhen { false }
    
        // create build.properties
        doLast {
            def resourcesDir = sourceSets.main.output.resourcesDir
            resourcesDir.mkdirs()
    
            def buildTime = OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC"))
            def contents = "build.time=${buildTime}\nbuild.version=${version}\n"
    
            def file = new File(resourcesDir, "build.properties")
            file.text = contents
        }
    }

Add this in your code to read the `build.properties` file and create a `BuildInfo` instance containinng version and build time information:

    public static final BuildInfo BUILD_INFO = BuildInfo.create(Main.class, "/build.properties");

## Changes

### 10 (to be released)

- **Java 17 is now required!**
- upgraded gradle to 7.2 for Java 17 support
- publish snapshots to snoatype snapshot repository
- geometry classes taking float parameters have been renamed to ...2f
- some classes (Pair, Vector2f, ...) have been converted to records
- updates to FileTreeNode to allow use as a base class
- added BuildInfo class
- add constants for different line ending sequences in TextUtil
- add command line option "--log-path-pattern" to LogUtil
- fix duplicate item output in LangUtil.surroundingItems() when regions overlap
- added several Font.withXXX()-methods to help quickly deriving fonts that differ only in one attribute 
- LangUtil.enumValues()
- options can be passed a handler that is executed when arguments.handle() is called
- many fixes and smaller improvements, added many Javadoc comments

**The following functionality has been removed because it is available in JDK 17**:
- TextUtil.byteArrayToHexString(): use HexFormat.of().formatHex()
- TextUtil.hexStringToByteArray(): use HexFormat.of().parseHex()

### 9.0.1

- add HtmlConverter.inlineTextDecorations()
- some JavaDoc corrections and completions
- update Gradle to 7.2-rc2 for JDK 17 compatibility (during compilation)

### 9.0

- migrate from bintray to sonatype 
- JDK 11+ required! It's finally time to dump Java 8 support. I won't put any more effort into supporting a Java version that has long reached EOL and is a maintenance burden because of its missing modularity support.
- build uses Gradle 7 to enable building on JDK 16
- removed usae of the JPMS Gradle plugin as it is not compatible with Gradle 7 and not needed anymore after dropping JDK 8 support and Gradle 7 adding modularity support.
- logback support has been replaced by log4j2 due to missing jigsaw suport in logback. This means if you have been using a Log4J to Logback bridge before, it's now time to do it the other way around.
- NamedParameterStatement supports many more data types; fixes for inserting null values.
- source parameter of CsvReader changed from String to URI
- `com.dua3.utility.cmd` and `com.dua3.utility.options` have been merged; there had been a lot of duplicated functionality, and the implementation in the `cmd` package was much cleaner; the result is again located in `com.dua3.utility.options` but the code is mostly based on what had been in the `cmd` package. Classes have been renamed because they are no more intended to be used only for command line arguments.
- move from JFrog Bintray to Sonatype OSSRH
- ImageUtil.load() does not return Optional
- made Image an interface
- removed the Image.write() method
- added Image.getArgb()
- some changes and fixes to AffineTransformation
- introduce class Dimension2d
- changes to FontUtil interface; rename FontUtil.getTextBounds() to getTextDimension(); loadFont replaced by loadFonts()
- introduce math.geometry package and contained classes

### 8.2.2

- remove bintray support
- publish builds to GitLab Packages (private access)
- TODO: publish to Mavencentral

### 8.2.1

 - fix: FontDef.merge() only worked if color was set

### 8.2.0

 - add double overload to MathUtil.clamp()
 - fix typo: ProgressTracker.State.SCHEDULED (low possibility of breaking client code)
 - (Swing): remove MigLayout from runtime dependencies 
 - (Swing): make SwingProgressView horizontally resizable
 - (Swing): fix resize behaviour of SwingLogPane

### Version 8.1.6

 - RichText.stylesAt()
 - RichText.runAt()
 - RichText.runs()
 - RichText.valueOf() with styles parameter

### Version 8.1.5

 - LangUtil.surroundingItems()

### Version 8.1.4

 - CmdParser.errorMessage()

### Version 8.1.3

 - command line parser improvements & bugfixes

### Version 8.1.2
 
 - fix XmlUtil.parse(String)
 - add XmlUtil.parse(URI)

### Version 8.1.1

 - non-throwing (at least when correctly configured, otherwise RTE), reusable XmlUtil.defaultInstance()

### Version 8.1
 
 - remove the Text class
 - some changes in XmlUtil

### Version 8.0.2

 - XmlUtil.xpath()

### Version 8.0.1

 - change argument of XmlUtil.format() from Document to its superclass Node

### Version 8

 - DomUtil renamed to XmlUtil, methods are instance methods now so that instaneces using different DocumentBuilder and Transformer implementations can be used
 - moved all XML related methods from IoUtil and TextUtil to XmlUtil
 - SwingUtil: added helper methods for adding basic drag and drop support

### Version 7.0.8

 - IOUtil: added IOUtil.copyAllBytes()
 - IOUtil: removed inaccessble Method StreamSupplier.lines()

### Version 7.0.7

 - NamedParameterStatement: don't throw exception in constructor if parameter info can not be queried; fix typo in method name

### Version 7.0.6

 - NamedParameterStatement: support null values

### Version 7.0.5

 - RichText performance improvements

### Version 7.0.4

 - FIX: possible negative index in SwingLogPane
 - javadoc
 - remove a stray unused interface

### Version 7.0.3
 
 - FIX: do not generate interleaved tags in HtmlConverter

### Version 7.0.2

 - added missing javadoc in many places
 - removed obsolete classes and methods (those should have been in use anyway and can in any case be easily replaced with the newer versions/replacements)

### Version 7.0.1

 - small fixes and cleanups

### Version 7

 - moved/renamed command line argument parsing classes to their own package, many changes and additions
 - move Font.FontDef to upper level
 - RichTextBuilder: methods push(Style)/pop(Style), implement Comparable; add textEquals(); bugfixes
 - Many RichText/TextAttributes/Style related refactorings
 - new classes HtmlConverter, StyledDocumentConverter, AnsiConverter to convert RichText to different formats
 - removed old StyledDocumentBuilder class
 - several changes in to the classes under util.text
 - removed ToStringBuilder class as it was never really used and most IDEs can generate toString() automatically
 - overload IOUtil.replaceExtension for Path
 - DataUtil.asFunction() to use Map instances as Function
 - TextUtil.appendHtmlEscapedCharacters(); mm2pt(); pt2mm()

### Version 6.7.1

 - update SpotBugs and remove rule filter
 - logging: removing log listeners from JUL/Logback/System.out

### Version 6.7

 - classes SwingLogPane and LogBuffer
 - RingBuffer implements the Collection interface 
 - RingBuffer.subList()
 
### Version 6.6.3.1

 - release to solve problems with build - no code changes

### Version 6.6.3

 - FIX: should be "font-size", not "size"

### Version 6.6.2

 - fix spelling of TextUtil.getTextBounds()
 - remove deprecated Font.isUnderlined()
 
### Version 6.6.1

 - FIX: Color.isOpaque() returned wrong value
 - FIX: Font.getCssStyle(): returned invalid value "regular" instead of "normal" for font-style
 - Color.isTransparent()
 - more work on command line parser

### Version 6.6

 - added simple command line parser class
 
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
