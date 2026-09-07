/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
public class ObserverDeclarationInspectionTest extends InspectionXmlFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(ObserverDeclarationInspection.class);
    }

    /**
     * Tests for duplicate observer name inspection warning in the same events.xml
     */
    public void testObserverNameUsedInSameFile() {
        myFixture.configureByFile(getFixturePath(ModuleEventsXml.FILE_NAME));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests for duplicate observer name inspection warning in a different events.xml
     */
    public void testObserverNameUsedInDifferentFile() {
        myFixture.configureByFile(getFixturePath(ModuleEventsXml.FILE_NAME));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests warning for disabling of non-existing observer.
     */
    public void testDisablingNonExistingObserver() {
        myFixture.configureByFile(getFixturePath(ModuleEventsXml.FILE_NAME));
        myFixture.testHighlighting(true, false, false);
    }
}
