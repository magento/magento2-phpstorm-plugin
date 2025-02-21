/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.groups;

import com.intellij.ide.actions.NonTrivialActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.util.IsClickedDirectoryInsideProject;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import java.util.Collection;

public class NewModuleFileGroup extends NonTrivialActionGroup {

    /**
     * Group for generate module file actions.
     */
    public NewModuleFileGroup() {
        super();

        this.getTemplatePresentation().setIcon(
                IconLoader.createLazy(() -> MagentoIcons.MODULE)
        );
    }

    @Override
    public void update(final AnActionEvent event) {
        final PsiElement psiElement = event.getData(PlatformDataKeys.PSI_ELEMENT);
        if (!(psiElement instanceof PsiDirectory)) {
            event.getPresentation().setVisible(false);
            return;
        }

        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project == null
                || !Settings.isEnabled(project)
                || !IsClickedDirectoryInsideProject.getInstance()
                .execute(project, (PsiDirectory) psiElement)) {
            event.getPresentation().setVisible(false);
            return;
        }

        // Skip processing if the IDE is in dumb mode
        if (com.intellij.openapi.project.DumbService.isDumb(project)) {
            event.getPresentation().setVisible(false);
            return;
        }

        String moduleName = null;
        VirtualFile psiDirectoryVirtualFile = ((PsiDirectory) psiElement).getVirtualFile();

        for (var entry : FileBasedIndex.getInstance().getAllKeys(ModuleNameIndex.KEY, project)) {
            Collection<VirtualFile> moduleVfs = FileBasedIndex.getInstance().getContainingFiles(
                    ModuleNameIndex.KEY, entry, GlobalSearchScope.projectScope(project)
            );

            for (VirtualFile moduleFile : moduleVfs) {
                if (moduleFile.getParent().getPath().equals(psiDirectoryVirtualFile.getPath())) {
                    moduleName = entry;
                    break;
                }
            }

            if (moduleName != null) {
                break;
            }
        }

        if (moduleName != null) {
            event.getPresentation().setVisible(true);
            return;
        }

        event.getPresentation().setVisible(false);
    }
}
