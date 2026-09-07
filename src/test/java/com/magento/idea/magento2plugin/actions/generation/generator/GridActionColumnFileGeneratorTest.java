/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.GridActionColumnData;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.magento.files.GridActionColumnFile;

public class GridActionColumnFileGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Book";
    private static final String ENTITY_ID_COLUMN = "book_id";
    private static final String EDIT_URL_PATH = "book_book_edit";
    private static final String DELETE_URL_PATH = "book_book_delete";
    private static final String ENTITY_DTO_TYPE = "Foo\\Bar\\Model\\Data\\BookData";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final EntityCreatorContext context = new EntityCreatorContext();
        context.putUserData(EntityCreatorContext.DTO_TYPE, ENTITY_DTO_TYPE);
        context.putUserData(EntityCreatorContext.ENTITY_ID, ENTITY_ID_COLUMN);
        GenerationContextRegistry.getInstance().setContext(context);
    }

    /**
     * Test generation of grid actions column file.
     */
    public void testGenerateGridActionColumnFile() {
        final GridActionColumnFile file = new GridActionColumnFile(MODULE_NAME, ENTITY_NAME);
        final GridActionColumnData data = new GridActionColumnData(
                MODULE_NAME,
                ENTITY_NAME,
                ENTITY_ID_COLUMN,
                EDIT_URL_PATH,
                DELETE_URL_PATH
        );
        final String filePath = this.getFixturePath(file.getFileName());
        final GridActionColumnFileGenerator generator =
                new GridActionColumnFileGenerator(data, myFixture.getProject(), false);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                "src/app/code/Foo/Bar/".concat(file.getDirectory()),
                generator.generate("test")
        );
    }
}
