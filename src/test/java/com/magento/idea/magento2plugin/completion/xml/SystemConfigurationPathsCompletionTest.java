/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import org.junit.jupiter.api.Test;

import com.magento.idea.magento2plugin.magento.files.ModuleConfigXmlFile;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;

public class SystemConfigurationPathsCompletionTest extends CompletionXmlFixtureTestCase {

    /**
     * Test system xml section element completion.
     */
    @Test
    public void testSystemXmlSectionMustProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleSystemXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);
        assertCompletionContains(filePath, "catalog");
    }

    /**
     * Test system xml group element completion.
     */
    @Test
    public void testSystemXmlGroupMustProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleSystemXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);
        assertCompletionContains(filePath, "frontend");
    }

    /**
     * Test system xml field element completion.
     */
    @Test
    public void testSystemXmlFieldMustProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleSystemXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);
        assertCompletionContains(filePath, "list_allow_all");
    }

    /**
     * Test config xml section element completion.
     */
    @Test
    public void testConfigXmlSectionMustProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleConfigXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);
        assertCompletionContains(filePath, "catalog");
    }

    /**
     * Test config xml group element completion.
     */
    @Test
    public void testConfigXmlGroupMustProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleConfigXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);
        assertCompletionContains(filePath, "frontend");
    }

    /**
     * Test config xml field element completion.
     */
    @Test
    public void testConfigXmlFieldMustProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleConfigXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);
        assertCompletionContains(filePath, "list_allow_all");
    }
}
