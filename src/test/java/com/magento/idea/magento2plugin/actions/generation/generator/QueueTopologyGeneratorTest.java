/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewMessageQueueAction;
import com.magento.idea.magento2plugin.actions.generation.data.QueueTopologyData;
import com.magento.idea.magento2plugin.magento.files.QueueTopologyXml;

public class QueueTopologyGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXCHANGE_NAME = "exchange-name";
    private static final String BINDING_ID = "bindingId";
    private static final String BINDING_TOPIC = "topic.name";
    private static final String BINDING_QUEUE = "queue.name";
    private static final String CONNECTION_NAME = "amqp";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";

    /**
     * Tests for generation of queue_topology.xml file.
     */
    public void testGenerateTopologyXmlFile() {
        final String filePath = this.getFixturePath(QueueTopologyXml.fileName);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final QueueTopologyGenerator topologyGenerator = new QueueTopologyGenerator(
                project,
                new QueueTopologyData(
                        EXCHANGE_NAME,
                        CONNECTION_NAME,
                        BINDING_ID,
                        BINDING_TOPIC,
                        BINDING_QUEUE,
                        MODULE_NAME
                )
        );

        final PsiFile file = topologyGenerator.generate(NewMessageQueueAction.ACTION_NAME);

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }
}
