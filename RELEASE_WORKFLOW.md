# Release Workflow

This is the operator runbook for publishing a new utility release. It implements the selective-publishing model in
[SELECTIVE-PUBLISHING-CONCEPT.md](doc/SELECTIVE-PUBLISHING-CONCEPT.md): a patch release publishes the changed libraries
and a new BOM; a major or minor release publishes every library and the BOM.

Do not edit `gradle/release-state.toml` by hand and do not create a release tag before publication. The Gradle tasks
and the protected GitHub Actions workflow manage both.

## 1. Choose the release version

Work on a GitHub-protected release branch (normally `main`) and start with a clean, up-to-date checkout:

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

The preparation script generates the dry-run plan first. Supply `--version` only when the default next version is not
the intended version.

```bash
./scripts/prepare-release.sh --type patch
./scripts/prepare-release.sh --type patch --version 23.1.4
./scripts/prepare-release.sh --type minor
./scripts/prepare-release.sh --type major --version 24.0.0
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

## 3. Persist, confirm, and push the prepared plan

The script first asks whether it should write and commit `gradle/prepared-release.toml`. It then asks a second time
before pushing the prepared-plan commit to the current branch's upstream. Answer yes to that second prompt only when
you authorize Maven Central publication. Answer no to keep the committed candidate local for later review.

The script commits the plan as `Prepare release X.Y.Z`, using the version recorded in the plan. The prepared plan is
the release input: do not make further source, build, dependency, or version changes on the branch before pushing it.
The workflow also requires that this commit remains the branch tip when publication begins.

Optionally perform the release-specific local checks:

```bash
./gradlew --no-configuration-cache verifyPreparedRelease checkReleaseCompatibility
```

For patch releases, `checkReleaseCompatibility` compares each selected library with its own previously published
artifact and rejects binary/API-incompatible changes.

Release validation, staging, publishing, and finalization operate on live Git, Maven Central, signing, and staging
state. Run those release-only tasks with `--no-configuration-cache`; normal development and test tasks continue to
use the project's configured configuration cache.

## 4. Monitor the protected GitHub Actions release workflow

Only the `Publish prepared release` GitHub Actions workflow has Maven Central and signing credentials. Pushing a
prepared plan to a protected release branch starts it automatically.

1. Open the repository on GitHub and select **Actions**.
2. Select **Publish prepared release**.
3. Monitor the run for the prepared-plan push until it completes.

The workflow can still be started manually to retry a committed prepared plan. Select the protected branch that
contains the plan and provide the same branch in its required `branch` input.

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
