/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.NewActionEntityControllerFileData;
import com.magento.idea.magento2plugin.magento.files.actions.NewActionFile;

public class NewActionEntityGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Company";
    private static final String EXPECTED_DIRECTORY =
            "/src/app/code/Foo/Bar/Controller/Adminhtml/" + ENTITY_NAME;
    private static final String NAMESPACE =
            "Foo\\Bar\\Controller\\Adminhtml\\" + ENTITY_NAME;
    private static final String ACL = "Foo_Bar::company_id";
    private static final String MENU = "Foo_Bar::menu_id";


    /**
     * Test generation of NewAction controller.
     */
    public void testGenerateNewActionEntityFile() {
        final NewActionEntityControllerFileData newActionEntityControllerFileData =
                new NewActionEntityControllerFileData(
                        ENTITY_NAME,
                        MODULE_NAME,
                        NAMESPACE,
                        ACL,
                        MENU
                );
        final NewActionEntityControllerFileGenerator newActionEntityControllerFileGenerator =
                new NewActionEntityControllerFileGenerator(
                        newActionEntityControllerFileData,
                        myFixture.getProject()
                );
        final PsiFile newActionEntityActionFile =
                newActionEntityControllerFileGenerator.generate("test");
        final String filePath = this.getFixturePath(
                new NewActionFile(MODULE_NAME, ENTITY_NAME).getFileName()
        );
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                newActionEntityActionFile
        );
    }
}
