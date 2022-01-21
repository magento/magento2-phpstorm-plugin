/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

public class FindOrCreateCrontabXml {
    private final Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ModuleIndex moduleIndex;

    /**
     * Constructor.
     *
     * @param project Project
     */
    public FindOrCreateCrontabXml(final Project project) {
        this.project = project;

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.moduleIndex = new ModuleIndex(project);
    }

    /**
     * Find or create crontab.xml file in the module.
     *
     * @param actionName String
     * @param moduleName String
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

        final CrontabXmlTemplate crontabXmlTemplate = new CrontabXmlTemplate();
        PsiFile crontabFile = FileBasedIndexUtil.findModuleConfigFile(
                crontabXmlTemplate.getFileName(),
                Areas.base,
                moduleName,
                project
        );

        // crontab.xml is already declared
        if (crontabFile != null) {
            return crontabFile;
        }

        // create a new empty crontab.xml file
        crontabFile = fileFromTemplateGenerator.generate(
            crontabXmlTemplate,
            new Properties(),
            parentDirectory,
            actionName
        );

        return crontabFile;
    }
}
