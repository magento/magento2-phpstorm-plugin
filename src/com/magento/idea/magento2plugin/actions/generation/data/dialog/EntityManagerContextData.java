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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class EntityManagerContextData implements GenerationContextData {

    private final Project project;
    private final String moduleName;
    private final String actionName;
    private final String indexViewAction;
    private final String editViewAction;
    private final String newViewAction;
    private final String deleteAction;
    private final NamespaceBuilder modelNamespaceBuilder;
    private final NamespaceBuilder resourceModelNamespaceBuilder;
    private final NamespaceBuilder collectionModelNamespaceBuilder;
    private final NamespaceBuilder dtoModelNamespaceBuilder;
    private final NamespaceBuilder dtoInterfaceNamespaceBuilder;
    private final NamespaceBuilder finalDtoTypeNamespaceBuilder;
    private final NamespaceBuilder dataProviderNamespaceBuilder;
    private final NamespaceBuilder entityListActionNamespaceBuilder;
    private final NamespaceBuilder entityDataMapperNamespaceBuilder;
    private final NamespaceBuilder saveEntityCommandNamespaceBuilder;
    private final NamespaceBuilder formViewNamespaceBuilder;
    private final NamespaceBuilder newControllerNamespaceBuilder;
    private final NamespaceBuilder saveControllerNamespaceBuilder;
    private final NamespaceBuilder genericButtonBlockNamespaceBuilder;
    private final List<Map<String, String>> entityProps;
    private final List<UiComponentFormButtonData> buttons;
    private final List<UiComponentFormFieldsetData> fieldsetData;
    private final List<UiComponentFormFieldData> fieldsData;

    public EntityManagerContextData(
            final @NotNull Project project,
            final @NotNull String moduleName,
            final @NotNull String actionName,
            final @NotNull String indexViewAction,
            final @NotNull String editViewAction,
            final @NotNull String newViewAction,
            final @NotNull String deleteAction,
            final @NotNull NamespaceBuilder modelNamespaceBuilder,
            final @NotNull NamespaceBuilder resourceModelNamespaceBuilder,
            final @NotNull NamespaceBuilder collectionModelNamespaceBuilder,
            final @NotNull NamespaceBuilder dtoModelNamespaceBuilder,
            final @NotNull NamespaceBuilder dtoInterfaceNamespaceBuilder,
            final @NotNull NamespaceBuilder finalDtoTypeNamespaceBuilder,
            final @NotNull NamespaceBuilder dataProviderNamespaceBuilder,
            final @NotNull NamespaceBuilder entityListActionNamespaceBuilder,
            final @NotNull NamespaceBuilder entityDataMapperNamespaceBuilder,
            final @NotNull NamespaceBuilder saveEntityCommandNamespaceBuilder,
            final @NotNull NamespaceBuilder formViewNamespaceBuilder,
            final @NotNull NamespaceBuilder newControllerNamespaceBuilder,
            final @NotNull NamespaceBuilder saveControllerNamespaceBuilder,
            final @NotNull NamespaceBuilder genericButtonBlockNamespaceBuilder,
            final @NotNull List<Map<String, String>> entityProps,
            final @NotNull List<UiComponentFormButtonData> buttons,
            final @NotNull List<UiComponentFormFieldsetData> fieldsetData,
            final @NotNull List<UiComponentFormFieldData> fieldsData
    ) {
        this.project = project;
        this.moduleName = moduleName;
        this.actionName = actionName;
        this.indexViewAction = indexViewAction;
        this.editViewAction = editViewAction;
        this.newViewAction = newViewAction;
        this.deleteAction = deleteAction;
        this.modelNamespaceBuilder = modelNamespaceBuilder;
        this.resourceModelNamespaceBuilder = resourceModelNamespaceBuilder;
        this.dtoModelNamespaceBuilder = dtoModelNamespaceBuilder;
        this.dtoInterfaceNamespaceBuilder = dtoInterfaceNamespaceBuilder;
        this.collectionModelNamespaceBuilder = collectionModelNamespaceBuilder;
        this.finalDtoTypeNamespaceBuilder = finalDtoTypeNamespaceBuilder;
        this.dataProviderNamespaceBuilder = dataProviderNamespaceBuilder;
        this.entityListActionNamespaceBuilder = entityListActionNamespaceBuilder;
        this.entityDataMapperNamespaceBuilder = entityDataMapperNamespaceBuilder;
        this.saveEntityCommandNamespaceBuilder = saveEntityCommandNamespaceBuilder;
        this.formViewNamespaceBuilder = formViewNamespaceBuilder;
        this.newControllerNamespaceBuilder = newControllerNamespaceBuilder;
        this.saveControllerNamespaceBuilder = saveControllerNamespaceBuilder;
        this.genericButtonBlockNamespaceBuilder = genericButtonBlockNamespaceBuilder;
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

    public String getIndexViewAction() {
        return indexViewAction;
    }

    public String getEditViewAction() {
        return editViewAction;
    }

    public String getNewViewAction() {
        return newViewAction;
    }

    public String getDeleteAction() {
        return deleteAction;
    }

    public NamespaceBuilder getModelNamespaceBuilder() {
        return modelNamespaceBuilder;
    }

    public NamespaceBuilder getResourceModelNamespaceBuilder() {
        return resourceModelNamespaceBuilder;
    }

    public NamespaceBuilder getCollectionModelNamespaceBuilder() {
        return collectionModelNamespaceBuilder;
    }

    public NamespaceBuilder getDtoModelNamespaceBuilder() {
        return dtoModelNamespaceBuilder;
    }

    public NamespaceBuilder getDtoInterfaceNamespaceBuilder() {
        return dtoInterfaceNamespaceBuilder;
    }

    public NamespaceBuilder getFinalDtoTypeNamespaceBuilder() {
        return finalDtoTypeNamespaceBuilder;
    }

    public NamespaceBuilder getDataProviderNamespaceBuilder() {
        return dataProviderNamespaceBuilder;
    }

    public NamespaceBuilder getEntityListActionNamespaceBuilder() {
        return entityListActionNamespaceBuilder;
    }

    public NamespaceBuilder getEntityDataMapperNamespaceBuilder() {
        return entityDataMapperNamespaceBuilder;
    }

    public NamespaceBuilder getSaveEntityCommandNamespaceBuilder() {
        return saveEntityCommandNamespaceBuilder;
    }

    public NamespaceBuilder getFormViewNamespaceBuilder() {
        return formViewNamespaceBuilder;
    }

    public NamespaceBuilder getNewControllerNamespaceBuilder() {
        return newControllerNamespaceBuilder;
    }

    public NamespaceBuilder getSaveControllerNamespaceBuilder() {
        return saveControllerNamespaceBuilder;
    }

    public NamespaceBuilder getGenericButtonBlockNamespaceBuilder() {
        return genericButtonBlockNamespaceBuilder;
    }

    public List<Map<String, String>> getEntityProps() {
        return entityProps;
    }

    public List<UiComponentFormButtonData> getButtons() {
        return buttons;
    }

    public List<UiComponentFormFieldsetData> getFieldsetData() {
        return fieldsetData;
    }

    public List<UiComponentFormFieldData> getFieldsData() {
        return fieldsData;
    }
}
