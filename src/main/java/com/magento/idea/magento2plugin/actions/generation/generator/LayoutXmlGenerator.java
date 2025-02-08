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
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        final XmlFile layoutXml = (XmlFile) findOrCreateLayoutXml.execute(
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
            final XmlTag rootTag = layoutXml.getRootTag();
            if (rootTag == null) {
                return;
            }

            XmlTag bodyTag = rootTag.findFirstSubTag(LayoutXml.ROOT_TAG_NAME);
            boolean bodyTagIsGenerated = false;
            if (bodyTag == null) {
                bodyTagIsGenerated = true;
                bodyTag = rootTag.createChildTag(LayoutXml.ROOT_TAG_NAME, null, "", false);
            }
            @NotNull final XmlTag[] referenceContainers =
                    bodyTag.findSubTags(LayoutXml.REFERENCE_CONTAINER_TAG_NAME);
            boolean isDeclared = false;
            XmlTag contentContainer = null;
            for (final XmlTag referenceContainerTag: referenceContainers) {
                @Nullable final XmlAttribute containerName =
                        referenceContainerTag.getAttribute(LayoutXml.NAME_ATTRIBUTE);
                if (containerName.getValue().equals(LayoutXml.CONTENT_CONTAINER_NAME)) {
                    contentContainer = referenceContainerTag;
                    @NotNull final XmlTag[] uiComponents =
                            bodyTag.findSubTags(LayoutXml.UI_COMPONENT_TAG_NAME);
                    for (final XmlTag uiComponent: uiComponents) {
                        @Nullable final XmlAttribute uiComponentName
                                = uiComponent.getAttribute(LayoutXml.NAME_ATTRIBUTE);
                        if (uiComponentName.getValue().equals(layoutXmlData.getUiComponentName())) {
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
                    contentContainer = bodyTag.createChildTag(
                        LayoutXml.REFERENCE_CONTAINER_TAG_NAME,
                        null,
                        "",
                        false
                    );
                    contentContainer.setAttribute(
                            LayoutXml.NAME_ATTRIBUTE,
                            LayoutXml.CONTENT_CONTAINER_NAME
                    );
                }
                final XmlTag uiComponentTag = contentContainer.createChildTag(
                        LayoutXml.UI_COMPONENT_TAG_NAME,
                        null,
                        null,
                        false
                );
                uiComponentTag.setAttribute(
                        LayoutXml.NAME_ATTRIBUTE,
                        layoutXmlData.getUiComponentName()
                );
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
    protected void fillAttributes(final Properties attributes) {}//NOPMD
}
