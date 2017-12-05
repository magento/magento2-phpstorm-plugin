package com.magento.idea.magento2plugin.reference.js;

import com.intellij.lang.javascript.patterns.JSPatterns;
import com.intellij.psi.*;
import com.magento.idea.magento2plugin.reference.provider.FilePathReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.ModuleNameReferenceProvider;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.StandardPatterns.string;

public class JsReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                JSPatterns.jsLiteralExpression().withText(string().matches(".*[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+.*")),
                new ModuleNameReferenceProvider()
        );

        registrar.registerReferenceProvider(
                JSPatterns.jsLiteralExpression().withText(string().matches(".*\\W([\\w-]+/)*[\\w\\.-]+.*")),
                new FilePathReferenceProvider()
        );
    }
}
