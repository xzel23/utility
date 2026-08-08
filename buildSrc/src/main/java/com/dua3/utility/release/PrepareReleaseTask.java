package com.dua3.utility.release;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the immutable release plan without capturing a Project or build-script
 * object in a task action. It is deliberately configuration-cache compatible: all
 * data required at execution time is exposed through task properties.
 */
public abstract class PrepareReleaseTask extends DefaultTask {
    private static final String GROUP = "com.dua3.utility";
    private static final List<String> MODULES = List.of(
            "utility",
            "utility-db",
            "utility-swing",
            "utility-fx",
            "utility-fx-icons",
            "utility-fx-icons-ikonli",
            "utility-fx-controls",
            "utility-fx-db",
            "utility-fx-web"
    );
    private static final List<String> SHARED_BUILD_INPUT_PATHS = List.of(
            "build.gradle.kts",
            "settings.gradle.kts",
            "gradle.properties",
            "gradle/wrapper",
            ":(glob)gradle/*.gradle.kts"
    );
    private static final List<String> DEPENDENCY_LOCKFILE_EXCLUSIONS = List.of(
            ":(exclude,glob)**/gradle.lockfile",
            ":(exclude)settings-gradle.lockfile"
    );
    private static final String VERSION_CATALOG_PATH = "gradle/version.toml";
    private static final Pattern TABLE = Pattern.compile("^\\[([A-Za-z0-9_.-]+)]$");
    private static final Pattern VALUE = Pattern.compile("^([A-Za-z][A-Za-z0-9_-]*)\\s*=\\s*(.+)$");
    private static final Pattern QUOTED_STRING = Pattern.compile("\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
    private static final Pattern VERSION = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$");
    private static final Pattern VERSION_CATALOG_ENTRY = Pattern.compile("^([A-Za-z][A-Za-z0-9_-]*)\\s*=");

    @InputDirectory
    public abstract DirectoryProperty getRepositoryDirectory();

    @InputFile
    public abstract RegularFileProperty getReleaseStateFile();

    @Input
    public abstract Property<String> getPreparedReleasePlanPath();

    @Input
    public abstract Property<String> getReleaseType();

    @Input
    public abstract Property<String> getRequestedReleaseVersion();

    @Input
    public abstract Property<String> getAdditionalReleaseModules();

    @Input
    public abstract Property<Boolean> getConfirmRelease();

    @TaskAction
    public void prepareRelease() {
        File repositoryDirectory = getRepositoryDirectory().get().getAsFile();
        File stateFile = getReleaseStateFile().get().getAsFile();
        File planFile = new File(getPreparedReleasePlanPath().get());

        if (planFile.exists()) {
            throw new GradleException("a prepared release plan already exists at " + planFile.getPath()
                    + "; publish, finalize, or resolve it first");
        }
        if (!runGit(repositoryDirectory, "status", "--porcelain").output().isBlank()) {
            throw new GradleException("the Git working tree must be clean before preparing a release");
        }

        String releaseType = getReleaseType().get().trim().toLowerCase();
        if (!Set.of("patch", "minor", "major").contains(releaseType)) {
            throw new GradleException("supply -PreleaseType=patch, -PreleaseType=minor, or -PreleaseType=major");
        }

        ReleaseState state = readReleaseState(stateFile);
        SemanticVersion previousBomVersion = SemanticVersion.parse(state.bomVersion());
        String requestedVersion = getRequestedReleaseVersion().get().trim();
        SemanticVersion targetVersion = requestedVersion.isEmpty()
                ? nextVersion(releaseType, previousBomVersion)
                : SemanticVersion.parse(requestedVersion);
        validateTargetVersion(releaseType, previousBomVersion, targetVersion);

        String releaseRevision = requireGitSuccess(repositoryDirectory, "resolving release revision", "rev-parse", "HEAD");
        String bomRevision = state.bomPublishedRevision();
        requireGitSuccess(repositoryDirectory, "checking published BOM revision", "cat-file", "-e", bomRevision + "^{commit}");
        if (runGit(repositoryDirectory, "merge-base", "--is-ancestor", bomRevision, releaseRevision).exitValue() != 0) {
            throw new GradleException("published BOM revision is not an ancestor of " + releaseRevision);
        }
        for (Map.Entry<String, ModuleState> entry : state.modules().entrySet()) {
            String moduleName = entry.getKey();
            String revision = entry.getValue().publishedRevision();
            requireGitSuccess(repositoryDirectory, "checking published revision for " + moduleName,
                    "cat-file", "-e", revision + "^{commit}");
            if (runGit(repositoryDirectory, "merge-base", "--is-ancestor", revision, releaseRevision).exitValue() != 0) {
                throw new GradleException("published revision for " + moduleName + " is not an ancestor of " + releaseRevision);
            }
        }

        Set<String> additionalModules = parseModuleList(getAdditionalReleaseModules().get());
        if (!MODULES.containsAll(additionalModules)) {
            throw new GradleException("additionalReleaseModules contains an unknown publishable module");
        }

        Map<String, String> selectedReasons = new LinkedHashMap<>();
        if (releaseType.equals("patch")) {
            VersionCatalogChange versionCatalogChange = versionCatalogChange(
                    repositoryDirectory,
                    state.bomPublishedRevision(),
                    releaseRevision
            );
            for (Map.Entry<String, ModuleState> entry : state.modules().entrySet()) {
                String moduleName = entry.getKey();
                ModuleState module = entry.getValue();
                boolean sourceChanged = gitHasChangesExcludingDependencyLockfiles(
                        repositoryDirectory,
                        module.publishedRevision(),
                        releaseRevision,
                        module.paths()
                );
                boolean sharedInputChanged = gitHasChanges(
                        repositoryDirectory,
                        module.publishedRevision(),
                        releaseRevision,
                        SHARED_BUILD_INPUT_PATHS
                ) || versionCatalogChange == VersionCatalogChange.SHARED_BUILD;
                if (sourceChanged && sharedInputChanged) {
                    selectedReasons.put(moduleName, "direct source and shared build input change");
                } else if (sourceChanged) {
                    selectedReasons.put(moduleName, "direct source change");
                } else if (sharedInputChanged) {
                    selectedReasons.put(moduleName, "shared build input change");
                }
            }
            for (String moduleName : additionalModules) {
                selectedReasons.putIfAbsent(moduleName, "explicit minimum internal dependency update");
            }
            if (selectedReasons.isEmpty() && versionCatalogChange == VersionCatalogChange.NONE) {
                throw new GradleException("no publishable module or publication-relevant dependency catalog entry changed");
            }
        } else {
            for (String moduleName : MODULES) {
                selectedReasons.put(moduleName, releaseType + " release");
            }
        }

        String targetVersionString = targetVersion.toString();
        Map<String, PlannedModule> modules = new LinkedHashMap<>();
        for (String moduleName : MODULES) {
            ModuleState oldModule = state.modules().get(moduleName);
            boolean selected = selectedReasons.containsKey(moduleName);
            modules.put(moduleName, new PlannedModule(
                    selected ? targetVersionString : oldModule.version(),
                    selected ? releaseRevision : oldModule.publishedRevision(),
                    selected,
                    selectedReasons.getOrDefault(moduleName, "retained published module")
            ));
        }
        ReleasePlan plan = new ReleasePlan(releaseType, targetVersionString, releaseRevision, modules);

        List<Coordinate> candidateCoordinates = new ArrayList<>();
        candidateCoordinates.add(new Coordinate("utility-bom", plan.bomVersion()));
        for (Map.Entry<String, PlannedModule> entry : plan.modules().entrySet()) {
            if (entry.getValue().selected()) {
                candidateCoordinates.add(new Coordinate(entry.getKey(), entry.getValue().version()));
            }
        }
        for (Coordinate coordinate : candidateCoordinates) {
            if (isMavenCentralCoordinatePublished(coordinate.artifactId(), coordinate.version())) {
                throw new GradleException("Maven Central already contains " + coordinate.artifactId() + ":"
                        + coordinate.version() + "; release coordinates are immutable");
            }
        }

        getLogger().lifecycle(renderPlan(plan));
        if (getConfirmRelease().get()) {
            writePlan(planFile, plan);
            getLogger().lifecycle("Wrote " + planFile.getPath()
                    + ". Commit it, then run release verification in a new Gradle invocation.");
        } else {
            getLogger().lifecycle("Dry run only. Re-run with -PconfirmRelease=true to write the prepared release plan.");
        }
    }

    private static Set<String> parseModuleList(String value) {
        if (value.isBlank()) {
            return Set.of();
        }
        return Set.copyOf(List.of(value.split(",")).stream().map(String::trim).filter(item -> !item.isEmpty()).toList());
    }

    private static SemanticVersion nextVersion(String releaseType, SemanticVersion previous) {
        return switch (releaseType) {
            case "patch" -> new SemanticVersion(previous.major(), previous.minor(), previous.patch() + 1);
            case "minor" -> new SemanticVersion(previous.major(), previous.minor() + 1, 0);
            case "major" -> new SemanticVersion(previous.major() + 1, 0, 0);
            default -> throw new IllegalStateException("validated before computing the version");
        };
    }

    private static void validateTargetVersion(String releaseType, SemanticVersion previous, SemanticVersion target) {
        boolean valid = switch (releaseType) {
            case "patch" -> target.major() == previous.major() && target.minor() == previous.minor()
                    && target.patch() > previous.patch();
            case "minor" -> target.major() == previous.major() && target.minor() > previous.minor() && target.patch() == 0;
            case "major" -> target.major() > previous.major() && target.minor() == 0 && target.patch() == 0;
            default -> false;
        };
        if (!valid) {
            throw new GradleException("invalid " + releaseType + " release version " + target + " after " + previous);
        }
    }

    private static ReleaseState readReleaseState(File file) {
        Map<String, Map<String, String>> values = parseToml(file);
        Map<String, String> release = requireTable(values, "release", file);
        String bomVersion = requireValue(release, "bomVersion", "release", file);
        Map<String, ModuleState> modules = new LinkedHashMap<>();
        for (String moduleName : MODULES) {
            Map<String, String> module = requireTable(values, "modules." + moduleName, file);
            modules.put(moduleName, new ModuleState(
                    requireValue(module, "version", "modules." + moduleName, file),
                    requireValue(module, "publishedRevision", "modules." + moduleName, file),
                    parseTomlArray(requireValue(module, "paths", "modules." + moduleName, file))
            ));
        }
        return new ReleaseState(
                bomVersion,
                requireValue(release, "publishedRevision", "release", file),
                modules
        );
    }

    private static Map<String, Map<String, String>> parseToml(File file) {
        if (!file.isFile()) {
            throw new GradleException("release file does not exist: " + file.getPath());
        }
        Map<String, Map<String, String>> values = new LinkedHashMap<>();
        String section = "";
        try {
            for (String rawLine : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                String line = rawLine.split("#", 2)[0].trim();
                if (line.isEmpty()) {
                    continue;
                }
                Matcher table = TABLE.matcher(line);
                if (table.matches()) {
                    section = table.group(1);
                    values.computeIfAbsent(section, ignored -> new LinkedHashMap<>());
                    continue;
                }
                Matcher value = VALUE.matcher(line);
                if (value.matches()) {
                    if (section.isEmpty()) {
                        throw new GradleException("value outside a TOML table in " + file.getPath() + ": " + line);
                    }
                    String parsedValue = value.group(2).trim();
                    if (parsedValue.length() >= 2 && parsedValue.startsWith("\"") && parsedValue.endsWith("\"")) {
                        parsedValue = parsedValue.substring(1, parsedValue.length() - 1);
                    }
                    values.computeIfAbsent(section, ignored -> new LinkedHashMap<>()).put(value.group(1), parsedValue);
                    continue;
                }
                throw new GradleException("unsupported release TOML syntax in " + file.getPath() + ": " + line);
            }
        } catch (IOException exception) {
            throw new GradleException("could not read " + file.getPath(), exception);
        }
        return values;
    }

    private static Map<String, String> requireTable(Map<String, Map<String, String>> values, String table, File file) {
        Map<String, String> result = values.get(table);
        if (result == null) {
            throw new GradleException("[" + table + "] table missing from " + file.getPath());
        }
        return result;
    }

    private static String requireValue(Map<String, String> values, String key, String table, File file) {
        String result = values.get(key);
        if (result == null) {
            throw new GradleException(table + "." + key + " missing from " + file.getPath());
        }
        return result;
    }

    private static List<String> parseTomlArray(String value) {
        List<String> values = new ArrayList<>();
        Matcher matcher = QUOTED_STRING.matcher(value);
        while (matcher.find()) {
            values.add(matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\"));
        }
        return values;
    }

    private static CommandResult runGit(File directory, String... arguments) {
        try {
            List<String> command = new ArrayList<>();
            command.add("git");
            command.addAll(List.of(arguments));
            Process process = new ProcessBuilder(command).directory(directory).redirectErrorStream(true).start();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            process.getInputStream().transferTo(output);
            return new CommandResult(process.waitFor(), output.toString(StandardCharsets.UTF_8).trim());
        } catch (IOException exception) {
            throw new GradleException("could not execute git", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GradleException("interrupted while executing git", exception);
        }
    }

    private static String requireGitSuccess(File directory, String description, String... arguments) {
        CommandResult result = runGit(directory, arguments);
        if (result.exitValue() != 0) {
            throw new GradleException(description + " failed ("
                    + (result.output().isBlank() ? "no output" : result.output()) + ")");
        }
        return result.output();
    }

    private static boolean gitHasChanges(File directory, String fromRevision, String toRevision, List<String> pathspecs) {
        List<String> arguments = new ArrayList<>(List.of("diff", "--quiet", fromRevision, toRevision, "--"));
        arguments.addAll(pathspecs);
        CommandResult result = runGit(directory, arguments.toArray(String[]::new));
        return switch (result.exitValue()) {
            case 0 -> false;
            case 1 -> true;
            default -> throw new GradleException("could not compare Git revisions: " + result.output());
        };
    }

    private static boolean gitHasChangesExcludingDependencyLockfiles(
            File directory,
            String fromRevision,
            String toRevision,
            List<String> pathspecs
    ) {
        List<String> filteredPathspecs = new ArrayList<>(pathspecs);
        filteredPathspecs.addAll(DEPENDENCY_LOCKFILE_EXCLUSIONS);
        return gitHasChanges(directory, fromRevision, toRevision, filteredPathspecs);
    }

    /**
     * Dependency catalog changes are published through the BOM, whereas toolchain and plugin changes affect every
     * module build. The catalog's project version is deliberately ignored: finalization changes it to the next
     * snapshot after the BOM has already been published.
     */
    private static VersionCatalogChange versionCatalogChange(
            File directory,
            String fromRevision,
            String toRevision
    ) {
        CommandResult result = runGit(
                directory,
                "diff",
                "--unified=0",
                fromRevision,
                toRevision,
                "--",
                VERSION_CATALOG_PATH
        );
        if (result.exitValue() != 0) {
            throw new GradleException("could not compare version catalog revisions: " + result.output());
        }

        VersionCatalogChange change = VersionCatalogChange.NONE;
        for (String line : result.output().split("\\R")) {
            if (!(line.startsWith("+") || line.startsWith("-")) || line.startsWith("+++") || line.startsWith("---")) {
                continue;
            }
            String changedLine = line.substring(1).trim();
            if (changedLine.isEmpty() || changedLine.startsWith("#")) {
                continue;
            }
            Matcher entry = VERSION_CATALOG_ENTRY.matcher(changedLine);
            boolean versionEntry = entry.find();
            if (versionEntry && entry.group(1).equals("projectVersion")) {
                continue;
            }
            if (versionEntry && isSharedBuildVersion(entry.group(1))) {
                return VersionCatalogChange.SHARED_BUILD;
            }
            change = VersionCatalogChange.BOM_ONLY;
        }
        return change;
    }

    private static boolean isSharedBuildVersion(String alias) {
        return alias.equals("jdkVersion") || alias.equals("javafxJdkVersion") || alias.endsWith("-plugin");
    }

    private static boolean isMavenCentralCoordinatePublished(String artifactId, String version) {
        String path = GROUP.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create("https://repo1.maven.org/maven2/" + path).toURL().openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return false;
            }
            if (responseCode >= 200 && responseCode <= 399) {
                return true;
            }
            throw new GradleException("could not determine whether " + artifactId + ":" + version
                    + " exists on Maven Central (HTTP " + responseCode + ")");
        } catch (IOException exception) {
            throw new GradleException("could not check Maven Central for " + artifactId + ":" + version, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void writePlan(File file, ReleasePlan plan) {
        StringBuilder content = new StringBuilder();
        content.append("[release]\n")
                .append("schemaVersion = 1\n")
                .append("releaseType = \"").append(tomlString(plan.releaseType())).append("\"\n")
                .append("bomVersion = \"").append(tomlString(plan.bomVersion())).append("\"\n")
                .append("sourceRevision = \"").append(tomlString(plan.sourceRevision())).append("\"\n");
        for (String moduleName : MODULES) {
            PlannedModule module = plan.modules().get(moduleName);
            content.append("\n[modules.").append(moduleName).append("]\n")
                    .append("version = \"").append(tomlString(module.version())).append("\"\n")
                    .append("sourceRevision = \"").append(tomlString(module.sourceRevision())).append("\"\n")
                    .append("selected = ").append(module.selected()).append("\n")
                    .append("reason = \"").append(tomlString(module.reason())).append("\"\n");
        }
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new GradleException("could not write " + file.getPath(), exception);
        }
    }

    private static String renderPlan(ReleasePlan plan) {
        StringBuilder text = new StringBuilder("Selective release plan\n")
                .append("  type: ").append(plan.releaseType()).append("\n")
                .append("  source revision: ").append(plan.sourceRevision()).append("\n")
                .append("  BOM: utility-bom:").append(plan.bomVersion()).append("\n")
                .append("  modules to publish:\n");
        for (Map.Entry<String, PlannedModule> entry : plan.modules().entrySet()) {
            if (entry.getValue().selected()) {
                text.append("    ").append(entry.getKey()).append(":").append(entry.getValue().version())
                        .append(" (").append(entry.getValue().reason()).append(")\n");
            }
        }
        if (plan.modules().values().stream().noneMatch(PlannedModule::selected)) {
            text.append("    (none; BOM-only dependency catalog release)\n");
        }
        text.append("  retained modules:\n");
        for (Map.Entry<String, PlannedModule> entry : plan.modules().entrySet()) {
            if (!entry.getValue().selected()) {
                text.append("    ").append(entry.getKey()).append(":").append(entry.getValue().version()).append("\n");
            }
        }
        return text.toString();
    }

    private static String tomlString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record ModuleState(String version, String publishedRevision, List<String> paths) {
    }

    private record ReleaseState(String bomVersion, String bomPublishedRevision, Map<String, ModuleState> modules) {
    }

    private record PlannedModule(String version, String sourceRevision, boolean selected, String reason) {
    }

    private record ReleasePlan(String releaseType, String bomVersion, String sourceRevision,
                               Map<String, PlannedModule> modules) {
    }

    private record Coordinate(String artifactId, String version) {
    }

    private record CommandResult(int exitValue, String output) {
    }

    private enum VersionCatalogChange {
        NONE,
        BOM_ONLY,
        SHARED_BUILD
    }

    private record SemanticVersion(int major, int minor, int patch) {
        private static SemanticVersion parse(String value) {
            Matcher matcher = VERSION.matcher(value);
            if (!matcher.matches()) {
                throw new GradleException("release version must be a stable major.minor.patch value: " + value);
            }
            return new SemanticVersion(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            );
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}
