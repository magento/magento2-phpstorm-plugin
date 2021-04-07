/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.EditEntityActionData;
import com.magento.idea.magento2plugin.magento.files.actions.EditActionFile;

public class EditEntityActionGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Book";
    private static final String EXPECTED_DIRECTORY =
            "/src/app/code/Foo/Bar/Controller/Adminhtml/" + ENTITY_NAME;
    private static final String ACL = "Foo_Bar::management";
    private static final String MENU = "Foo_Bar::management";

    /**
     * Test generation of edit entity controller file.
     */
    public void testGenerateEditEntityActionFile() {
        final EditActionFile file = new EditActionFile(MODULE_NAME, ENTITY_NAME);
        final EditEntityActionGenerator generator =
                new EditEntityActionGenerator(
                        new EditEntityActionData(
                                ENTITY_NAME,
                                MODULE_NAME,
                                ACL,
                                MENU
                        ),
                        myFixture.getProject(),
                        false
                );

        final PsiFile resultFile = generator.generate("test");
        final String filePath = this.getFixturePath(file.getFileName());
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                resultFile
        );
    }
}
