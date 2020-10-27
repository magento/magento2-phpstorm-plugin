/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.LayoutXml;

public class LayoutContainerCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    /**
     * The `name` attribute of the `referenceContainer` tag in layout XML must
     * have completion based on `name` attribute of `container` tags.
     */
    public void testReferenceContainerMustHaveCompletion() {
        final String filePath = this.getFixturePath(LayoutXml.DEFAULT_FILENAME);
        myFixture.configureByFile(filePath);

        assertCompletionContains(
                filePath,
                "test_index_index_container",
                "test_index_index_container2"
        );
    }
}
