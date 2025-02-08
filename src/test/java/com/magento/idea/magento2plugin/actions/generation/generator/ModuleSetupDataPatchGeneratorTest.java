/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.ModuleSetupDataPatchData;
import com.magento.idea.magento2plugin.magento.files.ModuleSetupDataPatchFile;

public final class ModuleSetupDataPatchGeneratorTest extends BaseGeneratorTestCase {

    private static final String CLASS_NAME = "TestClassPatch";

    /**
     * Test module README.md file generation.
     */
    public void testGenerateModuleSetupDataPatchFile() {
        final PsiFile expectedFile = myFixture.configureByFile(
                getFixturePath(CLASS_NAME + ModuleSetupDataPatchFile.EXTENSION)
        );
        final ModuleSetupDataPatchGenerator generator = new ModuleSetupDataPatchGenerator(
                new ModuleSetupDataPatchData(
                        "Foo",
                        "Bar",
                        getProjectDirectory(),
                        CLASS_NAME

                ),
                myFixture.getProject()
        );
        final PsiFile generatedFile = generator.generate("test");

        assertGeneratedFileIsCorrect(
                expectedFile,
                generatedFile
        );
    }
}
