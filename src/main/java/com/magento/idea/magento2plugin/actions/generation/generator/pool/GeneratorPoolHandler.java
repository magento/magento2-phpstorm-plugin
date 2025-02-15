/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool;

import com.intellij.openapi.diagnostic.Logger;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import com.magento.idea.magento2plugin.bundles.ExceptionBundle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public final class GeneratorPoolHandler {

    private static final Logger LOGGER = Logger.getInstance(GeneratorPoolHandler.class);
    private final GenerationContextData contextData;
    private final List<GeneratorHandler> pool;
    private final ExceptionBundle exceptionBundle;
    private final List<String> errors;

    /**
     * Generator pool handler constructor.
     *
     * @param contextData GenerationContextData
     */
    public GeneratorPoolHandler(final @NotNull GenerationContextData contextData) {
        this.contextData = contextData;
        pool = new LinkedList<>();
        exceptionBundle = new ExceptionBundle();
        errors = new LinkedList<>();
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
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException exception) {
            LOGGER.error(exception.getMessage());
            errors.add(this.exceptionBundle.message(
                    "exception.reflection.cannotInstantiate",
                    handlerClass.getSimpleName()
            ));
        } catch (NoSuchMethodException noSuchMethodException) {
            // This error can be caused if Handler class realization
            // doesn't have constructor with all required arguments.
            final String constructorDescWithTwoArgs = handlerClass.getSimpleName()
                    + " with arguments " + GenerationContextData.class.getSimpleName()
                    + ", " + DataObjectConverter.class.getSimpleName();
            final String constructorDescWithThreeArgs = handlerClass.getSimpleName()
                    + " with arguments " + GenerationContextData.class.getSimpleName() + ", "
                    + DataObjectConverter.class.getSimpleName() + ", "
                    + GeneratorRunnerValidator.class.getSimpleName();

            LOGGER.error(noSuchMethodException.getMessage());
            errors.add(this.exceptionBundle.message(
                    "exception.NoSuchMethod",
                    runnerValidator == null ? constructorDescWithTwoArgs
                            : constructorDescWithThreeArgs,
                    handlerClass.getSimpleName()
            ));
        }

        return this;
    }

    /**
     * Trigger generation process.
     */
    public void run() {
        if (hasErrorMessages()) {
            showErrorMessages();
            return;
        }
        for (final GeneratorHandler handler : pool) {
            handler.instantiateGenerator();

            if (handler.validate()) {
                handler.generate();
            }
        }
    }

    /**
     * Instantiate all generators.
     */
    public void instantiateAllGenerators() {
        for (final GeneratorHandler handler : pool) {
            handler.instantiateGenerator();
        }
    }

    /**
     * Check if Generator Pool Handler has any error messages.
     *
     * @return boolean
     */
    public boolean hasErrorMessages() {
        return !errors.isEmpty();
    }

    /**
     * Show error messages for the user.
     */
    private void showErrorMessages() {
        if (!errors.isEmpty()) {
            final String title = this.exceptionBundle.message(
                    "exception.common.title"
            );
            final String errorMessage = this.exceptionBundle.message(
                    "exception.common.informUs"
            );

            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    title,
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
