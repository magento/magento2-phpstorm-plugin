/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.magento.idea.magento2plugin.actions.generation.dialog.NewCronjobDialog;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;

import javax.swing.*;

public class NewCronjobValidator {
    private static NewCronjobValidator INSTANCE = null;

    private ValidatorBundle validatorBundle;
    private NewCronjobDialog dialog;

    public static NewCronjobValidator getInstance(NewCronjobDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewCronjobValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public NewCronjobValidator() {
        this.validatorBundle = new ValidatorBundle();
    }

    /**
     * Validate whenever new cronjob dialog data is ready for generation
     *
     * @return boolean
     */
    public boolean validate()
    {
        return true;
    }
}
