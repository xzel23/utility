# com.dua3.utility

A small library with utility classes.

## Requirements

 - JDK 11 or later (starting with version 2.0)

## Using with Gradle

Binary buils are available on bintray:

    repositories {
        ...
        
        maven { url  "https://dl.bintray.com/dua3/public" }
    }
    
    dependencies {
        ...
        
        compile        "com.dua3.utility:utility:3.0"
        compile        "com.dua3.utility:utility.db:3.0"
    }

## Changes

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
