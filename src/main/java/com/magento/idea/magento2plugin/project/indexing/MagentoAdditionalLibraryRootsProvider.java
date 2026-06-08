/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.indexing;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.RootsChangeRescanningInfo;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
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
    private static final String[] MAGENTO_INDEX_ROOTS = {
            "app/code",
            "app/design",
            "vendor",
            "lib/web"
    };

    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(
            final @NotNull Project project
    ) {
        final List<VirtualFile> roots = new ArrayList<>(getMagentoIndexRoots(project));

        if (roots.isEmpty()) {
            return List.of();
        }

        return List.of(SyntheticLibrary.newImmutableLibrary(roots));
    }

    @Override
    public @NotNull Collection<VirtualFile> getRootsToWatch(final @NotNull Project project) {
        return getMagentoIndexRoots(project);
    }

    public static void refreshRoots(final @NotNull Project project) {
        WriteAction.run(() -> ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(
                () -> { },
                RootsChangeRescanningInfo.TOTAL_RESCAN
        ));
    }

    private @NotNull Collection<VirtualFile> getMagentoIndexRoots(final @NotNull Project project) {
        final Set<VirtualFile> roots = new LinkedHashSet<>();

        if (!Settings.isEnabled(project)) {
            return roots;
        }

        final String magentoPath = Settings.getMagentoPath(project);

        if (magentoPath != null) {
            for (final String relativeRoot : MAGENTO_INDEX_ROOTS) {
                addDirectory(roots, MagentoVfsUtil.findByPath(magentoPath + "/" + relativeRoot));
            }
        }

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
