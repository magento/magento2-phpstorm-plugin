/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.magento.idea.magento2plugin.magento.files.SourceModelFile;

public class SourceModelData {
    private String className;
    private String moduleName;
    private String directory;

    public String getClassName() {
        return className;
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
            return SourceModelFile.DEFAULT_DIR;
        }

        return this.directory;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    public void setDirectory(final String directory) {
        this.directory = directory;
    }
}
