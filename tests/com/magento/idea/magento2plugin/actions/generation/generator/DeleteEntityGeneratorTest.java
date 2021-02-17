/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityControllerFileData;
import com.magento.idea.magento2plugin.magento.files.actions.DeleteActionFile;

public class DeleteEntityGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Company";
    private static final String ACL = "Foo_Bar::company_id";
    private static final String ENTITY_ID = "company_id";
    private static final String NAMESPACE =
            "Foo\\Bar\\Controller\\Adminhtml\\" + ENTITY_NAME;
    private static final String DELETE_COMMAND_FQN =
            "Foo\\Bar\\Command\\" + ENTITY_NAME + "\\DeleteByIdCommand";
    private static final String EXPECTED_DIRECTORY =
            "/src/app/code/Foo/Bar/Controller/Adminhtml/" + ENTITY_NAME;

    /**
     * Test generation of Delete controller.
     */
    public void testGenerateDeleteEntityFile() {
        final DeleteEntityControllerFileData deleteEntityControllerFileData =
                new DeleteEntityControllerFileData(
                        ENTITY_NAME,
                        MODULE_NAME,
                        NAMESPACE,
                        DELETE_COMMAND_FQN,
                        ACL,
                        ENTITY_ID
                );
        final DeleteEntityControllerFileGenerator deleteEntityControllerFileGenerator =
                new DeleteEntityControllerFileGenerator(
                        deleteEntityControllerFileData,
                        myFixture.getProject()
                );
        final PsiFile DeleteEntityActionFile =
                deleteEntityControllerFileGenerator.generate("test");
        final String filePath = this.getFixturePath(DeleteActionFile.getInstance().getFileName());
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                DeleteEntityActionFile
        );
    }
}
