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
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class DataFixtureReferenceProvider extends PsiReferenceProvider {
    @Override
    public @NotNull PsiReference[] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final List<PsiReference> psiReferences = new ArrayList<>();
        final @NotNull String tag = ((PhpDocTag) element.getParent().getParent()).getName();

        if ("@magentoApiDataFixture".equals(tag)) {
            final String name = element.getParent().getText();

            final List<PhpFile> dataFixtures = FixtureIndex.getInstance(element.getProject())
                    .getDataFixtures(name);

            if (!dataFixtures.isEmpty()) {
                final List<PsiElement> files = new ArrayList<>(dataFixtures);
                final TextRange range = new TextRange(0, name.length());

                psiReferences.add(new PolyVariantReferenceBase(element, range, files));
            }
        }

        return psiReferences.toArray(new PsiReference[0]);
    }
}
