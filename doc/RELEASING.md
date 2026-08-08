# Releasing

The utility build publishes a coherent BOM on every release. Major and minor releases publish every library module;
patch releases publish changed modules and the BOM. A patch containing only a publication-relevant dependency-catalog
change publishes the BOM alone. The BOM constrains each module to the version that actually exists in Maven Central.

`gradle/release-state.toml` records the last successfully published version and source revision of every library
module. `gradle/prepared-release.toml` is a short-lived, committed candidate plan. It is the only source of truth for
staging and publishing a prepared release.

## Prerequisites

- A clean checkout on the release branch, with the required source revisions available locally.
- A GitHub-protected release branch. Only a prepared plan pushed to such a branch can start publication automatically.
- Maven Central and signing credentials available only in the protected release environment. The build reads
  `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`, `SIGNING_SECRET_KEY`, and `SIGNING_PASSWORD`.
- A complete Gradle test environment, including the configured JDK toolchains.

## Prepare a release

Use the interactive preparation script. The release type is required; `--version` is optional when the conventional
next version is wanted.

```bash
./scripts/prepare-release.sh --type patch
./scripts/prepare-release.sh --type minor
./scripts/prepare-release.sh --type major --version 24.0.0
```

The script requires a clean branch that matches its upstream. It displays the same dry-run plan and validates Git
history, scoped changes, release-line version, and Maven Central coordinate availability. Gradle lockfile-only changes
do not select a module: they make dependency resolution reproducible but are not published module metadata. A
publication-relevant `gradle/version.toml` change can create a BOM-only patch release; use
`--additional-modules module-a,module-b` only when an unchanged dependent must publish a new minimum internal
dependency version.

After the dry run, the script asks whether to create and commit `gradle/prepared-release.toml`. It writes the target
release version to `gradle/version.toml` in the same commit, so CI compiles with release instrumentation rather than
snapshot instrumentation. It then asks a second time before pushing that commit. Answering yes to the second prompt
is the explicit authorization to start the release: the push to the protected release branch triggers the protected
GitHub Actions workflow. Answering no leaves the prepared plan committed locally, with no release started.

The prepared-plan commit must remain the tip of the release branch. The workflow rejects a candidate if another
commit is pushed before it begins publication.

## Verify, stage, and publish

```bash
./gradlew --no-configuration-cache verifyPreparedRelease checkReleaseCompatibility
./gradlew --no-configuration-cache stagePreparedRelease
./gradlew --no-configuration-cache publishPreparedRelease
```

These release-only tasks operate on live Git, Maven Central, signing, and staging state, so they intentionally run
without the configuration cache. Normal development and test tasks continue to use the configured cache.

The protected workflow normally performs the plan and compatibility checks after normal CI has built and tested the
candidate. It promotes the exact CI publication bundle, so it does not run the library test suite again. Run these
commands locally only for diagnosis or when following an approved recovery procedure.

`checkReleaseCompatibility` is mandatory for a patch release. It compares each selected module's public/protected
binary API and module descriptor with its own last Maven Central artifact. `stagePreparedRelease` runs the full
library test suite, clears stale staging output, and stages only the selected libraries plus the BOM. The protected
workflow uses `publishPreparedReleaseFromCi` instead: normal CI creates and tests an unsigned bundle, and the release
workflow verifies and signs that exact bundle before invoking JReleaser.

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

The protected workflow normally finalizes the release and pushes this commit and tag automatically. Invoke
`finalizeRelease` manually only for recovery after confirming the Maven Central outcome.

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

Normal development continues to use the `projectVersion` snapshot in `gradle/version.toml`. Preparing a release
temporarily replaces it with the stable release version; finalization advances it to the next patch snapshot.

## Release CI

`.github/workflows/release.yml` is the only workflow that receives Maven Central and signing credentials. A push of a
committed prepared plan to a protected release branch starts normal CI; only a successful CI run starts publication.
The release workflow downloads the checksummed bundle from that exact run, verifies the plan and artifacts, publishes
without rebuilding or testing, then finalizes the published state and tag. It retains manual dispatch to retry a
committed prepared plan by supplying the successful CI run ID; ordinary CI never receives release credentials or
modifies release files.
