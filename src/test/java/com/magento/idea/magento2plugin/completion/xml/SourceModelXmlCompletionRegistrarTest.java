/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import org.junit.jupiter.api.Test;

import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;
import com.magento.idea.magento2plugin.magento.files.ModuleWidgetXml;

public class SourceModelXmlCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] LOOKUP_STRINGS_CHECK = {
            "Magento\\Backend\\Model\\Source\\YesNo"
    };

    /**
     * Test source model xml element completion.
     */
    @Test
    public void testSourceModelXmlElementMustProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleSystemXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, LOOKUP_STRINGS_CHECK);
    }

    /**
     * Test source model xml element completion match with the file false positive.
     */
    @Test
    public void testSourceModelXmlElementMatchWithFilePositiveCase() {
        final String filePath = this.getFixturePath(ModuleSystemXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, LOOKUP_STRINGS_CHECK);
    }

    /**
     * Test source model xml element completion match with the file negative case.
     */
    @Test
    public void testSourceModelXmlElementMatchWithFileNegativeCase() {
        final String filePath = this.getFixturePath("not-system.xml");
        myFixture.copyFileToProject(filePath);

        assertFileNotContainsCompletions(
                filePath,
                LOOKUP_STRINGS_CHECK
        );
    }

    /**
     * Test source model attribute must provide completion.
     */
    @Test
    public void testSourceModelXmlAttributeMustProvideCompletion() {
        final String filePath = this.getFixturePath(ModuleWidgetXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, LOOKUP_STRINGS_CHECK);
    }

    /**
     * Test source model attribute match with the file positive case.
     */
    @Test
    public void testSourceModelXmlAttributeMatchWithFilePositiveCase() {
        final String filePath = this.getFixturePath(ModuleWidgetXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, LOOKUP_STRINGS_CHECK);
    }

    /**
     * Test source model attribute match with the file negative case.
     */
    @Test
    public void testSourceModelXmlAttributeMatchWithFileNegativeCase() {
        final String filePath = this.getFixturePath("not-widget.xml");
        myFixture.copyFileToProject(filePath);

        assertFileNotContainsCompletions(
                filePath,
                LOOKUP_STRINGS_CHECK
        );
    }
}
