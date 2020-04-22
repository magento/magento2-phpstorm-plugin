package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.BaseProjectTestCase;

import com.magento.idea.magento2plugin.magento.packages.File;

abstract public class InspectionXmlFixtureTestCase extends BaseProjectTestCase {

    private static final String testDataFolderPath = "testData" + File.separator + "inspections" + File.separator;
    private static final String fixturesFolderPath = "xml" + File.separator;

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
