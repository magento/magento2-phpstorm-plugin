/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.EntityDataMapperData;
import com.magento.idea.magento2plugin.magento.files.EntityDataMapperFile;

public class EntityDataMapperClassGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Mapper";
    private static final String ENTITY_NAME = "Unicorn";
    private static final String MODEL_NAME = "UnicornModel";
    private static final String DTO_NAME = "UnicornData";
    private static final String DTO_INTERFACE_NAME = "UnicornDataInterface";
    private static final boolean DTO_HAS_INTERFACE = true;

    /**
     * Test generation of entity data mapper class.
     */
    public void testGenerateEntityDataMapperFile() {
        final EntityDataMapperData entityDataMapperData = new EntityDataMapperData(
                MODULE_NAME,
                ENTITY_NAME,
                MODEL_NAME,
                DTO_NAME,
                DTO_INTERFACE_NAME,
                DTO_HAS_INTERFACE
        );
        final EntityDataMapperGenerator entityDataMapperGenerator =
                new EntityDataMapperGenerator(
                        entityDataMapperData,
                        myFixture.getProject(),
                        false
                );
        final EntityDataMapperFile entityDataMapperFile =
                new EntityDataMapperFile(MODULE_NAME, ENTITY_NAME);
        final String filePath = this.getFixturePath(entityDataMapperFile.getFileName());

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                entityDataMapperGenerator.generate("test")
        );
    }
}
