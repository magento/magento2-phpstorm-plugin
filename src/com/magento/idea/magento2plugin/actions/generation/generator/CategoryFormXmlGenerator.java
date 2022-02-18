/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.CategoryFormXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CategoryFormXmlFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CategoryFormXmlGenerator extends FileGenerator {

    private final CategoryFormXmlData categoryFormXmlData;
    private final String moduleName;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final XmlFilePositionUtil xmlFilePositionUtil;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private boolean allowedFieldsetNodeInclude = true;

    /**
     * Category form XML Generator.
     *
     * @param categoryFormXmlData Category form data class
     * @param project Project
     * @param moduleName module name
     */
    public CategoryFormXmlGenerator(
            final @NotNull CategoryFormXmlData categoryFormXmlData,
            final @NotNull Project project,
            final @NotNull String moduleName
    ) {
        super(project);

        this.categoryFormXmlData = categoryFormXmlData;
        this.moduleName = moduleName;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.xmlFilePositionUtil = new XmlFilePositionUtil();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    @Override
    public PsiFile generate(final String actionName) {
        final PsiDirectory directory = getFileDirectory();

        PsiFile categoryAdminFormXmlFile = FileBasedIndexUtil.findModuleViewFile(
                CategoryFormXmlFile.FILE_NAME,
                Areas.getAreaByString(Areas.adminhtml.name()),
                moduleName,
                project,
                CategoryFormXmlFile.SUB_DIRECTORY
        );

        if (categoryAdminFormXmlFile == null) {
            categoryAdminFormXmlFile = fileFromTemplateGenerator.generate(
                    new CategoryFormXmlFile(),
                    new Properties(),
                    directory,
                    actionName
            );
        }

        if (categoryAdminFormXmlFile == null) {
            showDeclarationCannotBeCreatedDialog();
            return null;
        }

        final XmlTag rootTag = ((XmlFile) categoryAdminFormXmlFile).getRootTag();

        if (rootTag == null) {
            showDeclarationCannotBeCreatedDialog();
            return null;
        }

        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(categoryAdminFormXmlFile);


        if (document == null) {
            showDeclarationCannotBeCreatedDialog();
            return null;
        }

        try {
            final XmlTag matchedFieldsetByName = findMatchedFieldsetByName(
                    findFieldsetTagsInRoot(rootTag),
                    categoryFormXmlData.getFieldSetName()
            );

            allowedFieldsetNodeInclude = matchedFieldsetByName == null;

            writeInFile(
                    matchedFieldsetByName == null ? rootTag : matchedFieldsetByName,
                    psiDocumentManager,
                    document
            );
        } catch (IOException e) {
            showDeclarationCannotBeCreatedDialog();
        }

        return reformatFile(categoryAdminFormXmlFile);
    }

    private void showDeclarationCannotBeCreatedDialog() {
        JOptionPane.showMessageDialog(
                null,
                validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Category Admin Form XML file"
                ),
                commonBundle.message("common.error"),
                JOptionPane.ERROR_MESSAGE
        );
    }

    private PsiDirectory getFileDirectory() {
        PsiDirectory directory =
                new ModuleIndex(project).getModuleDirectoryByModuleName(moduleName);

        for (final String handlerDirectory: CategoryFormXmlFile.DIRECTORY.split(File.separator)) {
            directory = directoryGenerator.findOrCreateSubdirectory(
                directory,
                handlerDirectory
            );
        }
        return directory;
    }

    private void writeInFile(
            final XmlTag targetTag,
            final PsiDocumentManager psiDocumentManager,
            final Document document
    ) throws IOException {
        final int insertPosition = xmlFilePositionUtil.getEndPositionOfTag(targetTag);

        final String declarationXml =
                getCodeTemplateUtil.execute(
                        CategoryFormXmlFile.DECLARATION_TEMPLATE,
                        getAttributes()
                );

        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.insertString(insertPosition, declarationXml);
            psiDocumentManager.commitDocument(document);
        });
    }

    private PsiFile reformatFile(final PsiFile categoryAdminFormXmlFile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            CodeStyleManager.getInstance(project).reformat(categoryAdminFormXmlFile);
        });

        return categoryAdminFormXmlFile;
    }

    @NotNull
    private List<XmlTag> findFieldsetTagsInRoot(final XmlTag rootTag) {
        final XmlTag[] subTags = rootTag.getSubTags();
        final List<XmlTag> fieldsetList = new ArrayList<>();

        for (final XmlTag subTag: subTags) {
            if (subTag.getName().equals(CategoryFormXmlFile.XML_TAG_FIELDSET)) {
                fieldsetList.add(subTag);
            }
        }
        return fieldsetList;
    }

    @Nullable
    private XmlTag findMatchedFieldsetByName(
            final List<XmlTag> fieldsetList,
            final String fieldsetName
    ) {
        for (final XmlTag fieldset: fieldsetList) {
            final String attributeValue = fieldset.getAttributeValue(
                    CategoryFormXmlFile.XML_ATTR_FIELDSET_NAME
            );
            if (attributeValue != null && attributeValue.equals(fieldsetName)) {
                return fieldset;
            }
        }

        return null;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("FIELDSET_NAME", categoryFormXmlData.getFieldSetName());
        attributes.setProperty("FIELD_NAME", categoryFormXmlData.getFieldName());
        attributes.setProperty("SORT_ORDER", Integer.toString(categoryFormXmlData.getSortOrder()));
        attributes.setProperty("FORM_ELEMENT", GetFormElementByAttributeInputUtil.execute(
                AttributeInput.getAttributeInputByCode(categoryFormXmlData.getAttributeInput())
        ));

        if (allowedFieldsetNodeInclude) {
            attributes.setProperty("INCLUDE_FIELDSET", "include");
        }
    }
}
