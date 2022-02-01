/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

public final class FindOrCreateMenuXml {
    private final Project project;

    public FindOrCreateMenuXml(final Project project) {
        this.project = project;
    }

    /**
     * Finds or creates module menu.xml.
     *
     * @param actionName String
     * @param moduleName String
     * @return PsiFile
     */
    public PsiFile execute(final String actionName, final String moduleName) {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(moduleName);

        if (parentDirectory == null) {
            return null;
        }
        final DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        final FileFromTemplateGenerator fileFromTemplateGenerator =
                new FileFromTemplateGenerator(project);
        final ArrayList<String> fileDirectories = new ArrayList<>();
        fileDirectories.add(Package.moduleBaseAreaDir);
        fileDirectories.add(Areas.adminhtml.toString());
        for (final String fileDirectory: fileDirectories) {
            parentDirectory = directoryGenerator
                    .findOrCreateSubdirectory(parentDirectory, fileDirectory);
        }
        final ModuleMenuXml moduleMenuXml = new ModuleMenuXml();
        PsiFile routesXml = FileBasedIndexUtil.findModuleConfigFile(
                moduleMenuXml.getFileName(),
                Areas.adminhtml,
                moduleName,
                project
        );
        if (routesXml == null) {
            routesXml = fileFromTemplateGenerator.generate(
                    moduleMenuXml,
                    new Properties(),
                    parentDirectory,
                    actionName
            );
        }
        return routesXml;
    }
}
