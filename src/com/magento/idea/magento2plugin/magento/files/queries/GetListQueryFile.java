/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.queries;

import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import java.io.File;
import org.jetbrains.annotations.NotNull;

public final class GetListQueryFile extends AbstractPhpFile {

    public static final String CLASS_NAME = "GetListQuery";
    public static final String HUMAN_READABLE_NAME = "Get list query class";
    public static final String TEMPLATE = "Magento Get List Query Model";
    private static final String DIRECTORY = "Query";
    private final String entityName;

    /**
     * Get list query file constructor.
     *
     * @param moduleName String
     * @param entityName String
     */
    public GetListQueryFile(
            final @NotNull String moduleName,
            final @NotNull String entityName
    ) {
        super(moduleName, CLASS_NAME);
        this.entityName = entityName;
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getDirectory() {
        return DIRECTORY.concat(File.separator).concat(entityName);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
