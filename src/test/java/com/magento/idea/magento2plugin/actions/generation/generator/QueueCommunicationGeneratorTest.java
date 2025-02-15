/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewMessageQueueAction;
import com.magento.idea.magento2plugin.actions.generation.data.QueueCommunicationData;
import com.magento.idea.magento2plugin.magento.files.QueueCommunicationXml;

public class QueueCommunicationGeneratorTest extends BaseGeneratorTestCase {
    private static final String TOPIC_NAME = "topic.name";
    private static final String HANDLER_NAME = "handlerName";
    private static final String HANDLER_TYPE = "Foo\\Bar\\Model\\Handler";
    private static final String HANDLER_METHOD = "execute";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";

    /**
     * Tests for generation of communication.xml file.
     */
    public void testGenerateCommunicationXmlFile() {
        final String filePath = this.getFixturePath(QueueCommunicationXml.fileName);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final QueueCommunicationGenerator communicationGenerator = new QueueCommunicationGenerator(
                project,
                new QueueCommunicationData(
                        TOPIC_NAME,
                        HANDLER_NAME,
                        HANDLER_TYPE,
                        HANDLER_METHOD,
                        MODULE_NAME
                )
        );

        final PsiFile file = communicationGenerator.generate(NewMessageQueueAction.ACTION_NAME);

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }
}
