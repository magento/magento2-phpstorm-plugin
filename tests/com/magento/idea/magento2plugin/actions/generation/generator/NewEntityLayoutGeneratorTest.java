/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.NewEntityLayoutData;
import com.magento.idea.magento2plugin.magento.files.NewEntityLayoutFile;
import com.magento.idea.magento2plugin.magento.packages.File;

public class NewEntityLayoutGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EDIT_ACTION = "book/book/edit";
    private static final String NEW_ACTION = "book/book/new";

    /**
     * Test new entity layout file generation.
     */
    public void testGenerateNewEntityLayoutFile() {
        final NewEntityLayoutData data = new NewEntityLayoutData(
                MODULE_NAME,
                NEW_ACTION,
                EDIT_ACTION
        );
        final NewEntityLayoutFile file =
                new NewEntityLayoutFile(data.getNewActionPath().replace(File.separator, "_"));

        final NewEntityLayoutGenerator generator = new NewEntityLayoutGenerator(
                data,
                myFixture.getProject(),
                false
        );
        final PsiFile newActionLayoutFile = generator.generate("test");
        final String filePath = this.getFixturePath(file.getFileName());
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                file.getDirectory(),
                newActionLayoutFile
        );
    }
}
