/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.EmailTemplatesXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

@SuppressWarnings({"PMD.OnlyOneReturn"})
public class FindOrCreateEmailTemplatesXml {
    private final Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ModuleIndex moduleIndex;

    /**
     * Find or create email templates XML controller.
     *
     * @param project Project
     */
    public FindOrCreateEmailTemplatesXml(final Project project) {
        this.project = project;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.moduleIndex = new ModuleIndex(project);
    }

    /**
     * Find or create email_templates.xml file in the module.
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

        final EmailTemplatesXml emailTemplatesXml = new EmailTemplatesXml();
        PsiFile emailTemplatesFile = FileBasedIndexUtil.findModuleConfigFile(
                emailTemplatesXml.getFileName(),
                Areas.base,
                moduleName,
                project
        );

        // email_templates.xml is already declared
        if (emailTemplatesFile != null) {
            return emailTemplatesFile;
        }

        // create a new empty email_templates.xml file
        emailTemplatesFile = fileFromTemplateGenerator.generate(
                emailTemplatesXml,
                new Properties(),
                parentDirectory,
                actionName
        );

        return emailTemplatesFile;
    }
}
