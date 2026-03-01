/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import org.junit.jupiter.api.Test;

import com.magento.idea.magento2plugin.magento.files.LayoutXml;

public class LayoutUIComponentCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    /**
     * Test ui component must have completion.
     */
    @Test
    public void testUIComponentMustHaveCompletion() {
        final String filePath = this.getFixturePath(LayoutXml.DEFAULT_FILENAME);
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath,
                "recently_viewed",
                "recently_viewed_2"
        );
    }
}
