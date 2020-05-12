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
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.UiFormFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.*;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ModuleUiFormGenerator extends FileGenerator {
    private final GetCodeTemplate getCodeTemplate;
    private final XmlFilePositionUtil positionUtil;
    private final UiFormFileData uiFormFileData;
    private final Project project;

    /**
     * Constructor.
     *
     * @param uiFormFileData UiFormFileData
     * @param project Project
     */
    public ModuleUiFormGenerator(
            final @NotNull UiFormFileData uiFormFileData,
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
        final PsiFile diXmlFile = createFile(
                uiFormFileData.getFormName(),
                uiFormFileData.getFormArea(),
                actionName
        );


//        WriteCommandAction.runWriteCommandAction(project, () -> {
//            final StringBuffer textBuf = new StringBuffer();
//            try {
//                textBuf.append(getCodeTemplate.execute(
//                        ModuleDiXml.TEMPLATE_PLUGIN,
//                        getAttributes())
//                );
//            } catch (IOException e) {
//                return;
//            }
//
//            final int insertPos = isTypeDeclared
//                    ? positionUtil.getEndPositionOfTag(PsiTreeUtil.getParentOfType(
//                    typeAttributeValue,
//                    XmlTag.class
//            ))
//                : positionUtil.getRootInsertPosition((XmlFile) diXmlFile);
//            if (textBuf.length() > 0 && insertPos >= 0) {
//                final PsiDocumentManager psiDocumentManager =
//                    PsiDocumentManager.getInstance(project);
//                final Document document = psiDocumentManager.getDocument(diXmlFile);
//                document.insertString(insertPos, textBuf);
//                final int endPos = insertPos + textBuf.length() + 1;
//                CodeStyleManager.getInstance(project).reformatText(diXmlFile, insertPos, endPos);
//                psiDocumentManager.commitDocument(document);
//            }
//        });

        return diXmlFile;
    }

    /**
     * Finds or creates module di.xml.
     *
     * @param formName String
     * @param area String
     * @param actionName String
     * @return PsiFile
     */
    protected PsiFile createFile(
            final String formName,
            final String area,
            final String actionName
    ) {
        final DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        final FileFromTemplateGenerator fileFromTemplateGenerator =
                FileFromTemplateGenerator.getInstance(project);

        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(formName);
        final ArrayList<String> fileDirectories = new ArrayList<>();
        fileDirectories.add(Package.moduleBaseAreaDir);
        if (!getArea(area).equals(Areas.base)) {
            fileDirectories.add(getArea(area).toString());
        }
        for (final String fileDirectory: fileDirectories) {
            parentDirectory = directoryGenerator
                .findOrCreateSubdirectory(parentDirectory, fileDirectory);
        }
        final ModuleDiXml moduleDiXml = new ModuleDiXml();
        PsiFile diXml = FileBasedIndexUtil.findModuleConfigFile(
                moduleDiXml.getFileName(),
                getArea(area),
                formName,
                project
        );
        if (diXml == null) {
            diXml = fileFromTemplateGenerator.generate(
                    moduleDiXml,
                    new Properties(),
                    parentDirectory,
                    actionName
            );
        }
        return diXml;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
//        if (!isTypeDeclared) {
//            attributes.setProperty("TYPE", uiFormFileData.getTargetClass().getPresentableFQN());
//        }
//        attributes.setProperty("NAME", uiFormFileData.getPluginName());
//        attributes.setProperty("PLUGIN_TYPE", uiFormFileData.getPluginFqn());
//        attributes.setProperty("PLUGIN_NAME", uiFormFileData.getPluginName());
//        attributes.setProperty("SORT_ORDER", uiFormFileData.getSortOrder());
    }

    private Areas getArea(final String area) {
        return Areas.getAreaByString(area);
    }
}
