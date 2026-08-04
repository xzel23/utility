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

## Release state

Introduce a version-controlled release-state file at the repository level, for example:

    text gradle/release-state.toml

The state belongs to the BOM/release definition, not to individual modules. It contains:

- The current BOM version.
- Each publishable module's current published version.
- The source revision represented by the last publication of each module.
- Optional module ownership paths used to determine whether a module changed.

Example structure:

```toml
    [release] bomVersion = "23.2.1"
    [modules.utility] version = "23.2.1" publishedRevision = "a1b2c3d4e5f6" paths = ["utility"]
    [modules.utility-db] version = "23.2.0" publishedRevision = "0123456789ab" paths = ["utility-db"]
    [modules.utility-fx] version = "23.2.1" publishedRevision = "fedcba987654" paths = ["utility-fx"]
```

The BOM module is special:

- Its version is `release.bomVersion`.
- It is always included in a patch release because the BOM's constraints and release-state metadata change.
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

Initially, the following files should be evaluated explicitly:

- Root build configuration.
- Gradle settings.
- Version catalog and dependency locks.
- Shared publishing/signing configuration.
- The release-state file itself.

Recommended policy:

1. **Changes to build logic that affect artifact contents, publication metadata, compilation, or dependencies** mark all affected modules as changed.
2. **Changes to release-state metadata made solely to record a successful release** must not cause another release.
3. **Documentation, CI-only, and repository-administration changes** do not mark library modules as changed unless deliberately configured to do so.
4. **Dependency changes** mark every module whose resolved published metadata or artifact can change.

The implementation should use a declarative mapping from shared paths to affected modules. This makes the policy auditable and avoids hidden release behavior.

## Dependency propagation

Directly changed modules are not always the only modules that must be republished.

If module `A` changes and module `B` exposes or embeds `A` in its published API or dependency metadata, `B` may also need a new publication. The release-preparation task must therefore:

1. Detect directly changed modules.
2. Build the graph of internal project dependencies.
3. Add dependent modules when their published POM, module metadata, or runtime artifact would reference a new version of a dependency.
4. Repeat until no additional module is added.

For example:

```text
    utility changes from 23.2.0 to 23.2.1 utility-fx depends on utility and publishes that dependency
    Result: utility -> 23.2.1 utility-fx -> 23.2.1
```

The exact propagation policy must distinguish between:

- `api` dependencies, which generally require propagation.
- `implementation` dependencies, which may still require propagation because Maven metadata exposes runtime dependencies.
- Dependencies that are shaded or otherwise not represented in published metadata.

## Gradle model changes

The current build assigns one version to every project. This must be replaced with module-specific version assignment.

At configuration time, Gradle should:

1. Read the release-state file.
2. Assign the BOM project the current BOM version.
3. Assign each publishable module its version from the matching module entry.
4. Configure BOM constraints using the stored version of each module, not `project.version`.
5. Configure publication tasks only for modules selected for the prepared release.

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

Behavior for a patch release:

1. Determine directly changed modules using path-scoped Git diffs.
2. Apply dependency propagation.
3. Increment the patch component for the BOM and selected modules.
4. Retain versions for unselected modules.
5. Generate and display a release plan.
6. Require an explicit confirmation flag before modifying release state.

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

### 2. Commit release state

After an approved release plan, update `gradle/release-state.toml` with:

- The new BOM version.
- Each selected module's new version.
- The release source revision as `publishedRevision` for each selected module.

Commit the state update as a release-preparation commit.

The recorded `publishedRevision` must identify the source revision from which artifacts are built, not necessarily the commit that contains the updated state file. This avoids the self-referential commit-ID problem.

Recommended sequence:

```text
    R = clean source commit selected for release prepareRelease records R in release state commit release-state update 
  publish artifacts built from the release-preparation commit tag the release-preparation commit
```


On the next patch release, module comparisons use the saved source revision `R` and scoped module paths. The release-state-only commit does not make unrelated modules appear changed.

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
5. Fail without modifying release state if staging or deployment fails.

Publishing must not derive its selected modules from an ad-hoc local Git state. It must use the persisted prepared plan to make CI execution reproducible.

### 4. Finalize

After Maven Central publication succeeds:

1. Verify that every expected BOM and module artifact is available in the target repository.
2. Create and push a Git tag for the BOM/release version.
3. Optionally record publication timestamps and repository URLs in a separate immutable release history file.

The release-state update should occur before publishing so that the build is reproducible. A failed publication is handled by correcting the problem and producing a new patch version; Maven Central versions must never be reused.

## CI integration

The release workflow should be separate from ordinary CI.

Normal CI:

- Builds and tests all affected projects as currently required.
- Does not modify release state.
- Does not publish releases.

Release CI:

1. Is manually dispatched or triggered by a protected release tag/branch.
2. Checks out the exact prepared release commit.
3. Verifies the release plan.
4. Runs the selected publication tasks.
5. Deploys only the generated staging artifacts.
6. Tags the commit only after successful deployment, if tagging is not performed earlier.

Credentials for signing and Maven Central deployment must remain available only to the protected release workflow.

## Suggested implementation phases

### Phase 1: Version model and BOM

- Add the release-state file.
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
- Implement propagation rules.
- Add tests for direct, transitive, `api`, and runtime dependency changes.

### Phase 4: Controlled publishing

- Limit staging publication to selected projects and the BOM.
- Persist the selected plan.
- Add a release CI workflow with protected credentials.
- Verify staged and deployed artifact sets.

### Phase 5: Migration

- Initialize each module's version and `publishedRevision` from the latest full release.
- Perform one full release using the new mechanism before relying on selective patch publishing.
- Document the operator workflow and recovery process.

## Todos / decisions required

- [x] Define the authoritative list of publishable modules. Should sample modules be excluded permanently?
- [ ] Define module ownership paths, especially for shared build logic and dependency-lock files.
- [x] Decide whether documentation-only changes should cause a module republish.
- [x] Define dependency propagation rules for `api`, `implementation`, optional dependencies, and shaded dependencies.
- [x] Decide whether a patch release must increment from the highest existing patch across all modules, or whether 
  only changed modules advance independently within a release line.
- [x] Decide whether snapshots use the same selective-publication model or continue publishing all modules.
- [x] Decide the exact Git-tagging point: before deployment, after deployment, or via a separate finalization workflow.
- [x] Define recovery behavior when Maven Central deployment partially succeeds or is rejected.
- [ ] Confirm whether artifact compatibility checks are required before a module is selected for publication.
- [ ] Confirm whether the release-state file should remain in `gradle/` or be located under the BOM module directory while still governing the entire multi-project build.

## Decisions

- **Define the authoritative list of publishable modules. Should sample modules be excluded permanently?**

  Samples and benchmarks should be permanently excluded. All library modules and the BOM should be included.

- **Define module ownership paths, especially for shared build logic and dependency-lock files.**

- **Decide whether documentation-only changes should cause a module republish.**

  No.

- **Define dependency propagation rules for `api`, `implementation`, optional dependencies, and shaded dependencies.**

  For implementation: no change. If a change would be be required, that means there would have to be changes to the 
  module anyway and it would automatically be included in the release.

  For api: propagate only x and y changes. z level changes should not change the public interface, so no update is 
  needed.

- **Decide whether a patch release must increment from the highest existing patch across all modules, or whether only 
  changed modules advance independently within a release line.**

  We use the same version as the BOM, so it will be easier to track which release introduced a change. This should 
  not be a problem for consumers as long as they rely on the BOM.

- **Decide whether snapshots use the same selective-publication model or continue publishing all modules.**

  Snapshots should only be published to the local Maven repository. It is not necessary to use the selective model. 
  This should be done the way of least effort.

- **Decide the exact Git-tagging point: before deployment, after deployment, or via a separate finalization workflow.**

  Git tagging should take place when the new BOM is created. We should have a prepareRelease task that updates versions,
  prepares the BOM, and tags the release.

- **Define recovery behavior when Maven Central deployment partially succeeds or is rejected.**

  As tags are set in prepareRelease, it should be possible to simply resubmit the publishing task should it fail
  due to network problems, timeouts etc.

- **Confirm whether artifact compatibility checks are required before a module is selected for publication.**

- **Confirm whether the release-state file should remain in `gradle/` or be located under the BOM module directory 
  while still governing the entire multi-project build.**

## Ongoing
