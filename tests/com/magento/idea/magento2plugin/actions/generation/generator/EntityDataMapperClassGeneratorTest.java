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
    private static final String NAMESPACE = "Foo\\Bar\\Mapper";
    private static final String CLASS_FQN = NAMESPACE + "\\UnicornDataMapper";
    private static final String MODEL_CLASS_FQN = "Foo\\Bar\\Model\\UnicornModel";
    private static final String DATA_MODEL_CLASS_FQN = "Foo\\Bar\\Api\\Data\\UnicornDataInterface";

    /**
     * Test generation of entity data mapper class.
     */
    public void testGenerateEntityDataMapperFile() {
        final EntityDataMapperData entityDataMapperData = new EntityDataMapperData(
                MODULE_NAME,
                ENTITY_NAME,
                NAMESPACE,
                CLASS_FQN,
                MODEL_CLASS_FQN,
                DATA_MODEL_CLASS_FQN
        );
        final EntityDataMapperGenerator entityDataMapperGenerator =
                new EntityDataMapperGenerator(
                        entityDataMapperData,
                        myFixture.getProject(),
                        false
                );
        final EntityDataMapperFile entityDataMapperFile = new EntityDataMapperFile(ENTITY_NAME);
        final String filePath = this.getFixturePath(entityDataMapperFile.getFileName());

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                entityDataMapperGenerator.generate("test")
        );
    }
}
