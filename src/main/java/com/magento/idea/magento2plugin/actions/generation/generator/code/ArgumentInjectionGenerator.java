/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.code;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.xml.DiArgumentData;
import com.magento.idea.magento2plugin.actions.generation.generator.FileGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.code.util.DiXmlTagManipulatorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class ArgumentInjectionGenerator extends FileGenerator {

    private final DiArgumentData data;
    private final ModuleDiXml file;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private String generationErrorMessage;

    /**
     * Argument injection generator constructor.
     *
     * @param data DiArgumentData
     * @param project Project
     */
    public ArgumentInjectionGenerator(
            final @NotNull DiArgumentData data,
            final @NotNull Project project
    ) {
        super(project);
        this.data = data;
        file = new ModuleDiXml();
        directoryGenerator = DirectoryGenerator.getInstance();
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
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
        final PsiDirectory moduleDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(data.getModuleName());

        if (moduleDirectory == null) {
            generationErrorMessage = "Could not find the target module directory";
            return null;
        }

        final XmlFile diXmlFile = getDiXmlFile(actionName, moduleDirectory);

        if (diXmlFile == null) {
            if (generationErrorMessage == null) {
                generationErrorMessage = "Could not generate the target di.xml file";
            }
            return null;
        }

        try {
            final XmlTag rootTag = diXmlFile.getRootTag();

            if (rootTag == null) {
                generationErrorMessage = "Could not read the target di.xml file";
                return null;
            }
            final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            final Document document = psiDocumentManager.getDocument(diXmlFile);

            if (document == null) {
                generationErrorMessage = "Could not get the document for the target di.xml file";
                return null;
            }
            XmlTag targetTag = getTargetTag(rootTag);

            if (targetTag == null) {
                final List<Pair<String, String>> attributes = new ArrayList<>();
                attributes.add(new Pair<>("name", data.getClazz()));

                targetTag = DiXmlTagManipulatorUtil.insertTag(
                        rootTag,
                        "type",
                        attributes
                );
            }

            if (targetTag == null) {
                return null;
            }
            DiXmlTagManipulatorUtil.insertArgumentInTypeTag(
                    targetTag,
                    data.getParameter(),
                    data.getValueType(),
                    data.getValue()
            );
        } catch (Exception exception) { // NOPMD
            generationErrorMessage = exception.getMessage();
        }
        final PsiFile diXmlFileToReformat = diXmlFile;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            CodeStyleManager.getInstance(project).reformat(diXmlFileToReformat);
        });

        return diXmlFileToReformat;
    }

    /**
     * Get generation error message.
     *
     * @return String
     */
    public String getGenerationErrorMessage() {
        return generationErrorMessage;
    }

    /**
     * Fill argument injection values.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.put("TYPE", data.getClazz());
    }

    private XmlTag getTargetTag(final @NotNull XmlTag rootTag) {
        final Collection<XmlTag> tags = PsiTreeUtil.findChildrenOfType(rootTag, XmlTag.class);
        final List<XmlTag> result = tags.stream()
                .filter(
                        xmlTag -> xmlTag.getName().equals("type")
                                && xmlTag.getAttributeValue("name") != null
                                && xmlTag.getAttributeValue("name").equals(data.getClazz())
                ).collect(Collectors.toList());

        return result.isEmpty() ? null : result.get(0);
    }

    private XmlFile getDiXmlFile(
            final @NotNull String actionName,
            final @NotNull PsiDirectory moduleDirectory
    ) {
        PsiFile diXmlFile = FileBasedIndexUtil.findModuleConfigFile(
                file.getFileName(),
                data.getArea(),
                data.getModuleName(),
                project
        );

        if (diXmlFile == null) {
            PsiDirectory diXmlFileDir;

            if (data.getArea().equals(Areas.base)) {
                diXmlFileDir = directoryGenerator.findOrCreateSubdirectory(
                        moduleDirectory,
                        Package.moduleBaseAreaDir
                );
            } else {
                diXmlFileDir = directoryGenerator.findOrCreateSubdirectories(
                        moduleDirectory,
                        String.format(
                                "%s/%s",
                                Package.moduleBaseAreaDir,
                                this.data.getArea().toString()
                        )
                );
            }

            if (diXmlFileDir == null) {
                generationErrorMessage = "Could not locate/generate the target scope directory";
                return null;
            }

            diXmlFile = fileFromTemplateGenerator.generate(
                    file,
                    new Properties(),
                    diXmlFileDir,
                    actionName
            );
        }

        return diXmlFile instanceof XmlFile ? (XmlFile) diXmlFile : null;
    }
}
