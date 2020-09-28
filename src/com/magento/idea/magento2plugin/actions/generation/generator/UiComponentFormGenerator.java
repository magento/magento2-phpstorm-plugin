/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.ButtonsXmlDeclarationGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.UiComponentFormXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class UiComponentFormGenerator extends FileGenerator {
    private final UiComponentFormFileData uiFormFileData;
    private final Project project;

    /**
     * Constructor.
     *
     * @param uiFormFileData UiFormFileData
     * @param project Project
     */
    public UiComponentFormGenerator(
            final @NotNull UiComponentFormFileData uiFormFileData,
            final Project project
    ) {
        super(project);
        this.uiFormFileData = uiFormFileData;
        this.project = project;
    }

    /**
     * Creates a module UI form file.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile formFile = createForm(
                actionName
        );

        createButtonClasses(actionName);



        return formFile;
    }

    protected void createButtonClasses(final String actionName) {
        for (final UiComponentFormButtonData buttonData: uiFormFileData.getButtons()) {
            new UiComponentFormButtonPhpClassGenerator(//NOPMD
                buttonData,
                project
            ).generate(actionName);
        }
    }

    /**
     * Finds or creates form.
     *
     * @param actionName String
     * @return PsiFile
     */
    protected PsiFile createForm(
            final String actionName
    ) {
        final DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        final FileFromTemplateGenerator fileFromTemplateGenerator =
                FileFromTemplateGenerator.getInstance(project);

        final String moduleName = uiFormFileData.getModuleName();
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(moduleName);
        final ArrayList<String> fileDirectories = new ArrayList<>();
        fileDirectories.add(Package.moduleViewDir);
        final String area = uiFormFileData.getFormArea();
        fileDirectories.add(getArea(area).toString());
        fileDirectories.add(Package.moduleViewUiComponentDir);
        for (final String fileDirectory: fileDirectories) {
            parentDirectory = directoryGenerator
                .findOrCreateSubdirectory(parentDirectory, fileDirectory);
        }
        final String formName = uiFormFileData.getFormName();
        final UiComponentFormXml uiComponentFormXml = new UiComponentFormXml(formName);
        XmlFile formFile = (XmlFile) FileBasedIndexUtil.findModuleConfigFile(
                uiComponentFormXml.getFileName(),
                getArea(area),
                formName,
                project
        );
        if (formFile == null) {
            formFile = (XmlFile) fileFromTemplateGenerator.generate(
                    uiComponentFormXml,
                    getAttributes(),
                    parentDirectory,
                    actionName
            );
        }

        new ButtonsXmlDeclarationGenerator(uiFormFileData, project).generate(formFile);

        return formFile;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAME", uiFormFileData.getFormName());
        attributes.setProperty("LABEL", uiFormFileData.getLabel());
        attributes.setProperty("BUTTONS", uiFormFileData.getButtons().isEmpty() ? "" : "true");
        attributes.setProperty("ROUTE", uiFormFileData.getRoute());
        attributes.setProperty(
                "SUBMIT_CONTROLLER",
                uiFormFileData.getSubmitControllerName().toLowerCase(new Locale("en","EN"))
        );
        attributes.setProperty(
                "SUBMIT_ACTION",
                uiFormFileData.getSubmitActionName().toLowerCase(new Locale("en","EN"))
        );
        attributes.setProperty("DATA_PROVIDER", uiFormFileData.getDataProviderFqn());
    }

    private Areas getArea(final String area) {
        return Areas.getAreaByString(area);
    }
}
