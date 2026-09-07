/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CategoryEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.CategoryFormXmlData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeScope;

public class CategoryAttributePropertySetupPatchGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";

    /**
     * Tests the generated patch file.
     */
    public void testGenerateFile() {
        final Project project = myFixture.getProject();

        final CategoryEntityData categoryEntityData = new CategoryEntityData();
        categoryEntityData.setCode("test_attribute");
        categoryEntityData.setInput("text");
        categoryEntityData.setVisible(true);
        categoryEntityData.setLabel("Test Attribute");
        categoryEntityData.setType("static");
        categoryEntityData.setRequired(false);
        categoryEntityData.setGroup("Content");
        categoryEntityData.setSortOrder(10);
        categoryEntityData.setScope(AttributeScope.GLOBAL.getScope());

        categoryEntityData.setDataPatchName("AddTestAttributeCategoryAttribute");
        categoryEntityData.setModuleName(MODULE_NAME);


        final EavAttributeSetupPatchGenerator setupPatchGenerator =
                new EavAttributeSetupPatchGenerator(categoryEntityData, project);
        final PsiFile dataPatchFile = setupPatchGenerator.generate("testGenerateFile");

        final String filePatch = this.getFixturePath("AddTestAttributeCategoryAttribute.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Setup/Patch/Data",
                dataPatchFile
        );
    }

    /**
     * Tests the generated form file.
     */
    public void testGenerateFormFile() {
        final Project project = myFixture.getProject();

        final CategoryEntityData categoryEntityData = new CategoryEntityData();
        categoryEntityData.setCode("test_attribute");
        categoryEntityData.setInput("text");
        categoryEntityData.setVisible(true);
        categoryEntityData.setLabel("Test Attribute");
        categoryEntityData.setType("static");
        categoryEntityData.setRequired(false);
        categoryEntityData.setGroup("Content");
        categoryEntityData.setSortOrder(10);
        categoryEntityData.setScope(AttributeScope.GLOBAL.getScope());

        categoryEntityData.setDataPatchName("AddTestAttributeCategoryAttribute");
        categoryEntityData.setModuleName(MODULE_NAME);

        final CategoryFormXmlData categoryFormXmlData = new CategoryFormXmlData(
                categoryEntityData.getGroup(),
                categoryEntityData.getCode(),
                categoryEntityData.getInput(),
                categoryEntityData.getSortOrder()
        );

        final CategoryFormXmlGenerator categoryFormXmlGenerator =
                new CategoryFormXmlGenerator(categoryFormXmlData, project,  MODULE_NAME);
        final PsiFile categoryForm = categoryFormXmlGenerator.generate("category_form");

        final String fileCategoryForm = this.getFixturePath("category_form.xml");
        final PsiFile expectedCategoryFile = myFixture.configureByFile(fileCategoryForm);

        assertGeneratedFileIsCorrect(
                expectedCategoryFile,
                "src/app/code/Foo/Bar/view/adminhtml/ui_component",
                categoryForm
        );
    }
}
