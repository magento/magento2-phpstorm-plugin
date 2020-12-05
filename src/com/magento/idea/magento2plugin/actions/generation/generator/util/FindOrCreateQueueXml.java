/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

public abstract class FindOrCreateQueueXml {
    private final Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ModuleIndex moduleIndex;

    /**
     * Constructor.
     */
    public FindOrCreateQueueXml(final Project project) {
        this.project = project;

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.moduleIndex = ModuleIndex.getInstance(project);
    }

    /**
     * Finds or creates message queue XML file.
     */
    public PsiFile execute(final String actionName, final String moduleName) {
        PsiDirectory parentDirectory = this.moduleIndex.getModuleDirectoryByModuleName(moduleName);
        final ArrayList<String> fileDirectories = new ArrayList<>();

        fileDirectories.add(Package.moduleBaseAreaDir);

        for (final String fileDirectory: fileDirectories) {
            parentDirectory = this.directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    fileDirectory
            );
        }

        final ModuleFileInterface xml = instantiateXmlFile();

        PsiFile xmlFile = FileBasedIndexUtil.findModuleConfigFile(
                xml.getFileName(),
                Areas.base,
                moduleName,
                project
        );

        // crontab.xml is already declared
        if (xmlFile != null) {
            return xmlFile;
        }

        // create a new empty XML file
        xmlFile = fileFromTemplateGenerator.generate(
                xml,
                new Properties(),
                parentDirectory,
                actionName
        );

        return xmlFile;
    }

    protected abstract ModuleFileInterface instantiateXmlFile();
}
