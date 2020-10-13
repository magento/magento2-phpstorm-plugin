/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;

public class CrontabReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {
    private static final String EXPECTED_REFERENCE = "Magento\\Catalog\\Cron\\RefreshSpecialPrices";

    /**
     * Test instance attribute of the crontab.xml file
     * must have reference.
     */
    public void testCrontabInstanceMustHaveReference() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertHasReferencePhpClass(EXPECTED_REFERENCE);
    }
}
