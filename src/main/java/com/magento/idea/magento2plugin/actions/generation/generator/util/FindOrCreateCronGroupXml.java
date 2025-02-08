/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CronGroupXmlTemplate;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

@SuppressWarnings({"PMD.OnlyOneReturn"})
public class FindOrCreateCronGroupXml {
    private final Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ModuleIndex moduleIndex;

    /**
     * Find or create CRON group XML controller.
     *
     * @param project Project
     */
    public FindOrCreateCronGroupXml(final Project project) {
        this.project = project;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.moduleIndex = new ModuleIndex(project);
    }

    /**
     * Find or create cron_groups.xml file in the module.
     *
     * @param actionName action name
     * @param moduleName module name
     *
     * @return PsiFile
     */
    public PsiFile execute(final String actionName, final String moduleName) {
        PsiDirectory parentDirectory = this.moduleIndex.getModuleDirectoryByModuleName(moduleName);

        if (parentDirectory == null) {
            return null;
        }
        final ArrayList<String> fileDirectories = new ArrayList<>();

        fileDirectories.add(Package.moduleBaseAreaDir);

        for (final String fileDirectory: fileDirectories) {
            parentDirectory = this.directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    fileDirectory
            );
        }

        final CronGroupXmlTemplate cronGroupXmlTemplate = new CronGroupXmlTemplate();
        PsiFile cronGroupsFile = FileBasedIndexUtil.findModuleConfigFile(
                cronGroupXmlTemplate.getFileName(),
                Areas.base,
                moduleName,
                project
        );

        // cron_groups.xml is already declared
        if (cronGroupsFile != null) {
            return cronGroupsFile;
        }

        // create a new empty cron_groups.xml file
        cronGroupsFile = fileFromTemplateGenerator.generate(
                cronGroupXmlTemplate,
                new Properties(),
                parentDirectory,
                actionName
        );

        return cronGroupsFile;
    }
}
