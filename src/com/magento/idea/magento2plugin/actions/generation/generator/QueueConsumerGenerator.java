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
import com.magento.idea.magento2plugin.actions.generation.data.QueueConsumerData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateQueueConsumerXml;
import com.magento.idea.magento2plugin.magento.packages.MessageQueueConnections;
import java.util.Properties;

public class QueueConsumerGenerator extends FileGenerator {
    private final QueueConsumerData consumerData;
    private final Project project;
    private final FindOrCreateQueueConsumerXml findOrCreateQueueConsumerXml;

    /**
     * Constructor.
     */
    public QueueConsumerGenerator(final Project project, final QueueConsumerData consumerData) {
        super(project);

        this.consumerData = consumerData;
        this.project = project;
        this.findOrCreateQueueConsumerXml = new FindOrCreateQueueConsumerXml(project);
    }

    @Override
    public PsiFile generate(final String actionName) {
        final XmlFile consumerXml = (XmlFile) findOrCreateQueueConsumerXml.execute(
                actionName,
                consumerData.getModuleName()
        );
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(consumerXml);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final XmlTag rootTag = consumerXml.getRootTag();
            if (rootTag == null) {
                return;
            }
            final XmlTag[] consumerTags = rootTag.findSubTags("consumer");
            boolean tagIsGenerated = true;
            for (final XmlTag tag: consumerTags) {
                if (consumerData.getConsumerName().equals(tag.getAttribute("name").getValue())) {
                    tagIsGenerated = false;
                    break;
                }
            }
            if (tagIsGenerated) {
                final XmlTag consumerTag = rootTag.createChildTag("consumer", null, null, false);
                consumerTag.setAttribute("name", consumerData.getConsumerName());
                consumerTag.setAttribute("queue", consumerData.getQueueName());
                consumerTag.setAttribute("connection", consumerData.getConnectionName());

                if (consumerData.getConnectionName().equals(MessageQueueConnections.DB.getType())) {
                    consumerTag.setAttribute("consumerInstance", consumerData.getConsumerClass());
                    consumerTag.setAttribute("maxMessages", consumerData.getMaxMessages());
                } else {
                    consumerTag.setAttribute("handler", consumerData.getHandler());
                }

                rootTag.addSubTag(consumerTag, false);
            }

            psiDocumentManager.commitDocument(document);
        });

        return consumerXml;
    }

    @Override
    protected void fillAttributes(Properties attributes) {}//NOPMD
}
