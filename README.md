# com.dua3.utility

A small library with utility classes.

## Requirements

 - JDK 11 or later

## Using with Gradle

Binary builds are available on jcenter:

    repositories {
        jcenter()
    }
    
    dependencies {
        compile        "com.dua3.utility:utility:3.1.0"
        compile        "com.dua3.utility:utility.db:3.1.0"
        ...        
    }

## Changes

### Version 3.1.0

 - Added `NamedParameterStatement.getParameterInfo()` and `NamedParameterStatement.getParameterInfo(String)` to query parameter meta data (i.e. the corresponding SQL type)
 - Added `NamedParameterStatement.getResultSet()`
 - Added `NamedParameterStatement.getUpdateCount()`
 
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
