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
import com.magento.idea.magento2plugin.actions.generation.data.QueueTopologyData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateQueueTopologyXml;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueueTopologyGenerator extends FileGenerator {
    private final QueueTopologyData topologyData;
    private final Project project;
    private final FindOrCreateQueueTopologyXml findOrCreateQueueTopologyXml;

    /**
     * Constructor.
     */
    public QueueTopologyGenerator(final Project project, final QueueTopologyData topologyData) {
        super(project);

        this.topologyData = topologyData;
        this.project = project;
        this.findOrCreateQueueTopologyXml = new FindOrCreateQueueTopologyXml(project);
    }

    @Override
    public PsiFile generate(final String actionName) {
        final XmlFile topologyXml = (XmlFile) findOrCreateQueueTopologyXml.execute(
                actionName,
                topologyData.getModuleName()
        );
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(topologyXml);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final XmlTag rootTag = topologyXml.getRootTag();
            if (rootTag == null) {
                return;
            }
            final XmlTag[] exchangeTags = rootTag.findSubTags("exchange");
            boolean exchangeTagIsGenerated = true;
            XmlTag exchangeTag = null;
            for (final XmlTag tag: exchangeTags) {
                if (topologyData.getExchangeName().equals(tag.getAttribute("name").getValue())) {
                    exchangeTagIsGenerated = false;
                    exchangeTag = tag;
                    break;
                }
            }
            if (exchangeTagIsGenerated) {
                exchangeTag = rootTag.createChildTag("exchange", null, "", false);

                exchangeTag.setAttribute("name", topologyData.getExchangeName());
                exchangeTag.setAttribute("type", "topic");
                exchangeTag.setAttribute("connection", topologyData.getConnectionName());
            }

            @NotNull final XmlTag[] bindingTags = exchangeTag.findSubTags("binding");
            boolean isDeclared = false;
            for (final XmlTag bindingTag: bindingTags) {
                @Nullable final XmlAttribute bindingId = bindingTag.getAttribute("id");
                if (topologyData.getBindingId().equals(bindingId.getValue())) {
                    isDeclared = true;
                }
            }

            if (!isDeclared) {
                final XmlTag bindingTag = exchangeTag.createChildTag("binding", null, null, false);

                bindingTag.setAttribute("id", topologyData.getBindingId());
                bindingTag.setAttribute("topic", topologyData.getBindingTopic());
                bindingTag.setAttribute("destinationType", "queue");
                bindingTag.setAttribute("destination", topologyData.getBindingQueue());

                exchangeTag.addSubTag(bindingTag, false);

                if (exchangeTagIsGenerated) {
                    rootTag.addSubTag(exchangeTag, false);
                }
            }

            psiDocumentManager.commitDocument(document);
        });

        return topologyXml;
    }

    @Override
    protected void fillAttributes(Properties attributes) {}//NOPMD
}
