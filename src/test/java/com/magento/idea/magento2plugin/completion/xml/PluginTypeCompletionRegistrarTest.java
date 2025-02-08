/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class PluginTypeCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    /**
     * The `type` attribute of the `plugin` tag in di.xml must
     * have completion based on PHP classes index
     */
    public void testPluginTypeMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath, "Magento\\Backend\\Model\\Source\\YesNo");
    }
}
