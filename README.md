# com.dua3.utility

A small library with utility classes. As of version 1.2, Java 11 is required.

## Requirements

 - JDK 11 or later

## Using with Gradle

Binary buils are available on bintray:

    repositories {
        ...
        
        maven { url  "https://dl.bintray.com/dua3/public" }
    }
    
    dependencies {
        ...
        
        def utilityVersion = "1.2.0"
        compile        "com.dua3.utility:utility:${utilityVersion}"
    }
