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
import com.intellij.openapi.util.Key;
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
    private static final Key<Collection<SyntheticLibrary>> LAST_LIBRARIES =
            Key.create("magento.additional.libraries");

    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(
            final @NotNull Project project
    ) {
        final Collection<SyntheticLibrary> libraries = createLibraries(project);
        project.putUserData(LAST_LIBRARIES, libraries);
        return libraries;
    }

    private @NotNull Collection<SyntheticLibrary> createLibraries(final @NotNull Project project) {
        final List<VirtualFile> roots = new ArrayList<>(getMagentoIndexRoots(project));

        if (roots.isEmpty()) {
            return List.of();
        }

        final MagentoIndexingExclusionService exclusions = MagentoIndexingExclusionService.getInstance(project);
        return List.of(exclusions.createLibrary(roots));
    }

    @Override
    public @NotNull Collection<VirtualFile> getRootsToWatch(final @NotNull Project project) {
        return getMagentoIndexRoots(project);
    }

    public static void refreshRoots(final @NotNull Project project) {
        MagentoIndexingExclusionService.getInstance(project).invalidateCache();
        final Runnable action = () -> {
            if (project.isDisposed()) {
                return;
            }
            final Collection<SyntheticLibrary> libraries = new MagentoAdditionalLibraryRootsProvider()
                    .createLibraries(project);
            final Collection<SyntheticLibrary> previous = project.getUserData(LAST_LIBRARIES);
            if (libraries.equals(previous == null ? List.of() : previous)) {
                return;
            }
            project.putUserData(LAST_LIBRARIES, libraries);
            final Set<VirtualFile> roots = new LinkedHashSet<>();
            for (final SyntheticLibrary library : libraries) {
                roots.addAll(library.getSourceRoots());
            }
            AdditionalLibraryRootsListenerHelper.getInstance()
                .handleAdditionalLibraryRootsChanged(
                    project,
                    null,
                    List.of(),
                    roots,
                    "Magento additional library roots"
                );
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
