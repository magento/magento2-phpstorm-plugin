/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleReadmeMdData;
import com.magento.idea.magento2plugin.magento.files.ModuleReadmeMdFile;

public final class ModuleReadmeMdGeneratorTest extends BaseGeneratorTestCase {

    /**
     * Test module README.md file generation.
     */
    public void testGenerateModuleReadmeMdFile() {
        final PsiFile expectedFile = myFixture.configureByFile(
                getFixturePath(ModuleReadmeMdFile.FILE_NAME)
        );
        final ModuleReadmeMdGenerator generator = new ModuleReadmeMdGenerator(
                new ModuleReadmeMdData(
                        "Foo",
                        "Bar",
                        getProjectDirectory()
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
