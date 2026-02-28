/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;

public class ObserverCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStringsEntities = new String[] {
        "Magento\\Catalog\\Observer\\TestObserver"
      };
    private static final String[] lookupStringsEvents = new String[] {
        "test_event_in_block"
      };

    @org.junit.jupiter.api.Test
    public void testEventsXmlMustHaveCompletion() {
        String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.jupiter.api.Test
    public void testNotEventsXmlMustBeEmpty() {
        String filePath = this.getFixturePath("notevents.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.jupiter.api.Test
    public void testEventsXmlEventNameMustHaveCompletion() {
        String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEvents);
    }
}
