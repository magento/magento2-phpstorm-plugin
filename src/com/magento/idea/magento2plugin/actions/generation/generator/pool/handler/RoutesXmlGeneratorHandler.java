/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool.handler;

import com.magento.idea.magento2plugin.actions.generation.data.RoutesXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import com.magento.idea.magento2plugin.actions.generation.generator.RoutesXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorRunnerValidator;
import org.jetbrains.annotations.NotNull;

public class RoutesXmlGeneratorHandler extends GeneratorHandler {

    /**
     * Routes XML generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     */
    public RoutesXmlGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter
    ) {
        this(contextData, dataObjectConverter, null);
    }

    /**
     * Routes XML generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     * @param runnerValidator GeneratorRunnerValidator
     */
    public RoutesXmlGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter,
            final GeneratorRunnerValidator runnerValidator
    ) {
        super(contextData, dataObjectConverter, runnerValidator);
    }

    @Override
    public void instantiateGenerator() {
        setGenerator(new RoutesXmlGenerator(
                (RoutesXmlData) getDataObjectConverter(),
                getProject()
        ));
    }
}
