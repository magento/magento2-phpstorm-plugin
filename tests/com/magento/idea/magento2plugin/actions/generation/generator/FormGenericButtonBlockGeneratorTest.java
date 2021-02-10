/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.FormGenericButtonBlockData;
import com.magento.idea.magento2plugin.magento.files.FormGenericButtonBlockFile;

public class FormGenericButtonBlockGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Book";
    private static final String ENTITY_ID = "book_id";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Block/Form";
    private static final String NAMESPACE = "Foo\\Bar\\Block\\Form";
    private static final String CLASS_FQN = NAMESPACE + "\\"
            + FormGenericButtonBlockFile.CLASS_NAME;

    /**
     * Test generation of form ui component generic button block.
     */
    public void testGenerateFormGenericButtonBlockFile() {
        final FormGenericButtonBlockData data = new FormGenericButtonBlockData(
                MODULE_NAME,
                ENTITY_NAME,
                ENTITY_ID,
                CLASS_FQN,
                NAMESPACE
        );
        final FormGenericButtonBlockGenerator generator =
                new FormGenericButtonBlockGenerator(
                        data,
                        myFixture.getProject(),
                        false
                );
        final String filePath = this.getFixturePath(
                new FormGenericButtonBlockFile().getFileName()
        );

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                generator.generate("test")
        );
    }
}
