/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool;

import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class GeneratorPoolHandler {

    private final GenerationContextData contextData;
    private final List<GeneratorHandler> pool;

    /**
     * Generator pool handler constructor.
     *
     * @param contextData GenerationContextData
     */
    public GeneratorPoolHandler(final @NotNull GenerationContextData contextData) {
        this.contextData = contextData;
        pool = new LinkedList<>();
    }

    /**
     * Add next generation handler.
     *
     * @param next GeneratorHandler
     *
     * @return GeneratorPoolHandler
     */
    public GeneratorPoolHandler addNext(final @NotNull GeneratorHandler next) {
        pool.add(next);
        return this;
    }

    /**
     * Add next generation handler by its type with provided data adapter.
     *
     * @param handlerClass Class
     * @param dataObjectConverter DataObjectConverter
     *
     * @return GeneratorPoolHandler
     */
    public <T extends GeneratorHandler> GeneratorPoolHandler addNext(
            final @NotNull Class<T> handlerClass,
            final @NotNull DataObjectConverter dataObjectConverter
    ) {
        return addNext(handlerClass, dataObjectConverter, null);
    }

    /**
     * Add next generation handler by its type with provided data adapter.
     *
     * @param handlerClass Class
     * @param dataObjectConverter DataAdapter
     * @param runnerValidator GeneratorRunnerValidator
     *
     * @return GeneratorPoolHandler
     */
    public <T extends GeneratorHandler> GeneratorPoolHandler addNext(
            final @NotNull Class<T> handlerClass,
            final @NotNull DataObjectConverter dataObjectConverter,
            final GeneratorRunnerValidator runnerValidator
    ) {
        try {
            if (runnerValidator == null) {
                final Constructor<T> handlerClassConstructor = handlerClass.getConstructor(
                        GenerationContextData.class,
                        DataObjectConverter.class
                );
                pool.add(handlerClassConstructor
                        .newInstance(contextData, dataObjectConverter));
            } else {
                final Constructor<T> handlerClassConstructor = handlerClass.getConstructor(
                        GenerationContextData.class,
                        DataObjectConverter.class,
                        GeneratorRunnerValidator.class
                );
                pool.add(handlerClassConstructor
                        .newInstance(contextData, dataObjectConverter, runnerValidator));
            }

        } catch (Exception exception) {
            //
        }

        return this;
    }

    /**
     * Trigger generation process.
     */
    public void run() {
        for (final GeneratorHandler handler : pool) {
            if (handler.validate()) {
                handler.generate();
            }
        }
    }
}
