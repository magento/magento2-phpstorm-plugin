/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData;

public class SourceModelGeneratorTest extends BaseGeneratorTestCase {
    private final static String MODULE_NAME = "Foo_Bar";
    private final static String SOURCE_MODEL_NAMESPACE = "Foo\\Bar\\Setup\\Patch\\Data";

    /**
     * Test Data patch for product's eav attribute generator.
     */
    public void testGenerateFile() {
        final Project project = myFixture.getProject();

        final SourceModelData sourceModelData = new SourceModelData();
        sourceModelData.setClassName("CustomSourceModel");
        sourceModelData.setModuleName(MODULE_NAME);
        sourceModelData.setNamespace(SOURCE_MODEL_NAMESPACE);

        final SourceModelGenerator sourceModelGeneratorGenerator =
                new SourceModelGenerator(project, sourceModelData);
        final PsiFile dataPatchFile = sourceModelGeneratorGenerator.generate("test");
        final String filePatch = this.getFixturePath("CustomSourceModel.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model/Source",
                dataPatchFile
        );
    }
}
