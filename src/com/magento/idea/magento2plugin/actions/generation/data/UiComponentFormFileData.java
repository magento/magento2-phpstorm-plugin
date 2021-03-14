/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import java.util.List;

public class UiComponentFormFileData {
    private final String formName;
    private final String formArea;
    private final String moduleName;
    private final String label;
    private final List<UiComponentFormButtonData> buttons;
    private final List<UiComponentFormFieldsetData> fieldsets;
    private final List<UiComponentFormFieldData> fields;
    private final String route;
    private final String submitControllerName;
    private final String submitActionName;
    private final String dataProviderFqn;
    private final String entityName;
    private final String entityId;

    /**
     * UI Form data file constructor.
     *
     * @param formName String
     * @param formArea String
     * @param moduleName String
     * @param buttons List[UiComponentFormButtonData]
     * @param fieldsets List[UiComponentFormFieldsetData]
     * @param fields List[UiComponentFormFieldData]
     * @param route String
     * @param submitControllerName String
     * @param submitActionName String
     * @param dataProviderFqn String
     */
    @SuppressWarnings({"PMD.ExcessiveParameterList"})
    public UiComponentFormFileData(
            final String formName,
            final String formArea,
            final String moduleName,
            final String label,
            final List<UiComponentFormButtonData> buttons,
            final List<UiComponentFormFieldsetData> fieldsets,
            final List<UiComponentFormFieldData> fields,
            final String route,
            final String submitControllerName,
            final String submitActionName,
            final String dataProviderFqn
    ) {
        this(
                formName,
                formArea,
                moduleName,
                label,
                buttons,
                fieldsets,
                fields,
                route,
                submitControllerName,
                submitActionName,
                dataProviderFqn,
                "Entity",
                "entity_id"
        );
    }

    /**
     * UI Form data file constructor.
     *
     * @param formName String
     * @param formArea String
     * @param moduleName String
     * @param buttons List[UiComponentFormButtonData]
     * @param fieldsets List[UiComponentFormFieldsetData]
     * @param fields List[UiComponentFormFieldData]
     * @param route String
     * @param submitControllerName String
     * @param submitActionName String
     * @param dataProviderFqn String
     * @param entityName String
     * @param entityId String
     */
    @SuppressWarnings({"PMD.ExcessiveParameterList"})
    public UiComponentFormFileData(
            final String formName,
            final String formArea,
            final String moduleName,
            final String label,
            final List<UiComponentFormButtonData> buttons,
            final List<UiComponentFormFieldsetData> fieldsets,
            final List<UiComponentFormFieldData> fields,
            final String route,
            final String submitControllerName,
            final String submitActionName,
            final String dataProviderFqn,
            final String entityName,
            final String entityId
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
        this.entityName = entityName;
        this.entityId = entityId;
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
     * @return List[UiComponentFormButtonData]
     */
    public List<UiComponentFormButtonData> getButtons() {
        return buttons;
    }

    /**
     * Get fieldsets.
     *
     * @return List[UiComponentFormFieldsetData]
     */
    public List<UiComponentFormFieldsetData> getFieldsets() {
        return fieldsets;
    }

    /**
     * Get fields.
     *
     * @return List[UiComponentFormFieldData]
     */
    public List<UiComponentFormFieldData> getFields() {
        return fields;
    }

    /**
     * Get route.
     *
     * @return String
     */
    public String getRoute() {
        return route;
    }

    /**
     * Get submit action name.
     *
     * @return String
     */
    public String getSubmitControllerName() {
        return submitControllerName;
    }

    /**
     * Get submit action name.
     *
     * @return String
     */
    public String getSubmitActionName() {
        return submitActionName;
    }

    /**
     * Get data provider FQN.
     *
     * @return String
     */
    public String getDataProviderFqn() {
        return dataProviderFqn;
    }

    /**
     * Get entity name.
     *
     * @return String
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Get entity id.
     *
     * @return String
     */
    public String getEntityId() {
        return entityId;
    }
}
