/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class ConstructorArgumentReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {
    /**
     * Tests for valid reference in di.xml constructor argument
     */
    public void testDiXmlConstructorArgumentMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath(ModuleDiXml.FILE_NAME));

        assertHasReferencetoConstructorParameter(
                "\\Magento\\Framework\\Logger\\LoggerInterface",
                "logger"
        );
    }

    /**
     * Tests for no reference in di.xml constructor argument
     */
    public void testDiXmlConstructorArgumentMustNotHaveReference() {
        myFixture.configureByFile(this.getFixturePath(ModuleDiXml.FILE_NAME));

        assertEmptyReference();
    }
}
