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
    private static final String moduleName = "Foo_Bar";
    private static final String moduleDir = "src/app/code/Foo/Bar/";

    public void testGenerateEventsXmlInBaseAreaFile() {
        String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        Project project = myFixture.getProject();
        String area = Areas.base.toString();
        PsiFile eventsXml = addEventToEventsXml(
                project,
                area,
                "test_event",
                "test_observer",
                "Foo\\Bar\\Observer\\Test\\TestEventObserver"
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                moduleDir + Package.moduleBaseAreaDir,
                eventsXml
        );
    }

    public void testGenerateEventsXmlInAdminhtmlAreaFile() {
        String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        Project project = myFixture.getProject();
        String area = Areas.adminhtml.toString();
        PsiFile eventsXml = addEventToEventsXml(
                project,
                area,
                "test_event",
                "test_observer",
                "Foo\\Bar\\Observer\\Test\\TestEventObserver"
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                moduleDir + Package.moduleBaseAreaDir + File.separator + area,
                eventsXml
        );
    }

    public void testAddTwoObserversToOneEventsXml() {
        String filePath = this.getFixturePath(ModuleEventsXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        Project project = myFixture.getProject();
        addEventToEventsXml(
                project,
                Areas.frontend.toString(),
                "test_event",
                "test_observer",
                "Foo\\Bar\\Observer\\Test\\TestEventObserver"
        );
        PsiFile eventsXml = addEventToEventsXml(
                project,
                Areas.frontend.toString(),
                "test_event_2",
                "test_observer_2",
                "Foo\\Bar\\Observer\\Test\\TestEventObserverTwo"
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                moduleDir + Package.moduleBaseAreaDir + File.separator + Areas.frontend.toString(),
                eventsXml
        );
    }

    private PsiFile addEventToEventsXml(
            Project project,
            String area,
            String eventName,
            String observerName,
            String observerClassFqn
    ) {
        ObserverEventsXmlData observerEventsXmlData = new ObserverEventsXmlData(
                area,
                moduleName,
                eventName,
                observerName,
                observerClassFqn
        );
        ObserverEventsXmlGenerator observerEventsXmlGenerator = new ObserverEventsXmlGenerator(
                observerEventsXmlData,
                project
        );

        return  observerEventsXmlGenerator.generate("test");
    }
}
