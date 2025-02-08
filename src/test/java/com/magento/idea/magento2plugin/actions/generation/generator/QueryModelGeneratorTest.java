/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.GetListQueryModelData;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQueryFile;

public class QueryModelGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Book";
    private static final String MODEL_NAME = "Book";
    private static final String COLLECTION_NAME = "Collection";
    private static final String ACL = "Foo_Bar::book_management";

    /**
     * Test generation of GetListQuery model for entity.
     */
    public void testGenerateGetListQueryModelFile() {
        final GetListQueryFile file = new GetListQueryFile(MODULE_NAME, ENTITY_NAME);
        final GetListQueryModelData getListQueryModelData = new GetListQueryModelData(
                MODULE_NAME,
                ENTITY_NAME,
                MODEL_NAME,
                COLLECTION_NAME,
                ACL,
                false
        );
        final GetListQueryModelGenerator getListQueryModelGenerator =
                new GetListQueryModelGenerator(
                        getListQueryModelData,
                        myFixture.getProject(),
                        false
                );
        final String filePath = this.getFixturePath(file.getFileName());

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                file.getDirectory(),
                getListQueryModelGenerator.generate("test")
        );
    }
}
