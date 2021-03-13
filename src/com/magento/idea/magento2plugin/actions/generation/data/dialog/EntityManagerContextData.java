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
public class EntityManagerContextData implements GenerationContextData {

    private final Project project;
    private final String moduleName;
    private final String actionName;
    private final boolean hasOpenFileFlag;
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
    private final NamespaceBuilder deleteEntityByIdCommandNamespaceBuilder;
    private final NamespaceBuilder formViewNamespaceBuilder;
    private final NamespaceBuilder newControllerNamespaceBuilder;
    private final NamespaceBuilder saveControllerNamespaceBuilder;
    private final NamespaceBuilder deleteControllerNamespaceBuilder;
    private final NamespaceBuilder editControllerNamespaceBuilder;
    private final NamespaceBuilder genericButtonBlockNamespaceBuilder;
    private final List<Map<String, String>> entityProps;
    private final List<UiComponentFormButtonData> buttons;
    private final List<UiComponentFormFieldsetData> fieldsetData;
    private final List<UiComponentFormFieldData> fieldsData;

    /**
     * Entity manager context data.
     *
     * @param project Project
     * @param moduleName String
     * @param actionName String
     * @param hasOpenFileFlag boolean
     * @param indexViewAction String
     * @param editViewAction String
     * @param newViewAction String
     * @param deleteAction String
     * @param modelNamespaceBuilder NamespaceBuilder
     * @param resourceModelNamespaceBuilder NamespaceBuilder
     * @param collectionModelNamespaceBuilder NamespaceBuilder
     * @param dtoModelNamespaceBuilder NamespaceBuilder
     * @param dtoInterfaceNamespaceBuilder NamespaceBuilder
     * @param finalDtoTypeNamespaceBuilder NamespaceBuilder
     * @param dataProviderNamespaceBuilder NamespaceBuilder
     * @param entityListActionNamespaceBuilder NamespaceBuilder
     * @param entityDataMapperNamespaceBuilder NamespaceBuilder
     * @param saveEntityCommandNamespaceBuilder NamespaceBuilder
     * @param deleteEntityByIdCommandNamespaceBuilder NamespaceBuilder
     * @param formViewNamespaceBuilder NamespaceBuilder
     * @param newControllerNamespaceBuilder NamespaceBuilder
     * @param saveControllerNamespaceBuilder NamespaceBuilder
     * @param deleteControllerNamespaceBuilder NamespaceBuilder
     * @param editControllerNamespaceBuilder NamespaceBuilder
     * @param genericButtonBlockNamespaceBuilder NamespaceBuilder
     * @param entityProps List
     * @param buttons List
     * @param fieldsetData List
     * @param fieldsData List
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public EntityManagerContextData(
            final @NotNull Project project,
            final @NotNull String moduleName,
            final @NotNull String actionName,
            final boolean hasOpenFileFlag,
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
            final @NotNull NamespaceBuilder deleteEntityByIdCommandNamespaceBuilder,
            final @NotNull NamespaceBuilder formViewNamespaceBuilder,
            final @NotNull NamespaceBuilder newControllerNamespaceBuilder,
            final @NotNull NamespaceBuilder saveControllerNamespaceBuilder,
            final @NotNull NamespaceBuilder deleteControllerNamespaceBuilder,
            final @NotNull NamespaceBuilder editControllerNamespaceBuilder,
            final @NotNull NamespaceBuilder genericButtonBlockNamespaceBuilder,
            final @NotNull List<Map<String, String>> entityProps,
            final @NotNull List<UiComponentFormButtonData> buttons,
            final @NotNull List<UiComponentFormFieldsetData> fieldsetData,
            final @NotNull List<UiComponentFormFieldData> fieldsData
    ) {
        this.project = project;
        this.moduleName = moduleName;
        this.actionName = actionName;
        this.hasOpenFileFlag = hasOpenFileFlag;
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
        this.deleteEntityByIdCommandNamespaceBuilder = deleteEntityByIdCommandNamespaceBuilder;
        this.formViewNamespaceBuilder = formViewNamespaceBuilder;
        this.newControllerNamespaceBuilder = newControllerNamespaceBuilder;
        this.saveControllerNamespaceBuilder = saveControllerNamespaceBuilder;
        this.deleteControllerNamespaceBuilder = deleteControllerNamespaceBuilder;
        this.editControllerNamespaceBuilder = editControllerNamespaceBuilder;
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

    @Override
    public boolean hasOpenFileFlag() {
        return hasOpenFileFlag;
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
     * Get model namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getModelNamespaceBuilder() {
        return modelNamespaceBuilder;
    }

    /**
     * Get resource model namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getResourceModelNamespaceBuilder() {
        return resourceModelNamespaceBuilder;
    }

    /**
     * Get collection model namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getCollectionModelNamespaceBuilder() {
        return collectionModelNamespaceBuilder;
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
     * Get final DTO type namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getFinalDtoTypeNamespaceBuilder() {
        return finalDtoTypeNamespaceBuilder;
    }

    /**
     * Get data provider namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getDataProviderNamespaceBuilder() {
        return dataProviderNamespaceBuilder;
    }

    /**
     * Get entity list action namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getEntityListActionNamespaceBuilder() {
        return entityListActionNamespaceBuilder;
    }

    /**
     * Get entity data mapper namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getEntityDataMapperNamespaceBuilder() {
        return entityDataMapperNamespaceBuilder;
    }

    /**
     * Get save entity command namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getSaveEntityCommandNamespaceBuilder() {
        return saveEntityCommandNamespaceBuilder;
    }

    /**
     * Get delete by id command namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getDeleteEntityByIdCommandNamespaceBuilder() {
        return deleteEntityByIdCommandNamespaceBuilder;
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
     * Get save controller namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getSaveControllerNamespaceBuilder() {
        return saveControllerNamespaceBuilder;
    }

    /**
     * Get delete controller namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getDeleteControllerNamespaceBuilder() {
        return deleteControllerNamespaceBuilder;
    }

    /**
     * Get edit controller namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getEditControllerNamespaceBuilder() {
        return editControllerNamespaceBuilder;
    }

    /**
     * Get generic button block namespace builder.
     *
     * @return NamespaceBuilder
     */
    public NamespaceBuilder getGenericButtonBlockNamespaceBuilder() {
        return genericButtonBlockNamespaceBuilder;
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
