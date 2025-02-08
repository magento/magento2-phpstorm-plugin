/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

public class CronjobClassData {
    private String directory;
    private String className;
    private String namespace;
    private String moduleName;

    /**
     * @param cronjobClassName
     * @param cronjobDirectory
     * @param cronjobNamespace
     * @param cronjobModule
     */
    public CronjobClassData(
        String cronjobClassName,
        String cronjobDirectory,
        String cronjobNamespace,
        String cronjobModule
    ) {
        this.className = cronjobClassName;
        this.directory = cronjobDirectory;
        this.namespace = cronjobNamespace;
        this.moduleName = cronjobModule;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
