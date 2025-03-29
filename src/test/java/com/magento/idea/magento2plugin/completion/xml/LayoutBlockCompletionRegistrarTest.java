/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.LayoutXml;

public class LayoutBlockCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    public void testReferenceBlockMustHaveCompletion() {
        String filePath = this.getFixturePath(LayoutXml.DEFAULT_FILENAME);
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath, new String[] {
            "test_index_index_block",
            "test_index_index_block2"
        });
    }
}
