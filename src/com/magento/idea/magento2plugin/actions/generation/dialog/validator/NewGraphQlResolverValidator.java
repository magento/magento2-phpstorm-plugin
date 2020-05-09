/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewGraphQlResolverDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;
import javax.swing.JOptionPane;

@SuppressWarnings({
        "PMD.OnlyOneReturn",
        "PMD.FieldNamingConventions",
        "PMD.DataflowAnomalyAnalysis",
        "PMD.NonThreadSafeSingleton",
        "PMD.NPathComplexity"
})
public class NewGraphQlResolverValidator {
    private static final String GRAPHQL_RESOLVER_NAME = "GraphQL Resolver Name";
    private static NewGraphQlResolverValidator INSTANCE;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private NewGraphQlResolverDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog New GraphQl resolver dialog
     *
     * @return NewGraphQlResolverValidator
     */
    public static NewGraphQlResolverValidator getInstance(final NewGraphQlResolverDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new NewGraphQlResolverValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    /**
     * New Graph Ql Resolver validator constructor.
     */
    public NewGraphQlResolverValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new graph Ql resolver dialog data is ready for generation.
     *
     * @return Boolean
     */
    public boolean validate() {
        final String errorTitle = commonBundle.message("common.error");
        final String resolverClassName = dialog.getGraphQlResolverClassName();

        if (!PhpNameUtil.isValidClassName(resolverClassName)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    GRAPHQL_RESOLVER_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (resolverClassName.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    GRAPHQL_RESOLVER_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!resolverClassName.matches(RegExUtil.ALPHANUMERIC)) {
            final String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    GRAPHQL_RESOLVER_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!Character.isUpperCase(resolverClassName.charAt(0))
                && !Character.isDigit(resolverClassName.charAt(0))
        ) {
            final String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    GRAPHQL_RESOLVER_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String graphQlResolverDirectory = dialog.getGraphQlResolverDirectory();
        if (graphQlResolverDirectory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "GraphQL Resolver Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!graphQlResolverDirectory.matches(RegExUtil.DIRECTORY)) {
            final String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    "GraphQL Resolver Directory"
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
