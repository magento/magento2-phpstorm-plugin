/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.actions.generation.data.AdminListViewEntityActionData;
import com.magento.idea.magento2plugin.magento.files.actions.AdminListViewActionFile;

public class AdminListViewEntityControllerGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Book";
    private static final String NAMESPACE = "Foo\\Bar\\Controller\\Adminhtml\\" + ENTITY_NAME;
    private static final String ACL = "Foo_Bar::book_management";
    private static final String MENU = "Foo_Bar::book_management_menu";
    private static final String CLASS_FQN = NAMESPACE + "\\" + AdminListViewActionFile.CLASS_NAME;

    /**
     * Test generation of list view entity controller (adminhtml).
     */
    public void testGenerateListViewControllerFile() {
        final AdminListViewActionFile file = new AdminListViewActionFile(ENTITY_NAME);
        final AdminListViewEntityActionData data = new AdminListViewEntityActionData(
                MODULE_NAME,
                ENTITY_NAME,
                NAMESPACE,
                CLASS_FQN,
                ACL,
                MENU
        );
        final String filePath = this.getFixturePath(file.getFileName());
        final AdminListViewEntityActionGenerator generator =
                new AdminListViewEntityActionGenerator(data, myFixture.getProject(), false);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                "src/app/code/Foo/Bar/".concat(file.getDirectory()),
                generator.generate("test")
        );
    }
}
