/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityControllerFileData;
import com.magento.idea.magento2plugin.magento.files.actions.SaveActionFile;

public class SaveEntityActionGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Company";
    private static final String DTO_NAME = "CompanyData";
    private static final String DTO_TYPE_INTERFACE = "CompanyInterface";
    private static final String EXPECTED_DIRECTORY =
            "/src/app/code/Foo/Bar/Controller/Adminhtml/" + ENTITY_NAME;
    private static final String ACL = "Foo_Bar::company_id";
    private static final String ENTITY_ID = "company_id";

    /**
     * Test generation of Save controller for entity without interface.
     */
    public void testGenerateSaveEntityActionWithoutInterfaceFile() {
        final SaveEntityControllerFileData saveEntityControllerFileData =
                new SaveEntityControllerFileData(
                        ENTITY_NAME,
                        MODULE_NAME,
                        ACL,
                        ENTITY_ID,
                        DTO_NAME,
                        "",
                        false,
                        false
                );
        final SaveEntityControllerFileGenerator saveEntityControllerFileGenerator =
                new SaveEntityControllerFileGenerator(
                        saveEntityControllerFileData,
                        myFixture.getProject(),
                        false
                );
        final PsiFile saveEntityActionFile = saveEntityControllerFileGenerator.generate("test");
        final String filePath = this.getFixturePath(
                new SaveActionFile(MODULE_NAME, ENTITY_NAME).getFileName()
        );
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                saveEntityActionFile
        );
    }

    /**
     * Test generation of Save controller for entity with interface.
     */
    public void testGenerateSaveEntityActionWithInterfaceFile() {
        final SaveEntityControllerFileData saveEntityControllerFileData =
                new SaveEntityControllerFileData(
                        ENTITY_NAME,
                        MODULE_NAME,
                        ACL,
                        ENTITY_ID,
                        DTO_NAME,
                        DTO_TYPE_INTERFACE,
                        true,
                        false
                );
        final SaveEntityControllerFileGenerator saveEntityControllerFileGenerator =
                new SaveEntityControllerFileGenerator(
                        saveEntityControllerFileData,
                        myFixture.getProject(),
                        false
                );
        final PsiFile saveEntityActionFile = saveEntityControllerFileGenerator.generate("test");
        final String filePath = this.getFixturePath(
                new SaveActionFile(MODULE_NAME, ENTITY_NAME).getFileName()
        );
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                saveEntityActionFile
        );
    }
}
