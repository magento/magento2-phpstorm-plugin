/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.php;

public class DataFixtureReferenceRegistrarTest extends ReferencePhpFixtureTestCase {

    /**
     * Tests that file path of @magentoApiDataFixture tag has a reference.
     */
    public void testDataFixtureMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath("ProductRepositoryInterfaceTest.php"));

        assertHasReferenceToFile(
                "dev/tests/integration/testsuite/Magento/Catalog/_files/product.php"
        );
    }
}
