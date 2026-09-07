/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class ConstructorArgumentCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    /**
     * Tests for completion of constructor argument in di.xml.
     */
    public void testDiXmlMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, "logger");
    }
}
