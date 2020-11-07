/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.indexes.FixtureIndex;
import java.util.ArrayList;
import java.util.List;

import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import org.jetbrains.annotations.NotNull;

public class DataFixtureReferenceProvider extends PsiReferenceProvider {
    @Override
    public @NotNull PsiReference[] getReferencesByElement(
            @NotNull PsiElement element,
            @NotNull ProcessingContext context
    ) {
        final List<PsiReference> psiReferences = new ArrayList<>();

        String text = element.getText();
        final @NotNull String tag = ((PhpDocTag) element.getParent().getParent()).getName();

        System.out.println(text);
        System.out.println(tag);

        if (tag.equals("@magentoApiDataFixture")) {
            String name = element.getParent().getText();

            final List<PhpFile> dataFixtures = FixtureIndex.getInstance(element.getProject()).getDataFixtures(name);

            if (dataFixtures.size() > 0) {
                final List<PsiElement> files = new ArrayList<>(dataFixtures);
                TextRange range = new TextRange(0, name.length());
                psiReferences.add(new PolyVariantReferenceBase(element, range, files));
            }
        }

        final String hello = "hello";

        return psiReferences.toArray(new PsiReference[0]);
    }
}
