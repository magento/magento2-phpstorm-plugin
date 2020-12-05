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
import java.util.Properties;

public class QueueConsumerGenerator extends FileGenerator {
    private final QueueConsumerData consumerData;
    private final Project project;
    private final FindOrCreateQueueConsumerXml findOrCreateQueueConsumerXml;

    /**
     * Constructor.
     */
    public QueueConsumerGenerator(Project project, QueueConsumerData consumerData) {
        super(project);

        this.consumerData = consumerData;
        this.project = project;
        this.findOrCreateQueueConsumerXml = new FindOrCreateQueueConsumerXml(project);
    }

    @Override
    public PsiFile generate(String actionName) {
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
            XmlTag[] consumerTags = rootTag.findSubTags("consumer");
            boolean tagIsGenerated = true;
            for (final XmlTag tag: consumerTags) {
                if (consumerData.getConsumerName().equals(tag.getAttribute("name").getValue())) {
                    tagIsGenerated = false;
                    break;
                }
            }
            if (tagIsGenerated) {
                XmlTag consumerTag = rootTag.createChildTag("consumer", null, null, false);
                consumerTag.setAttribute("name", consumerData.getConsumerName());
                consumerTag.setAttribute("queue", consumerData.getQueueName());
                consumerTag.setAttribute("consumerInstance", consumerData.getConsumerType());
                consumerTag.setAttribute("connection", consumerData.getConnectionName());
                consumerTag.setAttribute("maxMessages", consumerData.getMaxMessages());

                rootTag.addSubTag(consumerTag, false);
            }

            psiDocumentManager.commitDocument(document);
        });

        return consumerXml;
    }

    @Override
    protected void fillAttributes(Properties attributes) {}
}
