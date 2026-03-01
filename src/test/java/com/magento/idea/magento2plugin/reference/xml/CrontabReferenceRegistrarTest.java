/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import org.junit.jupiter.api.Test;

import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;

public class CrontabReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {
    private static final String EXPECTED_CLASS = "Magento\\Catalog\\Cron\\RefreshSpecialPrices";
    private static final String EXPECTED_METHOD = "execute";

    /**
     * Test instance attribute of the crontab.xml file must have reference.
     */
    @Test
    public void testCrontabInstanceMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath(CrontabXmlTemplate.FILE_NAME));

        assertHasReferencePhpClass(EXPECTED_CLASS);
    }

    /**
     * Tests for reference to valid PHP method in crontab.xml.
     */
    @Test
    public void testCrontabMethodMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath(CrontabXmlTemplate.FILE_NAME));

        assertHasReferenceToClassMethod(
                EXPECTED_CLASS,
                EXPECTED_METHOD
        );
    }

    /**
     * Tests for no reference to invalid PHP method in crontab.xml.
     */
    @Test
    public void testCrontabMethodMustNotHaveReference() {
        myFixture.configureByFile(this.getFixturePath(CrontabXmlTemplate.FILE_NAME));

        assertEmptyReference();
    }
}
