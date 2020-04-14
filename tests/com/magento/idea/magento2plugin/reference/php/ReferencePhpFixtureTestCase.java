/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.php;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import java.io.File;

abstract public class ReferencePhpFixtureTestCase extends BaseProjectTestCase {

    private static final String testDataFolderPath = "testData" + File.separator + "reference" + File.separator;
    private static final String fixturesFolderPath = "php" + File.separator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    protected String getFixturePath(String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }

    protected void assertHasReferenceToXmlAttributeValue(String attributeValue) {
        String referenceNotFound = "Failed that documents contains reference to XML attribute with value `%s`";

        PsiElement element = getElementFromCaret();
        PsiReference[] references = element.getReferences();
        for (PsiReference reference: references) {
            if (!(reference instanceof PolyVariantReferenceBase)) {
                continue;
            }
            assertEquals(
                    attributeValue,
                    ((XmlAttributeValueImpl) reference.resolve())
                            .getValue()
            );
            return;
        }

        fail(String.format(referenceNotFound, attributeValue));
    }

    private PsiElement getElementFromCaret() {
        return myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
    }
}
