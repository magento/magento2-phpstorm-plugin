/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import java.io.File;
import java.util.List;

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

    protected void assertHasHighlighting(String message) {
        String highlightingNotFound = "Failed that documents contains highlighting with the description `%s`";

        List<HighlightInfo> highlightingList = myFixture.doHighlighting();
        if (highlightingList.isEmpty()) {
            fail(String.format(highlightingNotFound, message));
        }

        for (HighlightInfo highlighting :
            highlightingList) {
            if (highlighting.getDescription().equals(message)) {
                return;
            }
        }
        fail(String.format(highlightingNotFound, message));
    }

    protected void assertHasNoHighlighting(String message) {
        String highlightingFound = "Failed that documents not contains highlighting with the description `%s`";

        List<HighlightInfo> highlightingList = myFixture.doHighlighting();
        if (highlightingList.isEmpty()) {
            return;
        }

        for (HighlightInfo highlighting :
            highlightingList) {
            if (highlighting.getDescription().equals(message)) {
                fail(String.format(highlightingFound, message));
            }
        }
    }
}
