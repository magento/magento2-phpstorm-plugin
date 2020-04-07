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
    private static final String fixturesFolderPath;
    private static final String classesSourceFile;
    private static final String multipleCompletionFolder;
    private static final String[] singleLookupStringsCheck;
    private static final String[] multipleLookupStringsCheck;

    static {
        multipleCompletionFolder = "multipleCompletion";
        fixturesFolderPath = "tests/com/magento/idea/magento2plugin/completion/xml/fixtures";
        classesSourceFile = "classes.php";
        singleLookupStringsCheck = new String[]{
                "Magento\\Config\\Model\\Config\\Source\\Yesno"
        };
        multipleLookupStringsCheck = new String[]{
                "Magento\\Backend\\Model\\Source\\Roles",
                "Magento\\Customer\\Model\\Source\\Roles",
                "Magento\\B2b\\Model\\Source\\Roles"
        };
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject(classesSourceFile);
        systemXmlVirtualFile = myFixture.copyFileToProject(ModuleSystemXml.FILE_NAME);
        widgetXmlVirtualFile = myFixture.copyFileToProject(ModuleWidgetXml.FILE_NAME);
    }

    @Override
    protected String getTestDataPath() {
        return fixturesFolderPath;
    }

    public void testThatSourceModelXmlElementMustProvideCompletion() {
        assertCompletionContains(ModuleSystemXml.FILE_NAME, singleLookupStringsCheck);
    }

    public void testThatSourceModelXmlElementMustProvideMultipleCompletion() {
        String multipleCompletionFile = String.join(
                File.separator,
                new String[]{multipleCompletionFolder, ModuleSystemXml.FILE_NAME}
        );
        assertCompletionContains(multipleCompletionFile, multipleLookupStringsCheck);
    }

    public void testThatSourceModelXmlElementMuchWithFilePositiveCase() {
        assertCompletionMuchWithFilePositiveCase(ModuleSystemXml.FILE_NAME, singleLookupStringsCheck);
    }

    public void testThatSourceModelXmlElementMuchWithFileNegativeCase() throws IOException {
        String negativeFilePath = "not" + ModuleSystemXml.FILE_NAME;

        assertCompletionMuchWithFileNegativeCase(
                negativeFilePath,
                VfsUtil.loadText(systemXmlVirtualFile),
                singleLookupStringsCheck
        );
    }

    public void testThatSourceModelXmlAttributeMustProvideCompletion() {
        assertCompletionContains(ModuleWidgetXml.FILE_NAME, singleLookupStringsCheck);
    }

    public void testThatSourceModelXmlAttributeMustProvideMultipleCompletion() {
        String multipleCompletionFile = String.join(
                File.separator,
                new String[]{multipleCompletionFolder, ModuleWidgetXml.FILE_NAME}
        );

        assertCompletionContains(multipleCompletionFile, multipleLookupStringsCheck);
    }

    public void testThatSourceModelXmlAttributeMuchWithFilePositiveCase() {
        assertCompletionMuchWithFilePositiveCase(ModuleWidgetXml.FILE_NAME, singleLookupStringsCheck);
    }

    public void testThatSourceModelXmlAttributeMuchWithFileNegativeCase() throws IOException {
        String negativeFilePath = "not" + ModuleWidgetXml.FILE_NAME;

        assertCompletionMuchWithFileNegativeCase(
                negativeFilePath,
                VfsUtil.loadText(widgetXmlVirtualFile),
                singleLookupStringsCheck
        );
    }
}
