/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.ui;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ui.UiComponentTemplateDeclarationIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutTemplatePathResolver;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UiComponentTemplateResolver {
    private static UiComponentTemplateResolver INSTANCE;

    private UiComponentTemplateResolver() {
    }

    public static UiComponentTemplateResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UiComponentTemplateResolver();
        }

        return INSTANCE;
    }

    public @NotNull List<PsiElement> resolveTemplateFiles(
            final @NotNull Project project,
            final @NotNull String templatePath
    ) {
        return KnockoutTemplatePathResolver.getInstance().resolveTemplateFiles(project, templatePath);
    }

    public @NotNull List<UiComponentNavigationData> resolveTemplateOwnerDeclarations(
            final @NotNull PsiFile templateFile
    ) {
        final Set<UiComponentNavigationData> results = new LinkedHashSet<>();
        final Project project = templateFile.getProject();

        for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                .getTemplateRequireJsPaths(templateFile)) {
            results.addAll(resolveByIndexKey(
                    project,
                    UiComponentTemplateDeclarationIndex.templatePathKey(templatePath)
            ));
        }

        return new ArrayList<>(results);
    }

    public @NotNull List<UiComponentNavigationData> resolveTemplateDeclarationsForParent(
            final @NotNull Project project,
            final @NotNull UiComponentNavigationData childDeclaration
    ) {
        final Set<UiComponentNavigationData> results = new LinkedHashSet<>();

        addTemplateDeclarationsByComponent(
                project,
                results,
                childDeclaration.getParentComponentName(),
                childDeclaration.getParentComponentJsPath()
        );

        return new ArrayList<>(results);
    }

    public @NotNull List<UiComponentNavigationData> resolveTemplateDeclarationsForOwner(
            final @NotNull Project project,
            final @NotNull UiComponentNavigationData owner
    ) {
        final Set<UiComponentNavigationData> results = new LinkedHashSet<>();

        addTemplateDeclarationsByComponent(
                project,
                results,
                owner.getComponentName(),
                owner.getComponentJsPath()
        );

        return new ArrayList<>(results);
    }

    public @NotNull List<UiComponentNavigationData> resolveTemplateDeclarationsForComponentJsPath(
            final @NotNull Project project,
            final @NotNull String componentJsPath
    ) {
        return resolveByIndexKey(project, UiComponentTemplateDeclarationIndex.componentJsPathKey(componentJsPath));
    }

    public @NotNull List<UiComponentNavigationData> resolveTemplateDeclarationsForTemplatePath(
            final @NotNull Project project,
            final @NotNull String templatePath
    ) {
        return resolveByIndexKey(project, UiComponentTemplateDeclarationIndex.templatePathKey(templatePath));
    }

    private void addTemplateDeclarationsByComponent(
            final @NotNull Project project,
            final @NotNull Set<UiComponentNavigationData> results,
            final @Nullable String componentName,
            final @Nullable String componentJsPath
    ) {
        if (componentName != null && !componentName.isBlank()) {
            results.addAll(resolveByIndexKey(
                    project,
                    UiComponentTemplateDeclarationIndex.componentNameKey(componentName)
            ));
        }
        if (componentJsPath != null && !componentJsPath.isBlank()) {
            results.addAll(resolveByIndexKey(
                    project,
                    UiComponentTemplateDeclarationIndex.componentJsPathKey(componentJsPath)
            ));
        }
    }

    private @NotNull List<UiComponentNavigationData> resolveByIndexKey(
            final @NotNull Project project,
            final @NotNull String key
    ) {
        final List<UiComponentNavigationData> results = new ArrayList<>();

        for (final Set<UiComponentNavigationData> declarations : FileBasedIndex.getInstance().getValues(
                UiComponentTemplateDeclarationIndex.KEY,
                key,
                GlobalSearchScope.allScope(project)
        )) {
            results.addAll(declarations);
        }

        return results;
    }
}
