# Modules Overview

This document provides an overview of all modules in the utility library.

## Version management

Import `utility-bom` and declare the version only for the BOM. Do **not** declare versions on individual utility
module dependencies: patch releases can publish a changed module at a newer version while the BOM retains the
previous version for unchanged modules. The BOM selects the compatible, actually published version of every module.

For Maven, import the BOM through `dependencyManagement`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.dua3.utility</groupId>
            <artifactId>utility-bom</artifactId>
            <version>${utility_bom_version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

For Gradle, add the BOM as a platform dependency:

```kotlin
dependencies {
    implementation(platform("com.dua3.utility:utility-bom:$utilityBomVersion"))
    implementation("com.dua3.utility:utility")
}
```

All module examples below therefore intentionally omit `<version>`.

## Core Modules

### utility

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility</artifactId>
</dependency>
```

**Description:**
The core utility module provides utility classes for various purposes, including:

- Concurrent programming (com.dua3.utility.concurrent)
- Data manipulation (com.dua3.utility.data)
- I/O operations (com.dua3.utility.io)
- Internationalization (com.dua3.utility.i18n)
- Language utilities (com.dua3.utility.lang)
- Mathematical operations (com.dua3.utility.math)
- Geometric operations (com.dua3.utility.math.geometry)
- Option handling (com.dua3.utility.options)
- Text manipulation (com.dua3.utility.text)
- XML processing (com.dua3.utility.xml)
- AWT utilities (com.dua3.utility.awt)

### utility-db

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility-db</artifactId>
</dependency>
```

**Description:**
This module provides database utilities in the com.dua3.utility.db package.

### utility-swing

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility-swing</artifactId>
</dependency>
```

**Description:**
This module provides Swing-related utilities.

## JavaFX Modules

### utility-fx

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility-fx</artifactId>
</dependency>
```

**Description:**
This module provides JavaFX utilities.

### utility-fx-icons

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility-fx-icons</artifactId>
</dependency>
```

**Description:**
This module provides icon utilities for JavaFX.

### utility-fx-icons-ikonli

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility-fx-icons-ikonli</artifactId>
</dependency>
```

**Description:**
This module provides Ikonli icon integration for JavaFX.

### utility-fx-controls

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility-fx-controls</artifactId>
</dependency>
```

**Description:**
This module provides custom controls for JavaFX.

### utility-fx-db

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility-fx-db</artifactId>
</dependency>
```

**Description:**
This module provides database utilities for JavaFX.

### utility-fx-web

**Maven Coordinates:**

```xml
<dependency>
    <groupId>com.dua3.utility</groupId>
    <artifactId>utility-fx-web</artifactId>
</dependency>
```

**Description:**
This module provides web-related utilities for JavaFX.

## Samples and benchmarks

Sample and benchmark projects are not published library artifacts and are not managed by `utility-bom`. Use the
published library modules above in applications and other consumers.
