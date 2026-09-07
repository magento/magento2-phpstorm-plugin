/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.EmailTemplateHtmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.EmailTemplateHtml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

public class ModuleEmailTemplateHtmlGenerator extends FileGenerator {
    private final EmailTemplateHtmlData emailTemplateData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ModuleIndex moduleIndex;
    private final DirectoryGenerator directoryGenerator;
    private final Project project;

    /**
     * Constructor.
     *
     * @param emailTemplateData EmailTemplateData
     * @param project Project
     */
    public ModuleEmailTemplateHtmlGenerator(
            final EmailTemplateHtmlData emailTemplateData,
            final Project project
    ) {
        super(project);

        this.emailTemplateData = emailTemplateData;
        this.project = project;
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.moduleIndex = new ModuleIndex(project);
    }

    /**
     * Generate email template HTML file.
     *
     * @param actionName Action name
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile templateFile = FileBasedIndexUtil.findModuleViewFile(
                this.emailTemplateData.getFileName(),
                getArea(this.emailTemplateData.getArea()),
                this.emailTemplateData.getModule(),
                this.project,
                Package.moduleViewEmailDir
        );

        if (templateFile != null) {
            return null;
        }

        PsiDirectory parentDirectory = this.moduleIndex.getModuleDirectoryByModuleName(
                this.emailTemplateData.getModule()
        );

        if (parentDirectory == null) {
            return null;
        }
        final ArrayList<String> fileDirectories = new ArrayList<>();

        fileDirectories.add(Package.moduleViewDir);
        fileDirectories.add(this.emailTemplateData.getArea());
        fileDirectories.add(Package.moduleViewEmailDir);

        for (final String fileDirectory: fileDirectories) {
            parentDirectory = this.directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    fileDirectory
            );
        }
        final EmailTemplateHtml emailTemplateHtml = EmailTemplateHtml.getInstance(
                this.emailTemplateData.getFileName()
        );

        return fileFromTemplateGenerator.generate(
                emailTemplateHtml,
                getAttributes(),
                parentDirectory,
                actionName
        );
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("SUBJECT", emailTemplateData.getSubject());
        attributes.setProperty("TYPE", emailTemplateData.getType());

        if (EmailTemplateHtml.HTML_TYPE.equals(emailTemplateData.getType())) {
            attributes.setProperty("HTML_TYPE", "true");
        }
    }

    private Areas getArea(final String area) {
        return Areas.getAreaByString(area);
    }
}
