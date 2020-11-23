/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CollectionData;

public class ModuleCollectionGeneratorTest extends BaseGeneratorTestCase {
    /**
     * Test generation of collection file.
     */
    public void testGenerateFile() {
        final Project project = myFixture.getProject();
        final CollectionData collectionFileData = new CollectionData(
                "Foo_Bar",
                "my_table",
                "TestModel",
                "TestCollection",
                "Foo\\Bar\\Model\\ResourceModel\\TestModel\\TestCollection",
                "Model/ResourceModel/TestModel",
                "Foo\\Bar\\Model\\ResourceModel\\TestModel",
                "TestResource",
                "Foo\\Bar\\Model\\ResourceModel\\TestModel\\TestResource",
                "Foo\\Bar\\Model\\TestModel"
        );
        final ModuleCollectionGenerator generator = new ModuleCollectionGenerator(
                collectionFileData,
                project
        );
        final PsiFile controllerFile = generator.generate("test");
        final String filePath = this.getFixturePath("TestCollection.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model/ResourceModel/TestModel",
                controllerFile
        );
    }
}
