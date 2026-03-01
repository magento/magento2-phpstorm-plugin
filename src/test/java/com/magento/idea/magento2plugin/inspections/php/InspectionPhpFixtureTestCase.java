/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import org.junit.jupiter.api.BeforeEach;

import com.magento.idea.magento2plugin.inspections.BaseInspectionsTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;

public abstract class InspectionPhpFixtureTestCase extends BaseInspectionsTestCase {

    private static final String testDataFolderPath =  "src/test/resources/testData" //NOPMD
            + File.separator
            + "inspections"
            + File.separator;

    private static final String fixturesFolderPath //NOPMD
            = "php" + File.separator;

    @BeforeEach
    public void setUp() throws Exception {
        myFixture.setTestDataPath(testDataFolderPath);
    }

    protected boolean isWriteActionRequired() {
        return false;
    }

    protected String getFixturePath(final String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }
}
