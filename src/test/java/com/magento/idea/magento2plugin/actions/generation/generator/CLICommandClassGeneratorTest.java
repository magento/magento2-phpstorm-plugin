/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandClassData;

public class CLICommandClassGeneratorTest extends BaseGeneratorTestCase {
    private static final String CLASS_NAME = "TestCLICommandPHPClass";
    private static final String PARENT_DIRECTORY = "Console/Command";
    private static final String COMMAND_NAME = "bar:test-command";
    private static final String DESCRIPTION = "This is the test command";
    private static final String NAMESPACE = "Foo\\Bar\\Console\\Command";
    private static final String MODULE_NAME = "Foo_Bar";

    /**
     * Test generation of the new CLI command class.
     */
    public void testGenerateCLICommandClass() {
        final Project project = myFixture.getProject();
        final CLICommandClassData classData = new CLICommandClassData(
                CLASS_NAME,
                PARENT_DIRECTORY,
                COMMAND_NAME,
                DESCRIPTION,
                NAMESPACE,
                MODULE_NAME
        );
        final CLICommandClassGenerator generator = new CLICommandClassGenerator(project, classData);
        final PsiFile cliCommandClass = generator.generate("test");

        final String filePath = this.getFixturePath(CLASS_NAME.concat(".php"));
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Console/Command",
                cliCommandClass
        );
    }
}
