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
    private static final String EXPECTED_DIRECTORY =
            "/src/app/code/Foo/Bar/Controller/Adminhtml/" + ENTITY_NAME;
    private static final String NAMESPACE =
            "Foo\\Bar\\Controller\\Adminhtml\\" + ENTITY_NAME;
    private static final String SAVE_COMMAND =
            "Foo\\Bar\\Command\\" + ENTITY_NAME + "\\SaveCommand";
    private static final String DTO_TYPE =
            "Foo\\Bar\\Model\\" + ENTITY_NAME + "Model" + "\\CompanyModel";
    private static final String DTO_TYPE_INTERFACE =
            "Foo\\Bar\\Api\\Data\\" + ENTITY_NAME + "Interface";
    private static final String ACL = "company_id";
    private static final String ENTITY_ID = "entity_id";

    /**
     * Test generation of Save controller for entity without interface.
     */
    public void testGenerateSaveEntityActionWithoutInterfaceFile() {
        final SaveEntityControllerFileData saveEntityControllerFileData =
                new SaveEntityControllerFileData(
                        ENTITY_NAME,
                        MODULE_NAME,
                        NAMESPACE,
                        SAVE_COMMAND,
                        DTO_TYPE,
                        ACL,
                        ENTITY_ID
                );
        final SaveEntityControllerFileGenerator saveEntityControllerFileGenerator =
                new SaveEntityControllerFileGenerator(
                        saveEntityControllerFileData,
                        myFixture.getProject(),
                        false
                );
        final PsiFile saveEntityActionFile = saveEntityControllerFileGenerator.generate("test");
        final String filePath = this.getFixturePath(SaveActionFile.getInstance().getFileName());
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
                        NAMESPACE,
                        SAVE_COMMAND,
                        DTO_TYPE_INTERFACE,
                        ACL,
                        ENTITY_ID
                );
        final SaveEntityControllerFileGenerator saveEntityControllerFileGenerator =
                new SaveEntityControllerFileGenerator(
                        saveEntityControllerFileData,
                        myFixture.getProject(),
                        true
                );
        final PsiFile saveEntityActionFile = saveEntityControllerFileGenerator.generate("test");
        final String filePath = this.getFixturePath(SaveActionFile.getInstance().getFileName());
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                saveEntityActionFile
        );
    }
}
