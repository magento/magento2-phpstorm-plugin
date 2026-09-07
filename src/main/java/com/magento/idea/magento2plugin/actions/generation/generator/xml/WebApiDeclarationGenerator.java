/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.xml;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.xml.WebApiXmlRouteData;
import com.magento.idea.magento2plugin.actions.generation.generator.FileGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleWebApiXmlFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public final class WebApiDeclarationGenerator extends FileGenerator {

    private final WebApiXmlRouteData data;
    private final ModuleWebApiXmlFile file;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final XmlFilePositionUtil xmlFilePositionUtil;
    private final CommonBundle commonBundle;
    private final ValidatorBundle validatorBundle;

    /**
     * Web API declaration generator constructor.
     *
     * @param data WebApiXmlRouteData
     * @param project Project
     */
    public WebApiDeclarationGenerator(
            final @NotNull WebApiXmlRouteData data,
            final @NotNull Project project
    ) {
        super(project);
        this.data = data;
        file = new ModuleWebApiXmlFile();
        directoryGenerator = DirectoryGenerator.getInstance();
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        getCodeTemplateUtil = new GetCodeTemplateUtil(project);
        xmlFilePositionUtil = new XmlFilePositionUtil();
        commonBundle = new CommonBundle();
        validatorBundle = new ValidatorBundle();
    }

    /**
     * Generate Web API XML declaration.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiDirectory moduleDirectory =
                new ModuleIndex(project).getModuleDirectoryByModuleName(data.getModuleName());

        if (moduleDirectory == null) {
            JOptionPane.showMessageDialog(
                    null,
                    validatorBundle.message(
                            "validator.file.cantBeCreated",
                            "Web API XML file"
                    ),
                    commonBundle.message("common.error"),
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
        final PsiDirectory fileDirectory =
                directoryGenerator.findOrCreateSubdirectory(
                        moduleDirectory,
                        Package.moduleBaseAreaDir
                );
        PsiFile webApiXmlFile = FileBasedIndexUtil.findModuleConfigFile(
                file.getFileName(),
                Areas.getAreaByString(Areas.base.name()),
                data.getModuleName(),
                project
        );

        if (webApiXmlFile == null) {
            webApiXmlFile = fileFromTemplateGenerator.generate(
                    file,
                    new Properties(),
                    fileDirectory,
                    actionName
            );
        }

        if (webApiXmlFile == null) {
            JOptionPane.showMessageDialog(
                    null,
                    validatorBundle.message(
                            "validator.file.cantBeCreated",
                            "Web API XML file"
                    ),
                    commonBundle.message("common.error"),
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        try {
            final XmlTag rootTag = ((XmlFile) webApiXmlFile).getRootTag();

            if (rootTag == null) {
                showDeclarationCannotBeCreatedDialog();
                return null;
            }
            final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            final Document document = psiDocumentManager.getDocument(webApiXmlFile);

            if (document == null) {
                showDeclarationCannotBeCreatedDialog();
                return null;
            }

            final String declarationXml =
                    getCodeTemplateUtil.execute(
                            ModuleWebApiXmlFile.DECLARATION_TEMPLATE,
                            getAttributes()
                    );
            final int insertPosition = xmlFilePositionUtil.getEndPositionOfTag(rootTag);

            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.insertString(insertPosition, declarationXml);
                psiDocumentManager.commitDocument(document);
            });
        } catch (IOException exception) {
            showDeclarationCannotBeCreatedDialog();
        }

        final PsiFile webApiXmlFileToReformat = webApiXmlFile;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            CodeStyleManager.getInstance(project).reformat(webApiXmlFileToReformat);
        });

        return webApiXmlFileToReformat;
    }

    /**
     * Show file cannot be created message dialog.
     */
    private void showDeclarationCannotBeCreatedDialog() {
        JOptionPane.showMessageDialog(
                null,
                validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Web API XML file"
                ),
                commonBundle.message("common.error"),
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Fill WEB API xml declaration values.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("ROUTE", data.getUrl());
        attributes.setProperty("METHOD", data.getHttpMethod());
        attributes.setProperty("SERVICE", data.getServiceClass());
        attributes.setProperty("SERVICE_METHOD", data.getServiceMethod());
        attributes.setProperty("RESOURCE", data.getAclResource());
    }
}
