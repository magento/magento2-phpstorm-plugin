/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class UiComponentFormButtonData {
    private final String buttonDirectory;
    private final String buttonClassName;
    private final String buttonModule;
    private final String buttonType;
    private final String namespace;
    private final String buttonLabel;
    private final String buttonSortOrder;
    private final String formName;
    private final String fqn;

    /**
     * UI component form button data.
     *
     * @param buttonDirectory String
     * @param buttonClassName String
     * @param buttonModule String
     * @param buttonType String
     * @param namespace String
     * @param buttonLabel String
     * @param buttonSortOrder String
     * @param formName String
     * @param fqn String
     */
    public UiComponentFormButtonData(
            final String buttonDirectory,
            final String buttonClassName,
            final String buttonModule,
            final String buttonType,
            final String namespace,
            final String buttonLabel,
            final String buttonSortOrder,
            final String formName,
            final String fqn
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
