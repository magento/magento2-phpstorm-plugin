/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class PhpClassMemberCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    /**
     * Tests for completion of init_parameter argument value in di.xml.
     */
    public void testDiXmlMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath, "Magento\\Backend\\Model\\Source\\YesNo::TEST_STRING");
    }
}
