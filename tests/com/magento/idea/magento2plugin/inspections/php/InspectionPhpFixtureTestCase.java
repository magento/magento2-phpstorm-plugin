package com.magento.idea.magento2plugin.inspections.php;

import com.magento.idea.magento2plugin.BaseProjectTestCase;

import java.io.File;

abstract public class InspectionPhpFixtureTestCase extends BaseProjectTestCase {

    private static final String testDataFolderPath = "testData" + File.separator + "inspections" + File.separator;
    private static final String fixturesFolderPath = "php" + File.separator;

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
