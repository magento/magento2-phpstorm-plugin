/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.actions.generation.generator.util.eav.AttributeMapperFactory;
import com.magento.idea.magento2plugin.actions.generation.generator.util.eav.AttributeMapperInterface;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.EavAttributeDataPatchFile;
import com.magento.idea.magento2plugin.magento.packages.eav.DataPatchDependency;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class EavAttributeSetupPatchGenerator extends PhpFileGenerator {

    private final EavEntityDataInterface data;
    protected final PhpClassTypesBuilder phpClassTypesBuilder;

    /**
     * Constructor.
     *
     * @param data    EavEntityDataInterface
     * @param project Project
     */
    public EavAttributeSetupPatchGenerator(
            final @NotNull EavEntityDataInterface data,
            final Project project
    ) {
        this(data, project, true);
    }

    /**
     * Php file generator constructor.
     *
     * @param project                Project
     * @param checkFileAlreadyExists boolean
     */
    public EavAttributeSetupPatchGenerator(
            final @NotNull EavEntityDataInterface data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
        this.phpClassTypesBuilder = new PhpClassTypesBuilder();
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new EavAttributeDataPatchFile(data.getModuleName(), data.getDataPatchName());
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        phpClassTypesBuilder
                .appendProperty("CLASS_NAME", data.getDataPatchName())
                .appendProperty("NAMESPACE", this.getFile().getNamespace())
                .appendProperty("ENTITY_CLASS", data.getEntityClass())
                .appendProperty("ATTRIBUTE_CODE", data.getCode())
                .appendProperty("ATTRIBUTE_SET", String.join("->", getAttributesList(data)))
                .append(
                        "IMPLEMENTS",
                        DataPatchDependency.DATA_PATCH_INTERFACE.getClassPatch()
                )
                .append(
                        "ENTITY_CLASS",
                        data.getEntityClass()
                )
                .append(
                        "MODULE_DATA_SETUP_INTERFACE",
                        DataPatchDependency.MODULE_DATA_SETUP_INTERFACE.getClassPatch()
                )
                .append(
                        "EAV_SETUP_FACTORY",
                        DataPatchDependency.EAV_SETUP_FACTORY.getClassPatch()
                )
                .append(
                        "EAV_SETUP",
                        DataPatchDependency.ENV_SETUP.getClassPatch()
                )
                .mergeProperties(attributes);
        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }

    private List<String> getAttributesList(final EavEntityDataInterface eavEntityData) {
        final AttributeMapperFactory attributeMapperFactory = new AttributeMapperFactory();
        final AttributeMapperInterface attributeMapper = attributeMapperFactory.createByEntityClass(
                eavEntityData.getEntityClass()
        );

        return attributeMapper.mapAttributesByEntityData(eavEntityData);
    }
}
