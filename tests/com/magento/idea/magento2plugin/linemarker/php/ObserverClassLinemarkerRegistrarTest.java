/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.linemarker.php;

public class ObserverClassLinemarkerRegistrarTest extends LinemarkerPhpFixtureTestCase {

    public void testObserverClassShouldHaveLinemarker() {
        String filePath = this.getFixturePath("TestObserver.php");
        myFixture.configureByFile(filePath);

        assertHasLinemarkerWithTooltipAndIcon("Navigate to configuration", "/fileTypes/xml.svg");
    }

    public void testRegularPhpClassShouldNotHaveLinemarker() {
        String filePath = this.getFixturePath("TestNotObserver.php");
        myFixture.configureByFile(filePath);

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to configuration", "/fileTypes/xml.svg");
    }
}
