/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagentoInitHostRequireJsReferenceProvider extends PsiReferenceProvider {
    private static final String MAGENTO_INIT_SCRIPT_TYPE = "text/x-magento-init";
    private static final String MAGE_INIT_ATTRIBUTE = "data-mage-init";
    private static final Pattern JSON_OBJECT_KEY_PATTERN = Pattern.compile("(['\"])([^'\"]+)\\1\\s*:");

    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        if (!Settings.isEnabled(element.getProject())) {
            return PsiReference.EMPTY_ARRAY;
        }
        final PsiElement host = getMagentoInitHost(element);

        if (host == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        final List<PsiReference> references = new ArrayList<>();
        final Matcher matcher = JSON_OBJECT_KEY_PATTERN.matcher(host.getText());

        while (matcher.find()) {
            final String requireJsPath = matcher.group(2);
            final List<PsiElement> targets = RequireJsPathResolver.getInstance()
                    .resolveJsFilesOrAlias(element.getProject(), requireJsPath);

            if (targets.isEmpty()) {
                continue;
            }
            references.add(
                    new PolyVariantReferenceBase(
                            host,
                            new TextRange(matcher.start(2), matcher.end(2)),
                            targets
                    )
            );
            NavigationInstrumentation.infoOnce(
                    "magento-init-host-reference-created-" + requireJsPath,
                    () -> "Magento init host reference created for '" + requireJsPath
                            + "' targets=" + targets.size()
            );
        }

        return references.toArray(PsiReference.EMPTY_ARRAY);
    }

    private @Nullable PsiElement getMagentoInitHost(final @NotNull PsiElement element) {
        final XmlAttributeValue attributeValue = PsiTreeUtil.getParentOfType(
                element,
                XmlAttributeValue.class,
                false
        );

        if (attributeValue != null && isMageInitAttributeValue(attributeValue)) {
            return attributeValue;
        }
        final XmlText xmlText = PsiTreeUtil.getParentOfType(element, XmlText.class, false);

        if (xmlText != null && isMagentoInitScriptText(xmlText)) {
            return xmlText;
        }

        return null;
    }

    private boolean isMageInitAttributeValue(final @NotNull XmlAttributeValue attributeValue) {
        final PsiElement parent = attributeValue.getParent();

        return parent instanceof XmlAttribute
                && MAGE_INIT_ATTRIBUTE.equals(((XmlAttribute) parent).getName());
    }

    private boolean isMagentoInitScriptText(final @NotNull XmlText xmlText) {
        final PsiElement parent = xmlText.getParent();

        return parent instanceof HtmlTag
                && MAGENTO_INIT_SCRIPT_TYPE.equals(((HtmlTag) parent).getAttributeValue("type"));
    }
}
