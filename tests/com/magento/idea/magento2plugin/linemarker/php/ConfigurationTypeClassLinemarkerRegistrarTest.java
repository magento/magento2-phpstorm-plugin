/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.linemarker.php;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class ConfigurationTypeClassLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    public void testTypeNameClassShouldHaveLinemarker() {
        String filePath = this.getFixturePath("Topmenu.php", "php");
        myFixture.configureByFile(filePath);

        assertHasLinemarkerWithTooltipAndIcon("Navigate to configuration", "/fileTypes/xml.svg");
    }

    public void testRegularPhpClassShouldNotHaveLinemarker() {
        String filePath = this.getFixturePath("ClassNotConfiguredInDiXml.php", "php");
        myFixture.configureByFile(filePath);

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to configuration", "/fileTypes/xml.svg");
    }
}
