/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.json;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.magento.idea.magento2plugin.reference.BaseReferenceTestCase;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;

public class MagentoInitReferenceContributorTest extends BaseReferenceTestCase {
    /**
     * JS component keys in text/x-magento-init scripts should reference JS files.
     */
    public void testXMagentoInitComponentMustHaveReference() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/templates/x-magento-init.phtml"
        );

        assertInjectedJsonReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    /**
     * JS component keys in data-mage-init attributes should reference JS files.
     */
    public void testDataMageInitComponentMustHaveReference() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/templates/data-mage-init.phtml"
        );

        assertInjectedJsonReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    /**
     * RequireJS aliases in data-mage-init attributes should reference mapped JS files.
     */
    public void testDataMageInitAliasMustHaveReference() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/templates/data-mage-init-alias.phtml"
        );

        assertInjectedJsonReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    private void assertInjectedJsonReferenceToFile(final String reference) {
        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        while (element != null && element.getReferences().length == 0) {
            element = element.getParent();
        }

        assertNotNull(
                "No injected JSON element with references found at caret",
                element
        );

        for (final PsiReference psiReference : element.getReferences()) {
            if (psiReference instanceof PolyVariantReferenceBase) {
                for (final ResolveResult resolveResult
                        : ((PolyVariantReferenceBase) psiReference).multiResolve(true)) {
                    if (isReferenceToFile(resolveResult.getElement(), reference)) {
                        return;
                    }
                }
                continue;
            }

            if (isReferenceToFile(psiReference.resolve(), reference)) {
                return;
            }
        }

        fail(String.format("Failed that injected JSON contains reference to file `%s`", reference));
    }

    private boolean isReferenceToFile(final PsiElement element, final String reference) {
        return element instanceof PsiFile
                && ((PsiFile) element).getVirtualFile().getPath().endsWith(reference);
    }
}
