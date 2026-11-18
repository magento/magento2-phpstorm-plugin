/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandClassData;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

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
    public void testGenerateCLICommandClass() throws Exception {
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
        // Linux CI supplies Symfony 7.4. Local runs can use an installed Magento vendor autoloader.
        final String autoload = System.getenv("SYMFONY_CONSOLE_AUTOLOAD");
        if (autoload != null) {
            final Path generated = Files.createTempFile("magento-generated-command", ".php");
            try {
                Files.writeString(generated, cliCommandClass.getText());
                final Process process = new ProcessBuilder("php", "-r",
                        "require $argv[1]; require $argv[2];"
                        + " $command = new Foo\\Bar\\Console\\Command\\TestCLICommandPHPClass();"
                        + " exit($command->run(new Symfony\\Component\\Console\\Input\\ArrayInput([]),"
                        + " new Symfony\\Component\\Console\\Output\\NullOutput()));",
                        autoload, generated.toString()).redirectErrorStream(true).start();
                try {
                    assertTrue("Generated command timed out", process.waitFor(15, TimeUnit.SECONDS));
                    assertEquals(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8), 0, process.exitValue());
                } finally {
                    process.destroyForcibly();
                }
            } finally {
                Files.deleteIfExists(generated);
            }
        }
    }

    public void testGenerateCLICommandClassEscapesSingleQuotes() {
        assertGeneratedDescription("Test command's bug", "Test command\\'s bug");
    }

    public void testGenerateCLICommandClassEscapesBackslashes() {
        assertGeneratedDescription("Read C:\\temp\\\\logs\\", "Read C:\\\\temp\\\\\\\\logs\\\\");
    }

    public void testGenerateCLICommandClassEscapesBackslashesBeforeQuotes() {
        assertGeneratedDescription("Test \\'quoted' command", "Test \\\\\\'quoted\\' command");
    }

    private void assertGeneratedDescription(final String description, final String escapedDescription) {
        final CLICommandClassData classData = new CLICommandClassData(
                CLASS_NAME,
                PARENT_DIRECTORY,
                COMMAND_NAME,
                description,
                NAMESPACE,
                MODULE_NAME
        );
        final PsiFile cliCommandClass = new CLICommandClassGenerator(
                myFixture.getProject(),
                classData
        ).generate("test");

        assertNotNull("CLI command class should be generated", cliCommandClass);
        assertNull(
                "Generated CLI command should have valid PHP syntax",
                PsiTreeUtil.findChildOfType(cliCommandClass, PsiErrorElement.class)
        );
        assertTrue(
                "Description should be escaped in the generated PHP string: " + cliCommandClass.getText(),
                cliCommandClass.getText().contains("$this->setDescription('" + escapedDescription + "');")
        );
    }
}
