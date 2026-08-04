# Release Workflow

This is the operator runbook for publishing a new utility release. It implements the selective-publishing model in
[SELECTIVE-PUBLISHING-CONCEPT.md](SELECTIVE-PUBLISHING-CONCEPT.md): a patch release publishes the changed libraries
and a new BOM; a major or minor release publishes every library and the BOM.

Do not edit `gradle/release-state.toml` by hand and do not create a release tag before publication. The Gradle tasks
and the protected GitHub Actions workflow manage both.

## 1. Choose the release version

Work on the protected release branch (normally `main`) and start with a clean, up-to-date checkout:

```bash
git switch main
git pull --ff-only
git status --short
```

The last command must produce no output. `prepareRelease` rejects a dirty tree.

Choose one release type:

| Release type | Default new version                          | Published artifacts       |
|--------------|----------------------------------------------|---------------------------|
| Patch        | Next `X.Y.Z` on the current BOM release line | Changed libraries and BOM |
| Minor        | Next `X.Y.0`                                 | Every library and BOM     |
| Major        | Next `X.0.0`                                 | Every library and BOM     |

Generate a dry-run plan. Supply `releaseVersion` only when the default next version is not the intended version.

```bash
./gradlew prepareRelease -PreleaseType=patch
./gradlew prepareRelease -PreleaseType=patch -PreleaseVersion=23.1.4
./gradlew prepareRelease -PreleaseType=minor
./gradlew prepareRelease -PreleaseType=major -PreleaseVersion=24.0.0
```

Review the printed BOM version, selected modules, retained module versions, source revision, and selection reasons.
For a patch release, the task fails if no library changed; a BOM-only patch release is intentionally forbidden.

Do not manually set `projectVersion` in `gradle/version.toml` for the release. Finalization advances it to the next
patch snapshot only after Maven Central publication succeeds.

## 2. Run tests before preparing the release

Run the normal verification suite locally:

```bash
./gradlew clean check
```

Resolve all failures before continuing. The protected release workflow runs these checks again while staging the
candidate, so the local run is an early safety check rather than the sole release validation.

## 3. Persist and review the prepared plan

Once the dry-run plan is correct, write the candidate plan:

```bash
./gradlew prepareRelease -PreleaseType=patch -PconfirmRelease=true
```

This creates `gradle/prepared-release.toml`. Commit and push that exact plan:

```bash
git add gradle/prepared-release.toml
git commit -m "Prepare release X.Y.Z"
git push origin main
```

Use the actual version shown by `prepareRelease` in the commit message. The prepared plan is the release input: do
not make further source, build, dependency, or version changes on the branch before publishing it.

Optionally perform the release-specific local checks:

```bash
./gradlew verifyPreparedRelease checkReleaseCompatibility
```

For patch releases, `checkReleaseCompatibility` compares each selected library with its own previously published
artifact and rejects binary/API-incompatible changes.

## 4. Start the protected GitHub Actions release workflow

Only the `Publish prepared release` GitHub Actions workflow has Maven Central and signing credentials.

1. Open the repository on GitHub and select **Actions**.
2. Select **Publish prepared release**.
3. Click **Run workflow**.
4. In the workflow-branch selector, choose the protected branch containing the committed prepared plan.
5. Set the required `branch` input to that same branch (normally `main`).
6. Click **Run workflow** and monitor the job to completion.

The workflow runs `publishPreparedRelease`, which verifies the plan, executes the library checks and patch
compatibility checks, stages only the selected libraries plus the BOM, signs the artifacts, and deploys them to Maven
Central. It then runs `finalizeRelease`, which verifies Central availability, updates `gradle/release-state.toml`,
sets the next development snapshot in `gradle/version.toml`, removes the prepared plan, commits those changes, and
pushes the final `vX.Y.Z` tag.

## 5. Verify completion

After the workflow succeeds:

```bash
git switch main
git pull --ff-only
git status --short
git tag --list 'vX.Y.Z'
```

Confirm that the finalization commit contains the updated release state and next snapshot version, that
`gradle/prepared-release.toml` is absent, and that the BOM and every selected module are available from Maven
Central. Do not expect unchanged modules to have a new artifact version; the released BOM selects their retained
published versions.

## Recovery

Never reuse a Maven Central coordinate. If deployment fails, first determine whether any target coordinate was
accepted:

- If nothing was accepted and Central/JReleaser confirms the deployment can be resumed, rerun the same prepared
  release workflow.
- If any coordinate was accepted, correct the problem and prepare a new patch version.
- If deployment succeeded but finalization did not, rerun finalization in the protected release environment; do not
  republish the artifacts.

For additional task-level detail, see [doc/RELEASING.md](doc/RELEASING.md).
