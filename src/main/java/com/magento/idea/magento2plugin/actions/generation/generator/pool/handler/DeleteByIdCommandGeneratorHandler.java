/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool.handler;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityByIdCommandData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import com.magento.idea.magento2plugin.actions.generation.data.php.WebApiInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.data.xml.WebApiXmlRouteData;
import com.magento.idea.magento2plugin.actions.generation.generator.DeleteEntityByIdCommandGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.php.WebApiInterfaceWithDeclarationGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorRunnerValidator;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import org.jetbrains.annotations.NotNull;

public class DeleteByIdCommandGeneratorHandler extends GeneratorHandler {

    /**
     * Delete by id command generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     */
    public DeleteByIdCommandGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter
    ) {
        this(contextData, dataObjectConverter, null);
    }

    /**
     * Delete by id command generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     * @param runnerValidator GeneratorRunnerValidator
     */
    public DeleteByIdCommandGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter,
            final GeneratorRunnerValidator runnerValidator
    ) {
        super(contextData, dataObjectConverter, runnerValidator);
    }

    @Override
    public void instantiateGenerator() {
        setGenerator(new DeleteEntityByIdCommandGenerator(
                (DeleteEntityByIdCommandData) getDataObjectConverter(),
                getProject()
        ));
    }

    @Override
    protected void afterFileGenerated(final PsiFile file) {
        if (!(file instanceof PhpFile)) {
            return;
        }
        final EntityCreatorContextData contextData = (EntityCreatorContextData) getContextData();
        final DeleteEntityByIdCommandData data =
                (DeleteEntityByIdCommandData) getDataObjectConverter();

        if (contextData.hasWebApi()) {
            final PhpClass serviceClass = GetFirstClassOfFile.getInstance().execute((PhpFile) file);

            if (serviceClass == null) {
                return;
            }
            final DeleteEntityByIdCommandFile fileConfiguration = new DeleteEntityByIdCommandFile(
                    data.getModuleName(),
                    data.getEntityName(),
                    true
            );
            new WebApiInterfaceWithDeclarationGenerator(
                    new WebApiInterfaceData(
                            data.getModuleName(),
                            PhpTypeMetadataParserUtil.getFqn(serviceClass),
                            fileConfiguration.getWebApiInterfaceName(),
                            PhpTypeMetadataParserUtil.getShortDescription(serviceClass),
                            PhpTypeMetadataParserUtil.getMethodsByNames(
                                    serviceClass,
                                    DeleteEntityByIdCommandFile.WEB_API_METHOD_NAME
                            )
                    ),
                    new WebApiXmlRouteData(
                            data.getModuleName(),
                            fileConfiguration.getWebApiUrl(),
                            HttpMethod.DELETE.name(),
                            fileConfiguration.getClassFqn(),
                            DeleteEntityByIdCommandFile.WEB_API_METHOD_NAME,
                            data.getAclResource()
                    ),
                    getProject()
            ).generate(contextData.getActionName(), contextData.checkIfHasOpenFileFlag());
        }
    }
}
