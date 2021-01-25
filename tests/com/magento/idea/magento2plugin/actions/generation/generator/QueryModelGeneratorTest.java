/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.GetListQueryModelData;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQuery;

public class QueryModelGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/"
            + GetListQuery.DIRECTORY;
    private static final String ENTITY_NAME = "Book";
    private static final String COLLECTION_TYPE = "Foo\\Bar\\Model\\ResourceModel\\"
            + ENTITY_NAME + "\\Collection";
    private static final String ENTITY_DATA_MAPPER_TYPE = "Foo\\Bar\\Mapper\\"
            + ENTITY_NAME + "DataMapper";

    /**
     * Test generation of GetListQuery model for entity.
     */
    public void testGenerateGetListQueryModelFile() {
        final GetListQueryModelData getListQueryModelData = new GetListQueryModelData(
                MODULE_NAME,
                ENTITY_NAME,
                COLLECTION_TYPE,
                ENTITY_DATA_MAPPER_TYPE
        );
        final GetListQueryModelGenerator getListQueryModelGenerator =
                new GetListQueryModelGenerator(
                        getListQueryModelData,
                        myFixture.getProject(),
                        false
                );
        final String filePath = this.getFixturePath(GetListQuery.getInstance().getFileName());

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                getListQueryModelGenerator.generate("test")
        );
    }
}
