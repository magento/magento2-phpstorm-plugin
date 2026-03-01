/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import org.junit.jupiter.api.Test;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class ConfigurationTypeCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStringsTypeName = new String[] {
        "Magento\\Backend\\Model\\Source\\YesNo"
      };

    @Test
    public void testDiXmlTypeNameMustHaveCompletion() {
        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsTypeName);
    }
}
