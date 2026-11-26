/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.util.magento.ui.TemplatePathResolver;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** JavaScript template property extraction; the public lookup API delegates to the shared resolver. */
public class KnockoutTemplatePathResolver {
    private static final KnockoutTemplatePathResolver INSTANCE = new KnockoutTemplatePathResolver();

    private KnockoutTemplatePathResolver() { }

    public static KnockoutTemplatePathResolver getInstance() {
        return INSTANCE;
    }

    public @NotNull Set<String> collectTemplatePaths(final @Nullable PsiFile psiFile) {
        if (!isJsFile(psiFile)) {
            return Collections.emptySet();
        }

        return collectTemplatePaths(psiFile.getText());
    }

    public @NotNull Set<String> collectTemplatePaths(final @Nullable CharSequence source) {
        if (source == null || source.length() == 0) {
            return Collections.emptySet();
        }
        final Set<String> templatePaths = new LinkedHashSet<>();
        final JsAst ast = MagentoJsParser.parse(source);

        for (final JsProperty property : ast.getProperties()) {
            final String templatePath = getTemplatePath(property);

            if (templatePath != null) {
                templatePaths.add(templatePath);
            }
        }

        return templatePaths;
    }

    public @Nullable String getTemplatePath(final @Nullable JsProperty property) {
        if (property == null || !isTemplateProperty(property) || property.getValueObject() != null) {
            return null;
        }
        if (property.getStringValue() != null) {
            return normalizeTemplatePath(property.getStringValue());
        }
        final String rawValue = property.getRawValueText();

        if (rawValue == null || !isQuotedText(rawValue)) {
            return null;
        }

        return normalizeTemplatePath(rawValue);
    }

    public @Nullable String getTemplatePath(
            final @Nullable String propertyName,
            final @Nullable String parentPropertyName,
            final @Nullable String rawValueText
    ) {
        if (!isTemplateProperty(propertyName, parentPropertyName)
                || rawValueText == null
                || !isQuotedText(rawValueText)) {
            return null;
        }

        return normalizeTemplatePath(rawValueText);
    }

    private boolean isTemplateProperty(final @NotNull JsProperty property) {
        final JsProperty parentProperty = property.getParentProperty();

        return isTemplateProperty(
                property.getName(),
                parentProperty == null ? null : parentProperty.getName()
        );
    }

    public boolean isTemplateProperty(
            final @Nullable String propertyName,
            final @Nullable String parentPropertyName
    ) {
        if (isTemplatePropertyName(propertyName)) {
            return true;
        }

        return "templates".equals(parentPropertyName);
    }

    private boolean isQuotedText(final @NotNull String text) {
        final String trimmed = text.trim();

        return (trimmed.startsWith("'") && trimmed.endsWith("'"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\""));
    }

    private static boolean isJsFile(final @Nullable PsiFile psiFile) {
        if (psiFile == null) {
            return false;
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile != null) {
            return "js".equalsIgnoreCase(virtualFile.getExtension());
        }

        return psiFile.getName().toLowerCase().endsWith(".js");
    }

    public boolean isTemplatePropertyName(final @Nullable String propertyName) {
        return TemplatePathResolver.getInstance().isTemplatePropertyName(propertyName);
    }

    public @Nullable String normalizeTemplatePath(final @Nullable String rawPath) {
        return TemplatePathResolver.getInstance().normalizeTemplatePath(rawPath);
    }

    public @NotNull List<PsiElement> resolveTemplateFiles(final @NotNull Project project,
                                                        final @NotNull String templatePath) {
        return TemplatePathResolver.getInstance().resolveTemplateFiles(project, templatePath);
    }

    public @NotNull Collection<VirtualFile> findTemplateFiles(final @NotNull Project project,
                                                             final @NotNull String rawTemplatePath) {
        return TemplatePathResolver.getInstance().findTemplateFiles(project, rawTemplatePath);
    }

    public @NotNull Set<String> getTemplateRequireJsPaths(final @NotNull PsiFile psiFile) {
        return TemplatePathResolver.getInstance().getTemplateRequireJsPaths(psiFile);
    }
}
