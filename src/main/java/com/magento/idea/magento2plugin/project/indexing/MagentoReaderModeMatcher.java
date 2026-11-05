/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.indexing;

import com.intellij.codeInsight.actions.ReaderModeMatcher;
import com.intellij.codeInsight.actions.ReaderModeProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Keeps Reader Mode away from project code that {@link MagentoAdditionalLibraryRootsProvider}
 * reports as a library, because Reader Mode skips local inspections in library files.
 */
public class MagentoReaderModeMatcher implements ReaderModeMatcher {
    private static final String[] EDITABLE_ROOTS = {
            "app/code",
            "app/design",
            "lib/web"
    };

    @Override
    public @Nullable Boolean matches(
            final @NotNull Project project,
            final @NotNull VirtualFile file,
            final @Nullable Editor editor,
            final ReaderModeProvider.@NotNull ReaderMode mode
    ) {
        if (file.isWritable() && isInEditableRoot(project, file)) {
            return Boolean.FALSE;
        }

        return null;
    }

    private static boolean isInEditableRoot(
            final @NotNull Project project,
            final @NotNull VirtualFile file
    ) {
        if (!Settings.isEnabled(project)) {
            return false;
        }
        final String magentoPath = Settings.getMagentoPath(project);

        if (magentoPath != null) {
            for (final String root : EDITABLE_ROOTS) {
                if (file.getPath().startsWith(magentoPath + "/" + root + "/")) {
                    return true;
                }
            }
        }
        final List<String> magentoFolders = Settings.getInstance(project).getMagentoFolders();

        if (magentoFolders != null) {
            for (final String folderUrl : magentoFolders) {
                if (file.getUrl().startsWith(folderUrl + "/")) {
                    return true;
                }
            }
        }

        return false;
    }
}
