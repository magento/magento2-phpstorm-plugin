/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MagentoInitRequireJsReferenceProvider extends PsiReferenceProvider {
    private static final String MAGENTO_INIT_SCRIPT_TYPE = "text/x-magento-init";
    private static final String MAGE_INIT_ATTRIBUTE = "data-mage-init";
    private final PsiReferenceProvider[] providers;

    public MagentoInitRequireJsReferenceProvider(final PsiReferenceProvider... providers) {
        this.providers = providers;
    }

    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        if (!Settings.isEnabled(element.getProject()) || !isMagentoInitJson(element)) {
            return PsiReference.EMPTY_ARRAY;
        }

        final String requireJsPath = getStringValue(element);
        final List<PsiElement> targets = requireJsPath == null
                ? List.of()
                : RequireJsPathResolver.getInstance().resolveJsFilesOrAlias(element.getProject(), requireJsPath);

        if (!targets.isEmpty()) {
            return new PsiReference[] {
                    new PolyVariantReferenceBase(element, getStringValueRange(element), targets)
            };
        }

        final List<PsiReference> result = new ArrayList<>();

        for (final PsiReferenceProvider provider : providers) {
            result.addAll(Arrays.asList(provider.getReferencesByElement(element, context)));
        }

        return result.toArray(PsiReference.EMPTY_ARRAY);
    }

    private String getStringValue(final @NotNull PsiElement element) {
        if (element instanceof JsonStringLiteral) {
            return ((JsonStringLiteral) element).getValue();
        }
        final String text = element.getText();

        if (text.length() >= 2) {
            final char first = text.charAt(0);
            final char last = text.charAt(text.length() - 1);

            if ((first == '\"' && last == '\"') || (first == '\'' && last == '\'')) {
                return text.substring(1, text.length() - 1);
            }
        }

        return text;
    }

    private @NotNull TextRange getStringValueRange(final @NotNull PsiElement element) {
        final String text = element.getText();

        if (text.length() >= 2) {
            final char first = text.charAt(0);
            final char last = text.charAt(text.length() - 1);

            if ((first == '\"' && last == '\"') || (first == '\'' && last == '\'')) {
                return new TextRange(1, text.length() - 1);
            }
        }

        return new TextRange(0, text.length());
    }

    private boolean isMagentoInitJson(final @NotNull PsiElement element) {
        final PsiLanguageInjectionHost host = InjectedLanguageManager
                .getInstance(element.getProject())
                .getInjectionHost(element);

        if (host instanceof XmlText) {
            final PsiElement parent = host.getParent();

            return parent instanceof HtmlTag
                    && MAGENTO_INIT_SCRIPT_TYPE.equals(((HtmlTag) parent).getAttributeValue("type"));
        }

        if (host instanceof XmlAttributeValue) {
            final PsiElement parent = host.getParent();

            return parent instanceof XmlAttribute
                    && MAGE_INIT_ATTRIBUTE.equals(((XmlAttribute) parent).getName());
        }

        return false;
    }
}
