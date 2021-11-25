/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModelData;

public class ModuleModelGeneratorTest extends BaseGeneratorTestCase {
    /**
     * Test generation of model file.
     */
    public void testGenerateFile() {
        final Project project = myFixture.getProject();
        final ModelData modelData = new ModelData(
                "Foo_Bar",
                "my_table",
                "TestModel",
                "TestResource",
                "Foo\\Bar\\Model\\TestModel",
                "Foo\\Bar\\Model",
                "Foo\\Bar\\Model\\ResourceModel\\TestResource"
        );
        final ModuleModelGenerator generator = new ModuleModelGenerator(
                modelData,
                project
        );
        final PsiFile controllerFile = generator.generate("test");
        final String filePath = this.getFixturePath("TestModel.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model",
                controllerFile
        );
    }
}
