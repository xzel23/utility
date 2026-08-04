# Selective Maven Publishing Concept

## Goal

Reduce Maven Central artifacts for patch releases by publishing only the utility modules that changed, while retaining a BOM that defines a coherent set of module versions.

A major or minor release remains a full release of all publishable modules.

## Release model

Versions use `major.minor.patch`.

| Release type | BOM version | Module versions | Published modules |
|---|---:|---|---|
| Major release | `X.0.0` | Every module becomes `X.0.0` | BOM and all modules |
| Minor release | `X.Y.0` | Every module becomes `X.Y.0` | BOM and all modules |
| Patch release | `X.Y.Z` | Changed modules become `X.Y.Z`; unchanged modules retain their previous version | BOM and changed modules |

For example, starting from a full `23.2.0` release:

```text
    Patch release 23.2.1
        utility 23.2.1 changed
        utility-db 23.2.0 unchanged
        utility-fx 23.2.1 changed
        utility-fx-controls 23.2.0 unchanged
        utility-bom 23.2.1 always published
```

The `23.2.1` BOM must constrain each module to its actual published version, rather than assigning the BOM version to every module.

## Published release state and prepared release plan

Keep the version-controlled published release state at the repository level:

    gradle/release-state.toml

The state belongs to the release definition, not to an individual module. It records only releases that have been
successfully published to Maven Central. It contains:

- The current BOM version.
- Each publishable module's current published version.
- The source revision represented by the last publication of each module.
- Optional module ownership paths used to determine whether a module changed.

Example structure:

```toml
[release]
schemaVersion = 1
bomVersion = "23.2.1"

[modules.utility]
version = "23.2.1"
publishedRevision = "a1b2c3d4e5f6"
paths = ["utility"]

[modules.utility-db]
version = "23.2.0"
publishedRevision = "0123456789ab"
paths = ["utility-db"]

[modules.utility-fx]
version = "23.2.1"
publishedRevision = "fedcba987654"
paths = ["utility-fx"]
```

A distinct, version-controlled prepared release plan, for example `gradle/prepared-release.toml`, records a candidate
release before publication. It contains the source revision, exact BOM and module coordinates, selected modules and
their selection reasons, and the expected artifact set. The plan is the sole input to publication and makes retrying
the same prepared release reproducible.

The BOM module is special:

- Its version is `release.bomVersion`.
- It is always included in a patch release because its constraints change.
- Its `publishedRevision` is useful for auditability but is not required to decide whether to publish it.

## Change detection

### Do not compare against repository `HEAD`

Comparing a module's saved commit ID directly with the current repository `HEAD` would incorrectly treat every module as changed whenever any unrelated file is committed.

Instead, compare the changes in a module's owned paths between its saved `publishedRevision` and the release commit being prepared:

```text
    git diff --quiet .. --
```


A module is changed when this command finds differences.

This allows a repository commit affecting only `utility/` to leave `utility-db`, `utility-fx`, and other independent modules unchanged.

### Shared build and release inputs

Changes outside a module directory may still affect its published artifact. Therefore, change detection must incorporate shared ownership rules.

Use a declarative mapping from paths to affected modules. This makes the policy auditable and avoids hidden release
behavior. Initially use the following conservative rules:

1. A module owns its directory, including production sources and resources, its build script, and module-specific
   generated-input configuration.
2. The root build configuration, Gradle settings, wrapper/toolchain configuration, and shared build or
   publishing/signing logic select all publishable library modules unless a narrower mapping is proven safe.
3. A version-catalog or dependency-lock change selects every module whose resolved compile/runtime dependency graph
   or published dependency metadata uses the changed entry. Until those relationships are mapped, select all
   publishable library modules.
4. The BOM build directory, the published release state, and the prepared release plan select only the BOM. A
   release-state or plan update alone must never select a library module.
5. Repository documentation, CI-only configuration, and repository-administration changes select no library modules.
   Documentation in a module's production source files does select that module because published sources and Javadoc
   artifacts change.

## Dependency propagation

An unchanged dependent module is not republished merely because an internal dependency has received a compatible
patch. Its existing POM and Gradle module metadata correctly describe the version against which it was published;
the new BOM deliberately aligns consumers to the patched dependency.

The release-preparation task must build the internal project-dependency graph and select a dependent module only
when it changed itself or must publish a new minimum dependency version. This applies equally to `api`,
`implementation`, and optional dependencies. Shaded or embedded dependencies require selection only if the dependent
artifact itself changes. Required patch compatibility checks and the full candidate-BOM test suite validate that
leaving an unchanged dependent at its existing version is safe.

## Gradle model changes

The current build assigns one version to every project. This must be replaced with module-specific version assignment.

At configuration time, Gradle should:

1. Read the published release state and, when present, use the prepared release plan as the candidate-version
   override.
2. Assign the BOM project the current BOM version.
3. Assign each publishable module its version from the matching module entry.
4. Configure BOM constraints using the stored version of each module, not `project.version`.
5. Configure publication tasks only for modules selected in the prepared release plan.

The resulting BOM constraints must conceptually be equivalent to:

```text
    com.dua3.utility:utility:23.2.1
    com.dua3.utility:utility-db:23.2.0
    com.dua3.utility:utility-fx:23.2.1
```


## Release workflow

### 1. Prepare release

Provide a dedicated Gradle task, for example:

```text
    prepareRelease
```


Inputs:

- Requested release type: `patch`, `minor`, or `major`.
- Optional requested target version.
- The Git revision to release, normally clean `HEAD`.

Validation:

- Working tree is clean.
- Release revision is available locally.
- Each stored `publishedRevision` exists and is an ancestor of the release revision.
- No module version would overwrite an existing Maven Central artifact.
- The selected version is a non-snapshot release version.
- For patch releases, the major/minor version matches the existing release line.
- A patch release selects at least one library module; otherwise no release is prepared.
- Required binary/API compatibility checks pass for every selected library module.

Behavior for a patch release:

1. Determine directly changed modules using path-scoped Git diffs.
2. Select dependents only when they changed themselves or must publish a new minimum internal dependency version.
3. Increment the patch component for the BOM and selected modules.
4. Retain versions for unselected modules.
5. Generate and display a release plan.
6. Require an explicit confirmation flag before creating the prepared release plan.

Behavior for major or minor releases:

1. Set every publishable module and the BOM to the requested `X.Y.0` version.
2. Select every publishable module.
3. Generate a full-release plan.

The generated plan should list:

- BOM version.
- Modules to publish and their old/new versions.
- Unchanged modules and retained versions.
- The Git revision being released.
- Reasons a module was selected, such as direct change or dependency propagation.
- Expected published coordinates and artifacts.

### 2. Commit prepared release plan

After an approved release plan, create `gradle/prepared-release.toml` with:

- The new BOM version and every selected module's new version.
- The release source revision represented by each selected module.
- The expected artifact coordinates and selected-module reasons.

Commit the plan as a release-preparation commit. The build uses this plan to assign candidate versions and to create
the BOM constraints. It is not yet published release state.

The recorded source revision must identify the source from which module artifacts are built, not necessarily the
commit that contains the prepared plan. This avoids the self-referential commit-ID problem.

Recommended sequence:

```text
    R = clean source commit selected for release
    prepareRelease records R in a prepared release plan
    commit the prepared plan
    publish artifacts built from the prepared-release commit
    finalize the release state and tag the finalized commit after successful deployment
```

On the next patch release, module comparisons use the published source revision `R` and scoped module paths. A
prepared-plan or release-state-only commit does not make unrelated modules appear changed.

### 3. Publish

Provide a root task, for example:

```text
    publishPreparedRelease
```


This task must:

1. Verify that the selected modules match the persisted release plan.
2. Run validation and tests.
3. Publish only the selected library modules plus the BOM to the staging repository.
4. Invoke the existing Maven Central release process for the staged artifacts.
5. Fail without promoting the prepared plan to published release state if staging or deployment fails.

Publishing must not derive its selected modules from an ad-hoc local Git state. It must use the persisted prepared plan to make CI execution reproducible.

### 4. Finalize

After Maven Central publication succeeds:

1. Verify that every expected BOM and module artifact is available in the target repository.
2. Promote the prepared plan to `gradle/release-state.toml`, updating the BOM and selected modules' published
   versions and source revisions, and remove the prepared plan.
3. Advance the development `projectVersion` to the next patch snapshot.
4. Commit the published state, then create and push a Git tag for the BOM/release version.
5. Optionally record publication timestamps and repository URLs in a separate immutable release history file.

The release tag therefore always means that the named artifacts are published and available. A transport failure with
an unambiguous staging/deployment outcome may retry the exact prepared plan. If any Maven Central coordinate was
accepted, that coordinate is permanently consumed; resolve the failure with a new patch version and plan. Maven
Central versions must never be reused.

If deployment succeeded but committing the published state or creating the tag fails, resume finalization without
republishing artifacts. Finalization must be idempotent.

## CI integration

The release workflow should be separate from ordinary CI.

Normal CI:

- Builds and tests all affected projects as currently required.
- Does not modify release state.
- Does not publish releases.

Release CI:

1. Is manually dispatched or triggered by a protected release branch or prepared-release commit.
2. Checks out the exact prepared release commit.
3. Verifies the release plan.
4. Runs the selected publication tasks.
5. Deploys only the generated staging artifacts.
6. Finalizes the release state and tags the finalized commit only after successful deployment.

Credentials for signing and Maven Central deployment must remain available only to the protected release workflow.

## Suggested implementation phases

### Phase 1: Version model and BOM

- Add the release-state file.
- Add the prepared-release plan schema.
- Replace global subproject version assignment with per-module versions.
- Generate BOM constraints from release state.
- Retain existing behavior by initializing every module to the same version.

### Phase 2: Release planning

- Implement Git revision and path-scoped change detection.
- Implement direct module selection.
- Produce a human-readable release plan without changing files.
- Add validation for clean working trees and stored revisions.

### Phase 3: Dependency propagation

- Obtain the internal Gradle project dependency graph.
- Implement the minimum-version dependency-selection rule.
- Add tests for direct, transitive, `api`, implementation, and shaded dependency changes.

### Phase 4: Controlled publishing

- Limit staging publication to selected projects and the BOM.
- Persist the selected plan and promote it to published state only after deployment.
- Add a release CI workflow with protected credentials.
- Verify staged and deployed artifact sets.

### Phase 5: Migration

- Initialize each module's version and `publishedRevision` from the latest full release.
- Perform one full release using the new mechanism before relying on selective patch publishing.
- Document the operator workflow and recovery process.

## Todos / decisions required

- [x] Define the authoritative list of publishable modules. Should sample modules be excluded permanently?
- [x] Define module ownership paths, especially for shared build logic and dependency-lock files.
- [x] Decide whether documentation-only changes should cause a module republish.
- [x] Define dependency propagation rules for internal dependencies.
- [x] Decide whether a patch release must increment from the highest existing patch across all modules, or whether 
  only changed modules advance independently within a release line.
- [x] Decide whether snapshots use the same selective-publication model or continue publishing all modules.
- [x] Decide the exact Git-tagging point: before deployment, after deployment, or via a separate finalization workflow.
- [x] Define recovery behavior when Maven Central deployment partially succeeds or is rejected.
- [x] Confirm whether artifact compatibility checks are required before a module is selected for publication.
- [x] Confirm whether the release-state file should remain in `gradle/` or be located under the BOM module directory while still governing the entire multi-project build.

## Decisions

- **Define the authoritative list of publishable modules. Should sample modules be excluded permanently?**

  Samples and benchmarks should be permanently excluded. All library modules and the BOM should be included.

- **Define module ownership paths, especially for shared build logic and dependency-lock files.**

  Use the conservative declarative mapping defined under **Shared build and release inputs**. Each module owns its
  directory; shared build, toolchain, publishing, settings, version-catalog, and dependency-lock inputs select all
  affected modules (all library modules until a narrower mapping is proven safe). The BOM build directory and release
  metadata select only the BOM.

- **Decide whether documentation-only changes should cause a module republish.**

  Repository documentation, CI documentation, and release notes do not. Documentation in a module's production
  source files does, because it changes the published sources and Javadoc artifacts.

- **Define dependency propagation rules for internal dependencies.**

  Do not republish an unchanged dependent only because an internal dependency received a compatible patch. Select it
  only when it changed itself or must publish a new minimum internal dependency version. This applies to `api`,
  `implementation`, and optional dependencies; shaded or embedded dependencies require selection only when the
  dependent artifact changes.

- **Decide whether a patch release must increment from the highest existing patch across all modules, or whether only 
  changed modules advance independently within a release line.**

  A selected module uses the same version as the BOM. The BOM patch number is globally monotonic within its release
  line; an unchanged module retains its last published version. Do not prepare a BOM-only patch release.

- **Decide whether snapshots use the same selective-publication model or continue publishing all modules.**

  Snapshots should only be published to the local Maven repository. It is not necessary to use the selective model. 
  This should be done the way of least effort.

- **Decide the exact Git-tagging point: before deployment, after deployment, or via a separate finalization workflow.**

  Commit a prepared release plan before publication, but create the final release tag only after Maven Central
  deployment succeeds and the published state is finalized. A temporary, clearly named prepared-release tag may be
  used if CI requires one.

- **Define recovery behavior when Maven Central deployment partially succeeds or is rejected.**

  Retry the exact prepared plan only after confirming that no Maven Central coordinate was accepted, or when the
  staging/deployment outcome unambiguously supports resumption. If any coordinate was accepted, it is permanently
  consumed; create a corrected plan with a new patch version. Do not create the final release tag until deployment
  succeeds.

- **Confirm whether artifact compatibility checks are required before a module is selected for publication.**

  Yes, for patch releases. Compare every selected module with its own last published artifact and fail on binary/API
  incompatible public or protected API and module-descriptor changes. Also run the full build and test suite against
  the candidate BOM. An intentional incompatible change requires a minor or major release.

- **Confirm whether the release-state file should remain in `gradle/` or be located under the BOM module directory 
  while still governing the entire multi-project build.**

  Keep the published release state at `gradle/release-state.toml`, next to the other root build inputs. It governs the
  entire multi-project build; the BOM consumes it but does not own it. Keep the candidate plan separately at
  `gradle/prepared-release.toml`.

## Ongoing
