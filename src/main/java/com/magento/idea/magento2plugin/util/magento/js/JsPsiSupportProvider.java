/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Optional JavaScript PSI integration point registered in {@code withJs.xml}.
 * When the JetBrains JavaScript plugin is installed, indexes, resolvers, line markers, and queries use
 * IntelliJ's native JS PSI rather than re-parsing JavaScript with {@link MagentoJsParser}.
 */
public interface JsPsiSupportProvider {
    ExtensionPointName<JsPsiSupportProvider> EP_NAME = ExtensionPointName.create(
            "com.magento.idea.magento2plugin.jsPsiSupportProvider"
    );

    static @Nullable JsPsiSupportProvider getInstance() {
        if (ApplicationManager.getApplication() == null) {
            return null;
        }
        return EP_NAME.getExtensionList().stream().findFirst().orElse(null);
    }

    static int versionDelta() {
        if (ApplicationManager.getApplication() == null) {
            return 0;
        }
        return EP_NAME.getExtensionList().size();
    }

    boolean isJavaScriptFile(@NotNull VirtualFile file);

    @Nullable RequireJsConfig parseRequireJsConfig(@NotNull PsiFile psiFile);

    @Nullable Set<String> collectKnockoutTemplatePaths(@NotNull PsiFile psiFile);

    @Nullable List<UiComponentNavigationData> collectUiComponentDeclarations(@NotNull PsiFile psiFile);

    @Nullable List<UiComponentNavigationData> collectEmbeddedUiComponentDeclarations(
            @NotNull PsiElement jsScope,
            @NotNull VirtualFile virtualFile
    );

    @Nullable String getDisplayAreaValue(@NotNull PsiElement element);

    @Nullable List<DisplayAreaAnchor> collectDisplayAreaAnchors(@NotNull PsiFile psiFile);

    record DisplayAreaAnchor(@NotNull String displayArea, int keyOffset) { }
}
