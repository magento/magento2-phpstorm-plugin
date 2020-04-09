/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXml;
import com.magento.idea.magento2plugin.magento.files.ModuleWidgetXml;

import java.io.File;
import java.io.IOException;

public class SourceModelXmlCompletionRegistrarTest extends CompletionLightJavaCodeInsightFixtureTestCase {

    private static VirtualFile systemXmlVirtualFile;
    private static VirtualFile widgetXmlVirtualFile;
    private static final String testDataFolderPath;
    private static final String fixturesFolderPath;
    private static final String classesSourceFile;
    private static final String[] lookupStringsCheck;

    static {
        testDataFolderPath = "testData/completion/common/";
        fixturesFolderPath = "xml/SourceModelXmlCompletionRegistrar";
        classesSourceFile = "classes.php";
        lookupStringsCheck = new String[]{
                "Magento\\Config\\Model\\Config\\Source\\Yesno"
        };
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject(classesSourceFile);
    }

    @Override
    protected String getTestDataPath() {
        return testDataFolderPath;
    }

    protected String getFixturePath(String folderName, String fileName) {
        return fixturesFolderPath + File.separator + folderName + File.separator + fileName;
    }

    public void testSourceModelXmlElementMustProvideCompletion() {
        String filePath = this.getFixturePath(
            "sourceModelXmlElementMustProvideCompletion",
            ModuleSystemXml.FILE_NAME
        );
        systemXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, lookupStringsCheck);
    }

    public void testSourceModelXmlElementMatchWithFilePositiveCase() {
        String filePath = this.getFixturePath(
                "sourceModelXmlElementMatchWithFilePositiveCase",
                ModuleSystemXml.FILE_NAME
        );
        systemXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsCheck);
    }

    public void testSourceModelXmlElementMatchWithFileNegativeCase() {
        String filePath = this.getFixturePath(
            "sourceModelXmlElementMatchWithFileNegativeCase",
            "not-system.xml"
        );
        systemXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFileNegativeCase(
            filePath,
            lookupStringsCheck
        );
    }

    public void testSourceModelXmlAttributeMustProvideCompletion() {
        String filePath = this.getFixturePath(
                "sourceModelXmlAttributeMustProvideCompletion",
                ModuleWidgetXml.FILE_NAME
        );
        widgetXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, lookupStringsCheck);
    }

    public void testSourceModelXmlAttributeMatchWithFilePositiveCase() {
        String filePath = this.getFixturePath(
                "sourceModelXmlAttributeMatchWithFilePositiveCase",
                ModuleWidgetXml.FILE_NAME
        );
        widgetXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsCheck);
    }

    public void testSourceModelXmlAttributeMatchWithFileNegativeCase() throws IOException {
        String filePath = this.getFixturePath(
            "sourceModelXmlAttributeMatchWithFileNegativeCase",
            "not-widget.xml"
        );
        widgetXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFileNegativeCase(
            filePath,
            lookupStringsCheck
        );
    }
}
