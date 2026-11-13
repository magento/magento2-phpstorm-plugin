/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.indexing;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.AdditionalLibraryRootsListenerHelper;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagentoAdditionalLibraryRootsProvider extends AdditionalLibraryRootsProvider {

    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(
            final @NotNull Project project
    ) {
        final List<VirtualFile> roots = new ArrayList<>(getMagentoIndexRoots(project));

        if (roots.isEmpty()) {
            return List.of();
        }

        final Set<VirtualFile> excludedRoots = MagentoIndexingExclusionService.getInstance(project)
                .getEffectiveExcludedVirtualFiles();

        final com.intellij.openapi.util.Condition<VirtualFile> condition =
                file -> file != null && MagentoIndexingExclusionService.getInstance(project)
                        .isExcludedPath(file.getPath());

        return List.of(SyntheticLibrary.newImmutableLibrary(roots, excludedRoots, condition));
    }

    @Override
    public @NotNull Collection<VirtualFile> getRootsToWatch(final @NotNull Project project) {
        return getMagentoIndexRoots(project);
    }

    public static void refreshRoots(final @NotNull Project project) {
        MagentoIndexingExclusionService.getInstance(project).invalidateCache();
        final Collection<VirtualFile> roots = new MagentoAdditionalLibraryRootsProvider()
                .getMagentoIndexRoots(project);

        final Runnable action = () -> {
            try {
                AdditionalLibraryRootsListenerHelper.getInstance()
                        .handleAdditionalLibraryRootsChanged(
                            project,
                            null,
                            List.of(),
                            roots,
                            "Magento additional library roots"
                        );
            } catch (Throwable ignored) {
                // Ignore if platform listener helper is unavailable in certain test environments
            }
        };

        if (com.intellij.openapi.application.ApplicationManager.getApplication().isDispatchThread()) {
            WriteAction.run(action::run);
        } else {
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> WriteAction.run(action::run));
        }
    }

    private @NotNull Collection<VirtualFile> getMagentoIndexRoots(final @NotNull Project project) {
        final Set<VirtualFile> roots = new LinkedHashSet<>();

        if (!Settings.isEnabled(project)) {
            return roots;
        }

        roots.addAll(MagentoVfsUtil.getMagentoScanRoots(project));

        final List<String> magentoFolders = Settings.getInstance(project).getMagentoFolders();

        if (magentoFolders != null) {
            for (final String folderUrl : magentoFolders) {
                addDirectory(roots, findByUrl(folderUrl));
            }
        }

        return roots;
    }

    private static void addDirectory(
            final @NotNull Set<VirtualFile> roots,
            final @Nullable VirtualFile file
    ) {
        if (file != null && file.isDirectory()) {
            roots.add(file);
        }
    }

    private static @Nullable VirtualFile findByUrl(final @Nullable String url) {
        if (url == null) {
            return null;
        }

        return VirtualFileManager.getInstance().findFileByUrl(url);
    }
}
