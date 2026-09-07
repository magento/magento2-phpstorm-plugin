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
import com.magento.idea.magento2plugin.actions.generation.data.QueueCommunicationData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateCommunicationXml;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueueCommunicationGenerator extends FileGenerator {
    public static final String ATTRIBUTE_NAME = "name";
    private final QueueCommunicationData communicationData;
    private final Project project;
    private final FindOrCreateCommunicationXml findOrCreateCommunicationXml;

    /**
     * Constructor.
     */
    public QueueCommunicationGenerator(
            final Project project,
            final @NotNull QueueCommunicationData communicationData
    ) {
        super(project);

        this.communicationData = communicationData;
        this.project = project;
        this.findOrCreateCommunicationXml = new FindOrCreateCommunicationXml(project);
    }

    @Override
    public PsiFile generate(final String actionName) {
        final XmlFile communicationXml = (XmlFile) findOrCreateCommunicationXml.execute(
                actionName,
                communicationData.getModuleName()
        );
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(communicationXml);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final XmlTag rootTag = communicationXml.getRootTag();
            if (rootTag == null) {
                return;
            }
            final XmlTag[] topicTags = rootTag.findSubTags("topic");
            boolean topicTagIsGenerated = true;
            XmlTag topicTag = null;
            for (final XmlTag tag: topicTags) {
                if (communicationData.getTopicName().equals(
                        tag.getAttribute(ATTRIBUTE_NAME).getValue())
                ) {
                    topicTagIsGenerated = false;
                    topicTag = tag;
                    break;
                }
            }
            if (topicTagIsGenerated) {
                topicTag = rootTag.createChildTag("topic", null, "", false);
                topicTag.setAttribute(ATTRIBUTE_NAME, communicationData.getTopicName());
                topicTag.setAttribute("request", "string");
                topicTag.setAttribute("response", "string");
            }

            @NotNull final XmlTag[] handlerTags = topicTag.findSubTags("handler");
            boolean isDeclared = false;
            for (final XmlTag handlerTag: handlerTags) {
                @Nullable final XmlAttribute handlerName = handlerTag.getAttribute(ATTRIBUTE_NAME);
                if (communicationData.getHandlerName().equals(handlerName.getValue())) {
                    isDeclared = true;
                }
            }

            if (!isDeclared) {
                final XmlTag handlerTag = topicTag.createChildTag("handler", null, null, false);

                handlerTag.setAttribute(ATTRIBUTE_NAME, communicationData.getHandlerName());
                handlerTag.setAttribute("type",communicationData.getHandlerType());
                handlerTag.setAttribute("method",communicationData.getHandlerMethod());

                topicTag.addSubTag(handlerTag, false);

                if (topicTagIsGenerated) {
                    rootTag.addSubTag(topicTag, false);
                }
            }

            psiDocumentManager.commitDocument(document);
        });

        return communicationXml;
    }

    @Override
    protected void fillAttributes(Properties attributes) {}//NOPMD
}
