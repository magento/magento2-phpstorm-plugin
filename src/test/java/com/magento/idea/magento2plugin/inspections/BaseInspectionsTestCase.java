/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import java.util.List;

/**
 * Configure test environment with Magento 2 project
 */
abstract public class BaseInspectionsTestCase extends BaseProjectTestCase {
    protected final InspectionBundle inspectionBundle = new InspectionBundle();

    protected void assertHasHighlighting(String message) {
        String highlightingNotFound = "Failed that documents contains highlighting with the description `%s`";

        List<HighlightInfo> highlightingList = myFixture.doHighlighting();
        if (highlightingList.isEmpty()) {
            fail(String.format(highlightingNotFound, message));
        }

        for (HighlightInfo highlighting :
                highlightingList) {
            if (highlighting.getDescription() == null) continue;
            if (highlighting.getDescription().equals(message)) {
                return;
            }
        }
        fail(String.format(highlightingNotFound, message));
    }

    protected void assertHasNoHighlighting(String message) {
        String highlightingNotFound = "Failed that documents not contains highlighting with the description `%s`";

        List<HighlightInfo> highlightingList = myFixture.doHighlighting();
        if (highlightingList.isEmpty()) {
            return;
        }

        for (HighlightInfo highlighting :
                highlightingList) {
            if (highlighting.getDescription() == null) continue;
            if (highlighting.getDescription().equals(message)) {
                fail(String.format(highlightingNotFound, message));
            }
        }
    }
}
