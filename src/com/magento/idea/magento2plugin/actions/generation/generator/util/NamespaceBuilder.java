/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.magento.idea.magento2plugin.magento.packages.Package;
import java.io.File;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.OnlyOneReturn"})
public class NamespaceBuilder {
    private final String moduleName;
    private final String className;
    private final String classDirectory;

    /**
     * Build PHP class namespace based on the module name, class name and directory.
     *
     * @param moduleName Module name
     * @param className Class name
     * @param classDirectory Class directory
     */
    public NamespaceBuilder(
            final String moduleName,
            final String className,
            final String classDirectory
    ) {
        this.moduleName = moduleName;
        this.className = className;
        this.classDirectory = classDirectory;
    }

    /**
     * Retrieve Class FQN.
     *
     * @return String
     */
    @NotNull
    public String getClassFqn() {
        final String classNamespace = this.getNamespace();

        return classNamespace + Package.FQN_SEPARATOR + this.className;
    }

    /**
     * Retrieve Class Namespace.
     *
     * @return String
     */
    public String getNamespace() {
        final String[] parts = this.moduleName.split(Package.VENDOR_MODULE_NAME_SEPARATOR);

        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }

        final String directoryPart = this.classDirectory.replace(
                File.separator,
                Package.FQN_SEPARATOR
        );

        return parts[0] + Package.FQN_SEPARATOR + parts[1] + Package.FQN_SEPARATOR + directoryPart;
    }
}
