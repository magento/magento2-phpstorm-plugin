/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewMessageQueueAction;
import com.magento.idea.magento2plugin.actions.generation.data.QueuePublisherData;
import com.magento.idea.magento2plugin.magento.files.QueuePublisherXml;

public class QueuePublisherGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXCHANGE_NAME = "exchange-name";
    private static final String TOPIC_NAME = "topic.name";
    private static final String CONNECTION_NAME = "amqp";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";

    /**
     * Tests for generation of queue_publisher.xml file.
     */
    public void testGeneratePublisherXmlFile() {
        final String filePath = this.getFixturePath(QueuePublisherXml.fileName);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final QueuePublisherGenerator publisherGenerator = new QueuePublisherGenerator(
                project,
                new QueuePublisherData(
                        TOPIC_NAME,
                        CONNECTION_NAME,
                        EXCHANGE_NAME,
                        MODULE_NAME
                )
        );

        final PsiFile file = publisherGenerator.generate(NewMessageQueueAction.ACTION_NAME);

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }
}
