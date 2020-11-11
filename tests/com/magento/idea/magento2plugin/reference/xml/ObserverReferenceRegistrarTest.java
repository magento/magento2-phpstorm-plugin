/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;

public class ObserverReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    /**
     * Tests for observer instance reference in events.xml.
     */
    public void testObserverInstanceMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath(ModuleEventsXml.FILE_NAME));

        assertHasReferencePhpClass("Magento\\Catalog\\Observer\\TestObserver");
    }

    /**
     * Tests for event name reference in events.xml.
     */
    public void testEventNameMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath(ModuleEventsXml.FILE_NAME));

        assertHasReferenceToMethodArgument("test_event_in_block");
    }

    /**
     * Tests for no event name reference in events.xml.
     */
    public void testEventNameMustNotHaveReference() {
        myFixture.configureByFile(this.getFixturePath(ModuleEventsXml.FILE_NAME));

        assertEmptyReference();
    }
}
