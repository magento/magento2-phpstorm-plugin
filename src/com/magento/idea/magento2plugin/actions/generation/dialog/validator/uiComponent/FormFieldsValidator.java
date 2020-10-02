package com.magento.idea.magento2plugin.actions.generation.dialog.validator.uiComponent;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentFormDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;

public class FormFieldsValidator {
    private final ValidatorBundle validatorBundle;
    private final NewUiComponentFormDialog dialog;
    private final String errorTitle;

    public FormFieldsValidator(final NewUiComponentFormDialog dialog) {
        validatorBundle = new ValidatorBundle();
        errorTitle = (new CommonBundle()).message("common.error");
        this.dialog = dialog;
    }

    public boolean validate() {
        boolean valid = true;
        final Iterator<UiComponentFormFieldData> fieldsIterator = getFieldsIterator();

        if (fieldsIterator != null) {
            while (fieldsIterator.hasNext()) {
                UiComponentFormFieldData field = fieldsIterator.next();

                String name = field.getName();
                if (!NotEmptyRule.getInstance().check(name)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Field Name"));
                    valid = false;
                    break;
                }
                if (!AlphanumericWithUnderscoreRule.getInstance().check(name)) {
                    showErrorMessage(validatorBundle.message(AlphanumericWithUnderscoreRule.MESSAGE, "Field Name"));
                    valid = false;
                    break;
                }
                if (!Lowercase.getInstance().check(name)) {
                    showErrorMessage(validatorBundle.message(Lowercase.MESSAGE, "Field Name"));
                    valid = false;
                    break;
                }

                String label = field.getLabel();
                if (!NotEmptyRule.getInstance().check(label)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Field Label"));
                    valid = false;
                    break;
                }

                String sortOrder = field.getSortOrder();
                if (!NotEmptyRule.getInstance().check(sortOrder)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Field Sort Order"));
                    valid = false;
                    break;
                }
                if (!NumericRule.getInstance().check(sortOrder)) {
                    showErrorMessage(validatorBundle.message(NumericRule.MESSAGE, "Field Sort Order"));
                    valid = false;
                    break;
                }

                String dataType = field.getDataType();
                if (!NotEmptyRule.getInstance().check(dataType)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Field Data Type"));
                    valid = false;
                    break;
                }

                String source = field.getSource();
                if (!NotEmptyRule.getInstance().check(source)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Field Source"));
                    valid = false;
                    break;
                }
                if (!AlphanumericWithUnderscoreRule.getInstance().check(source)) {
                    showErrorMessage(validatorBundle.message(AlphanumericWithUnderscoreRule.MESSAGE, "Field Source"));
                    valid = false;
                    break;
                }
                if (!Lowercase.getInstance().check(source)) {
                    showErrorMessage(validatorBundle.message(Lowercase.MESSAGE, "Field Source"));
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    private Iterator<UiComponentFormFieldData> getFieldsIterator() {
        final List<UiComponentFormFieldData> fields = dialog.getFields();
        if (fields.size() == 0) {
            return null;
        }
        return fields.iterator();
    }

    private void showErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }
}
