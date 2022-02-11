/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;
import com.magento.idea.magento2plugin.magento.files.ModuleWidgetXml;
import java.io.IOException;

public class SourceModelXmlCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStringsCheck = new String[]{
            "Magento\\Backend\\Model\\Source\\YesNo"
    };

    public void testSourceModelXmlElementMustProvideCompletion() {
        String filePath = this.getFixturePath(ModuleSystemXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, lookupStringsCheck);
    }

    public void testSourceModelXmlElementMatchWithFilePositiveCase() {
        String filePath = this.getFixturePath(ModuleSystemXmlFile.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsCheck);
    }

    public void testSourceModelXmlElementMatchWithFileNegativeCase() {
        String filePath = this.getFixturePath("not-system.xml");
        myFixture.copyFileToProject(filePath);

        assertFileNotContainsCompletions(
            filePath,
            lookupStringsCheck
        );
    }

    public void testSourceModelXmlAttributeMustProvideCompletion() {
        String filePath = this.getFixturePath(ModuleWidgetXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, lookupStringsCheck);
    }

    public void testSourceModelXmlAttributeMatchWithFilePositiveCase() {
        String filePath = this.getFixturePath(ModuleWidgetXml.FILE_NAME);
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsCheck);
    }

    public void testSourceModelXmlAttributeMatchWithFileNegativeCase() throws IOException {
        String filePath = this.getFixturePath("not-widget.xml");
        myFixture.copyFileToProject(filePath);

        assertFileNotContainsCompletions(
            filePath,
            lookupStringsCheck
        );
    }
}
