/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

public final class FindOrCreateLayoutXml {
    private final Project project;

    public FindOrCreateLayoutXml(final Project project) {
        this.project = project;
    }

    /**
     * Finds or creates module layout XML.
     *
     * @param actionName String
     * @param routeId String
     * @param controllerName String
     * @param controllerActionName String
     * @param moduleName String
     * @param area String
     * @return PsiFile
     */
    @SuppressWarnings({"PMD.UseObjectForClearerAPI"})
    public PsiFile execute(
            final String actionName,
            final String routeId,
            final String controllerName,
            final String controllerActionName,
            final String moduleName,
            final String area
    ) {
        final DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        final FileFromTemplateGenerator fileFromTemplateGenerator =
                FileFromTemplateGenerator.getInstance(project);

        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(moduleName);
        final ArrayList<String> fileDirectories = new ArrayList<>();
        fileDirectories.add(Package.moduleViewDir);
        fileDirectories.add(getArea(area).toString());
        fileDirectories.add(LayoutXml.PARENT_DIR);
        for (final String fileDirectory: fileDirectories) {
            parentDirectory = directoryGenerator
                    .findOrCreateSubdirectory(parentDirectory, fileDirectory);
        }
        final LayoutXml layoutXml = new  LayoutXml(routeId, controllerName, controllerActionName);
        PsiFile layoutXmlFile = FileBasedIndexUtil.findModuleViewFile(
                layoutXml.getFileName(),
                getArea(area),
                moduleName,
                project,
                LayoutXml.PARENT_DIR
        );
        if (layoutXmlFile == null) {
            layoutXmlFile = fileFromTemplateGenerator.generate(
                    layoutXml,
                    new Properties(),
                    parentDirectory,
                    actionName
            );
        }
        return layoutXmlFile;
    }

    private Areas getArea(final String area) {
        return Areas.getAreaByString(area);
    }
}
