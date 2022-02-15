/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.RoutesXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

public final class FindOrCreateRoutesXml {
    private final Project project;

    public FindOrCreateRoutesXml(final Project project) {
        this.project = project;
    }

    /**
     * Finds or creates module routes.xml.
     *
     * @param actionName String
     * @param moduleName String
     * @param area String
     * @return PsiFile
     */
    public PsiFile execute(final String actionName, final String moduleName, final String area) {
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
        fileDirectories.add(getArea(area).toString());
        for (final String fileDirectory: fileDirectories) {
            parentDirectory = directoryGenerator
                    .findOrCreateSubdirectory(parentDirectory, fileDirectory);
        }
        final RoutesXml moduleRoutesXml = new RoutesXml();
        PsiFile routesXml = FileBasedIndexUtil.findModuleConfigFile(
                moduleRoutesXml.getFileName(),
                getArea(area),
                moduleName,
                project
        );
        if (routesXml == null) {
            routesXml = fileFromTemplateGenerator.generate(
                    moduleRoutesXml,
                    getAttributes(area),
                    parentDirectory,
                    actionName
            );
        }
        return routesXml;
    }

    private Areas getArea(final String area) {
        return Areas.getAreaByString(area);
    }

    private Properties getAttributes(final String area) {
        final Properties attributes = new Properties();
        attributes.setProperty("ROUTER_ID", area.equals(Areas.frontend.toString())
                ? RoutesXml.ROUTER_ID_STANDARD
                : RoutesXml.ROUTER_ID_ADMIN
        );
        return attributes;
    }
}
