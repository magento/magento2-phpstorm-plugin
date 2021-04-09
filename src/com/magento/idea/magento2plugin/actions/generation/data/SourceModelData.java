/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.magento.idea.magento2plugin.magento.files.SourceModelPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;

public class SourceModelData {
    private String className;
    private String namespace;
    private String moduleName;
    private String directory;

    public String getClassName() {
        return className;
    }

    /**
     * Constructor.
     */
    public String getNamespace() {
        if (namespace == null) {
            namespace = getDefaultSourceModelNamespace();
        }

        return namespace;
    }

    /**
     * Provides default namespace.
     *
     * @return String
     */
    public String getDefaultSourceModelNamespace() {
        final String[] parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        final String directoryPart = getDirectory().replace(
                File.separator,
                Package.fqnSeparator
        );
        return parts[0] + Package.fqnSeparator + parts[1] + Package.fqnSeparator + directoryPart;
    }

    /**
     * Get default namespace.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get directory path.
     *
     * @return String
     */
    public String getDirectory() {
        if (this.directory == null) {
            return SourceModelPhp.DEFAULT_DIR;
        }

        return this.directory;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    public void setDirectory(final String directory) {
        this.directory = directory;
    }
}
