/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

public class LayoutUpdateHandleReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    /**
     * The `handle` attribute of the `update` tag in the layout XML must
     * have reference to the layout.
     */
    public void testLayoutUpdateHandleMustHaveReference() {
        final String filePath = this.getFixturePath("test_test_test.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile(
                "magento/module-catalog/view/frontend/layout/test_index_index.xml"
        );
    }
}
