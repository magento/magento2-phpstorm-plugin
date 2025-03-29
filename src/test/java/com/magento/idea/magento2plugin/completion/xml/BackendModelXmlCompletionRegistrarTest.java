/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleConfigXml;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;

public class BackendModelXmlCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] SYSTEM_XML_BACKEND_MODEL_LOOKUP_STRING_CHECK = {
            "Magento\\Backend\\Model\\Source\\Roles"
    };
    private static final String[] CONFIG_XML_BACKEND_MODEL_LOOKUP_STRING_CHECK = {
            "Magento\\Backend\\Model\\Source\\YesNo"
    };

    /**
     * Test system.xml file element completion.
     */
    public void testSystemXmlElementProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleSystemXmlFile.FILE_NAME);
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath, SYSTEM_XML_BACKEND_MODEL_LOOKUP_STRING_CHECK);
    }

    /**
     * Test system.xml file element completion won't show.
     */
    public void testSystemXmlElementCompletionWontShow() {
        final String filePath = this.getFixturePath(
                ModuleSystemXmlFile.FILE_NAME
        );
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    /**
     * Test system.xml backend model attribute match with the file.
     */
    public void testSystemXmlBackendModelAttributeMatchWithFile() {
        final String filePath = this.getFixturePath(
                ModuleSystemXmlFile.FILE_NAME
        );

        assertFileContainsCompletions(filePath, SYSTEM_XML_BACKEND_MODEL_LOOKUP_STRING_CHECK);
    }

    /**
     * Test system.xml backend model attribute doesn't match with the file.
     */
    public void testSystemXmlBackendModelAttributeDontMatchWithFile() {
        final String filePath = this.getFixturePath(
                "other-file-than-system.xml"
        );

        assertFileNotContainsCompletions(
                filePath,
                SYSTEM_XML_BACKEND_MODEL_LOOKUP_STRING_CHECK
        );
    }

    /**
     * Test config.xml file element completion.
     */
    public void testConfigXmlElementProvideCompletion() {
        final String filePath = this.getFixturePath(
                ModuleConfigXml.FILE_NAME
        );
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, CONFIG_XML_BACKEND_MODEL_LOOKUP_STRING_CHECK);
    }

    /**
     * Test config.xml file element completion won't show.
     */
    public void testConfigXmlElementCompletionWontShow() {
        final String filePath = this.getFixturePath(
                ModuleConfigXml.FILE_NAME
        );
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    /**
     * Test config.xml backend model attribute match with the file.
     */
    public void testConfigXmlBackendModelAttributeMatchWithFile() {
        final String filePath = this.getFixturePath(
                ModuleConfigXml.FILE_NAME
        );

        assertFileContainsCompletions(filePath, CONFIG_XML_BACKEND_MODEL_LOOKUP_STRING_CHECK);
    }

    /**
     * Test config.xml backend model attribute doesn't match with the file.
     */
    public void testConfigXmlBackendModelAttributeDontMatchWithFile() {
        final String filePath = this.getFixturePath(
                "other-file-than-config.xml"
        );

        assertFileNotContainsCompletions(
                filePath,
                CONFIG_XML_BACKEND_MODEL_LOOKUP_STRING_CHECK
        );
    }
}
