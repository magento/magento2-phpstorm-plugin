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
import com.magento.idea.magento2plugin.actions.generation.data.QueuePublisherData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateQueuePublisherXml;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueuePublisherGenerator extends FileGenerator {
    private final QueuePublisherData publisherData;
    private final Project project;
    private final FindOrCreateQueuePublisherXml findOrCreateQueuePublisherXml;

    /**
     * Constructor.
     */
    public QueuePublisherGenerator(
            final Project project,
            final QueuePublisherData publisherData
    ) {
        super(project);

        this.publisherData = publisherData;
        this.project = project;
        this.findOrCreateQueuePublisherXml = new FindOrCreateQueuePublisherXml(project);
    }

    @Override
    public PsiFile generate(final String actionName) {
        final XmlFile publisherXml = (XmlFile) findOrCreateQueuePublisherXml.execute(
                actionName,
                publisherData.getModuleName()
        );
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(publisherXml);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final XmlTag rootTag = publisherXml.getRootTag();
            if (rootTag == null) {
                return;
            }
            final XmlTag[] publisherTags = rootTag.findSubTags("publisher");
            boolean publisherTagIsGenerated = true;
            XmlTag publisherTag = null;
            for (final XmlTag tag: publisherTags) {
                if (publisherData.getTopicName().equals(tag.getAttribute("topic").getValue())) {
                    publisherTagIsGenerated = false;
                    publisherTag = tag;
                    break;
                }
            }
            if (publisherTagIsGenerated) {
                publisherTag = rootTag.createChildTag("publisher", null, "", false);

                publisherTag.setAttribute("topic", publisherData.getTopicName());
            }

            @NotNull final XmlTag[] connectionTags = publisherTag.findSubTags("connection");
            boolean isDeclared = false;
            for (final XmlTag connectionTag: connectionTags) {
                @Nullable final XmlAttribute connectionName = connectionTag.getAttribute("name");
                if (publisherData.getConnectionName().equals(connectionName.getValue())) {
                    isDeclared = true;
                }
            }

            if (!isDeclared) {
                final XmlTag connectionTag = publisherTag.createChildTag(
                        "connection", null, null, false
                );

                connectionTag.setAttribute("name", publisherData.getConnectionName());
                connectionTag.setAttribute("exchange",publisherData.getExchangeName());

                publisherTag.addSubTag(connectionTag, false);

                if (publisherTagIsGenerated) {
                    rootTag.addSubTag(publisherTag, false);
                }
            }

            psiDocumentManager.commitDocument(document);
        });

        return publisherXml;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {}//NOPMD
}
