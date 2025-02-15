/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

public class ViewModelFileData {
    private String viewModelDirectory;
    private String viewModelClassName;
    private String viewModelModule;
    private String namespace;

    public ViewModelFileData(
            String viewModelDirectory,
            String viewModelClassName,
            String viewModelModule,
            String namespace
    ) {
        this.viewModelDirectory = viewModelDirectory;
        this.viewModelClassName = viewModelClassName;
        this.viewModelModule = viewModelModule;
        this.namespace = namespace;
    }

    public String getViewModelClassName() {
        return viewModelClassName;
    }

    public String getViewModelDirectory() {
        return viewModelDirectory;
    }

    public String getViewModelModule() {
        return viewModelModule;
    }

    public String getNamespace() {
        return namespace;
    }
}
