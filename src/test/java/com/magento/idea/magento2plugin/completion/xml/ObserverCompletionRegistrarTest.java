/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;
import java.util.Collections;
import java.util.List;

public class ObserverCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStringsEntities = new String[] {
        "Magento\\Catalog\\Observer\\TestObserver"
      };
    private static final String[] lookupStringsEvents = new String[] {
        "test_event_in_block"
      };

    public void testEventsXmlMustHaveCompletion() {
        String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    public void testNotEventsXmlMustBeEmpty() {
        String filePath = this.getFixturePath("notevents.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    public void testEventsXmlEventNameMustHaveCompletion() {
        String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEvents);
    }

    public void testDeclaredAndDispatchedEventCompletionIsDeduplicated() {
        myFixture.addFileToProject("app/code/Test/Events/etc/events.xml",
                "<config><event name=\"portability_shared_event\"/>"
                        + "<event name=\"portability_declared_event\"/></config>");
        myFixture.addFileToProject("app/code/Test/Events/Dispatch.php",
                "<?php $events->dispatch('portability_shared_event');"
                        + "$events->dispatch('portability_dispatched_event');");
        myFixture.configureByText(ModuleEventsXml.FILE_NAME,
                "<config><event name=\"portability_<caret>\"/></config>");
        myFixture.completeBasic();

        final List<String> lookupStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupStrings);
        assertContainsElements(lookupStrings,
                "portability_shared_event", "portability_declared_event", "portability_dispatched_event");
        assertEquals(1, Collections.frequency(lookupStrings, "portability_shared_event"));
    }

}
