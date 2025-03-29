/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase;

@SuppressWarnings({
        "PMD.FieldNamingConventions",
        "PMD.LongVariable",
        "PMD.CommentSize"
})
public class CLICommandDiXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String CLASS_NAME = "TestCommand";
    private static final String SECOND_CLASS_NAME = "OneMoreCLICommand";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String PARENT_DIR = "Console\\Command";
    private static final String MODULE_DIR = "src/app/code/Foo/Bar/";

    /**
     * Test generation of the di.xml file with the CLI command initialization.
     */
    public void testInitializeCLICommand() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile diXml = initializeCLICommandInDiXml(CLASS_NAME);

        final String expectedDir = MODULE_DIR + Package.moduleBaseAreaDir;
        assertGeneratedFileIsCorrect(
                expectedFile,
                expectedDir,
                diXml
        );
    }

    /**
     * Test adding one more CLI command to the di.xml with already initialized CLI command.
     */
    public void testAddingTwoCLICommandToDiXml() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        initializeCLICommandInDiXml(CLASS_NAME);
        final PsiFile diXml = initializeCLICommandInDiXml(SECOND_CLASS_NAME);

        final String expectedDir = MODULE_DIR + Package.moduleBaseAreaDir;
        assertGeneratedFileIsCorrect(
                expectedFile,
                expectedDir,
                diXml
        );
    }

    private PsiFile initializeCLICommandInDiXml(final String className) {
        final NamespaceBuilder classFqn = new NamespaceBuilder(MODULE_NAME, className, PARENT_DIR);

        final String diItemName = new CamelCaseToSnakeCase().convert(className);
        final String itemName = MODULE_NAME.toLowerCase(new java.util.Locale("en","EN"))
                + "_"
                + diItemName;

        final CLICommandXmlData cliCommandDiXmlData = new CLICommandXmlData(
                MODULE_NAME,
                classFqn.getClassFqn(),
                itemName
        );

        final Project project = myFixture.getProject();
        final CLICommandDiXmlGenerator diXmlGenerator = new CLICommandDiXmlGenerator(
                project,
                cliCommandDiXmlData
        );

        return diXmlGenerator.generate("test");
    }
}
