/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.MessageQueueClassData;
import com.magento.idea.magento2plugin.magento.files.MessageQueueClassPhp;

public class MessageQueueClassGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";

    private static final String HANDLER_EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Queue/Handler";
    private static final String HANDLER_CLASS_NAME = "MyHandler";
    private static final String HANDLER_NAMESPACE = "Foo\\Bar\\Queue\\Handler";
    private static final String HANDLER_PATH = "Queue/Handler";
    private static final String HANDLER_FQN = "\\Foo\\Bar\\Queue\\Handler\\MyHandler";

    private static final String CONSUMER_EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Queue/Consumer";
    private static final String CONSUMER_CLASS_NAME = "MyConsumer";
    private static final String CONSUMER_NAMESPACE = "Foo\\Bar\\Queue\\Consumer";
    private static final String CONSUMER_PATH = "Queue/Consumer";
    private static final String CONSUMER_FQN = "\\Foo\\Bar\\Queue\\Handler\\MyConsumer";

    /**
     * Test handler class file generation.
     */
    public void testGenerateHandler() {
        final Project project = myFixture.getProject();
        final MessageQueueClassData messageQueueClassData = new MessageQueueClassData(
                HANDLER_CLASS_NAME,
                HANDLER_NAMESPACE,
                HANDLER_PATH,
                HANDLER_FQN,
                MessageQueueClassPhp.Type.HANDLER
        );
        final MessageQueueClassGenerator generator;
        generator = new MessageQueueClassGenerator(
            messageQueueClassData,
            MODULE_NAME,
            project
        );

        final PsiFile messageQueue = generator.generate("test");
        final String filePath = this.getFixturePath("MyHandler.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                HANDLER_EXPECTED_DIRECTORY,
                messageQueue
        );
    }

    /**
     * Test consumer class file generation.
     */
    public void testGenerateConsumer() {
        final Project project = myFixture.getProject();
        final MessageQueueClassData messageQueueClassData = new MessageQueueClassData(
                CONSUMER_CLASS_NAME,
                CONSUMER_NAMESPACE,
                CONSUMER_PATH,
                CONSUMER_FQN,
                MessageQueueClassPhp.Type.CONSUMER
        );
        final MessageQueueClassGenerator generator;
        generator = new MessageQueueClassGenerator(
            messageQueueClassData,
            MODULE_NAME,
            project
        );

        final PsiFile messageQueue = generator.generate("test");
        final String filePath = this.getFixturePath("MyConsumer.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                CONSUMER_EXPECTED_DIRECTORY,
                messageQueue
        );
    }
}
