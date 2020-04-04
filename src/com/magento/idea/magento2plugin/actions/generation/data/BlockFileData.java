/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

public class BlockFileData {
    private String blockDirectory;
    private String blockClassName;
    private String blockModule;
    private String namespace;

    public BlockFileData(
            String blockDirectory,
            String blockClassName,
            String blockModule,
            String namespace
    ) {
        this.blockDirectory = blockDirectory;
        this.blockClassName = blockClassName;
        this.blockModule = blockModule;
        this.namespace = namespace;
    }

    public String getBlockClassName() {
        return blockClassName;
    }

    public String getBlockDirectory() {
        return blockDirectory;
    }

    public String getBlockModule() {
        return blockModule;
    }

    public String getNamespace() {
        return namespace;
    }
}
