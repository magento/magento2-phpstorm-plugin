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
    private Project project;
    private DirectoryGenerator directoryGenerator;
    private FileFromTemplateGenerator fileFromTemplateGenerator;
    private ModuleIndex moduleIndex;

    public FindOrCreateCrontabXml(Project project) {
        this.project = project;

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.moduleIndex = ModuleIndex.getInstance(project);
    }

    /**
     * Find or create crontab.xml file in the module
     *
     * @param actionName
     * @param moduleName
     *
     * @return PsiFile
     */
    public PsiFile execute(String actionName, String moduleName) {
        PsiDirectory parentDirectory = this.moduleIndex.getModuleDirectoryByModuleName(moduleName);
        ArrayList<String> fileDirectories = new ArrayList<>();

        fileDirectories.add(Package.moduleBaseAreaDir);

        for (String fileDirectory: fileDirectories) {
            parentDirectory = this.directoryGenerator.findOrCreateSubdirectory(parentDirectory, fileDirectory);
        }

        CrontabXmlTemplate crontabXmlTemplate = new CrontabXmlTemplate();
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
