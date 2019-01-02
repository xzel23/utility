# com.dua3.utility

A small library with utility classes. As of version 1.2, Java 8 is required.

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
        
        compile        "com.dua3.utility:utility:2.1"
    }

## Changes

### version 2.1

 - IOUtil.loadText(): helper methodd to load text files where the character encoding is not known.
 
### version 2.0

 - Requires Java 11+
 - Removed Swing and JavaFX.
