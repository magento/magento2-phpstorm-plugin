/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool.handler;

import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import com.magento.idea.magento2plugin.actions.generation.generator.LayoutXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorRunnerValidator;
import org.jetbrains.annotations.NotNull;

public class FormLayoutGeneratorHandler extends GeneratorHandler {

    /**
     * Form layout generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     */
    public FormLayoutGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter
    ) {
        super(contextData, dataObjectConverter, null);
    }

    /**
     * Form layout generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     * @param generatorRunnerValidator GeneratorRunnerValidator
     */
    public FormLayoutGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter,
            final GeneratorRunnerValidator generatorRunnerValidator
    ) {
        super(contextData, dataObjectConverter, generatorRunnerValidator);
    }

    @Override
    public void instantiateGenerator() {
        setGenerator(new LayoutXmlGenerator(
                (LayoutXmlData) getDataObjectConverter(),
                getProject()
        ));
    }
}
