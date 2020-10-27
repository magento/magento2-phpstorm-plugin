/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.LayoutXml;

public class LayoutUIComponentCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    public void testUIComponentMustHaveCompletion() {
        String filePath = this.getFixturePath(LayoutXml.DEFAULT_FILENAME);
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath,
            "test_index_i",
            "test_index_index"
        );
    }
}
