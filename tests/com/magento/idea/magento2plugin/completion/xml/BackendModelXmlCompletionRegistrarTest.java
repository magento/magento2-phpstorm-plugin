/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.magento.files.ModuleConfigXml;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXml;
import java.io.File;

public class BackendModelXmlCompletionRegistrarTest extends CompletionLightJavaCodeInsightFixtureTestCase {

    private static VirtualFile systemXmlVirtualFile;
    private static VirtualFile configXmlVirtualFile;
    private static final String testDataFolderPath;
    private static final String fixturesFolderPath;
    private static final String classesSourceFile;
    private static final String[] systemXmlBackendModelLookupStringCheck;
    private static final String[] configXmlBackendModelLookupStringCheck;

    static {
        testDataFolderPath = "testData/completion/common/";
        fixturesFolderPath = "xml/BackendModelXmlCompletionRegistrar";
        classesSourceFile = "classes.php";
        systemXmlBackendModelLookupStringCheck = new String[]{
                "Magento\\Backend\\Model\\Source\\Roles"
        };
        configXmlBackendModelLookupStringCheck = new String[]{
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

    public void testSystemXmlBackendModelElementProvideCompletion() {
        String filePath = this.getFixturePath("systemXmlElementProvideCompletion", ModuleSystemXml.FILE_NAME);
        systemXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, systemXmlBackendModelLookupStringCheck);
    }

    public void testSystemXmlBackendModelElementCompletionWontShow() {
        String filePath = this.getFixturePath(
            "systemXmlElementCompletionWontShow",
            ModuleSystemXml.FILE_NAME
        );
        systemXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    public void testSystemXmlBackendModelAttributeMatchWithFile() {
        String filePath = this.getFixturePath(
            "systemXmlBackendModelAttributeMatchWithFile",
            ModuleSystemXml.FILE_NAME
        );

        assertCompletionMatchWithFilePositiveCase(filePath, systemXmlBackendModelLookupStringCheck);
    }

    public void testSystemXmlBackendModelAttributeDontMatchWithFile() {
        String filePath = this.getFixturePath(
            "systemXmlBackendModelAttributeDontMatchWithFile",
            "other-file-than-system.xml"
        );

        assertCompletionMatchWithFileNegativeCase(
                filePath,
                systemXmlBackendModelLookupStringCheck
        );
    }

    public void testConfigXmlBackendModelElementProvideCompletion() {
        String filePath = this.getFixturePath(
            "configXmlElementProvideCompletion",
            ModuleConfigXml.FILE_NAME
        );
        configXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, configXmlBackendModelLookupStringCheck);
    }

    public void testConfigXmlBackendModelElementCompletionWontShow() {
        String filePath = this.getFixturePath(
            "configXmlElementCompletionWontShow",
            ModuleConfigXml.FILE_NAME
        );
        configXmlVirtualFile = myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    public void testConfigXmlBackendModelAttributeMatchWithFile() {
        String filePath = this.getFixturePath(
            "configXmlBackendModelAttributeMatchWithFile",
            ModuleConfigXml.FILE_NAME
        );

        assertCompletionMatchWithFilePositiveCase(filePath, configXmlBackendModelLookupStringCheck);
    }

    public void testConfigXmlBackendModelAttributeDontMatchWithFile() {
        String filePath = this.getFixturePath(
            "configXmlBackendModelAttributeDontMatchWithFile",
            "other-file-than-config.xml"
        );

        assertCompletionMatchWithFileNegativeCase(
                filePath,
                configXmlBackendModelLookupStringCheck
        );
    }
}
