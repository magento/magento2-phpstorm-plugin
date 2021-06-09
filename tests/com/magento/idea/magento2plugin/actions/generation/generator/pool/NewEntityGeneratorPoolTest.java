/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool;

import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.actions.generation.generator.BaseGeneratorTestCase;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.provider.NewEntityGeneratorsProviderUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import java.util.ArrayList;

public class NewEntityGeneratorPoolTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ACTION_NAME = "Test";
    private static final String INDEX_PATH = "index";
    private static final String EDIT_PATH = "edit";
    private static final String NEW_PATH = "new";
    private static final String DELETE_PATH = "delete";

    /**
     * Test if generator pool handler instantiated without any error.
     */
    public void testGeneratorsInPoolInstantiating() {
        final NewEntityDialogData dialogData = getMockNewEntityDialogData();
        final EntityCreatorContextData contextData = getMockContext();
        final GeneratorPoolHandler generatorPoolHandler = new GeneratorPoolHandler(contextData);

        NewEntityGeneratorsProviderUtil.initializeGenerators(
                generatorPoolHandler,
                contextData,
                dialogData
        );

        assertFalse(
                "There are errors during generators instantiating.",
                generatorPoolHandler.hasErrorMessages()
        );
    }

    /**
     * Test if generators handlers doesn't have any DTO converter type errors.
     */
    public void testGeneratorsInPoolOnCorrectDtoConverterTypes() {
        final NewEntityDialogData dialogData = getMockNewEntityDialogData();
        final EntityCreatorContextData contextData = getMockContext();
        final GeneratorPoolHandler generatorPoolHandler = new GeneratorPoolHandler(contextData);

        NewEntityGeneratorsProviderUtil.initializeGenerators(
                generatorPoolHandler,
                contextData,
                dialogData
        );

        try {
            generatorPoolHandler.instantiateAllGenerators();
        } catch (ClassCastException exception) {
            fail("Wrong type provided for generator: " + exception.getMessage());
        }
    }

    /**
     * Get mocked new entity dialog data.
     *
     * @return NewEntityDialogData
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private NewEntityDialogData getMockNewEntityDialogData() {
        return new NewEntityDialogData(
                "test",
                "test",
                "test",
                "test",
                "test",
                true,
                true,
                false,
                "test",
                "test",
                "test",
                "test",
                true,
                true,
                true,
                true,
                true,
                true,
                "test",
                "test",
                "test",
                "test",
                10,
                "test",
                "test",
                "test"
        );
    }

    /**
     * Get mocked entity creator context data.
     *
     * @return EntityCreatorContextData
     */
    private EntityCreatorContextData getMockContext() {
        final NamespaceBuilder mockNamespace = new NamespaceBuilder(
                MODULE_NAME,
                "MockClass",
                "Mock/Directory"
        );

        return new EntityCreatorContextData(
                this.myFixture.getProject(),
                MODULE_NAME,
                ACTION_NAME,
                false,
                false,
                INDEX_PATH,
                EDIT_PATH,
                NEW_PATH,
                DELETE_PATH,
                mockNamespace,
                mockNamespace,
                mockNamespace,
                mockNamespace,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }
}
