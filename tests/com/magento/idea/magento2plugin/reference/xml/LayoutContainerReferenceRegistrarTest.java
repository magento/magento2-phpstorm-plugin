/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.LayoutXml;

public class LayoutContainerReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    /**
     * The `name` attribute of the `referenceContainer` tag in layout XML must
     * have reference to the `name` attribute of `container` tag.
     */
    public void testReferenceContainerMustHaveReference() {
        final String filePath = this.getFixturePath(LayoutXml.DEFAULT_FILENAME);
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlTag("container");
    }
}
