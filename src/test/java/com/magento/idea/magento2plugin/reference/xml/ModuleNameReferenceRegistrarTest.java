/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

public class ModuleNameReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    /**
     * Tests for module name reference in module.xml
     */
    public void testModuleNameMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath("module.xml"));

        assertHasReferenceToDirectory("module-catalog");
    }

    /**
     * Tests for module name reference under sequence node in module.xml
     */
    public void testSequenceModuleNameMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath("module.xml"));

        assertHasReferenceToDirectory("module-config");
    }
}
