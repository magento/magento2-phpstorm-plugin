/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXml;
import com.magento.idea.magento2plugin.magento.files.ModuleWidgetXml;
import java.io.IOException;

public class SourceModelXmlCompletionRegistrarTest extends CompletionLightJavaCodeInsightFixtureTestCase {

    private static VirtualFile systemXmlVirtualFile;
    private static VirtualFile widgetXmlVirtualFile;
    private static final String fixturesFolderPath;
    private static final String classesSourceFile;
    private static final String[] lookupStringsCheck;

    static {
        fixturesFolderPath = "testData/completion/common/";
        classesSourceFile = "classes.php";
        lookupStringsCheck = new String[]{
                "Magento\\Config\\Model\\Config\\Source\\Yesno"
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

    public void testSourceModelXmlElementMustProvideCompletion() {
        assertCompletionContains(ModuleSystemXml.FILE_NAME, lookupStringsCheck);
    }

    public void testSourceModelXmlElementMatchWithFilePositiveCase() {
        assertCompletionMatchWithFilePositiveCase(ModuleSystemXml.FILE_NAME, lookupStringsCheck);
    }

    public void testSourceModelXmlElementMatchWithFileNegativeCase() throws IOException {
        String negativeFilePath = "not" + ModuleSystemXml.FILE_NAME;

        assertCompletionMatchWithFileNegativeCase(
                negativeFilePath,
                VfsUtil.loadText(systemXmlVirtualFile),
                lookupStringsCheck
        );
    }

    public void testSourceModelXmlAttributeMustProvideCompletion() {
        assertCompletionContains(ModuleWidgetXml.FILE_NAME, lookupStringsCheck);
    }

    public void testSourceModelXmlAttributeMatchWithFilePositiveCase() {
        assertCompletionMatchWithFilePositiveCase(ModuleWidgetXml.FILE_NAME, lookupStringsCheck);
    }

    public void testSourceModelXmlAttributeMatchWithFileNegativeCase() throws IOException {
        String negativeFilePath = "not" + ModuleWidgetXml.FILE_NAME;

        assertCompletionMatchWithFileNegativeCase(
                negativeFilePath,
                VfsUtil.loadText(widgetXmlVirtualFile),
                lookupStringsCheck
        );
    }
}
