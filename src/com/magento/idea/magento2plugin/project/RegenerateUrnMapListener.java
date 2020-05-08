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
import com.magento.idea.magento2plugin.magento.packages.ComposerPackageModel;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponent;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoModule;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class RegenerateUrnMapListener extends MouseAdapter {
    protected final Project project;
    private static final String FRAMEWORK = "urn:magento:framework:";
    private static final String MODULE = "urn:magento:module:";


    public RegenerateUrnMapListener(final @NotNull Project project) {
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

        final Collection<VirtualFile> xsdFiles = FilenameIndex.getAllFilesByExt(project, "xsd");
        final Collection<MagentoComponent> components = componentManager.getAllComponents();

        ApplicationManager.getApplication().runWriteAction(
                new Runnable() {
                    @Override
                    public void run() {

                        for (final VirtualFile virtualFile: xsdFiles) {
                            final PsiFile psiFile = psiManager.findFile(virtualFile);
                            if (psiFile == null) {
                                continue;
                            }

                            final MagentoComponent xsdOwner =
                                    findComponentForXsd(psiFile, components);
                            if (xsdOwner == null) {
                                continue;
                            }

                            final String urnKey = buildUrnKeyForFile(psiFile, xsdOwner);
                            if (urnKey == null) {
                                continue;
                            }

                            // we need to attach resource to a project scope
                            // but with ExternalResourceManager itself it's not
                            // possible unfortunately
                            if (externalResourceManager instanceof ExternalResourceManagerEx) {
                                ((ExternalResourceManagerEx)externalResourceManager).addResource(
                                        urnKey, virtualFile.getCanonicalPath(), project
                                );
                            } else {
                                externalResourceManager.addResource(
                                        urnKey,
                                        virtualFile.getCanonicalPath()
                                );
                            }
                        }
                    }
                }
        );

        super.mouseClicked(event);
    }

    @Nullable
    protected MagentoComponent findComponentForXsd(
            final @NotNull PsiFile psiFile,
            final Collection<MagentoComponent> components
    ) {
        for (final MagentoComponent component: components) {
            if (component.isFileInContext(psiFile)) {
                return component;
            }
        }

        return null;
    }

    @Nullable
    protected String buildUrnKeyForFile(
            final @NotNull PsiFile psiFile,
            final @NotNull MagentoComponent magentoComponent
    ) {
        String prefix = null;

        if (magentoComponent instanceof MagentoModule) {
            prefix = MODULE + ((MagentoModule)magentoComponent).getMagentoName() + ":";
        } else {
            final ComposerPackageModel composerPackageModel = magentoComponent.getComposerModel();
            if ("magento2-library".equals(composerPackageModel.getType())) {
                prefix = FRAMEWORK;
            }
        }

        if (prefix == null) {
            return null;
        }

        final Stack<String> relativePath = new Stack<>();
        relativePath.push(psiFile.getName());

        final PsiManager psiManager = magentoComponent.getDirectory().getManager();
        PsiDirectory parentDir = psiFile.getParent();
        while (parentDir != null && !psiManager.areElementsEquivalent(
                parentDir,
                magentoComponent.getDirectory())
        ) {
            relativePath.push("/");
            relativePath.push(parentDir.getName());
            parentDir = parentDir.getParentDirectory();
        }

        final StringBuilder stringBuilder = new StringBuilder(prefix);
        while (!relativePath.empty()) {
            stringBuilder.append(relativePath.pop());
        }

        return stringBuilder.toString();
    }
}
