/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridDataProviderData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewUiComponentGridDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.UiComponentGridDataProviderPhp;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.util.RegExUtil;
import javax.swing.JOptionPane;

@SuppressWarnings({
        "PMD.OnlyOneReturn",
        "PMD.NonThreadSafeSingleton",
        "PMD.LongMethodRule",
        "PMD.ExcessiveMethodLength",
        "PMD.CyclomaticComplexity",
        "PMD.NPathComplexity"
})
public class NewUiComponentGridDialogValidator {
    private static final String DATA_PROVIDER_NAME = "Data Provider Class";
    private static final String NOT_EMPTY = "validator.notEmpty";
    private static NewUiComponentGridDialogValidator instance;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    /**
     * Get instance of a class.
     *
     * @return NewUiComponentGridDialogValidator
     */
    public static NewUiComponentGridDialogValidator getInstance() {
        if (null == instance) {
            instance = new NewUiComponentGridDialogValidator();
        }

        return instance;
    }

    /**
     * New Ui Component Grid validator constructor.
     */
    public NewUiComponentGridDialogValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new Ui Component Grid Dialog data is ready for generation.
     *
     * @param dialog Ui Component Grid Dialog
     * @return boolean
     */
    public boolean validate(final NewUiComponentGridDialog dialog) {
        return this.validateUiComponentData(dialog.getUiComponentGridData())
                && this.validateDataProviderData(dialog.getGridDataProviderData());
    }

    private boolean validateUiComponentData(final UiComponentGridData uiComponentGridData) {
        final String errorTitle = commonBundle.message("common.error");
        final String name = uiComponentGridData.getName();

        if (name.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!name.matches(RegExUtil.IDENTIFIER)) {
            final String errorMessage = validatorBundle.message(
                    "validator.identifier",
                    "Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String idFieldName = uiComponentGridData.getIdFieldName();

        if (idFieldName.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Entity ID field"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!idFieldName.matches(RegExUtil.IDENTIFIER)) {
            final String errorMessage = validatorBundle.message(
                    "validator.identifier",
                    "Entity ID field"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }


        final String area = uiComponentGridData.getArea();

        if (area.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Area"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String acl = uiComponentGridData.getAcl();

        if (area.equals(Areas.adminhtml.toString()) && acl.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Acl"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        return true;
    }

    private boolean validateDataProviderData(final UiComponentGridDataProviderData providerData) {
        final String errorTitle = commonBundle.message("common.error");
        final String type = providerData.getType();

        if (type.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Data Provider Type"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String collection = providerData.getCollectionFqn();

        if (type.equals(UiComponentGridDataProviderPhp.COLLECTION_TYPE)
                && collection.length() == 0
        ) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    "Data Provider Collection"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String name = providerData.getName();

        if (name.length() == 0) {
            final String errorMessage = this.validatorBundle.message(
                    NOT_EMPTY,
                    DATA_PROVIDER_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!PhpNameUtil.isValidClassName(name)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    DATA_PROVIDER_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!name.matches(RegExUtil.ALPHANUMERIC)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    DATA_PROVIDER_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!Character.isUpperCase(name.charAt(0))
                && !Character.isDigit(name.charAt(0))
        ) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    DATA_PROVIDER_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String parentDirectory = providerData.getPath();

        if (parentDirectory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    NOT_EMPTY,
                    "Data Provider Parent Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!parentDirectory.matches(RegExUtil.DIRECTORY)) {
            final String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    "Data Provider Parent Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        return true;
    }
}
