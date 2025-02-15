/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.javaee.ExternalResourceManager;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class RegenerateUrnMapListener extends MouseAdapter {
    protected final Project project;
    private static final String FRAMEWORK = "urn:magento:framework:";
    private static final String MODULE = "urn:magento:module:";
    private static final String COMPOSER_MODEL = "magento2-library";


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
        final ExternalResourceManager manager =
                ExternalResourceManager.getInstance();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final MagentoComponentManager componentManager =
                MagentoComponentManager.getInstance(project);

        ApplicationManager.getApplication().runWriteAction(
                new Runnable() {
                    @Override
                    public void run() {
                        final Collection<VirtualFile> xsdFiles
                                = FilenameIndex.getAllFilesByExt(project, "xsd");
                        final Collection<MagentoComponent> components
                                = componentManager.getAllComponents();
                        int processedFileCount = 0;

                        for (final VirtualFile file : xsdFiles) {
                            if (handleXsdFile(file, components, psiManager, manager)) {
                                continue;
                            }

                            processedFileCount++;
                        }

                        showNotification(processedFileCount);
                    }
                }
        );

        super.mouseClicked(event);
    }

    /**
     * Handles an XSD file by associating it with a resource in the ExternalResourceManager
     * and resolves its context with relevant components.
     *
     * @param virtualFile The virtual file representing the XSD file to be handled.
     * @param components A collection of MagentoComponent objects used to determine the
     *                   component context for the XSD file.
     * @param psiManager The PsiManager used to resolve the virtual file into a PsiFile.
     * @param externalResourceManager The manager used to add or map external resources.
     * @return {@code true} if the XSD file was processed successfully or required no actions;
     *         {@code false} if the file was successfully associated with a URN resource.
     */
    private boolean handleXsdFile(
            final VirtualFile virtualFile,
            final Collection<MagentoComponent> components,
            final PsiManager psiManager,
            final ExternalResourceManager externalResourceManager
    ) {
        final PsiFile psiFile = psiManager.findFile(virtualFile);
        if (psiFile == null) {
            return true;
        }

        final MagentoComponent xsdOwner =
                findComponentForXsd(psiFile, components);
        if (xsdOwner == null) {
            return true;
        }

        final String urnKey = buildUrnKeyForFile(psiFile, xsdOwner);
        if (urnKey == null) {
            return true;
        }

        // we need to attach resource to a project scope
        // but with ExternalResourceManager itself it's not
        // possible unfortunately
        if (externalResourceManager instanceof ExternalResourceManagerEx) {
            ((ExternalResourceManagerEx) externalResourceManager).addResource(
                    urnKey, virtualFile.getCanonicalPath(), project
            );
        } else {
            externalResourceManager.addResource(
                    urnKey,
                    virtualFile.getCanonicalPath()
            );
        }
        return false;
    }

    /**
     * Displays a notification based on the number of processed files for URN mapping generation.
     * If the {@code processedFileCount} is greater than zero, an information notification is shown
     * indicating the successful completion of URN map generation. Otherwise, a warning notification
     * is displayed indicating the failure of URN map generation.
     *
     * @param processedFileCount The number of files successfully processed for URN map generation.
     */
    @SuppressWarnings("PMD.UseNotifyAllInsteadOfNotify")
    private void showNotification(final int processedFileCount) {
        if (processedFileCount > 0) {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Magento Notifications")
                    .createNotification(
                            "URN map generation completed",
                            "Processed " + processedFileCount + " URN mappings.",
                            NotificationType.INFORMATION
                    )
                    .notify(project);
        } else {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Magento Notifications")
                    .createNotification(
                            "URN map generation failed",
                            "No URN mappings were generated. Check your configuration.",
                            NotificationType.WARNING
                    )
                    .notify(project);
        }
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
            if (COMPOSER_MODEL.equals(composerPackageModel.getType())) {
                prefix = FRAMEWORK;
            }
        }

        if (prefix == null) {
            return null;
        }

        final Deque<String> relativePath = new ArrayDeque<>();
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
        while (!relativePath.isEmpty()) {
            stringBuilder.append(relativePath.pop());
        }

        return stringBuilder.toString();
    }
}
