# Releasing

The utility build publishes a coherent BOM on every release. Major and minor releases publish every library module;
patch releases publish only changed modules and the BOM. The BOM constrains each module to the version that actually
exists in Maven Central.

`gradle/release-state.toml` records the last successfully published version and source revision of every library
module. `gradle/prepared-release.toml` is a short-lived, committed candidate plan. It is the only source of truth for
staging and publishing a prepared release.

## Prerequisites

- A clean checkout on the release branch, with the required source revisions available locally.
- Maven Central and signing credentials available only in the protected release environment. The build reads
  `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`, `SIGNING_PUBLIC_KEY`, `SIGNING_SECRET_KEY`, and `SIGNING_PASSWORD`.
- A complete Gradle test environment, including the configured JDK toolchains.

## Prepare a release

First review a dry-run plan. The release type is required; `releaseVersion` is optional when the conventional next
version is wanted.

```bash
./gradlew prepareRelease -PreleaseType=patch
./gradlew prepareRelease -PreleaseType=minor
./gradlew prepareRelease -PreleaseType=major -PreleaseVersion=24.0.0
```

The task validates the Git history, scoped changes, release-line version, and Maven Central coordinate availability.
For a patch release it fails when no library changed; it will not create a BOM-only release. Use
`-PadditionalReleaseModules=module-a,module-b` only when an unchanged dependent must publish a new minimum internal
dependency version.

After reviewing the output, persist the exact candidate plan:

```bash
./gradlew prepareRelease -PreleaseType=patch -PconfirmRelease=true
git add gradle/prepared-release.toml
git commit -m "Prepare release X.Y.Z"
```

Run subsequent release commands in a new Gradle invocation so the committed plan is used to configure the BOM and
every module version.

## Verify, stage, and publish

```bash
./gradlew --no-configuration-cache verifyPreparedRelease checkReleaseCompatibility
./gradlew --no-configuration-cache stagePreparedRelease
./gradlew --no-configuration-cache publishPreparedRelease
```

These release-only tasks operate on live Git, Maven Central, signing, and staging state, so they intentionally run
without the configuration cache. Normal development and test tasks continue to use the configured cache.

`checkReleaseCompatibility` is mandatory for a patch release. It compares each selected module's public/protected
binary API and module descriptor with its own last Maven Central artifact. `stagePreparedRelease` runs the full
library test suite, clears stale staging output, and stages only the selected libraries plus the BOM.
`publishPreparedRelease` then invokes JReleaser to deploy that staged set to Maven Central.

Do not create the final release tag at preparation time.

## Finalize

After Maven Central exposes every expected BOM and module artifact, finalize the release:

```bash
./gradlew --no-configuration-cache finalizeRelease -PconfirmFinalize=true
```

This updates `gradle/release-state.toml`, advances `projectVersion` in `gradle/version.toml` to the next patch
snapshot, removes the prepared plan, commits the published state, and creates the annotated `vX.Y.Z` tag. To push
the new commit and tag from a protected environment, add
`-PpushReleaseTag=true -PreleaseBranch=main` (substitute the protected release branch).

If Maven Central deployment succeeds but finalization fails, rerun `finalizeRelease`; it can create a missing final
tag without republishing artifacts. If deployment is interrupted, first determine whether any target coordinate was
accepted. Retry the same prepared plan only when the Central outcome clearly permits it. Once any coordinate has been
accepted, it is immutable: prepare a corrected release with a new patch version.

## Snapshot development

Snapshots are intentionally not uploaded to Maven Central. All library modules and the BOM can be published to the
local Maven repository with:

```bash
./gradlew publishSnapshotsToMavenLocal
```

Normal development continues to use the `projectVersion` snapshot in `gradle/version.toml`. A prepared release plan
overrides that development version only for the release build.

## Release CI

`.github/workflows/release.yml` is the only workflow that receives Maven Central and signing credentials. Dispatch it
for the protected branch that contains the committed prepared plan. It publishes the plan and then finalizes the
published state and tag; ordinary CI never receives release credentials or modifies release files.
