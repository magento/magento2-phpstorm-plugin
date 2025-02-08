/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.md;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.magento.idea.magento2plugin.actions.context.CustomGeneratorContextAction;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleReadmeMdData;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleReadmeMdGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleReadmeMdFile;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class NewReadmeMdAction extends CustomGeneratorContextAction {

    public static final String ACTION_NAME = "Magento 2 README File";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 README file";

    public NewReadmeMdAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final GetMagentoModuleUtil.MagentoModuleData moduleData = getModuleData();

        if (event.getProject() == null || moduleData == null || getDirectory() == null) {
            return;
        }
        final String[] templateData = moduleData.getName().split(Package.vendorModuleNameSeparator);

        if (templateData.length != 2) { //NOPMD
            return;
        }
        final ModuleReadmeMdGenerator generator = new ModuleReadmeMdGenerator(
                new ModuleReadmeMdData(
                        templateData[0],
                        templateData[1],
                        getDirectory()
                ),
                event.getProject()
        );
        generator.generate(ACTION_NAME, true);
    }

    @Override
    protected boolean isVisible(
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final PsiDirectory targetDirectory,
            final PsiFile targetFile
    ) {
        if (!moduleData.getType().equals(ComponentType.module)) {
            return false;
        }
        final PsiDirectory moduleDirectory = new ModuleIndex(targetDirectory.getProject())
                .getModuleDirectoryByModuleName(moduleData.getName());

        if (moduleDirectory == null) {
            return false;
        }
        final String[] templateData = moduleData.getName().split(Package.vendorModuleNameSeparator);

        return templateData.length == 2
                && targetDirectory.equals(moduleDirectory)
                && isFileCanBeCreated(moduleDirectory);
    }

    private @NotNull Boolean isFileCanBeCreated(
            final @NotNull PsiDirectory moduleDirectory
    ) {
        try {
            moduleDirectory.checkCreateFile(ModuleReadmeMdFile.FILE_NAME);
        } catch (IncorrectOperationException exception) {
            return false;
        }

        return true;
    }
}
