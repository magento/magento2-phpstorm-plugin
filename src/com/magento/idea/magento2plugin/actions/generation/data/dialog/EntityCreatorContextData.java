/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.dialog;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PMD.TooManyFields")
public class EntityCreatorContextData implements GenerationContextData {

    private final Project project;
    private final String moduleName;
    private final String actionName;
    private final boolean hasOpenFileFlag;
    private final boolean webApi;
    private final String indexViewAction;
    private final String editViewAction;
    private final String newViewAction;
    private final String deleteAction;
    private final NamespaceBuilder dtoModelNamespaceBuilder;
    private final NamespaceBuilder dtoInterfaceNamespaceBuilder;
    private final NamespaceBuilder formViewNamespaceBuilder;
    private final NamespaceBuilder newControllerNamespaceBuilder;
    private final List<Map<String, String>> entityProps;
    private final List<UiComponentFormButtonData> buttons;
    private final List<UiComponentFormFieldsetData> fieldsetData;
    private final List<UiComponentFormFieldData> fieldsData;

    /**
     * Entity creator context data.
     *
     * @param project Project
     * @param moduleName String
     * @param actionName String
     * @param hasOpenFileFlag boolean
     * @param hasWebApi boolean
     * @param indexViewAction String
     * @param editViewAction String
     * @param newViewAction String
     * @param deleteAction String
     * @param dtoModelNamespaceBuilder NamespaceBuilder
     * @param dtoInterfaceNamespaceBuilder NamespaceBuilder
     * @param formViewNamespaceBuilder NamespaceBuilder
     * @param newControllerNamespaceBuilder NamespaceBuilder
     * @param entityProps List
     * @param buttons List
     * @param fieldsetData List
     * @param fieldsData List
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public EntityCreatorContextData(
            final @NotNull Project project,
            final @NotNull String moduleName,
            final @NotNull String actionName,
            final boolean hasOpenFileFlag,
            final boolean hasWebApi,
            final @NotNull String indexViewAction,
            final @NotNull String editViewAction,
            final @NotNull String newViewAction,
            final @NotNull String deleteAction,
            final @NotNull NamespaceBuilder dtoModelNamespaceBuilder,
            final @NotNull NamespaceBuilder dtoInterfaceNamespaceBuilder,
            final @NotNull NamespaceBuilder formViewNamespaceBuilder,
            final @NotNull NamespaceBuilder newControllerNamespaceBuilder,
            final @NotNull List<Map<String, String>> entityProps,
            final @NotNull List<UiComponentFormButtonData> buttons,
            final @NotNull List<UiComponentFormFieldsetData> fieldsetData,
            final @NotNull List<UiComponentFormFieldData> fieldsData
    ) {
        this.project = project;
        this.moduleName = moduleName;
        this.actionName = actionName;
        this.hasOpenFileFlag = hasOpenFileFlag;
        this.webApi = hasWebApi;
        this.indexViewAction = indexViewAction;
        this.editViewAction = editViewAction;
        this.newViewAction = newViewAction;
        this.deleteAction = deleteAction;
        this.dtoModelNamespaceBuilder = dtoModelNamespaceBuilder;
        this.dtoInterfaceNamespaceBuilder = dtoInterfaceNamespaceBuilder;
        this.formViewNamespaceBuilder = formViewNamespaceBuilder;
        this.newControllerNamespaceBuilder = newControllerNamespaceBuilder;
        this.entityProps = entityProps;
        this.buttons = buttons;
        this.fieldsetData = fieldsetData;
        this.fieldsData = fieldsData;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getActionName() {
        return actionName;
    }

    @Override
    public boolean checkIfHasOpenFileFlag() {
        return hasOpenFileFlag;
    }

    /**
     * Check if entity creator should generate Web API for entity management services.
     *
     * @return boolean
     */
    public boolean hasWebApi() {
        return webApi;
    }

    /**
     * Get index action.
     *
     * @return String
     */
    public String getIndexViewAction() {
        return indexViewAction;
    }

    /**
     * Get edit action.
     *
     * @return String
     */
    public String getEditViewAction() {
        return editViewAction;
    }

    /**
     * Get new action.
     *
     * @return String
     */
    public String getNewViewAction() {
        return newViewAction;
    }

    /**
     * Get delete action.
     *
     * @return String
     */
    public String getDeleteAction() {
        return deleteAction;
    }

    /**
     * Get DTO model namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getDtoModelNamespaceBuilder() {
        return dtoModelNamespaceBuilder;
    }

    /**
     * Get DTO interface namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getDtoInterfaceNamespaceBuilder() {
        return dtoInterfaceNamespaceBuilder;
    }

    /**
     * Get form view namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getFormViewNamespaceBuilder() {
        return formViewNamespaceBuilder;
    }

    /**
     * Get new controller namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getNewControllerNamespaceBuilder() {
        return newControllerNamespaceBuilder;
    }

    /**
     * Get entity properties.
     *
     * @return List
     */
    public List<Map<String, String>> getEntityProps() {
        return entityProps;
    }

    /**
     * Get buttons.
     *
     * @return List
     */
    public List<UiComponentFormButtonData> getButtons() {
        return buttons;
    }

    /**
     * Get field sets data.
     *
     * @return List
     */
    public List<UiComponentFormFieldsetData> getFieldsetData() {
        return fieldsetData;
    }

    /**
     * Get fields data.
     *
     * @return List
     */
    public List<UiComponentFormFieldData> getFieldsData() {
        return fieldsData;
    }
}
