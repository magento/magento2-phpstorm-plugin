/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.linemarker.php;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class ObserverClassLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    public void testObserverClassShouldHaveLinemarker() {
        String filePath = this.getFixturePath("TestObserver.php", "php");
        myFixture.configureByFile(filePath);

        assertHasLinemarkerWithTooltipAndIcon("Navigate to configuration", "/fileTypes/xml.svg");
    }

    public void testRegularPhpClassShouldNotHaveLinemarker() {
        String filePath = this.getFixturePath("TestNotObserver.php", "php");
        myFixture.configureByFile(filePath);

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to configuration", "/fileTypes/xml.svg");
    }
}
