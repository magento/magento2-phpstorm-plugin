/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.highlighting;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagentoInitJsonErrorFilter extends HighlightErrorFilter implements HighlightInfoFilter {
    private static final String MAGENTO_INIT_SCRIPT_TYPE = "text/x-magento-init";
    private static final String MAGE_INIT_ATTRIBUTE = "data-mage-init";

    @Override
    public boolean shouldHighlightErrorElement(final @NotNull PsiErrorElement element) {
        final PsiFile injectedFile = element.getContainingFile();

        if (!isInjectedJsonFile(injectedFile)) {
            return true;
        }
        final PsiLanguageInjectionHost host = InjectedLanguageManager
                .getInstance(element.getProject())
                .getInjectionHost(element);

        return !shouldSuppressMagentoInitPhpJsonNoise(host);
    }

    @Override
    public boolean accept(final @NotNull HighlightInfo highlightInfo, final PsiFile file) {
        if (!highlightInfo.isFromInjection() || !HighlightSeverity.ERROR.equals(highlightInfo.getSeverity())) {
            return true;
        }
        final PsiLanguageInjectionHost host = findInjectedJsonHost(file, highlightInfo.getStartOffset());

        return !shouldSuppressMagentoInitPhpJsonNoise(host);
    }

    private @Nullable PsiLanguageInjectionHost findInjectedJsonHost(
            final @NotNull PsiFile file,
            final int offset
    ) {
        final InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(file.getProject());
        final PsiElement element = findElementAt(file, offset);

        if (element != null && isInjectedJsonFile(element.getContainingFile())) {
            final PsiLanguageInjectionHost host = injectedLanguageManager.getInjectionHost(element);

            if (host != null) {
                return host;
            }
        }
        final PsiElement injectedElement = injectedLanguageManager.findInjectedElementAt(file, offset);

        if (injectedElement != null && isInjectedJsonFile(injectedElement.getContainingFile())) {
            return injectedLanguageManager.getInjectionHost(injectedElement);
        }

        return null;
    }

    private @Nullable PsiElement findElementAt(final @NotNull PsiFile file, final int offset) {
        if (file.getTextLength() == 0) {
            return null;
        }

        return file.findElementAt(Math.max(0, Math.min(offset, file.getTextLength() - 1)));
    }

    private boolean isInjectedJsonFile(final @Nullable PsiFile file) {
        return file != null && file.getLanguage().isKindOf(JsonLanguage.INSTANCE);
    }

    private boolean shouldSuppressMagentoInitPhpJsonNoise(final @Nullable PsiLanguageInjectionHost host) {
        return isMagentoInitHost(host) && isPhtmlHost(host) && host.getText().contains("<?");
    }

    private boolean isMagentoInitHost(final @Nullable PsiLanguageInjectionHost host) {
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

    private boolean isPhtmlHost(final @Nullable PsiLanguageInjectionHost host) {
        return host != null
                && host.getContainingFile() != null
                && host.getContainingFile().getVirtualFile() != null
                && "phtml".equals(host.getContainingFile().getVirtualFile().getExtension());
    }
}
