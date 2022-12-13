/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.js;

import static com.intellij.patterns.StandardPatterns.string;
import static com.magento.idea.magento2plugin.util.RegExUtil.JsRegex;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.patterns.JSPatterns;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magento.idea.magento2plugin.reference.provider.FilePathReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.ModuleNameReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.RequireJsPreferenceReferenceProvider;
import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;

public class JsReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                JSPatterns.jsLiteralExpression()
                        .withText(string().matches(".*" + RegExUtil.Magento.MODULE_NAME + ".*")),
                new ModuleNameReferenceProvider()
        );

        // Targets property value -> {test: 'Sandbox_Test/js/test'}
        registrar.registerReferenceProvider(
                JSPatterns.jsLiteralExpression()
                        .withText(string().matches(JsRegex.FILE_PATH)),
                new FilePathReferenceProvider()
        );

        // Targets property key (JS:STRING_LITERAL) -> {'Sandbox_Test/js/test': true}
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(JSTokenTypes.STRING_LITERAL)
                        .withText(string().matches(JsRegex.FILE_PATH))
                        .withLanguage(JavascriptLanguage.INSTANCE),
                new FilePathReferenceProvider()
        );

        registrar.registerReferenceProvider(
                JSPatterns.jsLiteralExpression()
                        .withText(string().matches(".*\\W" + RegExUtil.FILE_PATH + ".*")),
                new RequireJsPreferenceReferenceProvider()
        );
    }
}
