/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.*;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.UiComponentFormXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.FirstLetterToLowercaseUtil;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class UiComponentFormGenerator extends FileGenerator {
    private final UiComponentFormFileData uiFormFileData;
    private final Project project;
    private final GetCodeTemplate getCodeTemplate;
    private final XmlFilePositionUtil positionUtil;

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
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
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

    protected void createButtonClasses(String actionName) {
        for (UiComponentFormButtonData buttonData: uiFormFileData.getButtons()) {
            new UiComponentFormButtonPhpClassGenerator(
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

        renderButtons(formFile);

        return formFile;
    }

    private void renderButtons(XmlFile formFile) {
        final PsiDocumentManager psiDocumentManager =
            PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(formFile);
        XmlFile finalFormFile = formFile;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            XmlTag rootTag = finalFormFile.getRootTag();
            if (rootTag == null) {
                return;
            }
            XmlTag settingsTag = rootTag.findFirstSubTag("settings");
            if (settingsTag == null) {
                return;
            }
            XmlTag buttonsTag = settingsTag.findFirstSubTag("buttons");

            for (UiComponentFormButtonData formButtonData : uiFormFileData.getButtons()) {
                XmlTag buttonTag = buttonsTag.createChildTag("button", null, null, false);
                buttonTag.setAttribute("name", FirstLetterToLowercaseUtil.convert(
                        formButtonData.getButtonClassName())
                );
                buttonTag.setAttribute("class",formButtonData.getFqn());
                buttonsTag.addSubTag(buttonTag, false);
            }

            for (UiComponentFormFieldsetData formFieldsetData : uiFormFileData.getFieldsets()) {
                final StringBuffer fieldsStringBuffer = new StringBuffer();

                for (UiComponentFormFieldData formFieldData : uiFormFileData.getFields()) {
                    if (!formFieldData.getFieldset().equals(formFieldsetData.getLabel())) {
                        continue;
                    }
                    try {
                        fieldsStringBuffer.append(getCodeTemplate.execute(
                                UiComponentFormXml.FIELD_TEMPLATE,
                                getFieldTemplateAttributes(formFieldData))
                        );
                    } catch (IOException e) {
                        return;
                    }
                }

                XmlTag fieldsetTag = rootTag.createChildTag("fieldset", null, fieldsStringBuffer.toString(), false);
                fieldsetTag.setAttribute("label", formFieldsetData.getLabel());
                fieldsetTag.setAttribute("sortOrder",formFieldsetData.getSortOrder());
                rootTag.addSubTag(fieldsetTag, false);
            }

            psiDocumentManager.commitDocument(document);
        });
    }

    private Properties getFieldTemplateAttributes(UiComponentFormFieldData formFieldData) {
        final Properties attributes = new Properties();
        attributes.setProperty("NAME", formFieldData.getName());
        attributes.setProperty("FORM_ELEMENT", formFieldData.getFormElementType());
        attributes.setProperty("SOURCE", formFieldData.getSource());
        attributes.setProperty("DATA_TYPE", formFieldData.getDataType());
        attributes.setProperty("LABEL", formFieldData.getLabel());
        attributes.setProperty("SORT_ORDER", formFieldData.getSortOrder());
        return attributes;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAME", uiFormFileData.getFormName());
        attributes.setProperty("LABEL", uiFormFileData.getLabel());
        attributes.setProperty("BUTTONS", uiFormFileData.getButtons().isEmpty() ? "" : "true");
        attributes.setProperty("ROUTE", uiFormFileData.getRoute());
        attributes.setProperty("SUBMIT_CONTROLLER", uiFormFileData.getSubmitControllerName().toLowerCase());
        attributes.setProperty("SUBMIT_ACTION", uiFormFileData.getSubmitActionName().toLowerCase());
        attributes.setProperty("DATA_PROVIDER", uiFormFileData.getDataProviderFqn());
    }

    private Areas getArea(final String area) {
        return Areas.getAreaByString(area);
    }
}
