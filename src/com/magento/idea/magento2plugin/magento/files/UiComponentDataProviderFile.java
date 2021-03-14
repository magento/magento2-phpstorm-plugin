/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.FieldNamingConventions",
        "PMD.NonThreadSafeSingleton",
        "PMD.RedundantFieldInitializer"
})
public class UiComponentDataProviderFile implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento UI Component Custom Data Provider Class";
    public static final String DIRECTORY = "Ui/DataProvider";
    public static final String FILE_EXTENSION = "php";
    public static final String CUSTOM_TYPE = "custom";
    public static final String COLLECTION_TYPE = "collection";
    private static UiComponentDataProviderFile INSTANCE = null;
    private String className;
    public static final String DEFAULT_DATA_PROVIDER =
            "Magento\\Framework\\View\\Element\\UiComponent\\DataProvider\\DataProvider";
    public static final String SEARCH_RESULT_FACTORY =
            "Magento\\Ui\\DataProvider\\SearchResultFactory";

    /**
     * Returns a new instance of the class.
     *
     * @param className DataProvider class name
     *
     * @return UiComponentDataProviderFile
     */
    public static UiComponentDataProviderFile getInstance(
            final @NotNull String className
    ) {
        if (null == INSTANCE) {
            INSTANCE = new UiComponentDataProviderFile();
        }

        INSTANCE.setClassName(className);

        return INSTANCE;
    }

    /**
     * Get namespace builder for file.
     *
     * @param moduleName String
     *
     * @return String
     */
    public @NotNull NamespaceBuilder getNamespaceBuilder(
            final @NotNull String moduleName
    ) {
        return new NamespaceBuilder(
                moduleName,
                className,
                DIRECTORY
        );
    }

    /**
     * Set class name.
     *
     * @param className String
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    @Override
    public String getFileName() {
        return String.format("%s.%s", className, FILE_EXTENSION);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
