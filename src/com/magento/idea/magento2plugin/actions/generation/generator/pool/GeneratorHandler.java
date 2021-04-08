/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import com.magento.idea.magento2plugin.actions.generation.generator.FileGenerator;
import org.jetbrains.annotations.NotNull;

public abstract class GeneratorHandler {

    private final GenerationContextData contextData;
    private final DataObjectConverter dataObjectConverter;
    private final GeneratorRunnerValidator runnerValidator;
    private FileGenerator generator;

    /**
     * Generator handler constructor.
     *
     * @param contextData Context
     * @param dataObjectConverter DataObjectConverter
     */
    public GeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter
    ) {
        this(contextData, dataObjectConverter, null);
    }

    /**
     * Generator handler constructor.
     *
     * @param contextData Context
     * @param dataObjectConverter DataObjectConverter
     * @param runnerValidator GeneratorRunnerValidator
     */
    public GeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter,
            final GeneratorRunnerValidator runnerValidator
    ) {
        this.contextData = contextData;
        this.dataObjectConverter = dataObjectConverter;
        this.runnerValidator = runnerValidator;
    }

    /**
     * Override this method to add conditions for generation.
     *
     * @return boolean
     */
    public boolean validate() {
        if (runnerValidator != null) {
            return runnerValidator.validate();
        }
        return true;
    }

    /**
     * Instantiate and retrieve generator.
     * Must be separated from generate method to test converter type casting.
     */
    public abstract void instantiateGenerator();

    /**
     * Set generator.
     *
     * @param generator FileGenerator
     */
    public void setGenerator(final FileGenerator generator) {
        this.generator = generator;
    }

    /**
     * Run generator.
     */
    public final void generate() {
        generator.generate(
                getContextData().getActionName(),
                getContextData().checkIfHasOpenFileFlag()
        );
    }

    /**
     * Get project.
     *
     * @return Project
     */
    protected Project getProject() {
        return getContextData().getProject();
    }

    /**
     * Get generation context DTO.
     *
     * @return GenerationContextData
     */
    protected GenerationContextData getContextData() {
        return contextData;
    }

    /**
     * Get data object converter.
     *
     * @return DataObjectConverter
     */
    protected DataObjectConverter getDataObjectConverter() {
        return dataObjectConverter;
    }
}
