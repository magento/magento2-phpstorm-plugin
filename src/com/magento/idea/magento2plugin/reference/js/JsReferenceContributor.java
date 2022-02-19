/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.js;

import static com.intellij.patterns.StandardPatterns.string;

import com.intellij.lang.javascript.patterns.JSPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magento.idea.magento2plugin.reference.provider.FilePathReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.ModuleNameReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.RequireJsPreferenceReferenceProvider;
import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;

public class JsReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                JSPatterns.jsLiteralExpression()
                        .withText(string().matches(".*" + RegExUtil.Magento.MODULE_NAME + ".*")),
                new ModuleNameReferenceProvider()
        );

        registrar.registerReferenceProvider(
                JSPatterns.jsLiteralExpression().withText(
                        string().matches(".*\\W" + RegExUtil.FILE_PATH + ".*")
                ),
                new FilePathReferenceProvider()
        );

        registrar.registerReferenceProvider(
                JSPatterns.jsLiteralExpression().withText(
                        string().matches(".*\\W" + RegExUtil.FILE_PATH + ".*")
                ),
                new RequireJsPreferenceReferenceProvider()
        );
    }
}
