/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.linemarker.php;

public class ConfigurationTypeClassLinemarkerRegistrarTest extends LinemarkerPhpFixtureTestCase {

    public void testTypeNameClassShouldHaveLinemarker() {
        String filePath = this.getFixturePath("Topmenu.php");
        myFixture.configureByFile(filePath);

        assertHasLinemarkerWithTooltipAndIcon("Navigate to configuration", "/fileTypes/xml.svg");
    }

    public void testRegularPhpClassShouldNotHaveLinemarker() {
        String filePath = this.getFixturePath("ClassNotConfiguredInDiXml.php");
        myFixture.configureByFile(filePath);

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to configuration", "/fileTypes/xml.svg");
    }
}
