/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewMessageQueueAction;
import com.magento.idea.magento2plugin.actions.generation.data.QueueConsumerData;
import com.magento.idea.magento2plugin.magento.files.QueueConsumerXml;

public class QueueConsumerGeneratorTest extends BaseGeneratorTestCase {
    private static final String CONSUMER_NAME = "consumer.name";
    private static final String QUEUE_NAME = "queue.name";
    private static final String CONSUMER_TYPE = "Foo\\Bar\\Model\\Consumer";
    private static final String MAX_MESSAGES = "100";
    private static final String CONNECTION_NAME = "amqp";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";

    /**
     * Tests for generation of queue_consumer.xml file.
     */
    public void testGenerateConsumerXmlFile() {
        final String filePath = this.getFixturePath(QueueConsumerXml.fileName);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final QueueConsumerGenerator consumerGenerator = new QueueConsumerGenerator(
                project,
                new QueueConsumerData(
                        CONSUMER_NAME,
                        QUEUE_NAME,
                        CONSUMER_TYPE,
                        MAX_MESSAGES,
                        CONNECTION_NAME,
                        MODULE_NAME
                )
        );

        final PsiFile file = consumerGenerator.generate(NewMessageQueueAction.ACTION_NAME);

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }
}
