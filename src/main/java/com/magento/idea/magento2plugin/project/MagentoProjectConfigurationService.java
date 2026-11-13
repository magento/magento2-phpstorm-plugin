/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.indexing.MagentoAdditionalLibraryRootsProvider;
import com.magento.idea.magento2plugin.project.indexing.MagentoIndexingExclusionService;
import com.magento.idea.magento2plugin.project.startup.ReaderModeHighlightingReset;
import com.magento.idea.magento2plugin.updates.MagentoUpdateService;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagentoProjectConfigurationService {

    private final Project project;

    public MagentoProjectConfigurationService(final @NotNull Project project) {
        this.project = project;
    }

    public static MagentoProjectConfigurationService getInstance(final @NotNull Project project) {
        return project.getService(MagentoProjectConfigurationService.class);
    }

    public enum InstallationType {
        RECOGNIZED_APPLICATION("Recognized Magento 2 application"),
        SOURCE_CHECKOUT("Supported source checkout"),
        PARTIAL_CHECKOUT("Supported partial checkout"),
        UNRECOGNIZED_DIRECTORY("Directory exists but not recognized as Magento"),
        FILE_NOT_DIRECTORY("Specified path is a file, not a directory"),
        NONEXISTENT("Path does not exist"),
        NOT_CONFIGURED("Not configured");

        private final String displayName;

        InstallationType(final String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public record InstallationDetails(
            @NotNull InstallationType type,
            boolean exists,
            boolean isDirectory,
            boolean composerJsonExists,
            boolean frameworkExists,
            @Nullable String version,
            @Nullable String edition,
            @NotNull List<String> missingDependencies,
            @NotNull String summary
    ) {}

    public record ConfigurationState(
            boolean enabled,
            @Nullable String configuredPath,
            @Nullable String resolvedPath,
            @Nullable Boolean excludeGeneratedFiles,
            @NotNull InstallationDetails installation,
            boolean indexingPending
    ) {}

    public record ConfigurationResult(
            boolean success,
            @NotNull String action,
            @NotNull String message,
            @NotNull ConfigurationState previousState,
            @NotNull ConfigurationState currentState,
            @NotNull List<String> validationErrors,
            boolean indexRefreshRequested,
            boolean reindexExecuted,
            @NotNull String nextUsableAction
    ) {}

    /**
     * Resolves a candidate Magento path against the IDE project base path and normalizes it.
     */
    public @Nullable String resolvePath(final @Nullable String rawPath) {
        if (StringUtil.isEmptyOrSpaces(rawPath)) {
            return null;
        }

        String trimmed = rawPath.trim().replace('\\', '/');
        while (trimmed.startsWith("./")) {
            trimmed = trimmed.substring(2);
        }

        final String projectBasePath = project.getBasePath();
        String candidatePath;

        if (trimmed.isEmpty() || ".".equals(trimmed)) {
            candidatePath = projectBasePath != null ? projectBasePath : trimmed;
        } else if (new File(trimmed).isAbsolute() || trimmed.startsWith("/")) {
            candidatePath = trimmed;
        } else if (projectBasePath != null) {
            candidatePath = projectBasePath + "/" + trimmed;
        } else {
            candidatePath = trimmed;
        }

        try {
            final Path normalized = Paths.get(candidatePath).normalize();
            String result = FileUtil.toSystemIndependentName(normalized.toString());
            if (result.length() > 1 && result.endsWith("/")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        } catch (final Exception ignored) {
            return FileUtil.toSystemIndependentName(candidatePath);
        }
    }

    /**
     * Inspects the Magento installation at the given path.
     */
    public @NotNull InstallationDetails inspectPath(final @Nullable String path) {
        if (StringUtil.isEmptyOrSpaces(path)) {
            return new InstallationDetails(
                    InstallationType.NOT_CONFIGURED,
                    false,
                    false,
                    false,
                    false,
                    null,
                    null,
                    List.of(),
                    "Magento path is not configured."
            );
        }

        final String resolved = resolvePath(path);
        final File ioFile = new File(Objects.requireNonNullElse(resolved, path));

        if (!ioFile.exists()) {
            // Also check VFS for in-memory or fixture environments
            final VirtualFile vf = resolveVirtualFile(resolved != null ? resolved : path);
            if (vf == null || !vf.exists()) {
                return new InstallationDetails(
                        InstallationType.NONEXISTENT,
                        false,
                        false,
                        false,
                        false,
                        null,
                        null,
                        List.of("Path does not exist"),
                        "Path does not exist: " + path
                );
            }
            if (!vf.isDirectory()) {
                return new InstallationDetails(
                        InstallationType.FILE_NOT_DIRECTORY,
                        true,
                        false,
                        false,
                        false,
                        null,
                        null,
                        List.of("Path is a file"),
                        "Path is a file, not a directory: " + path
                );
            }
        } else if (!ioFile.isDirectory()) {
            return new InstallationDetails(
                    InstallationType.FILE_NOT_DIRECTORY,
                    true,
                    false,
                    false,
                    false,
                    null,
                    null,
                    List.of("Path is a file"),
                    "Path is a file, not a directory: " + path
            );
        }

        final String effectivePath = resolved != null ? resolved : path;
        final boolean composerJsonExists = isFilePresent(effectivePath, "composer.json")
                || MagentoBasePathUtil.isComposerJsonExists(effectivePath);
        final boolean vendorFrameworkExists = isDirectoryPresent(effectivePath, Package.frameworkRootComposer)
                || isDirectoryPresent(effectivePath, Package.mageOsFrameworkRootComposer);
        final boolean internalFrameworkExists = isDirectoryPresent(effectivePath, Package.frameworkRootGit);
        final boolean frameworkExists = vendorFrameworkExists || internalFrameworkExists;
        final boolean vendorAutoloadExists = isFilePresent(effectivePath, "vendor/autoload.php");

        final Pair<String, String> versionData = MagentoVersionUtil.getVersionData(project, effectivePath);
        final String rawVersion = versionData != null ? versionData.getFirst() : null;
        final String rawEdition = versionData != null ? versionData.getSecond() : null;
        final boolean hasKnownVersion = rawVersion != null
                && !rawVersion.isBlank()
                && !MagentoVersionUtil.DEFAULT_VERSION.equalsIgnoreCase(rawVersion);

        final boolean isSourceCheckout = (internalFrameworkExists && (isDirectoryPresent(effectivePath, "app/code/Magento") || isDirectoryPresent(effectivePath, "app/code")))
                || MagentoVersionUtil.getSourceVersionData(project, effectivePath) != null;

        if (isSourceCheckout) {
            final List<String> sourceMissing = new ArrayList<>();
            if (!composerJsonExists) {
                sourceMissing.add("composer.json");
            }
            if (!vendorAutoloadExists) {
                sourceMissing.add("vendor dependencies (run composer install)");
            }
            final String summary = "Supported source checkout"
                    + (hasKnownVersion ? " (" + rawVersion + (rawEdition != null ? ", " + rawEdition : "") + ")" : " (version unknown)")
                    + (vendorAutoloadExists ? " with installed dependencies" : " (missing vendor dependencies)");
            return new InstallationDetails(
                    InstallationType.SOURCE_CHECKOUT,
                    true,
                    true,
                    composerJsonExists,
                    frameworkExists,
                    rawVersion,
                    rawEdition,
                    sourceMissing,
                    summary
            );
        }

        if (vendorFrameworkExists && composerJsonExists && hasKnownVersion) {
            final List<String> appMissing = new ArrayList<>();
            if (!vendorAutoloadExists) {
                appMissing.add("vendor/autoload.php");
            }
            return new InstallationDetails(
                    InstallationType.RECOGNIZED_APPLICATION,
                    true,
                    true,
                    true,
                    true,
                    rawVersion,
                    rawEdition,
                    appMissing,
                    "Recognized Magento 2 application (" + rawVersion
                            + (rawEdition != null ? ", " + rawEdition : "") + ")"
                            + (appMissing.isEmpty() ? "" : " (missing: " + String.join(", ", appMissing) + ")")
            );
        }

        if (composerJsonExists || frameworkExists || isDirectoryPresent(effectivePath, "app/code")) {
            final List<String> partialMissing = new ArrayList<>();
            if (!composerJsonExists) {
                partialMissing.add("composer.json");
            }
            if (!frameworkExists) {
                partialMissing.add("vendor/magento/framework");
            }
            if (!vendorAutoloadExists) {
                partialMissing.add("vendor/autoload.php");
            }
            if (!hasKnownVersion) {
                partialMissing.add("unknown platform version");
            }
            return new InstallationDetails(
                    InstallationType.PARTIAL_CHECKOUT,
                    true,
                    true,
                    composerJsonExists,
                    frameworkExists,
                    hasKnownVersion ? rawVersion : null,
                    rawEdition,
                    partialMissing,
                    "Supported partial checkout (missing: " + String.join(", ", partialMissing) + ")"
            );
        }

        return new InstallationDetails(
                InstallationType.UNRECOGNIZED_DIRECTORY,
                true,
                true,
                false,
                false,
                null,
                null,
                List.of("composer.json", "vendor/magento/framework"),
                "Directory exists but no Magento application structure was recognized."
        );
    }

    private boolean isDirectoryPresent(final String basePath, final String relativePath) {
        final File f = new File(basePath, relativePath);
        if (f.exists() && f.isDirectory()) {
            return true;
        }
        final VirtualFile vf = resolveVirtualFile(basePath);
        if (vf != null) {
            final VirtualFile sub = vf.findFileByRelativePath(relativePath);
            return sub != null && sub.isDirectory();
        }
        return false;
    }

    private boolean isFilePresent(final String basePath, final String relativePath) {
        final File f = new File(basePath, relativePath);
        if (f.exists() && f.isFile()) {
            return true;
        }
        final VirtualFile vf = resolveVirtualFile(basePath);
        if (vf != null) {
            final VirtualFile sub = vf.findFileByRelativePath(relativePath);
            return sub != null && !sub.isDirectory();
        }
        return false;
    }

    private @Nullable VirtualFile resolveVirtualFile(final String path) {
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        if (file == null) {
            file = LocalFileSystem.getInstance().findFileByPath(path);
        }
        if (file == null) {
            file = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(path));
        }
        if (file == null) {
            file = VirtualFileManager.getInstance().findFileByUrl(path);
        }
        if (file == null) {
            file = VirtualFileManager.getInstance().findFileByUrl("temp://" + (path.startsWith("/") ? path : "/" + path));
        }
        return file;
    }

    /**
     * Inspects the current configuration state.
     */
    public @NotNull ConfigurationState getCurrentState() {
        final Settings settings = Settings.getInstance(project);
        final String configuredPath = settings.magentoPath;
        final String resolvedPath = resolvePath(configuredPath);
        final InstallationDetails installation = inspectPath(resolvedPath != null ? resolvedPath : configuredPath);
        final boolean dumb = DumbService.getInstance(project).isDumb();

        return new ConfigurationState(
                settings.pluginEnabled,
                configuredPath,
                resolvedPath,
                settings.excludeGeneratedFiles,
                installation,
                dumb
        );
    }

    /**
     * Validates a path for Magento project configuration using the shared policy.
     */
    public @NotNull List<String> validatePathForConfiguration(final @Nullable String rawPath, final boolean enabled) {
        final List<String> errors = new ArrayList<>();
        if (!enabled) {
            return errors;
        }
        if (StringUtil.isEmptyOrSpaces(rawPath)) {
            errors.add("Magento installation path cannot be empty when enabling integration.");
            return errors;
        }
        final String resolved = resolvePath(rawPath);
        final InstallationDetails details = inspectPath(resolved != null ? resolved : rawPath);
        if (!details.exists()) {
            errors.add("Path does not exist: " + rawPath + (resolved != null ? " (resolved to: " + resolved + ")" : ""));
        } else if (!details.isDirectory()) {
            errors.add("Path is a file, not a directory: " + rawPath);
        } else if (details.type() == InstallationType.UNRECOGNIZED_DIRECTORY) {
            errors.add("Directory has no recognized Magento structure: " + rawPath);
        }
        return errors;
    }

    /**
     * Configures the Magento project integration.
     */
    public @NotNull ConfigurationResult configure(
            final @Nullable String rawPath,
            final @Nullable Boolean enabled,
            final @Nullable Boolean excludeGeneratedFiles,
            final @Nullable String projectPath
    ) {
        final ConfigurationState previousState = getCurrentState();
        final Settings settings = Settings.getInstance(project);
        final List<String> errors = new ArrayList<>();

        // Validate intended project path if provided
        if (!StringUtil.isEmptyOrSpaces(projectPath)) {
            final String normalizedExpected = FileUtil.toSystemIndependentName(projectPath.trim());
            final String currentBasePath = project.getBasePath() != null
                    ? FileUtil.toSystemIndependentName(project.getBasePath())
                    : null;
            if (currentBasePath != null
                    && !normalizedExpected.equals(currentBasePath)
                    && !normalizedExpected.equals(project.getName())) {
                errors.add("projectPath does not match the active project (expected: "
                        + currentBasePath + ", received: " + projectPath + ")");
            }
        }

        final boolean newEnabled = enabled != null ? enabled : previousState.enabled();
        final boolean isPathProvided = rawPath != null && !rawPath.trim().isEmpty();

        final String targetResolvedPath;
        if (isPathProvided) {
            targetResolvedPath = resolvePath(rawPath);
        } else if (!StringUtil.isEmptyOrSpaces(previousState.configuredPath())) {
            targetResolvedPath = previousState.resolvedPath() != null
                    ? previousState.resolvedPath()
                    : resolvePath(previousState.configuredPath());
        } else {
            targetResolvedPath = null;
        }

        InstallationDetails validatedDetails = null;

        if (newEnabled) {
            if (StringUtil.isEmptyOrSpaces(targetResolvedPath)) {
                errors.add("Magento installation path cannot be empty when enabling integration.");
            } else {
                validatedDetails = inspectPath(targetResolvedPath);
                if (!validatedDetails.exists()) {
                    errors.add("Path does not exist: " + (isPathProvided ? rawPath : targetResolvedPath)
                            + " (resolved to: " + targetResolvedPath + ")");
                } else if (!validatedDetails.isDirectory()) {
                    errors.add("Path is a file, not a directory: " + (isPathProvided ? rawPath : targetResolvedPath)
                            + " (resolved to: " + targetResolvedPath + ")");
                } else if (validatedDetails.type() == InstallationType.UNRECOGNIZED_DIRECTORY) {
                    errors.add("Directory has no recognized Magento structure: " + (isPathProvided ? rawPath : targetResolvedPath));
                }
            }
        } else if (isPathProvided && targetResolvedPath != null) {
            validatedDetails = inspectPath(targetResolvedPath);
            if (!validatedDetails.exists()) {
                errors.add("Path does not exist: " + rawPath + " (resolved to: " + targetResolvedPath + ")");
            } else if (!validatedDetails.isDirectory()) {
                errors.add("Path is a file, not a directory: " + rawPath + " (resolved to: " + targetResolvedPath + ")");
            }
        }

        if (!errors.isEmpty()) {
            final String nextAction;
            if (validatedDetails != null && !validatedDetails.exists()) {
                nextAction = "Provide a valid path to an existing Magento directory within the project or on disk.";
            } else if (validatedDetails != null && !validatedDetails.isDirectory()) {
                nextAction = "Provide a path to a directory, not a file.";
            } else if (validatedDetails != null && validatedDetails.type() == InstallationType.UNRECOGNIZED_DIRECTORY) {
                nextAction = "Directory has no recognized Magento structure. Verify the Magento root directory path.";
            } else if (StringUtil.isEmptyOrSpaces(targetResolvedPath)) {
                nextAction = "Provide a valid Magento directory path when enabling integration.";
            } else {
                nextAction = "Correct validation errors before configuring Magento integration.";
            }

            return new ConfigurationResult(
                    false,
                    "configure",
                    "Configuration validation failed: " + String.join("; ", errors),
                    previousState,
                    previousState,
                    errors,
                    false,
                    false,
                    nextAction
            );
        }

        // Determine exclusion policy:
        // When integration is first enabled, default to true unless explicitly overridden.
        // Preserve existing user choice when not overridden.
        final Boolean newExclude;
        if (excludeGeneratedFiles != null) {
            newExclude = excludeGeneratedFiles;
        } else if (settings.excludeGeneratedFiles != null) {
            newExclude = settings.excludeGeneratedFiles;
        } else if (newEnabled) {
            newExclude = Boolean.TRUE;
        } else {
            newExclude = null;
        }

        final String finalPathToSave = targetResolvedPath != null ? targetResolvedPath : previousState.configuredPath();

        // Check idempotency:
        final boolean pathUnchanged = Objects.equals(finalPathToSave, previousState.configuredPath());
        final boolean enabledUnchanged = newEnabled == previousState.enabled();
        final boolean excludeUnchanged = Objects.equals(newExclude, settings.excludeGeneratedFiles);

        if (pathUnchanged && enabledUnchanged && excludeUnchanged) {
            final ConfigurationState currentState = getCurrentState();
            return new ConfigurationResult(
                    true,
                    "configure",
                    "Magento project integration is already configured with identical settings (no changes required).",
                    previousState,
                    currentState,
                    List.of(),
                    false,
                    false,
                    determineNextAction(currentState)
            );
        }

        final Settings.State oldState = Objects.requireNonNull(settings.getState());

        String finalVersion = oldState.magentoVersion;
        String finalEdition = oldState.magentoEdition;
        if (newEnabled && finalPathToSave != null) {
            final Pair<String, String> versionData = MagentoVersionUtil.getVersionData(project, finalPathToSave);
            finalVersion = versionData != null ? versionData.getFirst() : null;
            finalEdition = versionData != null ? versionData.getSecond() : null;
        }

        final Settings.State newState = new Settings.State(
                newEnabled,
                finalPathToSave != null ? finalPathToSave : oldState.magentoPath,
                oldState.defaultLicenseName,
                oldState.mftfSupportEnabled,
                oldState.myDoNotAskContentConfigAgain,
                oldState.mcpCliToolCandidates,
                oldState.publishedReleasesUrl,
                finalVersion,
                finalEdition,
                oldState.myMagentoFolders,
                newExclude != null ? newExclude : oldState.excludeGeneratedFiles,
                oldState.additionalExcludedPaths,
                oldState.managedExcludedFolders
        );
        newState.managedExclusionOwners = oldState.managedExclusionOwners;

        // Apply via setState so notifyListeners fires properly
        settings.setState(newState);

        // Apply indexing exclusions (managed exclusions removed if !newEnabled without deleting preferences)
        MagentoIndexingExclusionService.getInstance(project).applyExclusions();

        // Refresh affected module/library/index state
        refreshProjectState(newEnabled);

        final ConfigurationState currentState = getCurrentState();
        final boolean refreshPending = currentState.indexingPending();

        return new ConfigurationResult(
                true,
                "configure",
                "Magento project integration updated successfully.",
                previousState,
                currentState,
                List.of(),
                refreshPending,
                true,
                determineNextAction(currentState)
        );
    }

    private void refreshProjectState(final boolean integrationEnabled) {
        MagentoAdditionalLibraryRootsProvider.refreshRoots(project);
        IndexManager.manualReindex();
        MagentoComponentManager.getInstance(project).flushModules();
        ReaderModeHighlightingReset.run(project);

        if (integrationEnabled) {
            final MagentoUpdateService updateService = project.getService(MagentoUpdateService.class);
            if (updateService != null) {
                updateService.checkForUpdates();
            }
        }
    }

    public @NotNull String determineNextAction(final @NotNull ConfigurationState state) {
        if (!state.enabled()) {
            return "Magento integration is disabled. Call configure_magento_project with enabled=true to activate.";
        }
        if (state.indexingPending()) {
            return "Wait for IDE indexing to finish before querying Magento modules or running inspections.";
        }
        final InstallationDetails installation = state.installation();
        if (installation.type() == InstallationType.NONEXISTENT) {
            return "Configured Magento directory does not exist. Call configure_magento_project with a valid magentoPath.";
        }
        if (installation.type() == InstallationType.FILE_NOT_DIRECTORY) {
            return "Configured path points to a file, not a directory. Specify a directory path.";
        }
        if (installation.type() == InstallationType.NOT_CONFIGURED) {
            return "Configure a Magento root path using configure_magento_project.";
        }
        if (installation.type() == InstallationType.SOURCE_CHECKOUT) {
            if (!installation.missingDependencies().isEmpty()) {
                return "Run \"composer install\" under the Magento root to install vendor dependencies, then query modules with magento_inspect.";
            }
            return "Query project configuration and declarations with magento_inspect.";
        }
        if (installation.type() == InstallationType.PARTIAL_CHECKOUT) {
            if (!installation.frameworkExists()) {
                return "Run \"composer install\" under the Magento root to install dependencies, then query modules with magento_inspect.";
            }
            return "Complete Magento dependencies installation, then inspect the project with magento_inspect.";
        }
        if (installation.type() == InstallationType.UNRECOGNIZED_DIRECTORY) {
            return "Directory has no recognized Magento structure. Verify the Magento root directory path.";
        }
        return "Query project configuration and declarations with magento_inspect.";
    }
}
