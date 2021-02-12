/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class TestFixtureLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    /**
     * Tests linemarkers for @magentoDataFixture tag.
     */
    public void testMagentoDataFixtureHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("Test.php", "php"));

        assertHasLinemarkerWithTooltipAndIcon("Navigate to fixtures", "icons/php-icon.svg");
    }

    /**
     * Tests linemarkers for @magentoApiDataFixture tag.
     */
    public void testMagentoApiDataFixtureHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("Test.php", "php"));

        assertHasLinemarkerWithTooltipAndIcon("Navigate to fixtures", "icons/php-icon.svg");
    }
}
