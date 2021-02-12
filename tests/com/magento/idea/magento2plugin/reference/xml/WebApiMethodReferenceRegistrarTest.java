/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

public class WebApiMethodReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    /**
     * Tests for reference to valid PHP method in webapi.xml.
     */
    public void testWebApiMethodMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath("webapi.xml"));

        assertHasReferenceToClassMethod(
                "Magento\\Catalog\\Api\\ProductRepositoryInterface",
                "save"
        );
    }

    /**
     * Tests for no reference to invalid PHP method in webapi.xml.
     */
    public void testWebApiMethodMustNotHaveReference() {
        myFixture.configureByFile(this.getFixturePath("webapi.xml"));

        assertEmptyReference();
    }
}
