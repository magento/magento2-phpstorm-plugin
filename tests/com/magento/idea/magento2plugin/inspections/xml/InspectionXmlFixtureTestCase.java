package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.magento.idea.magento2plugin.BaseProjectTestCase;

abstract public class InspectionXmlFixtureTestCase extends BaseProjectTestCase {

    private static final String testDataFolderPath = "testData/inspections/";
    private static final String fixturesFolderPath = "xml/";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    protected String getFixturePath(String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }
}
