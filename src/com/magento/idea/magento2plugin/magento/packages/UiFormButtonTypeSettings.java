/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import java.util.InputMismatchException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public enum UiFormButtonTypeSettings {

    SAVE("Save", "save primary", "''", "[\n"
            + "                'mage-init' => ['button' => ['event' => 'save']],\n"
            + "                'form-role' => 'save'\n"
            + "            ]", 10, "Save entity button."),
    DELETE("Delete", "delete", "'deleteConfirm(\\''\n"
            + "            . __('Are you sure you want to delete this $varName?')\n"
            + "            . '\\', \\'' . $this->getUrl(\n'*/*/delete',\n[$varIdConst => "
            + "$this->$varEntityIdAccessor]\n) . '\\')'", "[]", 20, "Delete entity button."),
    BACK("Back To Grid", "back",
            "sprintf(\"location.href = '%s';\", $this->getUrl('*/*/'))",
            "[]", 30, "Back to list button."),
    CUSTOM("Custom Button", "custom", "''", "[]", 0, "Custom button.");

    private final String label;
    private final String classes;
    private final String onClick;
    private final String dataAttrs;
    private final int sortOrder;
    private final String annotation;

    /**
     * Ui Form button settings Enum.
     *
     * @param label String
     * @param classes String
     * @param onClick String
     * @param dataAttrs String
     * @param sortOrder String
     */
    UiFormButtonTypeSettings(
            final @NotNull String label,
            final @NotNull String classes,
            final @NotNull String onClick,
            final @NotNull String dataAttrs,
            final int sortOrder,
            final String annotation
    ) {
        this.label = label;
        this.classes = classes;
        this.onClick = onClick;
        this.dataAttrs = dataAttrs;
        this.sortOrder = sortOrder;
        this.annotation = annotation;
    }

    /**
     * Get ENUM by its string representation.
     *
     * @param value String
     *
     * @return UiFormButtonTypeSettings
     */
    public static UiFormButtonTypeSettings getByValue(final @NotNull String value) {
        for (final UiFormButtonTypeSettings type : UiFormButtonTypeSettings.values()) {
            if (type.toString().equals(value)) {
                return type;
            }
        }

        throw new InputMismatchException(
                "Invalid property type value provided. Should be compatible with "
                        + UiFormButtonTypeSettings.class
        );
    }

    /**
     * Get button label.
     *
     * @return String
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get button classes.
     *
     * @return String
     */
    public String getClasses() {
        return classes;
    }

    /**
     * Get on click behavior.
     *
     * @param variables Map
     *
     * @return String
     */
    public String getOnClick(final Map<String, String> variables) {
        return parseVariables(onClick, variables);
    }

    /**
     * Get button data attributes.
     *
     * @param variables Map
     *
     * @return String
     */
    public String getDataAttrs(final Map<String, String> variables) {
        return parseVariables(dataAttrs, variables);
    }

    /**
     * Check if specified variable used for current button.
     *
     * @param variableName String
     *
     * @return boolean
     */
    public boolean isVariableExpected(final @NotNull String variableName) {
        return onClick.contains(variableName) || dataAttrs.contains(variableName);
    }

    /**
     * Get button sort order.
     *
     * @return int
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Get button annotation.
     *
     * @return String
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * Parse variables.
     *
     * @param string String
     * @param variables Map
     *
     * @return String
     */
    private String parseVariables(final String string, final Map<String, String> variables) {
        if (variables.isEmpty()) {
            return string;
        }
        String parsed = string;

        for (final Map.Entry<String, String> entry : variables.entrySet()) {
            parsed = parsed.replace(entry.getKey(), entry.getValue());
        }

        return parsed;
    }
}
