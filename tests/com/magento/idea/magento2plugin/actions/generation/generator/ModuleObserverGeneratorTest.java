/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.ModuleObserverData;
import com.magento.idea.magento2plugin.magento.files.ModuleObserverFile;

public final class ModuleObserverGeneratorTest extends BaseGeneratorTestCase {

    private static final String CLASS_NAME = "TestClassObserver";

    /**
     * Test module README.md file generation.
     */
    public void testGenerateModuleObserverFile() {
        final PsiFile expectedFile = myFixture.configureByFile(
                getFixturePath(CLASS_NAME + ModuleObserverFile.EXTENSION)
        );
        final ModuleObserverGenerator generator = new ModuleObserverGenerator(
                new ModuleObserverData(
                        "Foo",
                        "Bar",
                        "Foo\\Bar\\Observer",
                        "test_event_name",
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
