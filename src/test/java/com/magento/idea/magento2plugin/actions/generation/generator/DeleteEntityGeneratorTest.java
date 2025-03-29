/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.magento.files.actions.DeleteActionFile;

public class DeleteEntityGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ENTITY_NAME = "Company";
    private static final String ENTITY_DTO_TYPE = "Foo\\Bar\\Model\\Data\\CompanyData";
    private static final String ACL = "Foo_Bar::company_id";
    private static final String ENTITY_ID = "company_id";
    private static final String EXPECTED_DIRECTORY =
            "/src/app/code/Foo/Bar/Controller/Adminhtml/" + ENTITY_NAME;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final EntityCreatorContext context = new EntityCreatorContext();
        context.putUserData(EntityCreatorContext.DTO_TYPE, ENTITY_DTO_TYPE);
        context.putUserData(EntityCreatorContext.ENTITY_ID, ENTITY_ID);
        GenerationContextRegistry.getInstance().setContext(context);
    }

    /**
     * Test generation of Delete controller.
     */
    public void testGenerateDeleteEntityFile() {
        final DeleteEntityControllerFileData deleteEntityControllerFileData =
                new DeleteEntityControllerFileData(
                        ENTITY_NAME,
                        MODULE_NAME,
                        ACL,
                        ENTITY_ID,
                        false
                );
        final DeleteEntityControllerFileGenerator deleteEntityControllerFileGenerator =
                new DeleteEntityControllerFileGenerator(
                        deleteEntityControllerFileData,
                        myFixture.getProject()
                );
        final PsiFile deleteEntityActionFile =
                deleteEntityControllerFileGenerator.generate("test");
        final String filePath =
                this.getFixturePath(new DeleteActionFile(MODULE_NAME, ENTITY_NAME).getFileName());
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                deleteEntityActionFile
        );
    }
}
