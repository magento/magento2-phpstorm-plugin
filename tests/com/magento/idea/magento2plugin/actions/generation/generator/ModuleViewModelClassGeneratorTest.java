/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ViewModelFileData;

public class ModuleViewModelClassGeneratorTest extends BaseGeneratorTestCase {

    public void testGenerateViewModelFile() {
        Project project = myFixture.getProject();

        ViewModelFileData viewModelData = new ViewModelFileData(
                "ViewModel",
                "TestViewModel",
                "Foo_Bar",
                "Foo\\Bar\\ViewModel"
        );
        ModuleViewModelClassGenerator moduleViewModelClassGenerator = new ModuleViewModelClassGenerator(
                viewModelData,
                project
        );
        PsiFile viewModelFile = moduleViewModelClassGenerator.generate("test");

        String filePath = this.getFixturePath("TestViewModel.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/ViewModel",
                viewModelFile
        );
    }
}
