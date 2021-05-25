/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class PhpClassTypesBuilder {

    private final List<PhpClassTypeData> types;
    private final Properties properties;

    /**
     * Php class types builder.
     */
    public PhpClassTypesBuilder() {
        types = new ArrayList<>();
        properties = new Properties();
    }

    /**
     * Append property.
     *
     * @param propertyName String
     * @param propertyValue String
     *
     * @return PhpClassTypesBuilder
     */
    public PhpClassTypesBuilder appendProperty(
            final @NotNull String propertyName,
            final @NotNull String propertyValue
    ) {
        properties.setProperty(propertyName, propertyValue);
        return this;
    }

    /**
     * Append property or type.
     *
     * @param propertyName String
     * @param propertyValue String
     *
     * @return PhpClassTypesBuilder
     */
    public PhpClassTypesBuilder append(
            final @NotNull String propertyName,
            final @NotNull String propertyValue
    ) {
        return append(propertyName, propertyValue, true);
    }

    /**
     * Append property or type.
     *
     * @param propertyName String
     * @param propertyValue String
     * @param isAddToImports boolean
     *
     * @return PhpClassTypesBuilder
     */
    public PhpClassTypesBuilder append(
            final @NotNull String propertyName,
            final @NotNull String propertyValue,
            final boolean isAddToImports
    ) {
        if (isAddToImports && PhpClassGeneratorUtil.isValidFqn(propertyValue)) {
            return append(propertyName, propertyValue, null);
        }

        return appendProperty(propertyName, propertyValue);
    }

    /**
     * Append type.
     *
     * @param propertyName String
     * @param typeFqn String
     * @param alias String
     *
     * @return PhpClassTypesBuilder
     */
    public PhpClassTypesBuilder append(
            final @NotNull String propertyName,
            final @NotNull String typeFqn,
            final String alias
    ) {
        final PhpClassTypeData type = new PhpClassTypeData(
                typeFqn,
                PhpClassGeneratorUtil.getNameFromFqn(typeFqn),
                propertyName,
                alias
        );
        types.add(type);

        String propName;

        if (type.getAlias() == null) {
            propName = type.getName();
        } else {
            propName = hasDuplicatedName(type) ? type.getAlias() : type.getName();
        }

        properties.setProperty(type.getPropertyName(), propName);
        return this;
    }

    /**
     * Merge properties.
     *
     * @param properties Properties
     */
    public void mergeProperties(final @NotNull Properties properties) {
        final Set<Map.Entry<Object, Object>> entrySet = this.properties.entrySet();

        for (final Map.Entry<Object, Object> map : entrySet) {
            properties.setProperty(map.getKey().toString(), map.getValue().toString());
        }
    }

    /**
     * Get uses list for php class.
     *
     * @return List[String]
     */
    public List<String> getUses() {
        final List<String> uses = new ArrayList<>();

        for (final PhpClassTypeData type : types) {
            if (type.getFqnWithAlias() == null) {
                uses.add(type.getFqn());
            } else {
                uses.add(hasDuplicatedName(type) ? type.getFqnWithAlias() : type.getFqn());
            }
        }

        return uses;
    }

    /**
     * Check if any property exists.
     *
     * @return boolean
     */
    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    /**
     * Check if property exists.
     *
     * @param propertyName String
     *
     * @return boolean
     */
    public boolean hasProperty(final @NotNull String propertyName) {
        return properties.containsKey(propertyName);
    }

    /**
     * Check if there is any duplicated type name.
     *
     * @param typeData PhpClassTypeData
     *
     * @return boolean
     */
    private boolean hasDuplicatedName(final @NotNull PhpClassTypeData typeData) {
        for (final PhpClassTypeData type : types) {
            if (!typeData.equals(type) && typeData.getName().equals(type.getName())) {
                return true;
            }
        }

        return hasDuplicatesInProps(typeData);
    }

    /**
     * Check if there is any duplicate in properties.
     *
     * @param typeData PhpClassTypeData
     *
     * @return boolean
     */
    private boolean hasDuplicatesInProps(final @NotNull PhpClassTypeData typeData) {
        final Set<Map.Entry<Object, Object>> propertiesEntrySet = this.properties.entrySet();

        for (final Map.Entry<Object, Object> propMapEntry : propertiesEntrySet) {
            if (typeData.getPropertyName().equals(propMapEntry.getKey().toString())) {
                continue;
            }
            if (typeData.getName().equals(propMapEntry.getValue().toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Php class type DTO.
     */
    public static final class PhpClassTypeData {

        private static final String ALIAS_FORMAT = "%fqn as %alias";
        private final String fqn;
        private final String name;
        private final String propertyName;
        private final String alias;

        /**
         * Type data constructor.
         *
         * @param fqn String
         * @param name String
         * @param propertyName String
         * @param alias String
         */
        public PhpClassTypeData(
                final @NotNull String fqn,
                final @NotNull String name,
                final @NotNull String propertyName,
                final String alias
        ) {
            this.fqn = fqn;
            this.name = name;
            this.propertyName = propertyName;
            this.alias = alias;
        }

        /**
         * Get type FQN.
         *
         * @return String
         */
        public String getFqn() {
            return fqn;
        }

        /**
         * Get type FQN.
         *
         * @return String
         */
        public String getFqnWithAlias() {
            if (getAlias() == null) {
                return null;
            }

            return ALIAS_FORMAT.replace("%fqn", getFqn()).replace("%alias", getAlias());
        }

        /**
         * Get type name.
         *
         * @return String
         */
        public String getName() {
            return name;
        }

        /**
         * Get property name.
         *
         * @return String
         */
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * Get type alias.
         *
         * @return String
         */
        public String getAlias() {
            return alias;
        }
    }
}
