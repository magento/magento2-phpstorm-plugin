/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool.provider;

import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.AclXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.CollectionModelDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DataModelDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DataModelInterfaceDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DataProviderDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DbSchemaXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DeleteEntityByIdCommandDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.EntityDataMapperDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormDeleteControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormEditControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormGenericButtonBlockDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormLayoutDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormSaveControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.GetListQueryDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.GridActionColumnDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.GridLayoutXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.IndexActionDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.MenuXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.ModelDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.NewControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.NewEntityLayoutDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.PreferenceDiXmlFileDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.ResourceModelDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.RoutesXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.SaveEntityCommandDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.SearchResultsDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.UiComponentFormLayoutDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.UiComponentGridDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorPoolHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.AclXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.CollectionModelGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DataModelGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DataModelInterfaceGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DataModelPreferenceGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DataProviderGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DbSchemaWhitelistGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DbSchemaXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DeleteByIdCommandGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.EntityDataMapperGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormDeleteControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormEditControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormGenericButtonBlockGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormLayoutGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormSaveControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.GetListQueryGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.GridActionColumnGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.GridLayoutXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.IndexActionGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.MenuXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.ModelGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.NewControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.NewEntityLayoutGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.ResourceModelGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.RoutesXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.SaveCommandGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.SearchResultsGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.SearchResultsInterfaceGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.UiComponentFormLayoutGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.UiComponentGridGeneratorHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.CouplingBetweenObjects",
        "PMD.ExcessiveImports"
})
public final class NewEntityGeneratorsProviderUtil {

    private NewEntityGeneratorsProviderUtil() {}

    /**
     * Generators initializing excluded from dialog to better test coverage.
     *
     * @param generatorPoolHandler GeneratorPoolHandler
     * @param context EntityCreatorContextData
     * @param dialogData NewEntityDialogData
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void initializeGenerators(
            final @NotNull GeneratorPoolHandler generatorPoolHandler,
            final @NotNull EntityCreatorContextData context,
            final @NotNull NewEntityDialogData dialogData
    ) {
        generatorPoolHandler
                .addNext(
                        ModelGeneratorHandler.class,
                        new ModelDtoConverter(context, dialogData)
                )
                .addNext(
                        ResourceModelGeneratorHandler.class,
                        new ResourceModelDtoConverter(context, dialogData)
                )
                .addNext(
                        CollectionModelGeneratorHandler.class,
                        new CollectionModelDtoConverter(context, dialogData)
                )
                .addNext(
                        DataModelGeneratorHandler.class,
                        new DataModelDtoConverter(context, dialogData)
                )
                .addNext(
                        DataModelInterfaceGeneratorHandler.class,
                        new DataModelInterfaceDtoConverter(context, dialogData),
                        dialogData::hasDtoInterface
                )
                .addNext(
                        DataModelPreferenceGeneratorHandler.class,
                        new PreferenceDiXmlFileDtoConverter(context, dialogData),
                        dialogData::hasDtoInterface
                )
                .addNext(
                        RoutesXmlGeneratorHandler.class,
                        new RoutesXmlDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        AclXmlGeneratorHandler.class,
                        new AclXmlDtoConverter(context, dialogData)
                )
                .addNext(
                        MenuXmlGeneratorHandler.class,
                        new MenuXmlDtoConverter(context, dialogData)
                )
                .addNext(
                        IndexActionGeneratorHandler.class,
                        new IndexActionDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        GridLayoutXmlGeneratorHandler.class,
                        new GridLayoutXmlDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        EntityDataMapperGeneratorHandler.class,
                        new EntityDataMapperDtoConverter(context, dialogData)
                )
                .addNext(
                        GetListQueryGeneratorHandler.class,
                        new GetListQueryDtoConverter(context, dialogData)
                )
                .addNext(
                        DataProviderGeneratorHandler.class,
                        new DataProviderDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        GridActionColumnGeneratorHandler.class,
                        new GridActionColumnDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        UiComponentGridGeneratorHandler.class,
                        new UiComponentGridDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormLayoutGeneratorHandler.class,
                        new FormLayoutDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        NewEntityLayoutGeneratorHandler.class,
                        new NewEntityLayoutDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        SaveCommandGeneratorHandler.class,
                        new SaveEntityCommandDtoConverter(context, dialogData)
                )
                .addNext(
                        DeleteByIdCommandGeneratorHandler.class,
                        new DeleteEntityByIdCommandDtoConverter(context, dialogData)
                )
                .addNext(
                        FormSaveControllerGeneratorHandler.class,
                        new FormSaveControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormDeleteControllerGeneratorHandler.class,
                        new FormDeleteControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormEditControllerGeneratorHandler.class,
                        new FormEditControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormGenericButtonBlockGeneratorHandler.class,
                        new FormGenericButtonBlockDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        NewControllerGeneratorHandler.class,
                        new NewControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        UiComponentFormLayoutGeneratorHandler.class,
                        new UiComponentFormLayoutDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        DbSchemaXmlGeneratorHandler.class,
                        new DbSchemaXmlDtoConverter(context, dialogData)
                )
                .addNext(
                        DbSchemaWhitelistGeneratorHandler.class,
                        new DbSchemaXmlDtoConverter(context, dialogData)
                )
                .addNext(
                        SearchResultsInterfaceGeneratorHandler.class,
                        new SearchResultsDtoConverter(context, dialogData),
                        dialogData::hasWebApi
                )
                .addNext(
                        SearchResultsGeneratorHandler.class,
                        new SearchResultsDtoConverter(context, dialogData),
                        dialogData::hasWebApi
                );
    }
}
