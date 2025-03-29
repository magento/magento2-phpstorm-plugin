/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.IndexActionData;
import com.magento.idea.magento2plugin.magento.files.actions.IndexActionFile;

public class IndexControllerGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Book";
    private static final String ACL = "Foo_Bar::book_management";
    private static final String MENU = "Foo_Bar::book_management_menu";

    /**
     * Test generation of list view entity controller (adminhtml).
     */
    public void testGenerateIndexControllerFile() {
        final IndexActionFile file = new IndexActionFile(MODULE_NAME, ENTITY_NAME);
        final IndexActionData data = new IndexActionData(
                MODULE_NAME,
                ENTITY_NAME,
                ACL,
                MENU
        );
        final String filePath = this.getFixturePath(file.getFileName());
        final IndexActionGenerator generator =
                new IndexActionGenerator(data, myFixture.getProject(), false);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                "src/app/code/Foo/Bar/".concat(file.getDirectory()),
                generator.generate("test")
        );
    }
}
