/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;

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
        final PsiManager psiManager = PsiManager.getInstance(project);
        // There might be a more efficient way to grab all Interceptors and Proxies in the generated folder
        // But with my limited Java knowledge, I can't find it.
        final Collection<VirtualFile> phpFiles = FilenameIndex.getAllFilesByExt(project, "php");

        ApplicationManager.getApplication().runWriteAction(
                new Runnable() {
                    @Override
                    public void run() {
                        final Stack<String> skippedPaths = new Stack<>();
                        for (final VirtualFile virtualFile: phpFiles) {
                            final PsiFile psiFile = psiManager.findFile(virtualFile);
                            if (psiFile == null) {
                                continue;
                            }

                            // If we have a more efficient way to find these files, this check hopefully
                            // wouldn't be necessary
                            if (!psiFile.getName().contains("Interceptor") && !psiFile.getName().contains("Proxy")) {
                                continue;
                            }

                            skippedPaths.push(psiFile.getName());
                        }

                        // At this point we should have a stack with all paths to the interceptors and proxies
                        // However, it is unclear to me how (or even if) we can place these in the Skipped Paths
                        // list in the configuration under Settings > PHP > Debug > Step Filters > Skipped Paths
                        // since the code below doesn't work. We might have to resort to appending the data
                        // to .idea/php.xml manually.
                        PropertiesComponent.getInstance()
                                .setValue("settings.php.debug.skipped.paths", skippedPaths.toString());
                    }
                }
        );

        super.mouseClicked(event);
    }
}
