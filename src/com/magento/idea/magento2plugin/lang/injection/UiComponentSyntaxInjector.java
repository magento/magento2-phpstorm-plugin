/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.lang.injection;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.xml.XmlText;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UiComponentSyntaxInjector implements MultiHostInjector {

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Override
    public void getLanguagesToInject(
            final @NotNull MultiHostRegistrar registrar,
            final @NotNull PsiElement host
    ) {
        if (!Settings.isEnabled(host.getProject())) {
            return;
        }

        if (!isUiComponentTag(host)) {
            return;
        }
        PsiElement targetXmlText = null;

        for (final PsiElement element : host.getChildren()) {
            if (element instanceof XmlText) {
                targetXmlText = element;
                break;
            }
        }

        if (targetXmlText == null) {
            return;
        }
        registrar.startInjecting(JsonLanguage.INSTANCE);
        int startPosition = 0;

        for (final PsiElement element : targetXmlText.getChildren()) {
            if (!(element instanceof OuterLanguageElement)) {
                final int endPosition = Math.min(
                        startPosition + element.getTextLength(),
                        targetXmlText.getTextLength()
                );

                if (startPosition > element.getStartOffsetInParent()) {
                    startPosition = element.getStartOffsetInParent();
                }
                registrar.addPlace(
                        null,
                        null,
                        (PsiLanguageInjectionHost) targetXmlText,
                        new TextRange(startPosition, endPosition)
                );
            }
            startPosition += element.getTextLength();
        }
        registrar.doneInjecting();
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return List.of(HtmlTag.class);
    }

    private boolean isUiComponentTag(final @NotNull PsiElement host) {
        if (!(host instanceof HtmlTag)) {
            return false;
        }
        final HtmlTag tag = (HtmlTag) host;
        final String typeAttributeValue = tag.getAttributeValue("type");

        return typeAttributeValue != null && typeAttributeValue.equals("text/x-magento-init");
    }
}
