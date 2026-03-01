/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import org.junit.jupiter.api.Test;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class ConfigurationTypeReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    @Test
    public void testDiXmlTypeNameMustHaveReference() {
        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertHasReferencePhpClass("Magento\\Backend\\Model\\Source\\YesNo");
    }
}
