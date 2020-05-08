/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ObserverEventsXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;

public class ObserverEventsXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String MODULE_DIR = "src/app/code/Foo/Bar/";

    /**
     * Test checks whether 2 events.xml is generated correctly for the base area.
     */
    public void testGenerateEventsXmlInBaseAreaFile() {
        final String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final String area = Areas.base.toString();
        final PsiFile eventsXml = addEventToEventsXml(
                project,
                area,
                "test_event",
                "test_observer",
                "Foo\\Bar\\Observer\\Test\\TestEventObserver"
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                MODULE_DIR + Package.moduleBaseAreaDir,
                eventsXml
        );
    }

    /**
     * Test checks whether 2 events.xml is generated correctly for the adminhtml area.
     */
    public void testGenerateEventsXmlInAdminhtmlAreaFile() {
        final String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final String area = Areas.adminhtml.toString();
        final PsiFile eventsXml = addEventToEventsXml(
                project,
                area,
                "test_event",
                "test_observer",
                "Foo\\Bar\\Observer\\Test\\TestEventObserver"
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                MODULE_DIR + Package.moduleBaseAreaDir + File.separator + area,
                eventsXml
        );
    }

    /**
     * Test checks whether 2 events.xml is generated correctly with 2 observers.
     */
    public void testAddTwoObserversToOneEventsXml() {
        final String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        addEventToEventsXml(
                project,
                Areas.frontend.toString(),
                "test_event",
                "test_observer",
                "Foo\\Bar\\Observer\\Test\\TestEventObserver"
        );
        final PsiFile eventsXml = addEventToEventsXml(
                project,
                Areas.frontend.toString(),
                "test_event_2",
                "test_observer_2",
                "Foo\\Bar\\Observer\\Test\\TestEventObserverTwo"
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                MODULE_DIR + Package.moduleBaseAreaDir + File.separator + Areas.frontend.toString(),
                eventsXml
        );
    }

    private PsiFile addEventToEventsXml(
            final Project project,
            final String area,
            final String eventName,
            final String observerName,
            final String observerClassFqn
    ) {
        final ObserverEventsXmlData observerEventsXmlData = new ObserverEventsXmlData(
                area,
                MODULE_NAME,
                eventName,
                observerName,
                observerClassFqn
        );
        final ObserverEventsXmlGenerator observerEventsXmlGenerator =
                new ObserverEventsXmlGenerator(
                    observerEventsXmlData,
                    project
        );

        return  observerEventsXmlGenerator.generate("test");
    }
}
