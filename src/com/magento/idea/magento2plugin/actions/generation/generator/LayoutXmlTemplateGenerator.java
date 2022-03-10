/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateLayoutXml;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class LayoutXmlTemplateGenerator extends FileGenerator {

    private final LayoutXmlData layoutXmlData;
    private final Project project;
    private final FindOrCreateLayoutXml findOrCreateLayoutXml;

    /**
     * Constructor.
     *
     * @param layoutXmlData LayoutXmlData
     * @param project Project
     */
    public LayoutXmlTemplateGenerator(
            final @NotNull LayoutXmlData layoutXmlData,
            final Project project
    ) {
        super(project);
        this.layoutXmlData = layoutXmlData;
        this.project = project;
        this.findOrCreateLayoutXml = new FindOrCreateLayoutXml(project);
    }

    /**
     * Creates a module layout file.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final XmlFile layoutXml = (XmlFile) findOrCreateLayoutXml.execute(
                actionName,
                layoutXmlData.getRoute(),
                layoutXmlData.getControllerName(),
                layoutXmlData.getActionName(),
                layoutXmlData.getModuleName(),
                layoutXmlData.getArea()
        );

        if (layoutXml == null) {
            return null;
        }
        final PsiDocumentManager psiDocumentManager =
                PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(layoutXml);

        if (document == null) {
            return null;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            final XmlTag rootTag = layoutXml.getRootTag();

            if (rootTag == null) {
                return;
            }
            psiDocumentManager.commitDocument(document);
        });

        return layoutXml;
    }

    @Override
    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    protected void fillAttributes(final Properties attributes) {}
}
