/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

public class ObserverFileData {
    private String observerDirectory;
    private String observerClassName;
    private String observerModule;
    private String targetEvent;
    private String observerClassFqn;
    private String namespace;

    public ObserverFileData(
            String observerDirectory,
            String observerClassName,
            String observerModule,
            String targetEvent,
            String observerClassFqn,
            String namespace
    ) {
        this.observerDirectory = observerDirectory;
        this.observerClassName = observerClassName;
        this.observerModule = observerModule;
        this.targetEvent = targetEvent;
        this.observerClassFqn = observerClassFqn;
        this.namespace = namespace;
    }

    public String getObserverDirectory() {
        return observerDirectory;
    }

    public String getObserverClassName() {
        return observerClassName;
    }

    public String getObserverModule() {
        return observerModule;
    }

    public String getObserverClassFqn() {
        return observerClassFqn;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getTargetEvent() {
        return targetEvent;
    }
}
