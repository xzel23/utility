# com.dua3.utility

A small library with utility classes. As of version 1.2, Java 8 is required.

## Requirements

 - JDK 8 or later

## Using with Gradle

Binary buils are available on bintray:

    repositories {
        ...
        
        maven { url  "https://dl.bintray.com/dua3/public" }
    }
    
    dependencies {
        ...
        
        def utilityVersion = "1.2.1"
        compile        "com.dua3.utility:utility:${utilityVersion}"
    }
