/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import java.awt.event.MouseEvent;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class SearchGutterIconNavigationHandler<T extends PsiElement>
        implements GutterIconNavigationHandler<T> {

    private final Collection<? extends NavigatablePsiElement> myReferences;
    private final @NotNull String popupTitle;

    /**
     * Search gutter icon navigation handler constructor.
     *
     * @param references Collection
     * @param popupTitle String
     */
    public SearchGutterIconNavigationHandler(
            final Collection<? extends NavigatablePsiElement> references,
            final @NotNull String popupTitle
    ) {
        this.popupTitle = popupTitle;
        myReferences = references;
    }

    @Override
    public void navigate(final MouseEvent event, final T elt) {
        PsiElementListNavigator.openTargets(
                event,
                myReferences.toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY),
                popupTitle,
                "Open in Find Tool Window",// Ignored
                new DefaultPsiElementCellRenderer()
        );
    }
}
