/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.commands;

import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.WebApiInterfaceFile;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import org.jetbrains.annotations.NotNull;

public class DeleteEntityByIdCommandFile extends AbstractPhpFile {

    public static final String CLASS_NAME = "DeleteByIdCommand";
    public static final String HUMAN_READABLE_NAME = "Delete entity by id command class";
    public static final String TEMPLATE = "Magento Delete Entity By Id Command";
    public static final String WEB_API_METHOD_NAME = "execute";
    private static final String DIRECTORY = "Command";
    private static final String WEB_API_INTERFACE_NAME_PATTERN = "Delete%entityName%ByIdInterface";
    private static final String WEB_API_URL_PATTERN = "%entityName%/:entityId";
    private final String entityName;
    private final boolean hasWebApiInterface;

    /**
     * Delete entity by id command file constructor.
     *
     * @param moduleName String
     * @param entityName String
     */
    public DeleteEntityByIdCommandFile(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        this(moduleName, entityName, false);
    }

    /**
     * Delete entity by id command file constructor.
     *
     * @param moduleName String
     * @param entityName String
     * @param hasWebApiInterface boolean
     */
    public DeleteEntityByIdCommandFile(
            final @NotNull String moduleName,
            final @NotNull String entityName,
            final boolean hasWebApiInterface
    ) {
        super(moduleName, CLASS_NAME);
        this.entityName = entityName;
        this.hasWebApiInterface = hasWebApiInterface;
    }

    /**
     * Get Web API interface name.
     *
     * @return String
     */
    public String getWebApiInterfaceName() {
        return WEB_API_INTERFACE_NAME_PATTERN.replace("%entityName%", entityName);
    }

    /**
     * Get Web API url.
     *
     * @return String
     */
    public String getWebApiUrl() {
        return WEB_API_URL_PATTERN.replace(
                "%entityName%",
                CamelCaseToHyphen.getInstance().convert(entityName)
        );
    }

    @Override
    public @NotNull NamespaceBuilder getNamespaceBuilder() {
        if (hasWebApiInterface) {
            final WebApiInterfaceFile interfaceFile = new WebApiInterfaceFile(
                    getModuleName(),
                    getWebApiInterfaceName()
            );
            return interfaceFile.getNamespaceBuilder();
        }
        return super.getNamespaceBuilder();
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getDirectory() {
        return DIRECTORY.concat("/" + entityName);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
