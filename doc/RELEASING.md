# Releasing

This document describes the process for releasing new versions of the utility libraries.

## Prerequisites

Before you can release a new version, you need to have the following:

1. A GPG key for signing the artifacts
2. Access to the Sonatype Central Publisher Portal
3. The following variables set in your `~/.gradle/gradle.properties` file:
    - `sonatypeUsername`: Your Sonatype Central Publisher Portal username
    - `sonatypePassword`: Your Sonatype Central Publisher Portal password
    - `signing.keyId`: Your GPG key ID (last 8 characters of the key ID)
    - `signing.password`: Your GPG key passphrase
    - `signing.secretKeyRingFile`: Path to your GPG secret key ring file

## Release Process

The release process uses [JReleaser](https://jreleaser.org/) to automate the release process. JReleaser handles the
following tasks:

1. Signing the artifacts
2. Uploading the artifacts to Maven Central
3. Closing and releasing the staging repository

### Steps to Release

1. **Prepare the Release**

   Update the version in `settings.gradle.kts` to a non-SNAPSHOT version:

   ```kotlin
   val projectVersion = "X.Y.Z" // Replace X.Y.Z with the version you want to release
   ```

2. **Commit and Tag the Release**

   ```bash
   git add settings.gradle.kts
   git commit -m "Release version X.Y.Z"
   git tag -a vX.Y.Z -m "Release version X.Y.Z"
   ```

3. **Build the Project**

   ```bash
   ./gradlew clean build
   ```

4. **Publish to Maven Central**

   ```bash
   ./gradlew jreleaserDeploy
   ```

   This command will:
    - Build the project
    - Sign the artifacts
    - Upload the artifacts to Maven Central
    - Close and release the staging repository

5. **Verify the Release**

   Check that the artifacts are available on Maven Central:
   https://repo1.maven.org/maven2/com/dua3/utility/

6. **Prepare for Next Development Iteration**

   Update the version in `settings.gradle.kts` to the next SNAPSHOT version:

   ```kotlin
   val projectVersion = "X.Y.Z-SNAPSHOT" // Replace X.Y.Z with the next version
   ```

7. **Commit the Changes**

   ```bash
   git add settings.gradle.kts
   git commit -m "Prepare for next development iteration"
   git push
   git push --tags
   ```

## Publishing Snapshots

Snapshots are development versions that can be published to the Sonatype Snapshots repository for testing before an
official release.

### Prerequisites

The prerequisites for publishing snapshots are the same as for releasing:

1. A GPG key for signing the artifacts (optional for snapshots)
2. Access to the Sonatype Central Publisher Portal
3. The following variables set in your `~/.gradle/gradle.properties` file:
    - `sonatypeUsername`: Your Sonatype Central Publisher Portal username
    - `sonatypePassword`: Your Sonatype Central Publisher Portal password
    - `signing.keyId`: Your GPG key ID (last 8 characters of the key ID)
    - `signing.password`: Your GPG key passphrase
    - `signing.secretKeyRingFile`: Path to your GPG secret key ring file

### Steps to Publish Snapshots

1. **Ensure the Version is a SNAPSHOT Version**

   Make sure the version in `settings.gradle.kts` is a SNAPSHOT version:

   ```kotlin
   val projectVersion = "X.Y.Z-SNAPSHOT" // Replace X.Y.Z with the version you want to publish
   ```

2. **Build the Project**

   ```bash
   ./gradlew clean build
   ```

3. **Publish to Sonatype Snapshots Repository**

   ```bash
   ./gradlew publish
   ```

   This command will:
    - Build the project
    - Sign the artifacts (if configured)
    - Upload the artifacts to the Sonatype Snapshots repository

4. **Verify the Snapshot**

   Check that the artifacts are available on the Sonatype Snapshots repository:
   https://central.sonatype.com/repository/maven-snapshots/com/dua3/utility/

## Advanced Configuration

### Generating JReleaser Configuration

You can generate a JReleaser configuration file for reference by running:

```bash
./gradlew generateJReleaserConfig
```

This will create a `jreleaser-config.yml` file in the project root directory that contains the complete JReleaser
configuration. You can use this file as a reference to understand the JReleaser configuration or to customize it
further.

## Troubleshooting

If you encounter issues during the release process, you can check the JReleaser logs in the `build/jreleaser` directory.

For more detailed information about JReleaser, see
the [JReleaser documentation](https://jreleaser.org/guide/latest/index.html).
