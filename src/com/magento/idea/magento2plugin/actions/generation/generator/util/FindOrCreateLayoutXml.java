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
import org.jetbrains.annotations.NotNull;

public final class FindOrCreateLayoutXml {

    private final Project project;
    private final Properties properties;

    public FindOrCreateLayoutXml(final Project project) {
        this.project = project;
        properties = new Properties();
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
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(moduleName);

        if (parentDirectory == null) {
            return null;
        }
        final DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        final FileFromTemplateGenerator fileFromTemplateGenerator =
                new FileFromTemplateGenerator(project);
        final ArrayList<String> fileDirectories = new ArrayList<>();
        fileDirectories.add(Package.moduleViewDir);
        fileDirectories.add(getArea(area).toString());
        fileDirectories.add(LayoutXml.PARENT_DIR);
        for (final String fileDirectory: fileDirectories) {
            parentDirectory = directoryGenerator
                    .findOrCreateSubdirectory(parentDirectory, fileDirectory);
        }

        LayoutXml layoutXml = new LayoutXml(routeId, controllerName, controllerActionName);

        if (controllerName.isEmpty()) {
            layoutXml = new LayoutXml(routeId);
        }
        PsiFile layoutXmlFile = FileBasedIndexUtil.findModuleViewFile(
                layoutXml.getFileName(),
                getArea(area),
                moduleName,
                project,
                LayoutXml.PARENT_DIR
        );
        if (layoutXmlFile == null) {
            fillDefaultAttributes(area, properties);
            layoutXmlFile = fileFromTemplateGenerator.generate(
                    layoutXml,
                    properties,
                    parentDirectory,
                    actionName
            );
        }
        return layoutXmlFile;
    }

    private Areas getArea(final String area) {
        return Areas.getAreaByString(area);
    }

    private void fillDefaultAttributes(
            final @NotNull String area,
            final @NotNull Properties properties
    ) {
        if (Areas.adminhtml.toString().equals(area)) {
            properties.setProperty("IS_ADMIN", Boolean.TRUE.toString());
        } else {
            properties.setProperty("IS_ADMIN", Boolean.FALSE.toString());
        }
    }
}
