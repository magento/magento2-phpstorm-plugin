/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData;

public class SourceModelGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";

    /**
     * Test source model generation.
     */
    public void testGenerateFile() {
        final Project project = myFixture.getProject();

        final SourceModelData sourceModelData = new SourceModelData();
        sourceModelData.setClassName("CustomSourceModel");
        sourceModelData.setModuleName(MODULE_NAME);

        final SourceModelGenerator sourceModelGeneratorGenerator =
                new SourceModelGenerator(sourceModelData, project);
        final PsiFile dataPatchFile = sourceModelGeneratorGenerator.generate("test");
        final String filePatch = this.getFixturePath("CustomSourceModel.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model/Source",
                dataPatchFile
        );
    }

    /**
     * Test source model in custom directory generation.
     */
    public void testGenerateFileInCustomDirectory() {
        final Project project = myFixture.getProject();

        final SourceModelData sourceModelData = new SourceModelData();
        sourceModelData.setClassName("CustomSourceModel");
        sourceModelData.setModuleName(MODULE_NAME);
        sourceModelData.setDirectory("Custom/Source/Directory");

        final SourceModelGenerator sourceModelGeneratorGenerator =
                new SourceModelGenerator(sourceModelData, project);
        final PsiFile dataPatchFile = sourceModelGeneratorGenerator.generate("test custom dir");
        final String filePatch = this.getFixturePath("CustomSourceModel.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Custom/Source/Directory",
                dataPatchFile
        );
    }
}
