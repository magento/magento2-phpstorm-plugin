/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.ui.component;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentFormDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

public class FormFieldsetsValidator {

    private final ValidatorBundle validatorBundle;
    private final NewUiComponentFormDialog dialog;
    private final String errorTitle;

    /**
     * Validator Constructor.
     *
     * @param dialog NewUiComponentFormDialog
     */
    public FormFieldsetsValidator(final NewUiComponentFormDialog dialog) {
        validatorBundle = new ValidatorBundle();
        errorTitle = new CommonBundle().message("common.error");
        this.dialog = dialog;
    }

    /**
     * Validate fieldset table form fields in the NewUiComponentFormDialog dialog.
     *
     * @return boolean
     */
    public boolean validate() {
        boolean valid = true;
        final Iterator<UiComponentFormFieldsetData> fieldsetIterator = getFieldsetIterator();

        if (fieldsetIterator != null) {
            while (fieldsetIterator.hasNext()) {
                final UiComponentFormFieldsetData fieldset = fieldsetIterator.next();

                final String name = fieldset.getName();
                if (!NotEmptyRule.getInstance().check(name)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, "Fieldset Name")
                    );
                    valid = false;
                    break;
                }

                final String label = fieldset.getLabel();
                if (!NotEmptyRule.getInstance().check(label)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, "Fieldset Label")
                    );
                    valid = false;
                    break;
                }

                final String sortOrder = fieldset.getSortOrder();
                if (!NotEmptyRule.getInstance().check(sortOrder)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, "Fieldset Sort Order")
                    );
                    valid = false;
                    break;
                }
                if (!NumericRule.getInstance().check(sortOrder)) {
                    showErrorMessage(
                            validatorBundle.message(NumericRule.MESSAGE, "Fieldset Sort Order")
                    );
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    private Iterator<UiComponentFormFieldsetData> getFieldsetIterator() {
        final List<UiComponentFormFieldsetData> fieldsets = dialog.getFieldsets();
        if (fieldsets.isEmpty()) {
            return null;
        }
        return fieldsets.iterator();
    }

    private void showErrorMessage(final String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }
}
