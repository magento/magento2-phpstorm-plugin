/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.php;

public class ObserverDispatchEventReferenceRegistrarTest extends ReferencePhpFixtureTestCase {

    public void testDispatchCallParamMustHaveReference() {
        String filePath = this.getFixturePath("TestBlock.php");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("test_event_in_test_class");
    }
}
