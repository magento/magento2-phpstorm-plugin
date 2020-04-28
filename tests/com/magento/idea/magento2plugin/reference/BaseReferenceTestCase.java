/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttributeValue;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.inspections.BaseInspectionsTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;

abstract public class BaseReferenceTestCase extends BaseInspectionsTestCase {

    private static final String testDataFolderPath = "testData" + File.separator + "reference" + File.separator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    protected void assertHasReferenceToXmlAttributeValue(String reference) {
        String referenceNotFound = "Failed that element contains reference to the attribute value `%s`";

        PsiElement element = getElementFromCaret();
        for (PsiReference psiReference: element.getReferences()) {
            PsiElement resolved = psiReference.resolve();
            if (!(resolved instanceof XmlAttributeValue)) {
                continue;
            }

            if (((XmlAttributeValue) resolved).getValue().equals(reference)) {
                return;
            }
        }

        fail(String.format(referenceNotFound, reference));
    }

    protected void assertHasReferenceToFile(String reference) {
        String referenceNotFound = "Failed that element contains reference to the file `%s`";

        PsiElement element = getElementFromCaret();
        for (PsiReference psiReference: element.getReferences()) {
            PsiElement resolved = psiReference.resolve();
            if (!(resolved instanceof PsiFile)) {
                continue;
            }
            if (((PsiFile) resolved).getVirtualFile().getPath().endsWith(reference)) {
                return;
            }
        }

        fail(String.format(referenceNotFound, reference));
    }

    protected void assertHasReferencePhpClass(String phpClassFqn) {
        PsiElement element = getElementFromCaret();
        PsiReference[] references = element.getReferences();
        assertEquals(
            phpClassFqn,
            ((PhpClass) references[references.length -1].resolve())
                .getPresentableFQN()
        );
    }

    protected void assertEmptyReference() {
        PsiElement element = getElementFromCaret();
        assertEmpty(element.getReferences());
    }

    private PsiElement getElementFromCaret() {
        return myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
    }
}
