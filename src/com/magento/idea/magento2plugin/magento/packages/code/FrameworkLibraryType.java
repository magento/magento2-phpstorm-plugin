/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.code;

import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import org.jetbrains.annotations.NotNull;

public enum FrameworkLibraryType {
    API_SEARCH_CRITERIA_BUILDER("Magento\\Framework\\Api\\Search\\SearchCriteriaBuilder"),
    ABSTRACT_COLLECTION(
            "Magento\\Framework\\Model\\ResourceModel\\Db\\Collection\\AbstractCollection"
    ),
    COMPONENT_REGISTRAR("Magento\\Framework\\Component\\ComponentRegistrar"),
    COLLECTION_PROCESSOR("Magento\\Framework\\Api\\SearchCriteria\\CollectionProcessorInterface"),
    DATA_PERSISTOR("Magento\\Framework\\App\\Request\\DataPersistorInterface"),
    DATA_OBJECT("Magento\\Framework\\DataObject"),
    FILTER_BUILDER("Magento\\Framework\\Api\\FilterBuilder"),
    RESULT_INTERFACE("Magento\\Framework\\Controller\\ResultInterface"),
    RESULT_FACTORY("Magento\\Framework\\Controller\\ResultFactory"),
    RESPONSE_INTERFACE("Magento\\Framework\\App\\ResponseInterface"),
    LOGGER("Psr\\Log\\LoggerInterface"),
    REPORTING("Magento\\Framework\\Api\\Search\\ReportingInterface"),
    REQUEST("Magento\\Framework\\App\\RequestInterface"),
    SEARCH_CRITERIA("Magento\\Framework\\Api\\SearchCriteriaInterface"),
    SEARCH_CRITERIA_BUILDER("Magento\\Framework\\Api\\SearchCriteriaBuilder"),
    SEARCH_RESULT("Magento\\Framework\\Api\\SearchResultsInterface"),
    SEARCH_RESULT_IMPLEMENTATION("Magento\\Framework\\Api\\SearchResults"),
    URL("Magento\\Framework\\UrlInterface");

    /**
     * Factory type suffix.
     */
    private static final String FACTORY_SUFFIX = "Factory";

    /**
     * Framework Library type.
     */
    private final String type;

    /**
     * Framework Library type ENUM constructor.
     *
     * @param type String
     */
    FrameworkLibraryType(final @NotNull String type) {
        this.type = type;
    }

    /**
     * Get type.
     *
     * @return String
     */
    public @NotNull String getType() {
        return type;
    }

    /**
     * Get name from type FQN.
     *
     * @return String
     */
    public @NotNull String getTypeName() {
        return PhpClassGeneratorUtil.getNameFromFqn(getType());
    }

    /**
     * Get factory type for type.
     *
     * @return String
     */
    public @NotNull String getFactory() {
        return type.concat(FACTORY_SUFFIX);
    }

    /**
     * Get factory type name from factory type FQN.
     *
     * @return String
     */
    public @NotNull String getFactoryName() {
        return PhpClassGeneratorUtil.getNameFromFqn(getFactory());
    }
}
