/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool.handler;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.GetListQueryModelData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.GenerationContextData;
import com.magento.idea.magento2plugin.actions.generation.data.php.WebApiInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.data.xml.WebApiXmlRouteData;
import com.magento.idea.magento2plugin.actions.generation.generator.GetListQueryModelGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.php.WebApiInterfaceWithDeclarationGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorRunnerValidator;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQueryFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import org.jetbrains.annotations.NotNull;

public class GetListQueryGeneratorHandler extends GeneratorHandler {

    /**
     * Get list query generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     */
    public GetListQueryGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter
    ) {
        this(contextData, dataObjectConverter, null);
    }

    /**
     * Get list query generator handler.
     *
     * @param contextData GenerationContextData
     * @param dataObjectConverter DataObjectConverter
     * @param runnerValidator GeneratorRunnerValidator
     */
    public GetListQueryGeneratorHandler(
            final @NotNull GenerationContextData contextData,
            final @NotNull DataObjectConverter dataObjectConverter,
            final GeneratorRunnerValidator runnerValidator
    ) {
        super(contextData, dataObjectConverter, runnerValidator);
    }

    @Override
    public void instantiateGenerator() {
        setGenerator(new GetListQueryModelGenerator(
                (GetListQueryModelData) getDataObjectConverter(),
                getProject()
        ));
    }

    @Override
    protected void afterFileGenerated(final PsiFile file) {
        if (!(file instanceof PhpFile)) {
            return;
        }
        final EntityCreatorContextData contextData = (EntityCreatorContextData) getContextData();
        final GetListQueryModelData data = (GetListQueryModelData) getDataObjectConverter();

        if (contextData.hasWebApi()) {
            final PhpClass serviceClass = GetFirstClassOfFile.getInstance().execute((PhpFile) file);

            if (serviceClass == null) {
                return;
            }
            final GetListQueryFile fileConfiguration = new GetListQueryFile(
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
                                    GetListQueryFile.WEB_API_METHOD_NAME
                            )
                    ),
                    new WebApiXmlRouteData(
                            data.getModuleName(),
                            fileConfiguration.getWebApiUrl(),
                            HttpMethod.GET.name(),
                            fileConfiguration.getClassFqn(),
                            GetListQueryFile.WEB_API_METHOD_NAME,
                            data.getAclResource()
                    ),
                    getProject()
            ).generate(contextData.getActionName(), contextData.checkIfHasOpenFileFlag());
        }
    }
}
