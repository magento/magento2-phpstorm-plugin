/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class PluginLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    /**
     * Tests linemarker in a class which has plugins.
     */
    public void testClassWithPluginShouldHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("PluginClass.php", "php"));

        assertHasLinemarkerWithTooltipAndIcon("Navigate to plugins", "nodes/plugin.svg");
    }

    /**
     * Tests linemarker in a class which does not have plugins.
     */
    public void testClassWithoutPluginShouldNotHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("RegularClass.php", "php"));

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to plugins", "nodes/plugin.svg");
    }
}
