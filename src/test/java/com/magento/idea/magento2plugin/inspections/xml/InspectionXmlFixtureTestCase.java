/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import org.junit.jupiter.api.BeforeEach;

import com.magento.idea.magento2plugin.inspections.BaseInspectionsTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;

public abstract class InspectionXmlFixtureTestCase extends BaseInspectionsTestCase {

    private static final String testDataFolderPath = "testData" //NOPMD
            + File.separator
            + "inspections"
            + File.separator;

    private static final String fixturesFolderPath = "xml" + File.separator; //NOPMD

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    protected boolean isWriteActionRequired() {
        return false;
    }

    protected String getFixturePath(final String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }
}
