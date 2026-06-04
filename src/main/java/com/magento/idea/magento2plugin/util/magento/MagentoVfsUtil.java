/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MagentoVfsUtil {
    private static final String[] MAGENTO_SCAN_ROOTS = {"app/code", "app/design", "vendor", "lib/web"};

    private MagentoVfsUtil() {
    }

    public static @NotNull Collection<VirtualFile> findMagentoFiles(
            final @NotNull Project project,
            final @NotNull Predicate<VirtualFile> predicate
    ) {
        final Set<VirtualFile> result = new LinkedHashSet<>();

        for (final VirtualFile root : getMagentoScanRoots(project)) {
            VfsUtilCore.visitChildrenRecursively(root, new VirtualFileVisitor<>() {
                @Override
                public boolean visitFile(final @NotNull VirtualFile file) {
                    if (!file.isDirectory() && predicate.test(file)) {
                        result.add(file);
                    }

                    return true;
                }
            });
        }

        return result;
    }

    public static @Nullable VirtualFile findByPath(final @NotNull String path) {
        return VirtualFileManager.getInstance().findFileByUrl("file://" + path);
    }

    public static @NotNull Collection<VirtualFile> getMagentoScanRoots(final @NotNull Project project) {
        final String magentoPath = Settings.getMagentoPath(project);
        final Collection<VirtualFile> roots = new ArrayList<>();

        if (magentoPath == null) {
            return roots;
        }

        for (final String relativeRoot : MAGENTO_SCAN_ROOTS) {
            final VirtualFile root = findByPath(magentoPath + "/" + relativeRoot);

            if (root != null && root.isDirectory()) {
                roots.add(root);
            }
        }

        return roots;
    }
}
