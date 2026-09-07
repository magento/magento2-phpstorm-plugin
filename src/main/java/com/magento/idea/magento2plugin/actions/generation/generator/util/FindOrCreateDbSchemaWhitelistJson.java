/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaWhitelistJson;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class FindOrCreateDbSchemaWhitelistJson {
    private final Project project;

    /**
     * Constructor.
     *
     * @param project Project
     */
    public FindOrCreateDbSchemaWhitelistJson(final @NotNull Project project) {
        this.project = project;
    }

    /**
     * Get or create db_schema_whitelist.json file.
     *
     * @param actionName String
     * @param moduleName String
     *
     * @return PsiFile
     */
    public PsiFile execute(final @NotNull String actionName, final @NotNull String moduleName) {
        final DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        final FileFromTemplateGenerator fileFromTemplateGenerator =
                new FileFromTemplateGenerator(project);

        final ModuleDbSchemaWhitelistJson moduleDbSchemaWhitelistJson =
                ModuleDbSchemaWhitelistJson.getInstance();

        PsiFile dbSchemaWhitelistJson = FileBasedIndexUtil.findModuleConfigFile(
                moduleDbSchemaWhitelistJson.getFileName(),
                Areas.base,
                moduleName,
                project
        );

        if (dbSchemaWhitelistJson == null) {
            PsiDirectory parentDirectory = new ModuleIndex(project)
                    .getModuleDirectoryByModuleName(moduleName);

            if (parentDirectory == null) {
                return null;
            }
            parentDirectory = directoryGenerator
                    .findOrCreateSubdirectory(parentDirectory, Package.moduleBaseAreaDir);

            dbSchemaWhitelistJson = fileFromTemplateGenerator.generate(
                    moduleDbSchemaWhitelistJson,
                    new Properties(),
                    parentDirectory,
                    actionName
            );
        }

        return dbSchemaWhitelistJson;
    }
}
