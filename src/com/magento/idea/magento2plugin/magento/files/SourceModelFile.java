/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class SourceModelFile extends AbstractPhpFile {
    public static final String HUMAN_READABLE_NAME = "Source model class";
    public static final String TEMPLATE = "Magento Source Model Class";
    public static final String DEFAULT_DIR = "Model/Source";
    private String directory;

    /**
     * Constructor.
     *
     * @param className String
     */
    public SourceModelFile(
            @NotNull final String moduleName,
            @NotNull final String className
    ) {
        super(moduleName, className);
    }

    /**
     * Constructor.
     *
     * @param className String
     */
    public SourceModelFile(
            @NotNull final String moduleName,
            @NotNull final String className,
            final String directory
    ) {
        this(moduleName, className);
        this.directory = directory;
    }

    @Override
    public String getDirectory() {
        if (directory == null) {
            return DEFAULT_DIR;
        }

        return directory;
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
