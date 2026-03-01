/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import org.junit.jupiter.api.Test;

public class PhpServiceMethodCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    /**
     * Tests for the completion in `method` attribute of the `service` tag in webapi.xml.
     */
    @Test
    public void testWebApiXmlMustHaveCompletion() {
        final String filePath = this.getFixturePath("webapi.xml");
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath, "save");
    }
}
