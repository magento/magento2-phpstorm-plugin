/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.content.root;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.actions.MarkRootActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.MagentoPathUrlUtil;
import java.net.URI;
import java.net.MalformedURLException;
import org.jetbrains.annotations.NotNull;

public class MarkDirectoryAsMagentoContentRot extends MarkRootActionBase {
    private Project project;
    
    /**
     * This action is used to mark a selected directory as
     * a Magento content root within the project structure.
     */
    public MarkDirectoryAsMagentoContentRot() {
        super();
        final Presentation presentation = this.getTemplatePresentation();
        presentation.setIcon(MagentoIcons.MARK_AS);
    }

    @Override
    protected void modifyRoots(
            final VirtualFile virtualFile,
            final ContentEntry contentEntry
    ) {
        if (project != null) {
            final Settings settings = Settings.getInstance(project);
            Settings.getInstance(project).addMagentoFolder(virtualFile.getUrl());
            if (settings.getMagentoFolders() != null) {
                settings.getMagentoFolders().removeIf(folder -> {
                    final VirtualFile file;
                    try {
                        file = VfsUtil.findFileByURL(URI.create(folder).toURL());
                    } catch (IllegalArgumentException | MalformedURLException e) {
                        return false;
                    }
                    return file == null || !file.exists();
                });
            }
            
            ProjectView.getInstance(project).refresh();
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        final DataContext context = event.getDataContext();
        final PsiElement targetElement = LangDataKeys.PSI_ELEMENT.getData(context);
        final Module module = event.getData(PlatformCoreDataKeys.MODULE);
        if (module != null) {
            project = module.getProject();
        }

        if (targetElement instanceof PsiDirectory && project != null) {
            final String magentoPathUrl = MagentoPathUrlUtil.execute(project);
            final String directoryUrl = ((PsiDirectory) targetElement).getVirtualFile().getUrl();
            if (magentoPathUrl != null && magentoPathUrl.equals(directoryUrl)) {
                event.getPresentation().setEnabledAndVisible(false);
                return;
            }
            final Settings settings = Settings.getInstance(project);
            if (!settings.containsMagentoFolder(directoryUrl)) {
                event.getPresentation().setEnabledAndVisible(true);
                return;
            }
        }

        event.getPresentation().setEnabledAndVisible(false);
    }

    @Override
    protected boolean isEnabled(
            @NotNull final RootsSelection rootsSelection,
            @NotNull final Module module
    ) {
        return false;
    }
}
