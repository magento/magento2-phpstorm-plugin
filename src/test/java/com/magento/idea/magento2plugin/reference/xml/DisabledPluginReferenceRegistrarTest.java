/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class DisabledPluginReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    /**
     * Tests for disabled plugin name reference to original definition.
     */
    public void testDisabledPluginNameMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath(ModuleDiXml.FILE_NAME));

        assertHasReferenceToXmlAttributeValue("catalogTopmenu");
    }
}
