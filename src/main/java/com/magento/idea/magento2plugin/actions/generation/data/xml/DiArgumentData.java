/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.xml;

import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.DiArgumentType;
import org.jetbrains.annotations.NotNull;

public class DiArgumentData {

    private final String moduleName;
    private final String clazz;
    private final String parameter;
    private final Areas area;
    private final DiArgumentType valueType;
    private final String value;

    /**
     * DI argument DTO constructor.
     *
     * @param moduleName String
     * @param clazz String
     * @param parameter String
     * @param area Areas
     * @param valueType DiArgumentType
     * @param value String
     */
    public DiArgumentData(
            final @NotNull String moduleName,
            final @NotNull String clazz,
            final @NotNull String parameter,
            final @NotNull Areas area,
            final @NotNull DiArgumentType valueType,
            final @NotNull String value
    ) {
        this.moduleName = moduleName;
        this.clazz = clazz;
        this.parameter = parameter;
        this.area = area;
        this.valueType = valueType;
        this.value = value;
    }

    /**
     * Get module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get target class.
     *
     * @return String
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Get target parameter.
     *
     * @return String
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * Get target area.
     *
     * @return Areas
     */
    public Areas getArea() {
        return area;
    }

    /**
     * Get argument value xsi:type.
     *
     * @return DiArgumentType
     */
    public DiArgumentType getValueType() {
        return valueType;
    }

    /**
     * Get argument value.
     *
     * @return String
     */
    public String getValue() {
        return value;
    }
}
