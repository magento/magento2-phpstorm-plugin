/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.indexing;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Function;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.project.Settings.ManagedExclusion;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared PHP-independent service for inspecting and managing Magento indexing exclusions.
 */
public class MagentoIndexingExclusionService {

    public record ExclusionCandidate(
            @NotNull String relativePath,
            @NotNull String reason
    ) {}

    public record ExclusionEntry(
            @NotNull String path,
            @NotNull String relativePath,
            @NotNull String url,
            @NotNull String reason,
            boolean existsOnDisk,
            boolean isAdditional
    ) {}

    public record SkippedEntry(
            @NotNull String path,
            @NotNull String reason
    ) {}

    public record ExclusionPreview(
            boolean magentoPathValid,
            @Nullable String magentoPath,
            boolean exclusionEnabled,
            boolean pluginEnabled,
            @NotNull List<ExclusionEntry> effectiveExclusions,
            @NotNull List<SkippedEntry> skippedEntries,
            @NotNull List<String> currentManagedUrls,
            @NotNull List<String> urlsToAdd,
            @NotNull List<String> urlsToRemove,
            @NotNull String statusMessage
    ) {}

    public record ExclusionApplyResult(
            boolean success,
            @NotNull String action,
            @NotNull List<String> addedPaths,
            @NotNull List<String> removedPaths,
            @NotNull List<String> unchangedPaths,
            @NotNull List<SkippedEntry> skippedEntries,
            @NotNull List<ExclusionEntry> effectiveExclusions,
            boolean indexRefreshRequested,
            @NotNull String message
    ) {}

    /**
     * Documented default Magento output locations relative to configured Magento root.
     */
    public static final List<ExclusionCandidate> DEFAULT_CANDIDATES = List.of(
            new ExclusionCandidate(".cache", "Local tool cache"),
            new ExclusionCandidate("var/cache", "Magento application cache"),
            new ExclusionCandidate("var/page_cache", "Full page cache"),
            new ExclusionCandidate("var/view_preprocessed", "Preprocessed view templates and assets"),
            new ExclusionCandidate("var/log", "Application logs"),
            new ExclusionCandidate("var/report", "Crash reports"),
            new ExclusionCandidate("var/session", "File-based session storage"),
            new ExclusionCandidate("generated/metadata", "Compiled dependency injection metadata"),
            new ExclusionCandidate("pub/static", "Deployed static view files and frontend assets")
    );

    /**
     * Protected sub-paths relative to Magento root that must never be excluded.
     */
    private static final List<String> PROTECTED_MAGENTO_RELATIVE_PATHS = List.of(
            "app/code",
            "app/design",
            "app/etc",
            "vendor",
            "lib/web",
            "generated/code",
            "dev/tests"
    );

    private static class ExclusionSnapshot {
        final boolean active;
        final List<String> excludedPaths;
        final List<String> protectedPaths;
        final Set<VirtualFile> excludedVirtualFiles;

        ExclusionSnapshot(
                final boolean active,
                final @NotNull List<String> excludedPaths,
                final @NotNull List<String> protectedPaths,
                final @NotNull Set<VirtualFile> excludedVirtualFiles
        ) {
            this.active = active;
            this.excludedPaths = excludedPaths;
            this.protectedPaths = protectedPaths;
            this.excludedVirtualFiles = excludedVirtualFiles;
        }
    }

    private final Project project;
    private volatile ExclusionSnapshot cachedSnapshot = null;
    private record CacheDirectories(String rootUrl, List<ExclusionCandidate> candidates) {}
    private volatile CacheDirectories cachedCacheDirectories;
    private final AtomicBoolean cacheReconciliationPending = new AtomicBoolean();

    public MagentoIndexingExclusionService(final @NotNull Project project) {
        this.project = project;
        Settings.getInstance(project).addListener((newState, oldState) -> invalidateCache());
        project.getMessageBus().connect().subscribe(
                com.intellij.openapi.roots.ModuleRootListener.TOPIC,
                new com.intellij.openapi.roots.ModuleRootListener() {
                    @Override
                    public void rootsChanged(final @NotNull com.intellij.openapi.roots.ModuleRootEvent event) {
                        invalidateCache();
                    }
                }
        );
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(final @NotNull List<? extends VFileEvent> events) {
                final VirtualFile root = resolveDirectoryForPath(Settings.getMagentoPath(project));
                if (root == null) {
                    return;
                }
                boolean affected = false;
                for (final VFileEvent event : events) {
                    if (event.getFile() == null || !event.getFile().isDirectory()) {
                        continue;
                    }
                    String oldPath = null;
                    String newPath = null;
                    if (event instanceof VFilePropertyChangeEvent change
                            && VirtualFile.PROP_NAME.equals(change.getPropertyName())) {
                        oldPath = change.getOldPath();
                        newPath = change.getNewPath();
                    } else if (event instanceof VFileMoveEvent move) {
                        oldPath = move.getOldPath();
                        newPath = move.getNewPath();
                    }
                    if (event.getPath().startsWith(root.getPath() + "/")
                            || (oldPath != null && oldPath.startsWith(root.getPath() + "/"))) {
                        if (oldPath != null) {
                            updateMovedExclusionOwners(oldPath, newPath);
                            affected = true;
                        } else if (".cache".equals(event.getFile().getName())) {
                            affected = true;
                        }
                    }
                }
                if (!affected) {
                    return;
                }
                cachedCacheDirectories = null;
                invalidateCache();
                if (cacheReconciliationPending.compareAndSet(false, true)) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        cacheReconciliationPending.set(false);
                        if (!project.isDisposed()) {
                            reconcileExclusionsOnStartup();
                        }
                    });
                }
            }
        });
        project.getMessageBus().connect().subscribe(ModuleListener.TOPIC, new ModuleListener() {
            @Override
            public void modulesRenamed(
                    final @NotNull Project renamedProject,
                    final @NotNull List<? extends Module> modules,
                    final @NotNull Function<? super Module, String> oldNameProvider
            ) {
                final Map<String, String> renamedModules = new LinkedHashMap<>();
                for (final Module module : modules) {
                    renamedModules.put(oldNameProvider.fun(module), module.getName());
                }
                final Settings settings = Settings.getInstance(project);
                final List<ManagedExclusion> owners = new ArrayList<>();
                for (final ManagedExclusion owner : settings.getManagedExclusionOwners()) {
                    owners.add(new ManagedExclusion(
                            renamedModules.getOrDefault(owner.moduleName, owner.moduleName),
                            owner.contentEntryUrl,
                            owner.excludedUrl
                    ));
                }
                settings.setManagedExclusionOwners(owners);
                invalidateCache();
            }
        });
    }

    private void updateMovedExclusionOwners(final @NotNull String oldPath, final @NotNull String newPath) {
        final String oldUrl = VfsUtilCore.pathToUrl(oldPath);
        final String newUrl = VfsUtilCore.pathToUrl(newPath);
        final Settings settings = Settings.getInstance(project);
        final List<ManagedExclusion> owners = settings.getManagedExclusionOwners().stream()
                .map(owner -> new ManagedExclusion(owner.moduleName,
                        movedUrl(owner.contentEntryUrl, oldUrl, newUrl),
                        movedUrl(owner.excludedUrl, oldUrl, newUrl)))
                .toList();
        if (!owners.equals(settings.getManagedExclusionOwners())) {
            settings.setManagedExclusionOwners(owners);
            settings.setManagedExcludedFolders(settings.getManagedExcludedFolders().stream()
                    .map(url -> movedUrl(url, oldUrl, newUrl)).toList());
        }
    }

    private static @NotNull String movedUrl(
            final @NotNull String url, final @NotNull String oldUrl, final @NotNull String newUrl
    ) {
        return url.equals(oldUrl) || url.startsWith(oldUrl + "/") ? newUrl + url.substring(oldUrl.length()) : url;
    }

    public void invalidateCache() {
        this.cachedSnapshot = null;
    }

    private @NotNull ExclusionSnapshot getOrCreateSnapshot() {
        ExclusionSnapshot snapshot = cachedSnapshot;
        if (snapshot != null) {
            return snapshot;
        }

        synchronized (this) {
            snapshot = cachedSnapshot;
            if (snapshot != null) {
                return snapshot;
            }

            final Settings settings = Settings.getInstance(project);
            if (!settings.pluginEnabled || !settings.isExcludeGeneratedFiles()) {
                snapshot = new ExclusionSnapshot(false, List.of(), List.of(), Collections.emptySet());
                cachedSnapshot = snapshot;
                return snapshot;
            }

            final String magentoPath = Settings.getMagentoPath(project);
            if (magentoPath == null || !isPathValid(magentoPath)) {
                snapshot = new ExclusionSnapshot(false, List.of(), List.of(), Collections.emptySet());
                cachedSnapshot = snapshot;
                return snapshot;
            }

            final ResolvedExclusions resolved = resolveExclusions(
                    magentoPath,
                    settings.getAdditionalExcludedPaths()
            );

            final List<String> excludedPaths = new ArrayList<>();
            final Set<VirtualFile> virtualFiles = new LinkedHashSet<>();
            for (final ExclusionEntry entry : resolved.entries) {
                excludedPaths.add(entry.path());
                final VirtualFile vf = resolveDirectoryForPath(entry.path());
                if (vf != null && vf.isDirectory()) {
                    virtualFiles.add(vf);
                }
            }

            final String normalizedMagento = FileUtil.toSystemIndependentName(magentoPath.trim());
            final List<String> protectedPaths = new ArrayList<>();
            for (final String rel : PROTECTED_MAGENTO_RELATIVE_PATHS) {
                protectedPaths.add(normalizedMagento + "/" + rel);
            }

            snapshot = new ExclusionSnapshot(
                    true,
                    Collections.unmodifiableList(excludedPaths),
                    Collections.unmodifiableList(protectedPaths),
                    Collections.unmodifiableSet(virtualFiles)
            );
            cachedSnapshot = snapshot;
            return snapshot;
        }
    }

    public static MagentoIndexingExclusionService getInstance(final @NotNull Project project) {
        return project.getService(MagentoIndexingExclusionService.class);
    }

    public static @NotNull List<ExclusionCandidate> getDefaultExclusionCandidates() {
        return DEFAULT_CANDIDATES;
    }

    /**
     * Resolves a directory path as a VirtualFile.
     */
    public @Nullable VirtualFile resolveDirectoryForPath(final @Nullable String rawPath) {
        if (StringUtil.isEmptyOrSpaces(rawPath)) {
            return null;
        }

        final String normalized = FileUtil.toSystemIndependentName(rawPath.trim());

        final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(normalized);
        if (virtualFile != null && virtualFile.isDirectory()) {
            return virtualFile;
        }

        final File ioFile = new File(normalized);
        if (ioFile.exists() && ioFile.isDirectory()) {
            final VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(ioFile);
            if (vf != null && vf.isDirectory()) {
                return vf;
            }
        }

        final VirtualFile directUrlFile = VirtualFileManager.getInstance().findFileByUrl(rawPath.trim());
        if (directUrlFile != null && directUrlFile.isDirectory()) {
            return directUrlFile;
        }

        final VirtualFile byUrl = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(normalized));
        if (byUrl != null && byUrl.isDirectory()) {
            return byUrl;
        }

        final VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir != null) {
            if (normalized.equals(projectDir.getPath())) {
                return projectDir;
            }
            if (normalized.startsWith(projectDir.getPath() + "/")) {
                final String subRel = normalized.substring(projectDir.getPath().length() + 1);
                final VirtualFile sub = projectDir.findFileByRelativePath(subRel);
                if (sub != null && sub.isDirectory()) {
                    return sub;
                }
            }
            final VirtualFile rel = projectDir.findFileByRelativePath(normalized);
            if (rel != null && rel.isDirectory()) {
                return rel;
            }
            if (normalized.startsWith("/")) {
                final VirtualFile relNoSlash = projectDir.findFileByRelativePath(normalized.substring(1));
                if (relNoSlash != null && relNoSlash.isDirectory()) {
                    return relNoSlash;
                }
            }
        }

        for (final VirtualFile cr : ProjectRootManager.getInstance(project).getContentRoots()) {
            if (normalized.equals(cr.getPath())) {
                return cr;
            }
            if (normalized.startsWith(cr.getPath() + "/")) {
                final String subRel = normalized.substring(cr.getPath().length() + 1);
                final VirtualFile sub = cr.findFileByRelativePath(subRel);
                if (sub != null && sub.isDirectory()) {
                    return sub;
                }
            }
            final VirtualFile rel = cr.findFileByRelativePath(normalized);
            if (rel != null && rel.isDirectory()) {
                return rel;
            }
            if (normalized.startsWith("/")) {
                final VirtualFile relNoSlash = cr.findFileByRelativePath(normalized.substring(1));
                if (relNoSlash != null && relNoSlash.isDirectory()) {
                    return relNoSlash;
                }
            }
        }

        return null;
    }

    /**
     * Resolves the configured Magento directory as a VirtualFile.
     */
    public @Nullable VirtualFile resolveMagentoDirectory() {
        return resolveDirectoryForPath(Settings.getMagentoPath(project));
    }

    public boolean isPathValid(final @Nullable String rawPath) {
        return resolveDirectoryForPath(rawPath) != null;
    }

    /**
     * Checks whether a configured Magento path is present and valid.
     */
    public boolean isMagentoPathConfiguredAndValid() {
        return resolveMagentoDirectory() != null;
    }

    /**
     * Previews exclusions without modifying project state.
     */
    public @NotNull ExclusionPreview previewExclusions(
            final @Nullable Boolean enabledOverride,
            final @Nullable List<String> additionalPathsOverride
    ) {
        return previewExclusions(null, enabledOverride, additionalPathsOverride);
    }

    /**
     * Previews exclusions with optional Magento path, enabled state, and additional paths overrides.
     */
    public @NotNull ExclusionPreview previewExclusions(
            final @Nullable String magentoPathOverride,
            final @Nullable Boolean enabledOverride,
            final @Nullable List<String> additionalPathsOverride
    ) {
        final Settings settings = Settings.getInstance(project);
        final boolean pluginEnabled = settings.pluginEnabled;
        final boolean exclusionEnabled = enabledOverride != null
                ? enabledOverride
                : settings.isExcludeGeneratedFiles();
        final List<String> currentManaged = settings.getManagedExcludedFolders();

        if (!exclusionEnabled || !pluginEnabled) {
            final String message = !pluginEnabled
                    ? "Magento integration is disabled for this project."
                    : "Indexing exclusions are disabled.";
            return new ExclusionPreview(
                    isMagentoPathConfiguredAndValid(),
                    Settings.getMagentoPath(project),
                    exclusionEnabled,
                    pluginEnabled,
                    List.of(),
                    List.of(),
                    currentManaged,
                    List.of(),
                    currentManaged,
                    message
            );
        }

        final String magentoPath = magentoPathOverride != null
                ? Settings.normalizeMagentoPath(magentoPathOverride)
                : Settings.getMagentoPath(project);
        final boolean magentoPathValid = magentoPath != null && isPathValid(magentoPath);

        if (!magentoPathValid) {
            return new ExclusionPreview(
                    false,
                    magentoPath,
                    exclusionEnabled,
                    pluginEnabled,
                    List.of(),
                    List.of(),
                    currentManaged,
                    List.of(),
                    currentManaged,
                    "Configure a valid Magento installation path before indexing exclusions can be applied."
            );
        }

        final List<String> additionalPaths = additionalPathsOverride != null
                ? additionalPathsOverride
                : settings.getAdditionalExcludedPaths();

        final ResolvedExclusions resolved = resolveExclusions(magentoPath, additionalPaths);

        final Set<String> targetUrls = new LinkedHashSet<>();
        for (final ExclusionEntry entry : resolved.entries) {
            targetUrls.add(entry.url());
        }

        final List<String> urlsToAdd = new ArrayList<>();
        for (final String url : targetUrls) {
            if (!currentManaged.contains(url)) {
                urlsToAdd.add(url);
            }
        }

        final List<String> urlsToRemove = new ArrayList<>();
        for (final String url : currentManaged) {
            if (!targetUrls.contains(url)) {
                urlsToRemove.add(url);
            }
        }

        return new ExclusionPreview(
                true,
                magentoPath,
                true,
                true,
                resolved.entries,
                resolved.skipped,
                currentManaged,
                urlsToAdd,
                urlsToRemove,
                "Ready to apply " + resolved.entries.size() + " exclusion(s)."
        );
    }

    /**
     * Applies exclusions according to current project settings.
     */
    public @NotNull ExclusionApplyResult applyExclusions() {
        return applyExclusions(null, null);
    }

    /**
     * Applies exclusions with optional overrides for enabled state and additional paths.
     */
    public @NotNull ExclusionApplyResult applyExclusions(
            final @Nullable Boolean enabledOverride,
            final @Nullable List<String> additionalPathsOverride
    ) {
        final Settings settings = Settings.getInstance(project);

        if (enabledOverride != null) {
            settings.setExcludeGeneratedFiles(enabledOverride);
        }
        if (additionalPathsOverride != null) {
            settings.setAdditionalExcludedPaths(additionalPathsOverride);
        }

        final boolean shouldExclude = settings.pluginEnabled && settings.isExcludeGeneratedFiles();
        final List<String> currentManaged = new ArrayList<>(settings.getManagedExcludedFolders());

        // Perform cleanup before requiring a valid Magento path
        if (!shouldExclude) {
            final List<String> removed = removeManagedExclusions(currentManaged);
            settings.setManagedExclusionOwners(List.of());
            invalidateCache();
            notifyRootsChanged();
            return new ExclusionApplyResult(
                    true,
                    "apply",
                    List.of(),
                    removed,
                    List.of(),
                    List.of(),
                    List.of(),
                    !removed.isEmpty(),
                    !settings.pluginEnabled
                            ? "Magento integration is disabled. Managed exclusions removed."
                            : "Indexing exclusions are disabled. Managed exclusions removed."
            );
        }

        if (!isMagentoPathConfiguredAndValid()) {
            return new ExclusionApplyResult(
                    false,
                    "apply",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    false,
                    "Error: Magento installation path is not configured or is invalid. Configure a valid Magento path before exclusions can be applied."
            );
        }

        final String magentoPath = Objects.requireNonNull(Settings.getMagentoPath(project));
        final ResolvedExclusions resolved = resolveExclusions(
                magentoPath,
                settings.getAdditionalExcludedPaths()
        );

        final Map<String, ExclusionEntry> targetEntriesByUrl = new LinkedHashMap<>();
        for (final ExclusionEntry entry : resolved.entries) {
            targetEntriesByUrl.put(entry.url(), entry);
        }

        final List<String> urlsToRemove = new ArrayList<>();
        for (final String managedUrl : currentManaged) {
            if (!targetEntriesByUrl.containsKey(managedUrl)) {
                urlsToRemove.add(managedUrl);
            }
        }

        final List<String> newlyAdded = new ArrayList<>();
        final List<String> unchanged = new ArrayList<>();
        final Set<ManagedExclusion> updatedOwners = new LinkedHashSet<>(settings.getManagedExclusionOwners());
        final Module[] modules = ModuleManager.getInstance(project).getModules();

        final Runnable action = () -> {
            for (final Module module : modules) {
                try {
                    ModuleRootModificationUtil.updateModel(module, model -> {
                        final ContentEntry[] contentEntries = model.getContentEntries();
                        if (contentEntries == null || contentEntries.length == 0) {
                            return;
                        }

                        // Remove obsolete managed URLs
                        for (final String url : urlsToRemove) {
                            for (final ContentEntry entry : contentEntries) {
                                if (updatedOwners.contains(new ManagedExclusion(module.getName(), entry.getUrl(), url))
                                        && entry.getExcludeFolderUrls().contains(url)) {
                                    entry.removeExcludeFolder(url);
                                }
                            }
                        }

                        // Apply target URLs that belong to this module's entries
                        for (final Map.Entry<String, ExclusionEntry> target : targetEntriesByUrl.entrySet()) {
                            final String url = target.getKey();
                            final String path = target.getValue().path();
                            final ContentEntry matchingEntry = findBestMatchingContentEntry(contentEntries, path, url);

                            if (matchingEntry == null) {
                                continue;
                            }

                            final ManagedExclusion owner = new ManagedExclusion(module.getName(), matchingEntry.getUrl(), url);
                            final boolean alreadyExcluded = matchingEntry.getExcludeFolderUrls().contains(url);
                            if (alreadyExcluded) {
                                if (updatedOwners.contains(owner)) {
                                    if (!unchanged.contains(path)) {
                                        unchanged.add(path);
                                    }
                                    updatedOwners.add(owner);
                                }
                                // Preserve existing records without exact ownership, including legacy URL-only state.
                            } else {
                                matchingEntry.addExcludeFolder(url);
                                if (!newlyAdded.contains(path)) {
                                    newlyAdded.add(path);
                                }
                                updatedOwners.add(owner);
                            }
                        }
                    });
                } finally {
                    updatedOwners.removeIf(owner -> module.getName().equals(owner.moduleName)
                            && urlsToRemove.contains(owner.excludedUrl));
                    settings.setManagedExclusionOwners(new ArrayList<>(updatedOwners));
                }
            }
        };

        runWriteAction(action);
        invalidateCache();
        notifyRootsChanged();

        final boolean refreshPending = DumbService.getInstance(project).isDumb();
        return new ExclusionApplyResult(
                true,
                "apply",
                newlyAdded,
                urlsToRemove,
                unchanged,
                resolved.skipped,
                resolved.entries,
                !newlyAdded.isEmpty() || !urlsToRemove.isEmpty() || refreshPending,
                "Exclusions applied successfully."
        );
    }

    /**
     * Reconciles exclusions during project startup or when project integration is enabled.
     */
    public void reconcileExclusionsOnStartup() {
        final Settings settings = Settings.getInstance(project);
        if (!settings.pluginEnabled || !settings.isExcludeGeneratedFiles()) {
            if (!settings.getManagedExcludedFolders().isEmpty()) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!project.isDisposed()) {
                        applyExclusions();
                    }
                });
            }
            return;
        }

        if (!isMagentoPathConfiguredAndValid()) {
            return;
        }

        final ExclusionPreview preview = previewExclusions(null, null);
        if (!preview.urlsToAdd().isEmpty() || !preview.urlsToRemove().isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() -> {
                if (!project.isDisposed()) {
                    applyExclusions();
                }
            });
        }
    }

    /**
     * Resets indexing exclusions, removing all plugin-managed exclusions.
     */
    public @NotNull ExclusionApplyResult resetExclusions() {
        final Settings settings = Settings.getInstance(project);
        final List<String> currentManaged = new ArrayList<>(settings.getManagedExcludedFolders());
        final List<String> removed = removeManagedExclusions(currentManaged);

        settings.setManagedExclusionOwners(List.of());
        settings.setAdditionalExcludedPaths(null);
        settings.setExcludeGeneratedFiles(null);
        invalidateCache();
        notifyRootsChanged();

        return new ExclusionApplyResult(
                true,
                "reset",
                List.of(),
                removed,
                List.of(),
                List.of(),
                List.of(),
                !removed.isEmpty(),
                "Exclusions reset successfully. All plugin-managed exclusions were removed."
        );
    }

    /**
     * Formats effective exclusions and reasons for presentation in UI.
     */
    public @NotNull String getEffectiveExclusionsDisplayText() {
        final ExclusionPreview preview = previewExclusions(null, null);
        if (!preview.magentoPathValid()) {
            return "Configure a valid Magento installation path before indexing exclusions can be applied.";
        }
        if (!preview.exclusionEnabled() || !preview.pluginEnabled()) {
            return "Indexing exclusions are disabled.";
        }

        final StringBuilder sb = new StringBuilder();
        for (final ExclusionEntry entry : preview.effectiveExclusions()) {
            sb.append(entry.relativePath())
                    .append(" — ")
                    .append(entry.reason())
                    .append("\n");
        }
        if (!preview.skippedEntries().isEmpty()) {
            sb.append("\nSkipped:\n");
            for (final SkippedEntry skipped : preview.skippedEntries()) {
                sb.append(skipped.path())
                        .append(" (")
                        .append(skipped.reason())
                        .append(")\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Checks if a path is excluded by active indexing exclusions.
     * Evaluates purely in-memory using the precomputed snapshot.
     */
    public boolean isExcludedPath(final @NotNull String filePath) {
        final ExclusionSnapshot snapshot = getOrCreateSnapshot();
        if (!snapshot.active) {
            return false;
        }

        final String normalized = FileUtil.toSystemIndependentName(filePath);

        // Named cache directories are allowed inside otherwise protected source trees.
        for (final String excludedPath : snapshot.excludedPaths) {
            if (excludedPath.endsWith("/.cache")
                    && (normalized.equals(excludedPath) || normalized.startsWith(excludedPath + "/"))) {
                return true;
            }
        }
        for (final String protectedPath : snapshot.protectedPaths) {
            if (normalized.equals(protectedPath) || normalized.startsWith(protectedPath + "/")) {
                return false;
            }
        }

        for (final String excludedPath : snapshot.excludedPaths) {
            if (normalized.equals(excludedPath) || normalized.startsWith(excludedPath + "/")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns existing VirtualFiles for all effective excluded locations.
     */
    public @NotNull Set<VirtualFile> getEffectiveExcludedVirtualFiles() {
        return getOrCreateSnapshot().excludedVirtualFiles;
    }

    private static class ResolvedExclusions {
        final List<ExclusionEntry> entries = new ArrayList<>();
        final List<SkippedEntry> skipped = new ArrayList<>();
    }

    private @NotNull ResolvedExclusions resolveExclusions(
            final @NotNull String magentoPath,
            final @NotNull List<String> additionalPaths
    ) {
        final ResolvedExclusions result = new ResolvedExclusions();
        final String normalizedMagentoRoot = FileUtil.toSystemIndependentName(magentoPath.trim());
        final String projectRoot = getProjectRootDir();
        final VirtualFile magentoDir = resolveDirectoryForPath(magentoPath);
        final VirtualFile projectDir = resolveProjectDirectory();
        final String magentoUrl = magentoDir != null ? magentoDir.getUrl() : VfsUtilCore.pathToUrl(normalizedMagentoRoot);
        final String projectUrl = projectDir != null ? projectDir.getUrl() : (projectRoot != null ? VfsUtilCore.pathToUrl(projectRoot) : null);

        // 1. Process default candidates relative to Magento root
        final List<ExclusionCandidate> candidates = new ArrayList<>(DEFAULT_CANDIDATES);
        if (magentoDir != null) {
            candidates.addAll(discoverCacheDirectories(magentoDir));
        }
        for (final ExclusionCandidate candidate : candidates) {
            final String relative = candidate.relativePath();
            final String absolute = normalizedMagentoRoot + "/" + relative;
            final String url = magentoUrl + "/" + relative;
            final ValidationResult val = validatePath(absolute, normalizedMagentoRoot, projectRoot,
                    ".cache".equals(relative) || relative.endsWith("/.cache"));
            if (val.valid) {
                final File file = new File(absolute);
                final VirtualFile vf = VirtualFileManager.getInstance().findFileByUrl(url);
                if ((".cache".equals(relative) || relative.endsWith("/.cache"))
                        && (file.isFile() || (vf != null && !vf.isDirectory()))) {
                    result.skipped.add(new SkippedEntry(absolute, "Cache rule applies to directories only"));
                    continue;
                }
                final boolean exists = (file.exists() && file.isDirectory()) || (vf != null && vf.isDirectory());
                result.entries.add(new ExclusionEntry(
                        absolute,
                        relative,
                        url,
                        candidate.reason(),
                        exists,
                        false
                ));
            } else {
                result.skipped.add(new SkippedEntry(absolute, val.reason));
            }
        }

        // 2. Process explicit additional project-relative paths
        for (final String additional : additionalPaths) {
            final String normalizedRel = Settings.normalizeRelativePath(additional);
            if (normalizedRel.isEmpty()) {
                continue;
            }

            final String absolute = projectRoot != null
                    ? projectRoot + "/" + normalizedRel
                    : normalizedMagentoRoot + "/" + normalizedRel;

            final String url = projectUrl != null
                    ? projectUrl + "/" + normalizedRel
                    : (projectRoot != null
                            ? VfsUtilCore.pathToUrl(absolute)
                            : magentoUrl + "/" + normalizedRel);

            final ValidationResult val = validatePath(absolute, normalizedMagentoRoot, projectRoot, false);
            if (val.valid) {
                final File file = new File(absolute);
                final VirtualFile vf = VirtualFileManager.getInstance().findFileByUrl(url);
                final boolean exists = (file.exists() && file.isDirectory()) || (vf != null && vf.isDirectory());
                result.entries.add(new ExclusionEntry(
                        absolute,
                        normalizedRel,
                        url,
                        "User-configured project output directory",
                        exists,
                        true
                ));
            } else {
                result.skipped.add(new SkippedEntry(absolute, val.reason));
            }
        }

        return result;
    }

    private @NotNull List<ExclusionCandidate> discoverCacheDirectories(final @NotNull VirtualFile root) {
        final CacheDirectories cached = cachedCacheDirectories;
        if (cached != null && cached.rootUrl().equals(root.getUrl())) {
            return cached.candidates();
        }
        final List<ExclusionCandidate> candidates = new ArrayList<>();
        VfsUtilCore.visitChildrenRecursively(root, new VirtualFileVisitor<Void>() {
            @Override
            public boolean visitFile(final @NotNull VirtualFile file) {
                ProgressManager.checkCanceled();
                if (!file.isDirectory() || file.is(VFileProperty.SYMLINK)) {
                    return false;
                }
                if (file.equals(root)) {
                    return true;
                }
                final String relative = VfsUtilCore.getRelativePath(file, root, '/');
                if (".cache".equals(file.getName())) {
                    if (!".cache".equals(relative) && relative != null) {
                        candidates.add(new ExclusionCandidate(relative, "Local tool cache"));
                    }
                    return false;
                }
                return !".git".equals(file.getName()) && !".idea".equals(file.getName())
                        && DEFAULT_CANDIDATES.stream().noneMatch(candidate -> candidate.relativePath().equals(relative));
            }
        });
        candidates.sort(java.util.Comparator.comparing(ExclusionCandidate::relativePath));
        final List<ExclusionCandidate> result = List.copyOf(candidates);
        cachedCacheDirectories = new CacheDirectories(root.getUrl(), result);
        return result;
    }

    private record ValidationResult(boolean valid, @NotNull String reason) {}

    private @NotNull ValidationResult validatePath(
            final @NotNull String absolutePath,
            final @NotNull String magentoRoot,
            final @Nullable String projectRoot,
            final boolean cacheDirectory
    ) {
        final String normalized = FileUtil.toSystemIndependentName(absolutePath);

        // Cannot be project root or Magento root
        if (normalized.equals(magentoRoot) || (projectRoot != null && normalized.equals(projectRoot))) {
            return new ValidationResult(false, "Matches root directory");
        }

        // Cannot be an ancestor of Magento root or project root
        if (magentoRoot.startsWith(normalized + "/")) {
            return new ValidationResult(false, "Cannot exclude parent of Magento root: " + magentoRoot);
        }
        if (projectRoot != null && projectRoot.startsWith(normalized + "/")) {
            return new ValidationResult(false, "Cannot exclude parent of project root: " + projectRoot);
        }

        // Must be within project root or Magento root
        final boolean underMagento = normalized.startsWith(magentoRoot + "/");
        final boolean underProject = projectRoot != null && normalized.startsWith(projectRoot + "/");
        if (!underMagento && !underProject) {
            return new ValidationResult(false, "Resolved path is outside project or Magento root");
        }

        // Check protected Magento paths (in both directions: candidate cannot match, be inside, or enclose protected path)
        for (final String protectedPath : PROTECTED_MAGENTO_RELATIVE_PATHS) {
            final String absProtected = magentoRoot + "/" + protectedPath;
            if (!cacheDirectory && (normalized.equals(absProtected) || normalized.startsWith(absProtected + "/"))) {
                return new ValidationResult(false, "Matches protected source directory: " + protectedPath);
            }
            if (absProtected.startsWith(normalized + "/")) {
                return new ValidationResult(false, "Cannot exclude parent of protected directory: " + protectedPath);
            }
        }

        // Symlink canonical path validation if exists
        final File file = new File(normalized);
        if (file.exists()) {
            try {
                final Path realPath = file.toPath().toRealPath();
                final String realPathStr = FileUtil.toSystemIndependentName(realPath.toString());
                if (!realPathStr.equals(normalized)) {
                    // Check canonical targets against roots
                    if (realPathStr.equals(magentoRoot) || (projectRoot != null && realPathStr.equals(projectRoot))) {
                        return new ValidationResult(false, "Symlink target matches root directory");
                    }
                    if (magentoRoot.startsWith(realPathStr + "/")) {
                        return new ValidationResult(false, "Symlink target is parent of Magento root: " + magentoRoot);
                    }
                    if (projectRoot != null && projectRoot.startsWith(realPathStr + "/")) {
                        return new ValidationResult(false, "Symlink target is parent of project root: " + projectRoot);
                    }

                    final boolean realUnderMagento = realPathStr.startsWith(magentoRoot + "/");
                    final boolean realUnderProject = projectRoot != null && realPathStr.startsWith(projectRoot + "/");
                    if (!realUnderMagento && !realUnderProject) {
                        return new ValidationResult(false, "Symlink target is outside project scope");
                    }

                    // Check real path against protected Magento paths in both directions
                    for (final String protectedPath : PROTECTED_MAGENTO_RELATIVE_PATHS) {
                        final String absProtected = magentoRoot + "/" + protectedPath;
                        if (realPathStr.equals(absProtected) || realPathStr.startsWith(absProtected + "/")) {
                            return new ValidationResult(false, "Symlink points to protected source directory: " + protectedPath);
                        }
                        if (absProtected.startsWith(realPathStr + "/")) {
                            return new ValidationResult(false, "Symlink target is parent of protected directory: " + protectedPath);
                        }
                    }
                }
            } catch (IOException e) {
                // Ignore realPath failure, proceed with logical path
            }
        }

        // Verify it doesn't conflict with any module content roots (cannot match or enclose any content root)
        final VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
        for (final VirtualFile cr : contentRoots) {
            final String crPath = cr.getPath();
            if (normalized.equals(crPath)) {
                return new ValidationResult(false, "Cannot exclude project content root");
            }
            if (crPath.startsWith(normalized + "/")) {
                return new ValidationResult(false, "Cannot exclude parent of project content root: " + crPath);
            }
        }

        return new ValidationResult(true, "OK");
    }

    public @Nullable VirtualFile resolveProjectDirectory() {
        final VirtualFile dir = ProjectUtil.guessProjectDir(project);
        if (dir != null) {
            return dir;
        }
        final VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
        if (contentRoots.length > 0) {
            return contentRoots[0];
        }
        return null;
    }

    private @Nullable String getProjectRootDir() {
        final VirtualFile dir = resolveProjectDirectory();
        if (dir != null) {
            return dir.getPath();
        }
        final String basePath = project.getBasePath();
        return basePath != null ? FileUtil.toSystemIndependentName(basePath) : null;
    }

    private @Nullable ContentEntry findBestMatchingContentEntry(
            final @NotNull ContentEntry[] entries,
            final @NotNull String targetPath,
            final @NotNull String targetUrl
    ) {
        ContentEntry bestMatch = null;
        int longestPath = -1;

        for (final ContentEntry entry : entries) {
            final String entryUrl = entry.getUrl();
            final String entryPath = VfsUtilCore.urlToPath(entryUrl);
            if (targetUrl.equals(entryUrl) || targetUrl.startsWith(entryUrl + "/")
                    || targetPath.equals(entryPath) || targetPath.startsWith(entryPath + "/")) {
                if (entryPath.length() > longestPath) {
                    longestPath = entryPath.length();
                    bestMatch = entry;
                }
            }
        }

        return bestMatch;
    }

    private @NotNull List<String> removeManagedExclusions(final @NotNull List<String> urlsToRemove) {
        if (urlsToRemove.isEmpty()) {
            return List.of();
        }

        final List<String> removed = new ArrayList<>();
        final Settings settings = Settings.getInstance(project);
        final Set<ManagedExclusion> currentManaged = new LinkedHashSet<>(settings.getManagedExclusionOwners());
        final Module[] modules = ModuleManager.getInstance(project).getModules();

        final Runnable action = () -> {
            for (final Module module : modules) {
                try {
                    ModuleRootModificationUtil.updateModel(module, model -> {
                        for (final ContentEntry entry : model.getContentEntries()) {
                            for (final String url : urlsToRemove) {
                                if (currentManaged.contains(new ManagedExclusion(module.getName(), entry.getUrl(), url))
                                        && entry.getExcludeFolderUrls().contains(url)) {
                                    entry.removeExcludeFolder(url);
                                    if (!removed.contains(url)) {
                                        removed.add(url);
                                    }
                                }
                            }
                        }
                    });
                } finally {
                    currentManaged.removeIf(owner -> module.getName().equals(owner.moduleName)
                            && urlsToRemove.contains(owner.excludedUrl));
                    settings.setManagedExclusionOwners(new ArrayList<>(currentManaged));
                }
            }
        };

        runWriteAction(action);
        return removed;
    }

    private void runWriteAction(final @NotNull Runnable action) {
        final com.intellij.openapi.application.Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            WriteAction.run(action::run);
        } else {
            application.invokeAndWait(() -> WriteAction.run(action::run));
        }
    }

    private void notifyRootsChanged() {
        MagentoAdditionalLibraryRootsProvider.refreshRoots(project);
    }
}
