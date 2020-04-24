/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.magento.idea.magento2plugin.magento.packages.Package;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class NamespaceBuilder {
    private String moduleName;
    private String className;
    private String classDirectory;

    public NamespaceBuilder(String moduleName, String className, String classDirectory) {
        this.moduleName = moduleName;
        this.className = className;
        this.classDirectory = classDirectory;
    }

    /**
     * Retrieve Class FQN
     *
     * @return String
     */
    @NotNull
    public String getClassFqn() {
        String classNamespace = this.getCronjobNamespace();

        return classNamespace + Package.FQN_SEPARATOR + this.className;
    }

    /**
     * Retrieve Class Namespace
     *
     * @return String
     */
    public String getCronjobNamespace() {
        String[] parts = this.moduleName.split(Package.VENDOR_MODULE_NAME_SEPARATOR);

        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }

        String directoryPart = this.classDirectory.replace(File.separator, Package.FQN_SEPARATOR);

        return parts[0] + Package.FQN_SEPARATOR + parts[1] + Package.FQN_SEPARATOR + directoryPart;
    }
}
