package com.magento.idea.magento2plugin.actions.generation.dialog.validator.uiComponent;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentFormDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.*;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;

public class FormFieldsetsValidator {
    private final ValidatorBundle validatorBundle;
    private final NewUiComponentFormDialog dialog;
    private final String errorTitle;

    public FormFieldsetsValidator(final NewUiComponentFormDialog dialog) {
        validatorBundle = new ValidatorBundle();
        errorTitle = (new CommonBundle()).message("common.error");
        this.dialog = dialog;
    }

    public boolean validate() {
        boolean valid = true;
        final Iterator<UiComponentFormFieldsetData> fieldsetIterator = getFieldsetIterator();

        if (fieldsetIterator != null) {
            while (fieldsetIterator.hasNext()) {
                UiComponentFormFieldsetData fieldset = fieldsetIterator.next();

                String label = fieldset.getLabel();
                if (!NotEmptyRule.getInstance().check(label)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Fieldset Label"));
                    valid = false;
                    break;
                }

                String sortOrder = fieldset.getSortOrder();
                if (!NotEmptyRule.getInstance().check(sortOrder)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Fieldset Sort Order"));
                    valid = false;
                    break;
                }
                if (!NumericRule.getInstance().check(sortOrder)) {
                    showErrorMessage(validatorBundle.message(NumericRule.MESSAGE, "Fieldset Sort Order"));
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    private Iterator<UiComponentFormFieldsetData> getFieldsetIterator() {
        final List<UiComponentFormFieldsetData> fieldsets = dialog.getFieldsets();
        if (fieldsets.size() == 0) {
            return null;
        }
        return fieldsets.iterator();
    }

    private void showErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }
}
