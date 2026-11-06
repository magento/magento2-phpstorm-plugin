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
import com.magento.idea.magento2plugin.util.magento.IsFileInEditableModuleUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoPathUrlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Keeps Reader Mode away from project code that {@link MagentoAdditionalLibraryRootsProvider}
 * reports as a library, because Reader Mode skips local inspections in library files.
 * The vendor folder is left to the platform unless it is marked as a Magento content root,
 * because it holds dependencies that are not edited in place.
 */
public class MagentoReaderModeMatcher implements ReaderModeMatcher {

    @Override
    public @Nullable Boolean matches(
            final @NotNull Project project,
            final @NotNull VirtualFile file,
            final @Nullable Editor editor,
            final ReaderModeProvider.@NotNull ReaderMode mode
    ) {
        return isExcludedFromReaderMode(project, file) ? Boolean.FALSE : null;
    }

    /**
     * Checks that a file is writable project code that Reader Mode must not apply to.
     *
     * @param project the current project
     * @param file the file to check
     * @return true if Reader Mode must not apply to the file
     */
    public static boolean isExcludedFromReaderMode(
            final @NotNull Project project,
            final @NotNull VirtualFile file
    ) {
        return file.isWritable()
                && Settings.isEnabled(project)
                && (IsFileInEditableModuleUtil.execute(project, file)
                || IsFileInEditableModuleUtil.isUnderRoot(
                        file,
                        MagentoPathUrlUtil.getLibWebPath(project)
                ));
    }
}
