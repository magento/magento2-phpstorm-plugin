/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.util.magento.ui.TemplatePathResolver;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Optional JavaScript property extraction; the existing public lookup API delegates to the shared resolver. */
public class KnockoutTemplatePathResolver {
    private static final KnockoutTemplatePathResolver INSTANCE = new KnockoutTemplatePathResolver();

    private KnockoutTemplatePathResolver() { }

    public static KnockoutTemplatePathResolver getInstance() {
        return INSTANCE;
    }

    public @NotNull Set<String> collectTemplatePaths(final @NotNull JSFile jsFile) {
        final Set<String> templatePaths = new LinkedHashSet<>();
        final Collection<JSProperty> properties = PsiTreeUtil.findChildrenOfType(
                jsFile,
                JSProperty.class
        );

        for (final JSProperty property : properties) {
            final String templatePath = getTemplatePath(property);

            if (templatePath != null) {
                templatePaths.add(templatePath);
            }
        }

        return templatePaths;
    }

    public @Nullable String getTemplatePath(final @Nullable JSProperty property) {
        if (property == null || !isTemplateProperty(property)) {
            return null;
        }
        final JSExpression value = property.getValue();

        if (value == null || !isQuotedText(value.getText())) {
            return null;
        }

        return normalizeTemplatePath(value.getText());
    }

    private boolean isTemplateProperty(final @NotNull JSProperty property) {
        if (isTemplatePropertyName(property.getName())) {
            return true;
        }
        final PsiElement parent = property.getParent();

        if (parent == null) {
            return false;
        }
        final JSProperty parentProperty = PsiTreeUtil.getParentOfType(parent, JSProperty.class);

        return parentProperty != null && "templates".equals(parentProperty.getName());
    }

    private boolean isQuotedText(final @NotNull String text) {
        final String trimmed = text.trim();

        return (trimmed.startsWith("'") && trimmed.endsWith("'"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\""));
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
