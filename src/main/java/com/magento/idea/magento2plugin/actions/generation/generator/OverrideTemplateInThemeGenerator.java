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
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.OverridableFileType;
import com.magento.idea.magento2plugin.util.magento.GetComponentNameByDirectoryUtil;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.util.List;
import java.util.Objects;

public class OverrideTemplateInThemeGenerator extends OverrideInThemeGenerator {

    /**
     * OverrideTemplateInThemeGenerator constructor.
     *
     * @param project Project
     */
    public OverrideTemplateInThemeGenerator(final Project project) {
        super(project);
    }

    /**
     * Action entry point.
     *
     * @param baseFile PsiFile
     * @param themeName String
     */
    public void execute(final PsiFile baseFile, final String themeName) {

        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(baseFile.getContainingDirectory(), project);
        List<String> pathComponents; //NOPMD

        if (moduleData == null) {
            if (Objects.equals(baseFile.getVirtualFile().getExtension(), OverridableFileType.JS.getType())) {
                pathComponents = getLibPathComponets(baseFile);
            } else {
                return;
            }
        } else {

            if (moduleData.getType().equals(ComponentType.module)) {
                pathComponents = getModulePathComponents(
                        baseFile,
                        GetComponentNameByDirectoryUtil.execute(
                                baseFile.getContainingDirectory(),
                                project
                        )
                );
            } else if (moduleData.getType().equals(ComponentType.theme)) {
                pathComponents = getThemePathComponents(baseFile);
            } else {
                return;
            }
        }

        final ModuleIndex moduleIndex = new ModuleIndex(project);
        PsiDirectory directory = moduleIndex.getModuleDirectoryByModuleName(themeName);

        if (directory == null) {
            return;
        }
        directory = getTargetDirectory(directory, pathComponents);

        if (directory.findFile(baseFile.getName()) != null) {
            final PsiDirectory finalDirectory1 = directory;
            ApplicationManager.getApplication().invokeLater(() -> {
                JBPopupFactory.getInstance()
                        .createMessage(
                                validatorBundle.message(
                                        "validator.file.alreadyExists",
                                        baseFile.getName()
                                )
                        ).showCenteredInCurrentWindow(project);
                ApplicationManager.getApplication().invokeLater(() -> {
                    Objects.requireNonNull(
                            finalDirectory1.findFile(baseFile.getName())
                    ).navigate(true);
                });
            });
            return;
        }


        final PsiDirectory finalDirectory = directory;
        ApplicationManager.getApplication().runWriteAction(() -> {
            finalDirectory.copyFileFrom(baseFile.getName(), baseFile);
        });

        final PsiFile newFile = directory.findFile(baseFile.getName());
        assert newFile != null;
        final Module module = ModuleUtilCore.findModuleForPsiElement(newFile);
        final UpdateCopyrightProcessor processor = new UpdateCopyrightProcessor(
                project,
                module,
                newFile
        );
        processor.run();

        newFile.navigate(true);
    }
}
