/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleConfigXml;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXml;

public class BackendModelXmlCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] systemXmlBackendModelLookupStringCheck = new String[]{
                "Magento\\Backend\\Model\\Source\\Roles"
        };
    private static final String[] configXmlBackendModelLookupStringCheck = new String[]{
                "Magento\\Backend\\Model\\Source\\YesNo"
    };

    public void testSystemXmlElementProvideCompletion() {
        String filePath = this.getFixturePath(ModuleSystemXml.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath, systemXmlBackendModelLookupStringCheck);
    }

    public void testSystemXmlElementCompletionWontShow() {
        String filePath = this.getFixturePath(
            ModuleSystemXml.FILE_NAME
        );
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    public void testSystemXmlBackendModelAttributeMatchWithFile() {
        String filePath = this.getFixturePath(
            ModuleSystemXml.FILE_NAME
        );

        assertFileConatainsCompletions(filePath, systemXmlBackendModelLookupStringCheck);
    }

    public void testSystemXmlBackendModelAttributeDontMatchWithFile() {
        String filePath = this.getFixturePath(
            "other-file-than-system.xml"
        );

        assertFileNotConatainsCompletions(
                filePath,
                systemXmlBackendModelLookupStringCheck
        );
    }

    public void testConfigXmlElementProvideCompletion() {
        String filePath = this.getFixturePath(
            ModuleConfigXml.FILE_NAME
        );
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, configXmlBackendModelLookupStringCheck);
    }

    public void testConfigXmlElementCompletionWontShow() {
        String filePath = this.getFixturePath(
            ModuleConfigXml.FILE_NAME
        );
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    public void testConfigXmlBackendModelAttributeMatchWithFile() {
        String filePath = this.getFixturePath(
            ModuleConfigXml.FILE_NAME
        );

        assertFileConatainsCompletions(filePath, configXmlBackendModelLookupStringCheck);
    }

    public void testConfigXmlBackendModelAttributeDontMatchWithFile() {
        String filePath = this.getFixturePath(
            "other-file-than-config.xml"
        );

        assertFileNotConatainsCompletions(
                filePath,
                configXmlBackendModelLookupStringCheck
        );
    }
}
