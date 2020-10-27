/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.inspections.BaseInspectionsTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;

public abstract class BaseReferenceTestCase extends BaseInspectionsTestCase {

    private static final String testDataFolderPath = "testData" //NOPMD
            + File.separator
            + "reference"
            + File.separator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    protected void assertHasReferenceToXmlAttributeValue(final String reference) {
        final String referenceNotFound //NOPMD
                = "Failed that element contains reference to the attribute value `%s`";

        final PsiElement element = getElementFromCaret();
        for (final PsiReference psiReference : element.getReferences()) {
            final PsiElement resolved = psiReference.resolve();
            if (!(resolved instanceof XmlAttributeValue)) {
                continue;
            }

            if (((XmlAttributeValue) resolved).getValue().equals(reference)) {
                return;
            }
        }

        fail(String.format(referenceNotFound, reference));
    }

    protected void assertHasReferenceToXmlTag(final String tagName) {
        final String referenceNotFound //NOPMD
                = "Failed that element contains reference to the XML tag `%s`";

        final PsiElement element = getElementFromCaret();
        for (final PsiReference psiReference : element.getReferences()) {
            final PsiElement resolved = psiReference.resolve();
            if (!(resolved instanceof XmlTag)) {
                continue;
            }

            if (((XmlTag) resolved).getName().equals(tagName)) {
                return;
            }
        }

        fail(String.format(referenceNotFound, tagName));
    }

    protected void assertHasReferenceToXmlFile(final String fileName) {
        final String referenceNotFound //NOPMD
                = "Failed that element contains reference to the XML tag `%s`";

        final PsiElement element = getElementFromCaret();
        for (final PsiReference psiReference : element.getReferences()) {
            final PsiElement resolved = psiReference.resolve();
            if (!(resolved instanceof XmlFile)) {
                continue;
            }

            if (((XmlFile) resolved).getName().equals(fileName)) {
                return;
            }
        }

        fail(String.format(referenceNotFound, fileName));
    }

    protected void assertHasReferenceToFile(final String reference) {
        final String referenceNotFound //NOPMD
                = "Failed that element contains reference to the file `%s`";

        final PsiElement element = getElementFromCaret();
        for (final PsiReference psiReference : element.getReferences()) {
            final PsiElement resolved = psiReference.resolve();
            if (!(resolved instanceof PsiFile)) {
                continue;
            }
            if (((PsiFile) resolved).getVirtualFile().getPath().endsWith(reference)) {
                return;
            }
        }

        fail(String.format(referenceNotFound, reference));
    }

    @SuppressWarnings("PMD")
    protected void assertHasReferencePhpClass(final String phpClassFqn) {
        final PsiElement element = getElementFromCaret();
        final PsiReference[] references = element.getReferences();
        String result = ((PhpClass) references[references.length - 1]
                .resolve())
                .getPresentableFQN();
        assertEquals(
                phpClassFqn,
                result
        );
    }

    protected void assertEmptyReference() {
        final PsiElement element = getElementFromCaret();
        assertEmpty(element.getReferences());
    }

    private PsiElement getElementFromCaret() {
        return myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
    }
}
