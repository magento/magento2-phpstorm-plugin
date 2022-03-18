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
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetComponentNameByDirectoryUtil;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.util.List;

public class OverrideLayoutInThemeGenerator extends OverrideInThemeGenerator {

    /**
     * OverrideLayoutInThemeGenerator constructor.
     *
     * @param project Project
     */
    public OverrideLayoutInThemeGenerator(final Project project) {
        super(project);
    }

    /**
     * Action entry point.
     *
     * @param baseFile PsiFile
     * @param themeName String
     * @param isOverride boolean
     */
    public void execute(
            final PsiFile baseFile,
            final String themeName,
            final boolean isOverride
    ) {
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(baseFile.getContainingDirectory(), project);

        if (moduleData == null) {
            return;
        }
        List<String> pathComponents;

        if (moduleData.getType().equals(ComponentType.module)) {
            pathComponents = getModulePathComponents(
                    baseFile,
                    GetComponentNameByDirectoryUtil.execute(
                            baseFile.getContainingDirectory(),
                            project
                    )
            );
            if (isOverride) {
                pathComponents.add("override");
                pathComponents.add("base");
            }
        } else if (moduleData.getType().equals(ComponentType.theme)) {
            pathComponents = getThemePathComponents(baseFile);
            
            if (isOverride) {
                pathComponents.add("override");
                pathComponents.add("theme");
                final String[] parentThemeName =
                        moduleData.getName().split(Package.V_FILE_SEPARATOR);
                pathComponents.add(parentThemeName[1]);
                pathComponents.add(parentThemeName[2]);
            }
        } else {
            return;
        }

        final ModuleIndex moduleIndex = new ModuleIndex(project);
        PsiDirectory directory = moduleIndex.getModuleDirectoryByModuleName(themeName);

        if (directory == null) {
            return;
        }
        directory = getTargetDirectory(directory, pathComponents);

        if (directory.findFile(baseFile.getName()) != null) {
            JBPopupFactory.getInstance()
                    .createMessage(
                            validatorBundle.message(
                                    "validator.file.alreadyExists",
                                    baseFile.getName())
                    )
                    .showCenteredInCurrentWindow(project);
            directory.findFile(baseFile.getName()).navigate(true);
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
