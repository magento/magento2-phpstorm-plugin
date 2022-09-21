/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.maddyhome.idea.copyright.actions.UpdateCopyrightProcessor;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class OverrideTemplateInModuleGenerator {

    private final Project project;
    private final ValidatorBundle validatorBundle;

    /**
     * OverrideInThemeGenerator constructor.
     *
     * @param project Project
     */
    public OverrideTemplateInModuleGenerator(final Project project) {
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
    }

    /**
     * Action entry point.
     *
     * @param baseFile PsiFile
     * @param moduleName String
     */
    public void execute(final @NotNull PsiFile baseFile, final String moduleName) {
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(baseFile.getContainingDirectory(), project);

        if (moduleData == null || !moduleData.getType().equals(ComponentType.module)) {
            return;
        }
        final ModuleIndex moduleIndex = new ModuleIndex(project);
        PsiDirectory directory = moduleIndex.getModuleDirectoryByModuleName(moduleName);

        if (directory == null) {
            return;
        }
        final List<String> pathComponents = getModulePathComponents(baseFile);
        directory = getTargetDirectory(directory, pathComponents);
        final PsiFile existentFile = directory.findFile(baseFile.getName());

        if (existentFile != null) {
            JBPopupFactory.getInstance()
                    .createMessage(
                            validatorBundle.message(
                                    "validator.file.alreadyExists",
                                    baseFile.getName()
                            )
                    )
                    .showCenteredInCurrentWindow(project);
            existentFile.navigate(true);
            return;
        }
        final PsiDirectory finalDirectory = directory;

        ApplicationManager.getApplication().runWriteAction(() -> {
            finalDirectory.copyFileFrom(baseFile.getName(), baseFile);
        });
        final PsiFile newFile = finalDirectory.findFile(baseFile.getName());

        if (newFile == null) {
            JBPopupFactory.getInstance()
                    .createMessage(
                            validatorBundle.message(
                                    "validator.file.cantBeCreated",
                                    baseFile.getName()
                            )
                    )
                    .showCenteredInCurrentWindow(project);
            return;
        }
        final Module module = ModuleUtilCore.findModuleForPsiElement(newFile);
        final UpdateCopyrightProcessor processor = new UpdateCopyrightProcessor(
                project,
                module,
                newFile
        );
        processor.run();
        newFile.navigate(true);
    }

    private List<String> getModulePathComponents(final PsiFile file) {
        final List<String> pathComponents = new ArrayList<>();
        final List<String> allowedAreas = new ArrayList<>();
        allowedAreas.add(Areas.frontend.toString());
        allowedAreas.add(Areas.adminhtml.toString());
        allowedAreas.add(Areas.base.toString());
        PsiDirectory parent = file.getParent();

        while (parent != null
                && !allowedAreas.contains(parent.getName())) {
            pathComponents.add(parent.getName());
            parent = parent.getParent();
        }

        if (parent != null && allowedAreas.contains(parent.getName())) {
            pathComponents.add(parent.getName());
        }
        pathComponents.add("view");
        Collections.reverse(pathComponents);

        return pathComponents;
    }

    private PsiDirectory getTargetDirectory(
            final PsiDirectory directory,
            final List<String> pathComponents
    ) {
        final DirectoryGenerator generator = DirectoryGenerator.getInstance();

        return generator.findOrCreateSubdirectories(
                directory,
                pathComponents.stream().collect(Collectors.joining(File.separator))
        );
    }
}
