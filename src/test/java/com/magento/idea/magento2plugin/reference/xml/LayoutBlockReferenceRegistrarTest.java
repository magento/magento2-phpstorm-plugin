/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.LayoutXml;

public class LayoutBlockReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    public void testReferenceBlockMustHaveReference() {
        String filePath = this.getFixturePath(LayoutXml.DEFAULT_FILENAME);
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlTag("block");
    }
}
