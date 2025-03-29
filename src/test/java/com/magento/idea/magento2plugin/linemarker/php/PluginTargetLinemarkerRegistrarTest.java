/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class PluginTargetLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    /**
     * Tests linemarkers in a class which plugs in to a class and its method.
     */
    public void testPluginToClassShouldHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("Topmenu.php", "php"));

        assertHasLinemarkerWithTooltipAndIcon("Navigate to target method", "nodes/method.svg");
        assertHasLinemarkerWithTooltipAndIcon("Navigate to target class", "nodes/class.svg");
    }

    /**
     * Tests linemarkers in a class which plugs in to an interface and its method.
     */
    public void testPluginToInterfaceShouldHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("MviewState.php", "php"));

        assertHasLinemarkerWithTooltipAndIcon("Navigate to target method", "nodes/method.svg");
        assertHasLinemarkerWithTooltipAndIcon("Navigate to target class", "nodes/class.svg");
    }

    /**
     * Tests linemarkers in a regular class which does not plug in to any class or interface.
     */
    public void testRegularClassShouldNotHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("ClassNotConfiguredInDiXml.php", "php"));

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to target method", "/nodes/method.svg");
        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to target class", "nodes/class.svg");
    }
}
