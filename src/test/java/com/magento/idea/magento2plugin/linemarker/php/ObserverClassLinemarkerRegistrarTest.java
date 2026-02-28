/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class ObserverClassLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    /**
     * Tests linemarkers in the Observer class.
     */
    @org.junit.jupiter.api.Test
    public void testObserverClassShouldHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("TestObserver.php", "php"));

        assertHasLinemarkerWithTooltipAndIcon("Navigate to configuration", "fileTypes/xml.svg");
    }

    /**
     * Tests linemarkers in the regular class.
     */
    @org.junit.jupiter.api.Test
    public void testRegularPhpClassShouldNotHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("TestNotObserver.php", "php"));

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to configuration", "fileTypes/xml.svg");
    }
}
