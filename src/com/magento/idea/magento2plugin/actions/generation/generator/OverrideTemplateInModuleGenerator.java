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
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetComponentNameByDirectoryUtil;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public void execute(final PsiFile baseFile, final String moduleName) {

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
        final List<String> pathComponents = getModulePathComponents(
                baseFile,
                GetComponentNameByDirectoryUtil.execute(
                        baseFile.getContainingDirectory(),
                        project
                )
        );
        directory = getTargetDirectory(directory, pathComponents);

        if (directory.findFile(baseFile.getName()) != null) {
            JBPopupFactory.getInstance()
                    .createMessage(
                            validatorBundle.message(
                                    "validator.file.alreadyExists",
                                    baseFile.getName()
                            )
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

    protected List<String> getModulePathComponents(final PsiFile file, final String componentName) {
        final List<String> pathComponents = new ArrayList<>();
        PsiDirectory parent = file.getParent();

        while (!parent.getName().equals(Areas.frontend.toString())
                && !parent.getName().equals(Areas.adminhtml.toString())
                && !parent.getName().equals(Areas.base.toString())
        ) {
            pathComponents.add(parent.getName());
            parent = parent.getParent();
        }

        pathComponents.add(getArea(file));
        pathComponents.add("view");
        Collections.reverse(pathComponents);
        return pathComponents;
    }

    protected String getArea(final PsiFile baseFile) {
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(baseFile.getContainingDirectory(), project);
        final String filePath = baseFile.getVirtualFile().getPath();
        final PsiDirectory viewDir = moduleData.getViewDir();
        final String relativePath = filePath.replace(
                viewDir.getVirtualFile().getPath(),
                ""
        );

        return relativePath.split(Package.V_FILE_SEPARATOR)[1];
    }

    /**
     *  Get target directory.
     *
     * @param directory PsiDirectory
     * @param pathComponents List[String]
     *
     * @return PsiDirectory
     */
    protected PsiDirectory getTargetDirectory(
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
