/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityCommandData;
import com.magento.idea.magento2plugin.magento.files.commands.SaveEntityCommandFile;

public class SaveEntityCommandGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Book";
    private static final String ENTITY_ID = "book_id";
    private static final String MODEL_NAME = ENTITY_NAME + "Model";
    private static final String RESOURCE_MODEL_NAME = ENTITY_NAME + "Resource";
    private static final String DTO_NAME = ENTITY_NAME + "Data";
    private static final boolean IS_DTO_HAS_INTERFACE = false;
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Command/" + ENTITY_NAME;
    private static final String ACL = "Foo_Bar::book_management";

    /**
     * Test generation of SaveCommand model for entity.
     */
    public void testGenerateSaveEntityCommandFile() {
        final SaveEntityCommandData saveEntityCommandData = new SaveEntityCommandData(
                MODULE_NAME,
                ENTITY_NAME,
                ENTITY_ID,
                MODEL_NAME,
                RESOURCE_MODEL_NAME,
                DTO_NAME,
                "",
                IS_DTO_HAS_INTERFACE,
                ACL
        );
        final SaveEntityCommandGenerator saveEntityCommandGenerator =
                new SaveEntityCommandGenerator(
                        saveEntityCommandData,
                        myFixture.getProject(),
                        false
                );
        final String filePath = this.getFixturePath(
                new SaveEntityCommandFile(MODULE_NAME, ENTITY_NAME).getFileName()
        );

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                saveEntityCommandGenerator.generate("test")
        );
    }
}
