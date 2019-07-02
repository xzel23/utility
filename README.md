# com.dua3.utility

A library with utility classes.

## Requirements

 - JDK 11 or later

## Using with Gradle

Binary builds are available on [jcenter](https://mvnrepository.com/artifact/com.dua3.utility):

    repositories {
        jcenter()
    }
    
    dependencies {
        compile        "com.dua3.utility:utility:4.0.0"
        compile        "com.dua3.utility:utility.db:4.0.0"
        ...        
    }

## Changes

### Version 4.0.1

- `JdbcDataSource.setUser()` and `JdbcDataSource.setPassword()` suppoert `null` argument to unset value.

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

- Added methods to measure text in TextUtil. The concrete implementation is using either AWT or JavaFX and is loaded via a ServiceLoader. The AWT implementation is included in utility.swing whereas a JavaFX implementation is inlcuded in the fx.util subproject of the fx project.

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
