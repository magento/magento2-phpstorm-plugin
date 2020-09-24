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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateLayoutXml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Properties;

public class LayoutXmlGenerator extends FileGenerator {
    private final LayoutXmlData layoutXmlData;
    private final Project project;
    private final FindOrCreateLayoutXml findOrCreateLayoutXml;

    /**
     * Constructor.
     *
     * @param layoutXmlData LayoutXmlData
     * @param project Project
     */
    public LayoutXmlGenerator(
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
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        XmlFile layoutXml = (XmlFile) findOrCreateLayoutXml.execute(
                actionName,
                layoutXmlData.getRoute(),
                layoutXmlData.getControllerName(),
                layoutXmlData.getActionName(),
                layoutXmlData.getModuleName(),
                layoutXmlData.getArea()
        );
        final PsiDocumentManager psiDocumentManager =
                PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(layoutXml);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            XmlTag rootTag = layoutXml.getRootTag();
            if (rootTag == null) {
                return;
            }

            XmlTag bodyTag = rootTag.findFirstSubTag("body");
            boolean bodyTagIsGenerated = false;
            if (bodyTag == null) {
                bodyTagIsGenerated = true;
                bodyTag = rootTag.createChildTag("body", null, "", false);
            }
            @NotNull XmlTag[] referenceContainers = bodyTag.findSubTags("referenceContainer");
            boolean isDeclared = false;
            XmlTag contentContainer = null;
            for (XmlTag referenceContainerTag: referenceContainers) {
                @Nullable XmlAttribute containerName = referenceContainerTag.getAttribute("name");
                if (containerName.getValue().equals("content")) {
                    contentContainer = referenceContainerTag;
                    @NotNull XmlTag[] uiComponents = bodyTag.findSubTags("uiComponent");
                    for (XmlTag uiComponent: uiComponents) {
                        @Nullable XmlAttribute uiComponentName = uiComponent.getAttribute("name");
                        if (uiComponentName.getValue().equals(layoutXmlData.getFormName())) {
                            isDeclared = true;
                        }
                    }
                    break;
                }
            }

            if (!isDeclared) {
                boolean contentContainerIsGenerated = false;
                if (contentContainer == null) {
                    contentContainerIsGenerated = true;
                    contentContainer = bodyTag.createChildTag("referenceContainer", null, "", false);
                    contentContainer.setAttribute("name", "content");
                }
                XmlTag uiComponentTag = contentContainer.createChildTag("uiComponent", null, null, false);
                uiComponentTag.setAttribute("name", layoutXmlData.getFormName());
                contentContainer.addSubTag(uiComponentTag, false);

                if (contentContainerIsGenerated) {
                    bodyTag.addSubTag(contentContainer, false);
                }

                if (bodyTagIsGenerated) {
                    rootTag.addSubTag(bodyTag,false);
                }
            }

            psiDocumentManager.commitDocument(document);
        });
        return layoutXml;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
    }
}
