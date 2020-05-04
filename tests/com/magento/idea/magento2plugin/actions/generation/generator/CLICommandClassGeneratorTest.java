/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandClassData;

public class CLICommandClassGeneratorTest extends BaseGeneratorTestCase {
    private static final String className = "TestCLICommandPHPClass";
    private static final String parentDirectory = "Console";
    private static final String commandName = "bar:test-command";
    private static final String description = "This is the test command";
    private static final String namespace = "Foo\\Bar\\Console";
    private static final String moduleName = "Foo_Bar";

    /**
     * Test generation of the new CLI command class.
     */
    public void testGenerateCLICommandClass()
    {
        final Project project = myFixture.getProject();
        final CLICommandClassData classData = new CLICommandClassData(
                className,
                parentDirectory,
                commandName,
                description,
                namespace,
                moduleName
        );
        final CLICommandClassGenerator generator = new CLICommandClassGenerator(project, classData);
        final PsiFile cliCommandClass = generator.generate("test");

        final String filePath = this.getFixturePath(className.concat(".php"));
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Console",
                cliCommandClass
        );
    }
}
