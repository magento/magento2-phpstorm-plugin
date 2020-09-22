/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class UiComponentFormButtonData {
    private String buttonDirectory;
    private String buttonClassName;
    private String buttonModule;
    private String buttonType;
    private String namespace;
    private String buttonLabel;
    private String buttonSortOrder;
    private String formName;
    private String fqn;

    public UiComponentFormButtonData(
            String buttonDirectory,
            String buttonClassName,
            String buttonModule,
            String buttonType,
            String namespace,
            String buttonLabel,
            String buttonSortOrder,
            String formName,
            String fqn
    ) {
        this.buttonDirectory = buttonDirectory;
        this.buttonClassName = buttonClassName;
        this.buttonModule = buttonModule;
        this.buttonType = buttonType;
        this.namespace = namespace;
        this.buttonLabel = buttonLabel;
        this.buttonSortOrder = buttonSortOrder;
        this.formName = formName;
        this.fqn = fqn;
    }

    public String getButtonClassName() {
        return buttonClassName;
    }

    public String getButtonDirectory() {
        return buttonDirectory;
    }

    public String getButtonModule() {
        return buttonModule;
    }

    public String getButtonType() {
        return buttonType;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getButtonSortOrder() {
        return buttonSortOrder;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public String getFormName() {
        return formName;
    }

    public String getFqn() {
        return fqn;
    }
}
