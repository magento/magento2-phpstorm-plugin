/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.ResourceModelData;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;

public class ModuleResourceModelGeneratorTest extends BaseGeneratorTestCase {

    private static final String EXPECTED_DIR = "src/app/code/Foo/Bar/Model/ResourceModel";
    private static final String ENTITY_ID_COLUMN = "entity_id";
    private static final String ENTITY_DTO_TYPE = "Foo\\Bar\\Model\\Data\\EntityData";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final EntityCreatorContext context = new EntityCreatorContext();
        context.putUserData(EntityCreatorContext.DTO_TYPE, ENTITY_DTO_TYPE);
        context.putUserData(EntityCreatorContext.ENTITY_ID, ENTITY_ID_COLUMN);
        GenerationContextRegistry.getInstance().setContext(context);
    }

    /**
     * Test generation of resource model file.
     */
    public void testGenerateFile() {
        GenerationContextRegistry.getInstance().setContext(null);
        final Project project = myFixture.getProject();
        final ResourceModelData resourceModelData = new ResourceModelData(
                "Foo_Bar",
                "my_table",
                "TestResourceModel",
                ENTITY_ID_COLUMN
        );
        final ModuleResourceModelGenerator generator = new ModuleResourceModelGenerator(
                resourceModelData,
                project
        );
        final PsiFile resourceModelFile = generator.generate("test");
        final String filePath = this.getFixturePath("TestResourceModel.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIR,
                resourceModelFile
        );
    }

    /**
     * Test generation of resource model file with reference to entity id column.
     */
    public void testGenerateFileWithDtoReference() {
        final Project project = myFixture.getProject();
        final ResourceModelData resourceModelData = new ResourceModelData(
                "Foo_Bar",
                "my_table",
                "TestResourceModel",
                ENTITY_ID_COLUMN
        );
        final ModuleResourceModelGenerator generator = new ModuleResourceModelGenerator(
                resourceModelData,
                project
        );
        final PsiFile resourceModelFile = generator.generate("test");
        final String filePath = this.getFixturePath("TestResourceModel.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIR,
                resourceModelFile
        );
    }
}
