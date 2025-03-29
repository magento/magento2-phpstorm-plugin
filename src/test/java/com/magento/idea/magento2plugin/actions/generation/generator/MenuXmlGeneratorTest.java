/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.MenuXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;

public class MenuXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc/adminhtml";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ACL_ID = "Foo_Bar::acl";
    private static final String MENU_IDENTIFIER = "Foo_Bar::menu";
    private static final String MENU_TITLE = "My Title";
    private static final String PARENT_MENU = "Magento_Customer::customer";
    private static final String ACTION_NAME = "test";

    /**
     * Test checks whether menu.xml file generated correctly.
     */
    public void testGenerateMenuXmlFile() {
        final String filePath = this.getFixturePath(ModuleMenuXml.fileName);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                generateMenuXmlFile()
        );
    }

    /**
     * Generate Menu XML file.
     *
     * @return PsiFile
     */
    private PsiFile generateMenuXmlFile() {
        final MenuXmlGenerator menuXmlGenerator = new MenuXmlGenerator(
                new MenuXmlData(
                        PARENT_MENU,
                        "10",
                        MODULE_NAME,
                        MENU_IDENTIFIER,
                        MENU_TITLE,
                        ACL_ID,
                        ACTION_NAME
                ),
                myFixture.getProject()
        );
        return menuXmlGenerator.generate(ACTION_NAME);
    }
}
