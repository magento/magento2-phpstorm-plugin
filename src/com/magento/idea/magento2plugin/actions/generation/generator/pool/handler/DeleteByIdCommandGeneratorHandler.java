/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool.handler;

import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityByIdCommandData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import com.magento.idea.magento2plugin.actions.generation.generator.DeleteEntityByIdCommandGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorRunnerValidator;
import org.jetbrains.annotations.NotNull;

public class DeleteByIdCommandGeneratorHandler extends GeneratorHandler {

    /**
     * Delete by id command generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     */
    public DeleteByIdCommandGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter
    ) {
        this(contextData, dataObjectConverter, null);
    }

    /**
     * Delete by id command generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     * @param runnerValidator GeneratorRunnerValidator
     */
    public DeleteByIdCommandGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter,
            final GeneratorRunnerValidator runnerValidator
    ) {
        super(contextData, dataObjectConverter, runnerValidator);
    }

    @Override
    protected void generate() {
        new DeleteEntityByIdCommandGenerator(
                (DeleteEntityByIdCommandData) getDataObjectConverter(),
                getProject()
        ).generate(getContextData().getActionName(), true);
    }
}
