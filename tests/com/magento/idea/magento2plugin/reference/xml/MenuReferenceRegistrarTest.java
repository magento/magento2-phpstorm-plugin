/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;

public class MenuReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    /**
     * The `parent` attribute of the `add` tag in a menu XML must
     * have reference to the `id` attribute of the another `add` tag.
     */
    public void testAddTagMustHaveCompletion() {
        final String filePath = this.getFixturePath(ModuleMenuXml.fileName);
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlTag(ModuleMenuXml.addTag);
    }
}
