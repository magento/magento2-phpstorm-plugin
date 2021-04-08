/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModelData;

public class ModuleModelGeneratorTest extends BaseGeneratorTestCase {

    private static final String EXPECTED_DIR = "src/app/code/Foo/Bar/Model";

    /**
     * Test generation of model file.
     */
    public void testGenerateFile() {
        final Project project = myFixture.getProject();
        final ModelData modelData = new ModelData(
                "Foo_Bar",
                "my_table",
                "TestModel",
                "TestResource"
        );
        final ModuleModelGenerator generator = new ModuleModelGenerator(
                modelData,
                project
        );
        final PsiFile modelFile = generator.generate("test");
        final String filePath = this.getFixturePath("TestModel.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIR,
                modelFile
        );
    }

    /**
     * Test generation of model file where resource model name equal to the model name.
     */
    public void testGenerateWithTheSameNameForResourceModel() {
        final PsiFile modelFile = new ModuleModelGenerator(
                new ModelData(
                        "Foo_Bar",
                        "my_table",
                        "Test",
                        "Test"
                ),
                myFixture.getProject()
        ).generate("test");

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(this.getFixturePath("Test.php")),
                EXPECTED_DIR,
                modelFile
        );
    }
}
