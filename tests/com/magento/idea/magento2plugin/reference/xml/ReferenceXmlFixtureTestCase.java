package com.magento.idea.magento2plugin.reference.xml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.magento.idea.magento2plugin.BaseProjectTestCase;

import java.io.File;

abstract public class ReferenceXmlFixtureTestCase extends BaseProjectTestCase {

    private static final String testDataFolderPath = "testData" + File.separator + "reference" + File.separator;
    private static final String fixturesFolderPath = "xml" + File.separator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    protected String getFixturePath(String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }

    protected void assertHasReferenceToXmlAttributeValue(String reference) {
        PsiElement element = getElementFromCaret();
        assertEquals(reference, ((XmlAttributeValue) element.getReferences()[0].resolve()).getValue());
    }

    protected void assertEmptyReference() {
        PsiElement element = getElementFromCaret();
        assertEmpty(element.getReferences());
    }

    private PsiElement getElementFromCaret() {
        return myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
    }
}
