package com.magento.idea.magento2plugin.actions.generation.generator.code;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.magento.files.UiComponentFormXml;
import com.magento.idea.magento2plugin.util.FirstLetterToLowercaseUtil;
import java.io.IOException;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ButtonsXmlDeclarationGenerator {
    private final UiComponentFormFileData uiFormFileData;
    private final Project project;
    private final GetCodeTemplate getCodeTemplate;

    /**
     * Constructor.
     *
     * @param uiFormFileData UiFormFileData
     * @param project Project
     */
    public ButtonsXmlDeclarationGenerator(
            final @NotNull UiComponentFormFileData uiFormFileData,
            final Project project
    ) {
        this.uiFormFileData = uiFormFileData;
        this.project = project;
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
    }

    /**
     * Injects button declarations to file.
     *
     * @param formFile XmlFile
     */
    public void generate(final XmlFile formFile) {
        final PsiDocumentManager psiDocumentManager =
                PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(formFile);
        final XmlFile finalFormFile = formFile;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            final XmlTag rootTag = finalFormFile.getRootTag();
            if (rootTag == null) {
                return;
            }
            final XmlTag settingsTag = rootTag.findFirstSubTag("settings");
            if (settingsTag == null) {
                return;
            }
            final XmlTag buttonsTag = settingsTag.findFirstSubTag("buttons");

            for (final UiComponentFormButtonData formButtonData : uiFormFileData.getButtons()) {
                final XmlTag buttonTag = buttonsTag.createChildTag("button", null, null, false);
                buttonTag.setAttribute("name", FirstLetterToLowercaseUtil.convert(
                        formButtonData.getButtonClassName())
                );
                buttonTag.setAttribute("class",formButtonData.getFqn());
                buttonsTag.addSubTag(buttonTag, false);
            }

            for (final UiComponentFormFieldsetData formFieldsetData
                    : uiFormFileData.getFieldsets()) {
                final StringBuffer fieldsStringBuffer = new StringBuffer();//NOPMD

                for (final UiComponentFormFieldData formFieldData : uiFormFileData.getFields()) {
                    if (!formFieldData.getFieldset().equals(formFieldsetData.getLabel())) {
                        continue;
                    }
                    try {
                        fieldsStringBuffer.append(getCodeTemplate.execute(
                                    UiComponentFormXml.FIELD_TEMPLATE,
                                    fillAttributes(formFieldData)
                            )
                        );
                    } catch (IOException e) {
                        return;
                    }
                }

                final XmlTag fieldsetTag = rootTag.createChildTag(
                        "fieldset",
                        null,
                        fieldsStringBuffer.toString(),
                        false
                );
                fieldsetTag.setAttribute("label", formFieldsetData.getLabel());
                fieldsetTag.setAttribute("sortOrder",formFieldsetData.getSortOrder());
                rootTag.addSubTag(fieldsetTag, false);
            }

            psiDocumentManager.commitDocument(document);
        });
    }

    private Properties fillAttributes(final UiComponentFormFieldData formFieldData) {
        final Properties attributes = new Properties();
        attributes.setProperty("NAME", formFieldData.getName());
        attributes.setProperty("FORM_ELEMENT", formFieldData.getFormElementType());
        attributes.setProperty("SOURCE", formFieldData.getSource());
        attributes.setProperty("DATA_TYPE", formFieldData.getDataType());
        attributes.setProperty("LABEL", formFieldData.getLabel());
        attributes.setProperty("SORT_ORDER", formFieldData.getSortOrder());
        return attributes;
    }
}
