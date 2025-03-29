/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;

public class MenuCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    /**
     * The `parent` attribute of the `add` tag in the men XML must
     * have completion based on the index.
     */
    public void testAddTagMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleMenuXml.fileName);
        myFixture.configureByFile(filePath);

        assertCompletionContains(
                filePath,
                "Magento_Catalog::catalog",
                "Magento_Catalog::inventory"
        );
    }
}
