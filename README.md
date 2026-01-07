# com.dua3.utility

[![MIT License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Language](https://img.shields.io/badge/language-Java-blue.svg?style=flat-square)](https://github.com/topics/java)
[![Javadoc](https://img.shields.io/badge/docs-javadoc-blue.svg)](https://xzel23.github.io/utility/)
[![build](https://github.com/xzel23/utility/actions/workflows/CI.yml/badge.svg)](https://github.com/xzel23/utility/actions/workflows/CI.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=xzel23_utility&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=xzel23_utility)

Some libraries with utility classes.

| library         | description                                         | exported module          |
|-----------------|-----------------------------------------------------|--------------------------|
| utility         | general purpose utilities                           | com.dua3.utility         |
| utility-db      | database utilities                                  | com.dua3.utility.db      |
| utility-fx      | JavaFX utilities                                    | com.dua3.utility.fx      |
| utility-logging | logging utilities and simple logging implementation | com.dua3.utility.logging |
| utility-samples | samples                                             | com.dua3.utility.samples |
| utility-swing   | swing utilities                                     | com.dua3.utility.swing   |

## License

This library is developed by Axel Howind and available under the MIT License. Refer to the accompanying
file [LICENSE](LICENSE) for details.

## Javadoc

The aggregated Javadoc for all modules is available on [GitHub Pages](https://xzel23.github.io/utility/).

## Source

Source code is available at https://github.com/xzel23/utility.

## Requirements

- JDK 21 or later, version 17 of the library requires JDK 17 or later (except for for JavaFX related modules that
  already require at least Java 21).
- Version 17 that still supports Java 17 will receive important bugfix updates until the next LTS release (Java 25) is
  released.
- The project uses Gradle toolchains to automatically download the required JDKs.
- JavaFX dependencies are managed by the JavaFX plugin.
- Building on Windows ARM is not supported because of missing support in Gradle and the toolchain resolver and
  JavaFX plugins.

## Gradle Tasks

### Inspecting Task Inputs and Outputs

The project includes a custom Gradle task called `showTaskIO` that displays the inputs and outputs of any specified
Gradle task. This is useful for:

- Debugging build issues
- Understanding task dependencies
- Analyzing what affects task up-to-date checks
- Optimizing build performance

To use this task, run:

```bash
./gradlew showTaskIO -PtaskName=<taskName>
```

Replace `<taskName>` with the name of the task you want to inspect (e.g., `compileJava`, `jar`, `test`).

The output includes:

- Input properties with their values
- Input files with existence status
- Output files with existence status
- Output directories with existence status

Example:

```bash
./gradlew showTaskIO -PtaskName=jar
```

If no task name is provided, it defaults to the `help` task.

## Using the library

The Binaries are available on Maven Central Repository.

Java 21+ is needed for the current Version, for Java 17 projects, use version 17.x of the library.

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

**Log4J-API** is used for logging.

You can use whatever logging implementation you want, for configuration refer to the Log4J documentation. You can also
look at the swing samples that use a SwingLogPane and route all logging output regardless of source (Log4J, SLF4J, JUL)
to the logging implementation.

### utility-logging

This project adds a small lightweight implementation that can be used instead of SLF4J SimpleLogger. It is intended as a
lightweight logging implementation in desktop applications or during development and does not support advanced features
such as log rotation or writing to a database.

Configuration is done by providing a file called `logging.properties` on the classpath.

* configure log level:
   ```
    logger.level = (ERROR|WARN|INFO|DEBUG|TRACE)(, <prefix>:(ERROR|WARN|INFO|DEBUG|TRACE))*
   ```
  setting ony the global level:
   ```
    logger.level = INFO
   ```
  setting global level and different levels for packages:
   ```
    logger.level = WARN, com.dua3.:DEBUG, my.current.project.:TRACE
   ```

* configure logging to console
   ```
    logger.console.stream = system.out|system.err
    logger.console.colored = true|false
   ```

* configure a log buffer (see below):
   ```
   logger.buffer = #entries
   ```

### Using SLF4J with different logging frameworks

Just to save time searching the internet for this:

* **JUL (java.util.logging)**

    * add "org.slf4j:jul-to-slf4j:<version>" to your dependencies

    * when using JPMS modules, add `requires jul.to.slf4j;` to your module-info.java

    * initialize the bridge handler:
       ```
       static {
           java.util.logging.LogManager.getLogManager().reset();
           SLF4JBridgeHandler.install();
       }
       ```

* **Log4J2**

    * add "org.apache.logging.log4j:log4j-to-slf4j:2.18.0" to your dependencies

### SwingLogPane (Swing-widget for log viewing)

If you want to show logs in a swing application, configure a log buffer and add a SwingLogPane to your UI. Have a look
at the code of `SwingComponentsSample` for details.

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

Add this in your code to read the `build.properties` file and create a `BuildInfo` instance containing version and build
time information:

    public static final BuildInfo BUILD_INFO = BuildInfo.create(Main.class, "/build.properties");

## Cabe

Starting with version 14, utility uses [JSpecify](https://jspecify.dev) annotations for all method parameters.

The resulting Jar files are instrumented using [Cabe](https://github.com/xzel23/cabe).

When you run your code with assertions disabled, virtually no overhead is introduced, as assertions are removed at
JVM level.

When running your code with exceptions enabled, parameters are checked for invalid null values and an AssertionError
will be generated when null is passed for a `@NonNull` annotated parameter. The assertion message contains the name of
the parameter.

## Benchmarks

The project includes JMH (Java Microbenchmark Harness) benchmarks to measure the performance of various utility methods.

### Running the benchmarks

To run the benchmarks, use the following Gradle command from the project root:

```
./gradlew jmh
```

This will execute all benchmark tests and generate a results file at `utility/build/results/jmh/results.txt`.

You can customize the benchmark execution by modifying the JMH configuration in the `utility/build.gradle.kts` file.

### Running the JavaFX samples

To run the JavaJF samples right from within your IDE, make sure to use a JDK that comes with JavaFX included like
Bellsoft Liberica JDK _FULL_ or Azul JDK _FX_. Otherwise you might see an error that the required JavaFX runtime classes
could not be loaded.

## Changes

### 20.4.3 (in development)

- IMPORTANT: deprecate for removal the I18NProvider interface; use `I18N.init(basename, locale)` instead
- fix: remove --enable-native-access=javafx.graphics from native-image configuration for base project that does not use
  JavaFX
- fix: required field message should only be shown when a required field marker is set and should contain the configured symbol
- support changing the locale at runtime
- add `LangUtil.asUnmodifiableMap(Properties)`
- declare `AbstractOptionBuilder.handler()` public

### 20.4.2

- Add the possibility to define the next page depending on button
- Add desktop support to `ApplicationUtil`
- Allow null arguments for `IoUtil.isURI()`, `isValidfileName()`, `isPortableFilename()`
- add `MessageFormatterArgs.nonI18N()`

### 20.4.1

- fix ellipsis displayed as question mark in english locales
- add page validation method to input dialogs

### 20.4.0

- IMPORTANT: deprecate for removal most methods in Controls that do not return a fluent builder
- remove flickering when displaying dialogs in dark mode
- DialogBuilder, AlertBuilder, WizardDialogBuilder: set default modality to `Modality.WINDOW_MODAL` instead of 
  `Modality.APPLICATION_MODAL`
- fix WizardDialog layout calculations
- changed input dialogs to display the required marker on the left and the error marker on the right, also a hint 
  for required fields is added automatically when required fields are present; added colors to required and error 
  markers and update on dark mode toggle
- add translations for dialogs and messages for all official EU languages, chinese, japanese, korean, and many others
- update GraalVM native image support configuration files
- add `Controls.comboBox()`, `Controls.comboBoxEx()`
- remove the standard `--log-level` command line switch
- add support for switching input dialog layout between label before or above input field
- improved support for CSS styling of dialogs and messages

### 20.3.0

- Switch from Gradle toolchains to jdkprovider plugin -> Library can now be built directly on Windows ARM
- Update native window decorations when entering dark/light mode
- Add GraalVM native image support configuration files
- Add dark mode support to FxDialogSample
- minor bugfixes and improvements

### 20.2.0

- added `Platform.isNativeImage()`
- added overloads taking `MessageFormatterArgs` to `AboutDialogBuilder`
- added `MessageFormatter.literal()`
- dependency updates and code cleanup

### 20.1.0

#### Added
- WriterOutputStream implementation to complement `ReaderInputStream`. (b851f47)
- Methods to parse PEM-encoded public keys and certificates. (fdf2dba)
- TaskProcessorEventDriven: `addTaskTimeout()` to apply timeouts to already submitted tasks via key. (42eb691)
- Re-introduced `Value` and `ReadOnlyValue` interfaces with a thread-safe `SimpleValue` implementation (previously removed in 20.0.0). (c598406)
- Overloads accepting `MessageFormatterArgs` to ease i18n usage. (87ce86a)

#### Changed
- Extracted record parameter handling into reusable `RecordParams` utility. (26a9f66)
- Unified certificate encoding/decoding methods; updated hashing to use `TextUtil`. (1874d0b)
- Simplified test utility calls; made certain cryptographic methods static. (d308572)
- Treat `keyOrPattern` as a pattern if `{` is present; otherwise treat as key. (e8f02f3)
- Added `@Nullable` annotations to `equals()` implementations; refined `Option.equals()`. (0f2a481)
- `WizardDialog` will not shrink anymore. Set preferred width/height to prevent all size changes
  when navigating through pages.

#### Fixed
- Corrected warning about inconsistent headless mode configuration. (fb752de)
- `Option.equals()` comparison correctness and nullability annotations improvements. (0f2a481)

#### Tests
- Added tests for parsing/handling PEM-encoded certificates and chains. (4cc5f9c)
- Added tests for `KeyUtil` PEM and private key handling (including password-protected keys). (8a8150f)

#### Refactor / Cleanup
- Removed unused `Base64` import from `CryptUtil`. (2014e55)
- General code cleanup. (6c2e4c6)

#### Build/Versioning
- Version bump. (2557857)

#### Breaking changes
- The 'hidden' parameter in some methods of the `InputBuilder` interface was replaced by `visible`. User code
  usually does not use these methods directly.

### 20.0.0

- **Minimum Java version**: Java 21. Java 25 is required for `StreamGathererUtil` and dark mode detection.
  If your project uses Java 24, please upgrade.

- **BOM artifact:** A new artefact `utility-bom` has been introduced that specifies the version for all 
  modules.

- **Fixes and improvements:** Many small improvements and bug fixes across the board, both for correctness and 
  performance.

- **JavaFX overhaul:** Much of the code has been rewritten or undergone major refactorings to make the interface
  more consistent and easier to use. 

- **New package `com.dua3.utility.application`:** adds utilities and classes to support application dvelopment:
    - A standard way to access the application Preferences. 
    - Managing the applications user interface mode (light/dark/system default) and track the system setting.
      Look at the `FxLogPaneSample` or `DarkModeSample` to see how to use this in JavaFX or Swing applications.
    - A recently used documents implementation that automatically persists the list of recently used documents
      between application runs.
  
- **Run tasks for samples:** To make running the samples easier, run tasks have been added to the build files.

- **CI workflow and deployment:**
    - The CI workflow has been completely rewritten.
    - The Project has been migrated from OSS-RH to Maven Central Publish Portal.
    - Deployments are done through GitHub actions using JReleaser, snapshots are published to Maven snapshots.
    - Javadoc is automatically published to GitHub Pages after each succesfull CI build.

#### Changes per package

- Package `utility.application`
    - New utility class `ApplicationUtil`.
    - New class `DarkModeDetector`.
    - New record `LicenseData`.
    - New class `RecentlyUsedDocuments`.

- Package `utility.concurrent`
    - added `scheduleTaskGroup()` to schedule a group of tasks that will be displayed with a title in the Swing and JavaFX ProgressView implementations. 

- Package `utility.crypt`
    - Introduced a new package that replaces the old `CryptUtil` class in `utility.lang`.
      that has a more secure API and adds many new features:
        - password generation
        - password strength evaluation
        - certificate creation (depends on BouncyCastle being present)
        - key generation and handling
        - key store handling
        - asymmetric encryption
        - message signing
        - ECIES support (depends on BouncyCastle being present)
        - HMAC support (depends on BouncyCastle being present)
        - Argon2 hash support (depends on BouncyCastle being present)

- Package `utility.data`
    - `DataUtil.convert()` now supports `valueOf(primitive)` and primitive arguement constructors.
    - `DataUtil.convert()` now supports converting between array types.
    - Added `Color.luminance()`.

- Package `utility.db`
    - Fixed a bug that prevented parsing of command line options to specify a JDBC connection
    - Added convenience method `createArgumentsParser()`.

- Package `utility.fx`
    - `AboutDialogBuilder.name()` was changed to `AboutDialogBuilder.applicationName()` and `AboutDialog` was removed.
    - Fixed validation of numeric fields in Dialogs created using `Dialogs.input()`.
    - Show markers vor invalid input in Dialogs created using `Dialogs.input()`.
    - Small fixes and improvements.
    - Dialog builders support the new `MessageFormatter` class, making creating localized dialogs less verbose.
      The `MessageFormatter` class can be used with either the standard Java `MessageFormat` or `String.format()`
      conventions.
    - `Dialogs.information()`, `Dialogs.warning()` etc. have been replaced by `Dialogs.alert()` which takes a parameter
      of type `Alert.AlertType`.
    - Added new class `InputValidatorFactory` to make input validation implementation less verbose.
    - Added error message tooltips for input fields with invalid data.
    - Fixed input dialog validation not detecting invalid data when the user clicked 'OK' or 'Next'
      before the relevant fields obtained focus.

- Package `utility.io`
    - Fix a race condition that sometimes would lead to a failure of the `IoUtil.testRedirectStandardStreams()`
      tests when a test on another thread used standard I/O at the same time (which it really should not).
    - hardened `IoUtil.unzip()` and provided an overload to use custom limits for unzipping.
    - added `IoUtil.getApplicationDataDir()` and `IoUtil.getUserDir()`  to get the canonical directory
      for application data storage of the platform and the user's home directory.

- Package `utility.lang`
    - Add new field `build.key` and method `digest()` to the `BuildInfo` class.
    - Added `Version` class and refactored BuildInfo class to use it.
    - Added `LangUtil.checkArg()` that throws `IllegalArgumentException`.
    - Added `LangUtil.getOrThrow()` that throws `NoSuchElementException` when the key is not contained in the map.
    - Added `LangUtil.formatThrowable()` and `LangUtil.appendThrowable()`.
    - Check format string matches arguments in `LangUtil.check()` and `LangUtil.checkArg()`.
    - `LangUtil.newUuidV7()` to create UUID v7 instances.
    - `LangUtil.reverseInPlace()` to reverse array contents
    - `LangUtil.isWrapperFor()` to test if a class is a primitive wrapper for another class
    - Added `addIf()`, `addIfNonNull()`, `applyIfNonNull()`, `applyIfNotEmpty()`, `newWeakHashSet()`.
    - `RingBuffer` implements `SequencedCollection`
    - Added `ReversedSequencedCollectionWrapper` to facilitate implementing `reversed()` for `SequencedCollection`
      implementations
    - Removed `StreamGathererUtil.filterAndMap()` and related methods - these did offer significant value over chaining
      `filter()` and `map()`.

- Package `utility.logging`
    - Removed `LogBuffer.size()` - the method could not be used meaningfully in a multithreaded environment.
    - `LogBuffer.setCapacity()` to change the capacity of an existing buffer

- Package `utility.math`
    - fix `MathUtil.pow10()` for negative arguments < -4
    - Added several mathematical constants.

- Package `utility.options`
    - The package has been completely refactored to be more consistent and allow for finer control.
      It is now for example easier to create options that have enum or record generic type.
      The different `Option` implementing classes have been replaced by a single `Option<T>` generic class.
    - `ArgumentsParser.help()` output was changed to be more informative and use system line ends as its intended use is
      to display a help message in the system terminal.
    - Use `Option.isEquivalent()` instead of `equals()` when checking for specific options.

- Package `utility.swing`
    - `FileInput` was changed to work around the missing "New Directory" button in open dialogs on macOS.
    - Race conditions and missed updates in `LogTableModel` have been fixed.

- Package `utility.text`
    - `TextUtil`
        - Added `TextUtil.toCharArray()|charsToBytes()|bytesToChars()`.
        - Added `removeLeading|Trailing|LeadingAndTrailing()` to efficently strip elements from the start or end of a
          list.
        - Removed the MD5-methods. MD5 can still be used by passing "MD5" as algorithm name. Reason: MD5 is considered
          cryptographically weak and insecure, and the API should not make using an unsecure algorithm easier to use
          than
          a secure one. Which algorithms are considered safe is always subject to ongoing research, so be neutral about
          algorithms.
        - Added `isNullOrBlank()`
        - Fixed an issue where `TextUtil.wrap()` would drop the last line if not ended with a line-end character.
        - Add `TextUtil.asCharSequence(chars[])`.
    - Fix some `RichText.split()` issues; the method should now always produce results consistent with String.split()
    - Add `MessageFormatter` class
    - Remove generic parameter from FontUtil class

### 19.2.1

- fix: incorrect implementation of FxUtil.convert(Affine)
- fix: ClosePath not implemented by FxUtil.convert(Path2f)
- fix: LogBuffer thread-safety issues and externalization
- PathBuilder2f: add overloads for passing float values x, y instead of Vector2f
- run FX tests in forked mode to avoid exceptions due to starting and stopping the Platform multiple times in the same
  JVM
- increase unit test coverage

### 19.2.0

- the Gradle build was changed to use the toolchain and JavaFX plugins again. This facilitates building the library
  on all platforms except Windows ARM. Building on Windows ARM is not supported. This does not affect using the library
  on that platform.
- fix: SwingGraphics.drawImage() draws at wrong position
- fix: SwingGraphics.clip(Rectangle2f) was missing a corner
- fix: the comparator returned by IoUtil.lexicalPathComparator() considered null paths to be greater than non-null paths
- improve performance of TeeOutputStream
- updates and fixes to the ProgressView/ProgressTracker and related classes
- the rendered images created during rendering tests can be downloaded from GitHub to check if the rendering works
  correctly; this is necessary because even on the same OS there may be differences in rendering depending on the
  installed fonts or rendering pipeline (software rendering didn't completely solve this)
- increase unit test coverage

### 19.1.1

- fix: ClassCastException in ImmutableSortedMap.containsKey()
- fix: wrong results for subSet(), headSet(), tailSet() obtained from reversed instances of ImmutableListBackedSortedSet
- fix: TextUtil.contentEquals() always returns false for non-empty strings
- fix: TextUtil.quoteIfNeeded() quoting the empty string
- increased unit test coverage

### 19.1.0

- POSSIBLY BREAKING: OptionException constructors all take the causing Option as first Argument and
  OptionException is derived from the new class ArgumentsException. In cases where the number of free
  arguments (i.e., arguments that do not belong to any option) does not match the definition of the
  ArgumentsParser, an ArgumentsException instead of an OptionException is thrown.
- Added flags to configure CsvReader, see the unit test for examples.
- fix: always return an array of the same type as the argument in ImmutableListBackedSortedSet.toArray(T[])
- fix: NPE in TextAttributes.hashCode() when an attribute value is null
- fix some minor issues and Qodana warnings
- fix: possible inconsistent hash values for RichText
- fix: possible XXE in XmlUtil.prettyPrint()
- fix: possible exponential runtime for evaluating regex in Jdbc support classes
- FontDef is not Cloneable anymore, FontDef.copy() was introduced instead
- added coverage and Sonarcloud scanning in addition to Qodana to CI
- added more unit tests, including some tests for swing classes
- small fixes and improvements
- code cleanup
- updated Gradle wrapper, plugins, dependencies

### 19.0.0

- BREAKING: This version has some breaking changes to the Font classes:
    - The font classes now have a `families` Attribute of type `List<String>` instead of
      a single `family` attribute of type `String`. This was necessary to support alternative
      font families (like in CSS).
    - new method Font.getType() returns either `FontType.MONOSPACED` or `FontType.Proportional`.
    - `Style.FONT_TYPE` was replaced by `Style.FONT_CLASS` and `Style.FONT_FAMILY`
    - added assertions to check for valid attribute types
    - Changes to the FontUtil implementations; if a JavaFX or AWT font is requested, the different
      families are tested for availability on the system and the first available one is used to
      instantiate the platform font. You can add one of `"serif"`, `"sans-serif"`, and `"monospace"`
      at the end of the family list (these are always available) to make sure a matching fallback
      font is selected.
    - The HtmlConverter will use `<code>` tags if the font family is set to `monospace`, i.e, if
      `monospace` is the first family listed in the font's list of font families and CSS is not
      used (pass the HtmlConversionOption `useCss(false)` when creating the converter). Otherwise,
      a `<span>` is used to set the font.
- updated plugins and dependencies
- some minor fixes and improvements

### 18.6.0

This version is all about reducing memory consumption. The new classes are used internally by RichText and
RichTextBuilder but can be universally used. Large RichText instances (several million characters, each having
different attributes), memory usage went down by 50% without noticeable impact on runtime performance.

- LangUtil: added two new methods, LangUtil.asUnmodifiableSortedListSet() and LangUtil.asUnmodifiableList(),
  and the implementing classes that offer memory efficient Set and Map implementations
- LangUtil.isOfKnownImmutableType()
- TextAttributes storage has been changed from a TreeMap to a custom SortedSet implementation (s.a.) backed by
  a sorted array, greatly reducing memory consumption (in tests up to 80%). This change is transparent.
- FxUtil.runOnNextFrame()
- ImmutableSortedListBackedSet as a memory efficient immutable SortedSet implementation
- ImmutableSortedMap as a memory efficient Map implementation for immutable maps
- CompactableSortedMap to provide a mutable map that can be compacted to an ImmutableSortedMap to reduce memory
  consumption and will be restored to a standard Map when mutated
- RichTextBuilder now internally uses CompactableSortedMap to reduce memory consumption when large RichText instances
  are created.

### 18.5.1

- fix PinBoard.scroll() units when scale != 1
- PinBoard.getViewPortInBoardCoordinates()
- PinBoard.getPositionInBoard()

### 18.5.0

- BREAKING: PinBoard interpretation of the additional parameters to some of the scrolling methods
  was changed from a viewport relative offset to an absolute offset in pixel
- PinBoard: introduce a display scale to zoom the pinboard contents; several fixes to the PinBoard class
- PinBoard: optimized rendering to remove flickering when changing scale
- SliderWithButtons: implements InputControl; support for setting ticks
- InputPaneBuilder.slider()
- added some conversion methods to better support StringProperty and DoubleProperty

### 18.4.1

- add RichText.runStream()
- LangUtil: add constants for empty arrays of primitive types
- code cleanup and performance improvements
- removed the unused markerLevelMap from LoggerSlf4j

### 18.4.0

- This version brings many changes for image handling.

  BREAKING CHANGES
    - The pixel format has been changed from INT_ARGB to INT_ARGB_PRE. This was
      necessary to be able to share the image data between JavaFX and AWT implementations.
    - AwtImage.bufferedImage() was removed as AwtImage is now derived from BufferedImae.
      Pass the AwtImage instance directly whenever a BufferedImage is needed.

  DESCRIPTION OF THE CHANGES

  A new MutableImage interface has been introduced. The getBuffer() method returns a
  low-level ImageBuffer instance that writes directly through to the pixel data.

  To create a MutableImage, use ImageUtil.createBufferedImage(). The returned instance
  is a subclass of java.awt.BufferedImage in both the AWT and JavaFX implementations.
  This makes it possible to use libraries that work directly on java.awt.BufferedImage
  in both the JavaFX and AWT implementations.

  FxImage has been changed to an interface. FxImageUtil.create() will now return an
  instance of FxStandardImage whereas FxImageUtil will return an FxImage instance that
  can be directly passed as a BufferedImage to code that works on BufferedImage instances.
  Any changes made to the image data will write directly through to the JavaFX image.

### 18.3.1

- code cleanups and performance improvements

### 18.3.0

- POSSIBLY BREAKING: HtmlTag.headerChange() now returns int instead of OptionalInt and the static factory methods have
  also
  been changed accordingly. This should not be a problem for most since while part of the public API, the methods
  should in general not be used directly, but from the HtmlConverter class.
- Fixed the Qodana scan during CI build which broke due to Java 24 code being included in the multi-release JAR.
- TextUtil.lexicographicalComparator()
- IoUtil.lexicalPathComparator()
- StreamGatherUtil.mapAndFilter()
- add unit tests for Java 24 code
- some code cleanup and Javadoc additions

### 18.2.0

- added StreamGatherUtil (only available in Java 24+)
- updated ikonli to 12.4.0
- DataUtil.isSorted()
- RichText.isBlank()

### 18.1.2

- declare LangUtil.wrapException() public
- FileType.getExtensionPatterns()
- FileType refactorings
- Dialogs.openFile()
- Dialogs.saveFile()
- nullability fixes
- code cleanup

### 18.1.1

not released

### 18.1.0

- Chnages to the HtmlConverter class to track the current font; needed for eliding unnecessary font definitions
- code refactored to use Java 21 features
- code cleanups
- Javadoc additions and corrections

### 18.0.0 (Java 21)

IMPORTANT: The minimum Java version is 21. The following functionality has been removed as
it is now provided directly by JDK classes:

- NetUtil removed: this utility class has become obsolete with the introduction of new methods in the URL/URI classes.
- ThreadFactoryBuilder removed: As of JDK 21, use Thread.ofPlatform().factory().
- MathUtil.clamp() removed. Use Math.clamp() introduced in Java 21 instead. Note the different argument order.

### 17.1.3

- IMPORTANT: last release may not run correctly on Java 17, please upgrade to 17.1.3+
- add forbiddenapis plugin and set correct release when compiling
- add Counter and Histogram classes
- precalculate Font hash
- small refactorings
- fix SimpleValue.removeChangeListener
- simplify HSVColor constructor

### 17.1.2

- RichTextBuilder implements CharSequence
- RichTextBuilder.deleteCharAt()
- Refactored RichTextBuilder to use less memory and perform better

### 17.1.1

- FxRefresh: skip refreshs if the refresher has been stopped
- small refactorings

### 17.1.0

- BREAKING: ToRichText.appendTo() and ToRichText.toRichText() default implementations have been removed
- RichTextBuilder internal stack has been change from ArrayDeque to ArrayList
- HtmlConverter refactorings

### 17.0.0

- fix target compatibility not being set in modules
- fix compilation on Windows ARM
- fix TextUtil.setLineEnds() and toXXXLineEnds() not appending the trailing line end
- fix TextUtil tests failing on windows due to different line end characters
- add TextUtil.isNewlineTerminated(), TextUtil.setLineEnds()
- fix PredefinedDateTimeFormatTest failing depending on the used Java version due to changes in chinese date and time
  formatting
- make the qodana scan run again
- Javadoc corrections and additions
- SwingUtil.setRenderingQuality() has been renamed to setRenderingQualityHigh()

### 16.2.0

- BREAKING Font(FontData, Color) has been declared protected; it was not intended to be called directly from user code
- cache Font instances
- improve performance
- fix title not displayed centered in AbdoutDialog

### 16.1.6

- fix a runtime error when utility-log4j is used in a jlink application

### 16.1.5

- added some jmh benchmarks
- performance improvements
- add more MathUtil unit tests
- added missing package-info.java

### 16.1.4

- add namespace related methods to XmlUtil
- add more XmlUtil unit tests

### 16.1.3

- declare I18N.mergeBundle() public
- CompressedCharacters.inputStream()
- documentation fixes and additions

### 16.1.2

- add classes CompressedBytes and CompressedCharacters
- IOUtil.getInputStream() supports getting an InputStream from a Reader

### 16.1.1

- FIX: use correct argument order for Math.clamp in PinBoardSkin

### 16.1

- Java 21 is required for compilation and JavaFX related modules; all other modules require Java 17+.
- deprecated the MathUtil.clamp() methods as Math.clamp() was added to the JDK.
  These methods will be removed once the base JDK changes to 21.
- add a SystemInfo record to retrieve system information; run the DialogSample class and show details
  in the About dialog of the application for example output
- add Graphics.transform()
- RichText: Fixes and performance improvements

### 16.0.1

- correct name of overloaded method to Graphics.inverseTransform() (was Graphics.transformToLocal())
- support copying RichText to to the clipboard (uses HTML format)
- support getting texts (String), images, and paths from the clipboard
- fix HtmlConverter.useCss() ignoring the passed parameter
- fix LangUtil.isOneOf(null, args...) throwing NPE (note that the remaining elements @NonNull)
- add convenience methods Style.create(Font) and Style.create(Font, Color, Color)

### 16

- bump to major version because of a few breaking changes
- FragmentedText class added that calculates RichText layout for rendering
- added some methods and overloads to the Graphics class
- added some methods and overloads to geometry classes
- added RichText.split(Pattern), RichText.split(Pattern, int)
- fixes to code and Javadoc
- JavaFX: added logging of warnings when exceptions are thrown during runLater() and runAndWait()
- JavaFX: RichText rendering
- added AutoLock class
- small fixes and improvements
- many fixes and additions to both JavaFX and Swing rendering

### 15.1.2

- add logging of exceptions to PlatformHelper.runLater()
- add AutoLock class
- add location information to LogWindow/LogPane messages

### 15.1.1

- add XmlUtil.parse(Reader)

### 15.1.0

- TextUtil.base64Decode() allows whitespace and linebreaks
- Graphics.strokePath(), Graphics.fillPath()
- Graphics: many fixes, drawing arcs, circles, ellipses
- PinBoardSkin add more scroll related methods and fix bugs
- fix race condition in LogEntriesObservableList
- bugfixes, code clean-ups, JavaDoc additions

### 15.0.2

- fix AwtFontUtil.deriveFont() always returning font instances with black text color

### 15.0.1

- added data.Converter interface
- added fx.PropertyConverter class to convert properties to properties of different types
- the Controls fluent builder interface supports bidirectional binding the selected state of ToggleButton and CheckBox
  instances to boolean properties

### 15.0.0

- BREAKING: font handling has been refactored for both AWT and JavaFX. Use FontUtil to get Font instances.
  To derive a font from a base font, use FontUtil as well.

  The Font class has been enhanced with methods to retrieve metric data like ascent, descent, and space width

  Memory footprint should be slightly reduced and performance increased for applications that dynamically allocate
  Font instances.

- small fixes and improvements

### 14.1.0

- BREAKING: when passing `null` as a non-nullable parameter, an `IllegalArgumentException` will be thrown
  (used to throw `NullPointerException`)
- Font.delta() now accepts null arguments
- IconView icon size and color properties are now bound bidirectional to the properties of the underlying ikonli icon

### 14.0.1

- add missing @Nullable in RichText.equalizer()
- update log4j to 2.24.2
- use log4j-bom
- improve logging in setTempFilePermissionsNonPosix();
- update cabe to final release

### 14

**NOTE:** starting with this release, for development builds of this library (i.e., builds that are neither
release nor release candidate builds), return values of all methods returning a @NullMarked or @NonNull
object (or derived) type are checked and will unconditionally throw an AssertionFailedError if the return
value is found to be inconsistent.

Any AssertionError you encounter (with a message "invalid return value") means that you have found a bug
in the library and are asked to open an issue against the library.

Release candidates and final releases will not contain any return value check.

Changes:

- org.jspecify annotations are used in the code base; all modules annotated with `@NullMarked`.
  That you may not pass `null` in any method or constructor parameter that is not explicitly
  annotated as `@Nullable`.
- module definitions have been reviewed.
- change type parameter for IoUtil.closeAll() to `? extends AutoCloseable`
- complete Javadoc
- code cleanup and bug fixes
- XmlUtil.defaultInstance() and jaxpInstance() both return new instances for every invocation
  because the returned instances are not thread safe

### 13.1.2

- IoUtil.closeAll()
- code cleanup
- prevent serialization of logging related classes for security reasons
- javadoc

### 13.1.1

- FIX: do not throw an exception when temp directory permissions cannot be set, log a warning message instead. The
  reason is that especially the Files.setWriteable() does not seem to always return correct results, even when the  
  directory does have the requested permissions.

### 13.1

- BREAKING: in the fx module, the methods Validator.matches() and Validator.notEmpty() have been renamed to setRegex()
  and disallowEmpty() to better express the fact that the content is not checked immediately but instead a rule is added
  that is validated at validation time
- IMPORTANT: resolved ambiguity of LangUtil.ConsumerThrows.andThen() overloads by renaming the version taking an
  argument of ConsumerThrows to andThenTry(); this might require a source change but code compiled using the previous
  version should still work in the same way
- IoUtil: added methods createSecureTempDirectory(), createSecureTempDirectoryAndDeleteOnExit(), deleteRecursiveOnExit()
- small improvements and code cleanup

### 13.0.2

- add overload of DataUtil.diff() that takes a map factory

### 13.0.1

- TextUtil.toString(Object obj, String valueIfNull)
- use HexFormat for converting Color instances to hex format
- add Text Filter to FxLogPane

### 13

- new utility-fx modules with JavaFX related classes and components
- StreamUtil.zip() has been changed to take an operation as third parameter that defines the combining operation
- StreamUtil.concat() did not close streams
- SwingImageUtil, SwingFontUtil have been renamed to AwtImageUtil, AwtFontUtil as they are usable also in non-Swing
  applications
- Color methods have been renamed to reduce ambiguity
- SwingUtil.getDisplayScale() to retrieve the actual scaling factor for the display (taking into account UHD and retina
  displays)
- AffineTransformation2f.combine()
- return value of getTextDimension() changed to Rectangle2f (this gives access to the baseline value)
- Rectangle2f.getXCenter(), getYCenter(), min(), max(), center(), dimension(), withCenter()
- Dimension2f.scaled()
- Font.scaled()
- TextUtil.isBlank(CharSequence)
- toolkit agnostic Graphics interface and implementations for Swing and JavaFX
- code cleanups and reorganizations
- Javadoc additions
- many small fixes and improvements

### 12.3

- added i18n package
- ConsoleHandler (utility-logging): make colored output configurable at runtime
- added getGlobalDispatcher to the LogUtilLog4J and LogUtilSLF4J classes
- improve logging performance
- add LogUtilLog4J.init(LogLevel) to initialize the logging system and reroute all logging through Log4J
- log filtering
- extracted common code from the two Swing Logging samples
- added documentation to the LogUtil classes about rerouting logging implementations
- code cleanup and minor fixes
- StopWatch.log...() returns an Object that does the formatting in the toString() method instead of a Supplier<String>
  to integrate better with logging frameworks (this way, lambda support is not needed)
- TextUtil.nonEmptyOr()

### 12.2.1

- add LangUtil.require...() overloads for double and float arguments
- LangUtil.isBetween() will throw IllegalArgumentsException when an invalid intervall is specified
  instead of using an assertion

### 12.2

- support for options that prevent validation when present, i.e., to pass a help flag without the parser throwing
  an exception when there are problems with other options
- moved to Arguments and made public argument validation methods
- add separate desciption for arguments
- added LangUtil.require...() methods to check integer arguments
- added LangUtil.formatLazy()
- fix: LoggerFactorySlf4j should not dispose the default handler
- add LogEntryDispatcher.getHandlers()
- make Stopwatch(Supplier<String> name) protected

### 12.1.2

- IoUtil.toURI(Path) returns relative URI when called with relative Path instance

### 12.1.1

- code cleanup and refactorings
- fix a malformed logging message
- set modification date for zip entries
- fix creation of extra empty file in zip

### 12.1.0

- add TextUtil.digest() methods to support other digests than MD5
- digests can be calculated on streams without loading the full data into memory
- add IoUtil.zip() and unzip() methods
- javadoc additions

### 12.0.9

- allow null attr parameter for SwingDocumentFilter methods replace() and insertString()

### 12.0.8

- javadoc updates
- refactorings and small fixes
- fix race condition in SwingLogPane
- update dependencies and plugins
- code refactorings and small fixes

### 12.0.7

- remove obsolete null-checks
- update gradle, cabe

### 12.0.6

- TextUtil.quote(), TextUtil.quoteIfNeeded(), TextUtil.joinQuoted()
- use Java compatible quoting in Arguments.toString()

### 12.0.5

- add Arguments.toString()
- add Option.toString()
- fix: TextUtil.toSystemLineEnds() appended a newline character even if the argument did not end with newline
- fix: ArgumentsParserTest failed on Windows

### 12.0.4

- fix runtime error (cabe)

### 12.0.3

- fix record equals() not accepting null arguments
- make output of ArgumentsParser.help() more compact

### 12.0.2

- Use assertions to check parameters of internal methods, throw NPE for parameters to methods that are part of the
  public API. From now on, snapshots and beta versions will unconditionally throw AssertionErrors when null is passed
  to a parameter that isn't nullable. In release versions, standard assertions are used for internal methods while
  methods that are part of the public API will throw NullPointerExceptions.

### 12.0.1

- remove obsolete classes in utility.math.geometry
- fix locale dependent output of toString() implementations
- code cleanup
- dependency updates

### 12.0.0

- BREAKING: ArgumentsParser has been split into ArgumentsParserBuilder and ArgumentsParser; see unit tests for examples
- BREAKING: RichText.trim() now works the same as String.trim(), i.e. only considers character codes less than or equal
  to ' ' as whitespace. Use RichText.strip() to remove all whitespace.
- BREAKING: OpenMode.includes() has been renamed to isIncluded()
- BREAKING: removed Pair.toMap() static methods. Use Map.ofEntries() instead.
- BREAKING: LineOutputStream returns lines with line end characters removed
- BREAKING: removed PredefinedDateFormat - use PredefinedDateTimeFormat instead
- Pair<T1,T2> now implements Map.Entry<T1,T2>
- added RichText.strip(), RichText.stripLeading(), RichText.stripTrailing()
- added ThreadFactoryBuilder class
- enable automatic download of Gradle JVM toolchains in build
- util-logging: reworked util-logging, simplified SwingLogPane use, fixed multithreading issues in SwingLogPane
- util-db: added NamedParameterStatement.setInstant(), cleaned up NamedParameterStatement javadoc
- SimpleNamespaceContext: added methods get Prefixes(), getNamespaceURIs
- TextUtil.transform(): accept Object instances as substitutions (String.valueOf() is used for formatting of instances)
- reduce code duplication
- increase unit test coverage
- add @Nullable annotations for record types where needed (because cabe 2.x adds support for record types)
- add javadoc
- fix RichText.split() with non-trivial regex and limit 0 not skipping trailing empty segments in result
- fix build issues on windows
- fix issues in FontDef CSS conversion
- fix font size not extracted when getting FontDef directly from TextAttributes
- fix Vector2f returning NaN for zero denominator
- fix IoUtil.glob() and IoUtil.findFiles() returning paths with inconsistent root under certain circumstances
- fix smaller issues
- update plugins and dependencies

### 11.1.3

- update log4j to 2.21.0 (according to release notes now fully JLink compatible)

### 11.1.2

- update plugins and dependencies
- Pair.mapFirst() and Pair.mapSecond()
- code cleanups

### 11.1.0

- overload TextUtil.getMD5String(byte[])
- Value interface as an abstraction for observable values that is meant to be used in place of JavaFX ObservableValue or
  Swing Properties in library code that is supposed not to have dependencies on either JavaFX or java.desktop

### 11.0.0

- (BREAKING) remove deprecations
- (BREAKING) remove LangUtil.checkIndex()
- (BREAKING) reverse order of arguments in CryptUtil.encrypt()/decrypt() with string arguments to match those of byte[]
  arguments
- lots of Javadoc updates and additions
- add ComboBoxEx
- add ArgumentsDialog
- add SwingDocumentFilter
- add samples project
- reduce code duplication in RichTextBuilder
- exception handling in drag and drop support
- DbUtil.stream()
- make FontDef Cloneable
- RichText.equalizer() returns a customizable equalizer for comparing texts ignoring certain properties
- RichText.wrap() combines styles instead of replacing them
- IOUtil.glob() and IOUtil.listFiles() to search directory trees

### 10.3.4

- add FileInput drag&drop support to FileInput

### 10.3.3

- add Qodana to CI pipeline
- code cleanups
- added LangUtil.asFunction()

### 10.3.2

- Fix XmlUtil.prettyPrint(String) indentation

### 10.3.1

- Fix XmlUtil.prettyPrint(String) not indenting

### 10.3.0

- StreamUtil.stream() to create streams from iterators and iterables
- IoUtil.prettyPrint(String) support namespaces; attributes written in alphabetical order

### 10.2.10

- IoUtil.prettyPrint(String) retains comments in output
- code and javadoc cleanup

### 10.2.9

- reimplemented XmlUtil.prettyPrint(String) with a StAX parser so that no DOM has to be created when pretty-printing

### 10.2.8

- DataUtil.convert converting to/from URI, URL, Path, File

### 10.2.7

- FileInput swing component

### 10.2.6

- support value "auto" for configuring colored log output. colors are enabled if a terminal is attached and the TERM
  environment variable is set. this is the new default for this option.
- update plugins, junit

### 10.2.5

- add overloads for XmlUtil.prettyPrint()
- update gradle, dependencies

### 10.2.4

- update plugins
- fix compile time warnings in build script

### 10.2.3

- add XmlUtil.prettyPrint(String)
- use ProgressView.update(PROGRESS_INDETERMINATE) to set indeterminate state
- add assertion messages
- code cleanup

### 10.2.2

- update dependencies
- fix javadoc problems
- make Option fields private
- FileType.read(): add overloads taking options
- fix AssertionError when writing null value in CSV

### 10.2.1

- fix handling of default namespace URI in SimpleNamespaceContext
- use version catalog defined in settings.gradle.kts for dependencies and plugins
- update SLF4J to 2.0.1

### 10.2.0

- add SimpleNamespaceContext for use with XPath
- add XmlUtil.xpath(node) to create namespace aware XPath instances (namespace information is automatically extracted
  from the node and its parents)
- fix SLF4J warnings on console when running unit tests
- small fixes and improvements

### 10.1.2

- make XMLUtil namespace aware by default
- log levels can be configured by prefix in logging.properties file

### 10.1.1

- print exception stack traces in log
- some fixes for SwingProgressView

### 10.1.0

- **Logging is done through SLF4J**
- moved the logging stuff to its own library utility-logging so that you don't need to pull everything unless needed

### 10.0.0

- **Java 17 required!**
- use Kotlin DSL in gradle build scripts
- changed module names to com.dua3.utility again
- upgraded gradle to 7.4 for Java 17 support
- publish snapshots to sonatype snapshot repository
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
- LangUtil.formatStackTrace(Exception)
- IOUtil.stringStream(String)
- LangUtil.map(OptionalXXX, XXXFunction) for mapping primitive Optionals
- improve output and performance of XmlUtil.prettyPrint()
- fix MathUtil.gcd for negative arguments
- many fixes and smaller improvements, added many Javadoc comments, improve test coverage
- XmlUtil defaultInstance() is now unsynchronized and should be faster
- added LangUtil.orElse() and LangUtil.orElseGet() for Optional-like functionality without creating an Optional instance
  first
- added [cabe](https://github.com/xzel23/cabe) annotations to parameters
- rename `IOUtil`, `IOOptions` to `IoUtil`, `IoOptions`
- reorder parameters to the different LogAdapter `addListener()` and `removeListener()` methods and add overloads for
  these methods that add/remove listeners to /from the framework's root logger.
- remove LogUtil.format() as it is not shorter than the equivalent lambda (i.e. `LogUtil.format("message %s", arg)` can
  be replaced by `() -> "message %s".formatted(arg)`)
- remove MathUtil.toDecimalString()
- remove SandboxURLHandler
- remove methods taking java.io.File; use Path instead
- Color is now an interface with implementations RGBColor and HSVColor
- RichText and Run classes provide an overloaded equals() method that accepts a predicate to give fine-grained control
  to exclude certain attributes from equality checks
- Platform: added methods to help with quoting arguments for ProcessBuilder
- LangUtil.defaultToString()
- rename RichText.textEquals() to equalsText()
- add equalsTextAndFont(), equalsIgnoreCase()
- add TextUTil.normalizeLineEnds(), TextUtil.toUnixLineEnds(), TextUtil.toWindowsLineEnds(), TextUtil.toSystemLineEnds()
- fix unit test failures on Windows
- StreamUtil.concat()
- StreamUtil.merge()
- Bearer interface
- LangUtil.triStateSelect()
- added FontDef.fontspec()
- small fixes and improvements

**The following functionality has been removed because it is available in JDK 17**:

- TextUtil.byteArrayToHexString(): use HexFormat.of().formatHex()
- TextUtil.hexStringToByteArray(): use HexFormat.of().parseHex()

### 9.1.1

- fix AIOOBE in SwingLogPane when messages come in at a high rate

### 9.1.0

- BACKPORT: StreamUtil.merge()

### 9.0.1

- add HtmlConverter.inlineTextDecorations()
- some JavaDoc corrections and completions
- update Gradle to 7.2-rc2 for JDK 17 compatibility (during compilation)

### 9.0

- migrate from bintray to sonatype
- JDK 11+ required! It's finally time to dump Java 8 support. I won't put any more effort into supporting a Java version
  that has long reached EOL and is a maintenance burden because of its missing modularity support.
- build uses Gradle 7 to enable building on JDK 17
- removed use of the JPMS Gradle plugin as it is not compatible with Gradle 7 and not needed anymore after dropping JDK
  8 support and Gradle 7 added modularity support.
- logback support has been replaced by log4j2 due to missing jigsaw support in logback. This means if you have been
  using a Log4J to Logback bridge before, it's now time to do it the other way around.
- NamedParameterStatement supports many more data types; fixes for inserting null values.
- source parameter of CsvReader changed from String to URI
- `com.dua3.utility.cmd` and `com.dua3.utility.options` have been merged; there had been a lot of duplicated
  functionality, and the implementation in the `cmd` package was much cleaner; the result is again located
  in `com.dua3.utility.options` but the code is mostly based on what had been in the `cmd` package. Classes have been
  renamed because they are no more intended to be used only for command line arguments.
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

- DomUtil renamed to XmlUtil, methods are instance methods now so that instances using different DocumentBuilder and
  Transformer implementations can be used
- moved all XML related methods from IoUtil and TextUtil to XmlUtil
- SwingUtil: added helper methods for adding basic drag and drop support

### Version 7.0.8

- IOUtil: added IOUtil.copyAllBytes()
- IOUtil: removed inaccessible Method StreamSupplier.lines()

### Version 7.0.7

- NamedParameterStatement: don't throw exception in constructor if parameter info can not be queried; fix typo in method
  name

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
- removed obsolete classes and methods (those should have been in use anyway and can in any case be easily replaced with
  the newer versions/replacements)

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

- Font: fix fontspec parsing when font size does not contain a fractional part; do not output fractional part in
  fontspec if unneeded

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

- new method FileType.isCompound() is used to exclude file types from lookup by extension (default implementation
  returns false; see javadoc for details)

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
- update SpotBugs and SpotBugs plugin

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

- added methods in LangUtil to resolve localized resources
- minor cleanups

### Version 5.1.1

- Methods in IOUtil that take a filename as a String argument have been changed to return correct results when instead
  of a filename a path is given.

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
- `DataUtil.convert()`: be more strict with boolean conversions. Converting a `String s` to `Boolean` will yield `null`
  if `s` is `null`, `Boolean.TRUE` if and only if `s` equals ignoring case `"true"`, `Boolean.FALSE` if and only if `s`
  equals ignoring case `"false"`. In all other cases `IllegalArgumentException` is thrown.

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

- `DataUtil.convert()` and `DataUtil.convertToArray()` for converting objects to other types. Conversion is done as
  follows:
    * if value is {@code null}, {@code null} is returned;
    * if the target class is assignment compatible, a simple cast is performed;
    * if the target class is `String`, `Object.toString()` is used;
    * if the target class is an integer type and the value is of type double, a conversion without loss of precision is
      tried;
    * if the target class provides a method `public static T valueOf(T)` and `value instanceof U`, that method is
      invoked;
    * if `useConstructor` is `true` and the target class provides a constructor taking a single argument of value's
      type, that constructor is used;
    * otherwise an exception is thrown.

### Version 4.0.8

- new class `DataUtil`, methods for data conversion

### Version 4.0.7

- new helper method `TextUtil.align()`

### Version 4.0.6

- __BREAKING:__ default separator for CSV changed from semicolon to comma to make it compliant with RFCc4180. To change
  the separator, use `CsvIo.getOptionValues(CsvIo.OPTION_SEPARATOR, ';')` when creating the `CsvReader`/`CsvWriter`
  instance.

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

- changed the build system to make life easier for me as developer :) - version information is stored in a file
  named `version` in the project root. This makes it possible to consistently update dependencies information
  automatically using a script.

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
- new classes `Option` (an option to control object behavior), `OptionSet` (a set of options to control object
  behavior), and `Options` (a set of options and corresponding values)
- new package `com.dua3.utility.data` with classes `TreeNode` (to build tree data structures) and `FileTreeNode` to
  create a tree of files and subdirectories of a directory
- moved the `Pair` and `Color` classes to the `com.dua3.utility.data` package
- renamed `Options` to `OptionValues`

### Version 3.1.4

- add `uses` declarations in `module-info.java` files (fix ServiceLoader issues when run from eclipse)
- spotbugs: use exclude filter for false positives; don't ignore failures

### Version 3.1.3

- Added methods to measure text in TextUtil. The concrete implementation is using either AWT or JavaFX and is loaded via
  a ServiceLoader. The AWT implementation is included in utility.swing whereas a JavaFX implementation is included in
  the fx.util subproject of the fx project.

### Version 3.1.2

- skipped

### Version 3.1.1

- Fix NamedParameterStatement throwing exception when using ojdbc8.jar with certain statements.

### Version 3.1.0

- The git repository moved to https://gitlab.com/com.dua3/lib/utility.git
- Added `NamedParameterStatement.getParameterInfo()` and `NamedParameterStatement.getParameterInfo(String)` to query
  parameter meta data (i.e. the corresponding SQL type)
- Added `NamedParameterStatement.getResultSet()`
- Added `NamedParameterStatement.getUpdateCount()`
- Added `DbUtil.toLocalTime()`

### VERSION 3.0.1

- changed license to MIT to make it possible to use in pre-V3 GPL projects

### VERSION 3.0

- BREAKING: package `com.dua3.utility.db` was moved to a separate library `utility.db`. You have to update dependencies
  and module-info.
- REMOVED: `Font.getAwtFont()` and `Font.getTextWidth()`; these introduced a dependency on java.desktop and can be
  easily replaced where needed.
- The utility.db library now include methods to dynamically load JDBC drivers. This allows to use jlink on applications
  that use non-modular JDBC-drivers.
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
