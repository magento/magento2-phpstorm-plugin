/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.javaee.ExternalResourceManager;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.magento.idea.magento2plugin.magento.packages.ComposerPackageModel;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponent;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Stack;

class GenerateDebugStepFiltersListener extends MouseAdapter {
    protected final Project project;


    public GenerateDebugStepFiltersListener(final @NotNull Project project) {
        super();
        this.project = project;
    }

    /**
     * Handler for mouse click.
     *
     * @param event MouseEvent
     */
    @Override
    public void mouseClicked(final MouseEvent event) {
        final ExternalResourceManager externalResourceManager =
                ExternalResourceManager.getInstance();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final MagentoComponentManager componentManager =
                MagentoComponentManager.getInstance(project);

        ApplicationManager.getApplication().runWriteAction(
                new Runnable() {
                    @Override
                    public void run() {

                    }
                }
        );

        super.mouseClicked(event);
    }
}
