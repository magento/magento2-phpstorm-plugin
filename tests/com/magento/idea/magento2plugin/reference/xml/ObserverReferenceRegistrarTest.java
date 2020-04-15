/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;

public class ObserverReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    public void testEventsXmlMustHaveReference() {
        String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertHasReferencePhpClass("Magento\\Catalog\\Observer\\TestObserver");
    }
}
