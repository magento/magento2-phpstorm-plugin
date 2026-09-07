/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.lang.psi.search;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.SearchScopeProvider;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagentoSearchScopesProvider implements SearchScopeProvider {

    @Override
    public @Nullable @NlsContexts.Separator String getDisplayName() {
        return "Magento";
    }

    @Override
    public @NotNull List<SearchScope> getSearchScopes(
            final @NotNull Project project,
            final @NotNull DataContext dataContext
    ) {
        if (!Settings.isEnabled(project)) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new AllFilesExceptTestsScope(project));
    }
}
