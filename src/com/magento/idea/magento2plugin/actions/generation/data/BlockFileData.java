/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.psi.elements.PhpClass;

public class BlockFileData {
    private String blockDirectory;
    private String blockClassName;
    private String blockModule;
    private PhpClass targetClass;
    private String blockFqn;
    private String namespace;
    private boolean inheritClass;

    public BlockFileData(
            String blockDirectory,
            String blockClassName,
            String blockModule,
            PhpClass targetClass,
            String blockFqn,
            String namespace,
            boolean inheritClass
    ) {
        this.blockDirectory = blockDirectory;
        this.blockClassName = blockClassName;
        this.blockModule = blockModule;
        this.targetClass = targetClass;
        this.blockFqn = blockFqn;
        this.namespace = namespace;
        this.inheritClass = inheritClass;
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

    public PhpClass getTargetClass() {
        return targetClass;
    }

    public String getBlockFqn() {
        return blockFqn;
    }

    public String getNamespace() {
        return namespace;
    }
}
