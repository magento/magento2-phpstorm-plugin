/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool.handler;

import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import com.magento.idea.magento2plugin.actions.generation.generator.DbSchemaWhitelistJsonGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorRunnerValidator;
import org.jetbrains.annotations.NotNull;

public class DbSchemaWhitelistGeneratorHandler extends GeneratorHandler {

    /**
     * Db schema whitelist generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     */
    public DbSchemaWhitelistGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter
    ) {
        this(contextData, dataObjectConverter, null);
    }

    /**
     * Db schema whitelist generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     * @param runnerValidator GeneratorRunnerValidator
     */
    public DbSchemaWhitelistGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter,
            final GeneratorRunnerValidator runnerValidator
    ) {
        super(contextData, dataObjectConverter, runnerValidator);
    }

    @Override
    public void instantiateGenerator() {
        setGenerator(new DbSchemaWhitelistJsonGenerator(
                getProject(),
                (DbSchemaXmlData) getDataObjectConverter(),
                getContextData().getModuleName()
        ));
    }
}
