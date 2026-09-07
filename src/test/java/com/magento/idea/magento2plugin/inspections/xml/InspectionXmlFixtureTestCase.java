/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.inspections.BaseInspectionsTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;

public abstract class InspectionXmlFixtureTestCase extends BaseInspectionsTestCase {

    private static final String testDataFolderPath = TEST_DATA_ROOT //NOPMD
            + File.separator
            + "inspections"
            + File.separator;

    private static final String fixturesFolderPath = "xml" + File.separator; //NOPMD

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setFixtureTestDataPath(testDataFolderPath);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    protected String getFixturePath(final String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }
}
