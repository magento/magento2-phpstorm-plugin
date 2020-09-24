/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import java.util.ArrayList;

@SuppressWarnings({"PMD.DataClass"})
public class UiComponentFormFileData {
    private final String formName;
    private final String formArea;
    private final String moduleName;
    private final String label;
    private final ArrayList<UiComponentFormButtonData> buttons;
    private final ArrayList<UiComponentFormFieldsetData> fieldsets;
    private final ArrayList<UiComponentFormFieldData> fields;
    private String route;
    private String submitControllerName;
    private String submitActionName;
    private String dataProviderFqn;

    /**
     * UI Form data file constructor.
     *
     * @param formName String
     * @param formArea String
     * @param moduleName String
     * @param buttons ArrayList<UiComponentFormButtonData>
     * @param fieldsets ArrayList<UiComponentFormFieldsetData>
     * @param fields ArrayList<UiComponentFormFieldData>
     */
    public UiComponentFormFileData(
            final String formName,
            final String formArea,
            final String moduleName,
            final String label,
            final ArrayList<UiComponentFormButtonData> buttons,
            final ArrayList<UiComponentFormFieldsetData> fieldsets,
            final ArrayList<UiComponentFormFieldData> fields,
            final String route,
            String submitControllerName,
            String submitActionName,
            String dataProviderFqn
    ) {
        this.formName = formName;
        this.formArea = formArea;
        this.moduleName = moduleName;
        this.label = label;
        this.buttons = buttons;
        this.fieldsets = fieldsets;
        this.fields = fields;
        this.route = route;
        this.submitControllerName = submitControllerName;
        this.submitActionName = submitActionName;
        this.dataProviderFqn = dataProviderFqn;
    }

    /**
     * Get action directory.
     *
     * @return String
     */
    public String getFormName() {
        return formName;
    }

    /**
     * Get controller area.
     *
     * @return String
     */
    public String getFormArea() {
        return formArea;
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
     * Get label.
     *
     * @return String
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get buttons.
     *
     * @return ArrayList<UiComponentFormButtonData>
     */
    public ArrayList<UiComponentFormButtonData> getButtons() {
        return buttons;
    }

    /**
     * Get fieldsets.
     *
     * @return ArrayList<UiComponentFormFieldsetData>
     */
    public ArrayList<UiComponentFormFieldsetData> getFieldsets() {
        return fieldsets;
    }

    /**
     * Get fields.
     *
     * @return ArrayList<UiComponentFormFieldData>
     */
    public ArrayList<UiComponentFormFieldData> getFields() {
        return fields;
    }

    public String getRoute() {
        return route;
    }

    public String getSubmitControllerName() {
        return submitControllerName;
    }

    public String getSubmitActionName() {
        return submitActionName;
    }

    public String getDataProviderFqn() {
        return dataProviderFqn;
    }
}
