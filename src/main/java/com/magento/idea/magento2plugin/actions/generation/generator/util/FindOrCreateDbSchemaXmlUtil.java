/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.Properties;

public final class FindOrCreateDbSchemaXmlUtil {
    private final Project project;

    public FindOrCreateDbSchemaXmlUtil(final Project project) {
        this.project = project;
    }

    /**
     * Finds or creates module db_schema.xml file.
     *
     * @param actionName String
     * @param moduleName String
     *
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
        parentDirectory = directoryGenerator
                .findOrCreateSubdirectory(parentDirectory, Package.moduleBaseAreaDir);

        final ModuleDbSchemaXml moduleDbSchemaXml = ModuleDbSchemaXml.getInstance();
        PsiFile dbSchemaXml = FileBasedIndexUtil.findModuleConfigFile(
                moduleDbSchemaXml.getFileName(),
                Areas.getAreaByString(Areas.base.name()),
                moduleName,
                project
        );
        if (dbSchemaXml == null) {
            dbSchemaXml = fileFromTemplateGenerator.generate(
                moduleDbSchemaXml,
                new Properties(),
                parentDirectory,
                actionName
            );
        }
        return dbSchemaXml;
    }
}
