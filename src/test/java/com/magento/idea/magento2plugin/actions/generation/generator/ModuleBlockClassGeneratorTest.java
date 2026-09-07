/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.BlockFileData;

public class ModuleBlockClassGeneratorTest extends BaseGeneratorTestCase {

    /**
     * Test Block generator
     */
    public void testGenerateFile() {
        Project project = myFixture.getProject();

        BlockFileData blockData = new BlockFileData(
            "Block/Test",
            "ViewBlock",
            "Foo_Bar",
            "Foo\\Bar\\Block\\Test"
        );
        ModuleBlockClassGenerator moduleBlockClassGenerator = new ModuleBlockClassGenerator(blockData, project);
        PsiFile blockFile = moduleBlockClassGenerator.generate("test");

        String filePath = this.getFixturePath("ViewBlock.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(expectedFile, "src/app/code/Foo/Bar/Block/Test", blockFile);
    }
}
