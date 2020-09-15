/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

public class LayoutUpdateHandleCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    /**
     * The `handle` attribute of the `update` tag in the layout XML must
     * have completion based on layout index.
     */
    public void testUpdateHandleMustHaveCompletion() {
        final String filePath = this.getFixturePath("test_test_test.xml");
        myFixture.configureByFile(filePath);

        assertCompletionContains(
                filePath,
                "test_index_index",
                "test_index_index2"
        );
    }
}
