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
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.magento.idea.magento2plugin.magento.packages.ComposerPackageModel;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponent;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoModule;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class RegenerateUrnMapListener implements ActionListener {
    protected final Project project;
    private static final String FRAMEWORK = "urn:magento:framework:";
    private static final String MODULE = "urn:magento:module:";
    private static final String COMPOSER_MODEL = "magento2-library";


    public RegenerateUrnMapListener(final @NotNull Project project) {
        this.project = project;
    }

    /**
     * Handler for the regenerate button action.
     *
     * @param event ActionEvent
     */
    @Override
    @SuppressWarnings("PMD.UseNotifyAllInsteadOfNotify")
    public void actionPerformed(final ActionEvent event) {
        if (DumbService.getInstance(project).isDumb()) {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Magento Notifications")
                    .createNotification(
                            "URN map generation unavailable",
                            "Indexing is in progress."
                                    + " Please wait for it to complete"
                                    + " before running URN mapping generation.",
                            NotificationType.WARNING
                    )
                    .notify(project);
            return;
        }

        final ModalityState modalityState = ModalityState.current();
        ReadAction.nonBlocking(this::collectUrnMappings)
                .expireWith(project)
                .finishOnUiThread(modalityState, this::applyUrnMappings)
                .submit(AppExecutorUtil.getAppExecutorService());
    }

    /**
     * Builds a URN mapping for an XSD file and its owning Magento component.
     *
     * @param virtualFile The virtual file representing the XSD file to be handled.
     * @param components A collection of MagentoComponent objects used to determine the
     *                   component context for the XSD file.
     * @param psiManager The PsiManager used to resolve the virtual file into a PsiFile.
     * @return the mapping, or {@code null} when the file does not belong to a supported component.
     */
    @Nullable
    private UrnMapping buildUrnMapping(
            final VirtualFile virtualFile,
            final Collection<MagentoComponent> components,
            final PsiManager psiManager
    ) {
        final PsiFile psiFile = psiManager.findFile(virtualFile);
        if (psiFile == null) {
            return null;
        }

        final MagentoComponent xsdOwner =
                findComponentForXsd(psiFile, components);
        if (xsdOwner == null) {
            return null;
        }

        final String urnKey = buildUrnKeyForFile(psiFile, xsdOwner);
        final String canonicalPath = virtualFile.getCanonicalPath();
        if (urnKey == null || canonicalPath == null) {
            return null;
        }

        return new UrnMapping(urnKey, canonicalPath);
    }

    private List<UrnMapping> collectUrnMappings() {
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Collection<MagentoComponent> components = MagentoComponentManager
                .getInstance(project)
                .getAllComponents();
        final Collection<VirtualFile> xsdFiles = FilenameIndex.getAllFilesByExt(project, "xsd");
        final List<UrnMapping> mappings = new ArrayList<>();

        for (final VirtualFile file : xsdFiles) {
            final UrnMapping mapping = buildUrnMapping(file, components, psiManager);
            if (mapping != null) {
                mappings.add(mapping);
            }
        }

        return mappings;
    }

    private void applyUrnMappings(final List<UrnMapping> mappings) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            final ExternalResourceManager manager = ExternalResourceManager.getInstance();
            for (final UrnMapping mapping : mappings) {
                // ExternalResourceManagerEx is required to keep mappings project-scoped.
                if (manager instanceof ExternalResourceManagerEx) {
                    ((ExternalResourceManagerEx) manager).addResource(
                            mapping.urn,
                            mapping.path,
                            project
                    );
                } else {
                    manager.addResource(mapping.urn, mapping.path);
                }
            }
        });

        showNotification(mappings.size());
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

    private static final class UrnMapping {
        private final String urn;
        private final String path;

        private UrnMapping(final String urn, final String path) {
            this.urn = urn;
            this.path = path;
        }
    }
}
