/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.ui.component;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentFormDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

public class FormFieldsValidator {

    private final ValidatorBundle validatorBundle;
    private final NewUiComponentFormDialog dialog;
    private final String errorTitle;

    /**
     * Validator Constructor.
     *
     * @param dialog NewUiComponentFormDialog
     */
    public FormFieldsValidator(final NewUiComponentFormDialog dialog) {
        validatorBundle = new ValidatorBundle();
        errorTitle = new CommonBundle().message("common.error");
        this.dialog = dialog;
    }

    /**
     * Validate field table form fields in the NewUiComponentFormDialog dialog.
     *
     * @return boolean
     */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity"})
    public boolean validate() {
        boolean valid = true;
        final Iterator<UiComponentFormFieldData> fieldsIterator = getFieldsIterator();

        if (fieldsIterator != null) {
            while (fieldsIterator.hasNext()) {
                final UiComponentFormFieldData field = fieldsIterator.next();

                final String name = field.getName();
                if (!NotEmptyRule.getInstance().check(name)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Field Name"));
                    valid = false;
                    break;
                }
                if (!AlphanumericWithUnderscoreRule.getInstance().check(name)) {
                    showErrorMessage(
                            validatorBundle.message(
                                    AlphanumericWithUnderscoreRule.MESSAGE,
                                    "Field Name"
                            )
                    );
                    valid = false;
                    break;
                }
                if (!Lowercase.getInstance().check(name)) {
                    showErrorMessage(validatorBundle.message(Lowercase.MESSAGE, "Field Name"));
                    valid = false;
                    break;
                }

                final String label = field.getLabel();
                if (!NotEmptyRule.getInstance().check(label)) {
                    showErrorMessage(validatorBundle.message(NotEmptyRule.MESSAGE, "Field Label"));
                    valid = false;
                    break;
                }

                final String sortOrder = field.getSortOrder();
                if (!NotEmptyRule.getInstance().check(sortOrder)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, "Field Sort Order")
                    );
                    valid = false;
                    break;
                }
                if (!NumericRule.getInstance().check(sortOrder)) {
                    showErrorMessage(
                            validatorBundle.message(NumericRule.MESSAGE, "Field Sort Order")
                    );
                    valid = false;
                    break;
                }

                final String dataType = field.getDataType();
                if (!NotEmptyRule.getInstance().check(dataType)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, "Field Data Type")
                    );
                    valid = false;
                    break;
                }

                final String source = field.getSource();
                if (!NotEmptyRule.getInstance().check(source)) {
                    showErrorMessage(
                            validatorBundle.message(NotEmptyRule.MESSAGE, "Field Source")
                    );
                    valid = false;
                    break;
                }
                if (!AlphanumericWithUnderscoreRule.getInstance().check(source)) {
                    showErrorMessage(
                            validatorBundle.message(
                                    AlphanumericWithUnderscoreRule.MESSAGE,
                                    "Field Source")
                    );
                    valid = false;
                    break;
                }
                if (!Lowercase.getInstance().check(source)) {
                    showErrorMessage(
                            validatorBundle.message(Lowercase.MESSAGE, "Field Source")
                    );
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    private Iterator<UiComponentFormFieldData> getFieldsIterator() {
        final List<UiComponentFormFieldData> fields = dialog.getFields();
        if (fields.isEmpty()) {
            return null;
        }
        return fields.iterator();
    }

    private void showErrorMessage(final String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }
}
