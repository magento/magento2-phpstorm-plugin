/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ProductEntityData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeScopes;

public class EavAttributeSetupPatchGeneratorTest extends BaseGeneratorTestCase {

    /**
     * Test Data patch for product's eav attribute generator.
     */
    public void testGenerateFile() {
        final Project project = myFixture.getProject();

        final ProductEntityData productEntityData = new ProductEntityData();
        productEntityData.setCode("test");
        productEntityData.setVisibleInGrid(false);
        productEntityData.setHtmlAllowedOnFront(false);
        productEntityData.setVisibleOnFront(false);
        productEntityData.setVisible(true);
        productEntityData.setScope(AttributeScopes.GLOBAL.getScope());
        productEntityData.setLabel("Test Label");
        productEntityData.setType("static");
        productEntityData.setUsedInGrid(false);
        productEntityData.setRequired(false);
        productEntityData.setInput("boolean");
        productEntityData.setFilterableInGrid(false);
        productEntityData.setSortOrder(10);
        productEntityData.setGroup("General");

        productEntityData.setDataPatchName("AddTestAttribute");
        productEntityData.setNamespace("Foo\\Bar\\Setup\\Patch\\Data");
        productEntityData.setModuleName("Foo_Bar");

        final EavAttributeSetupPatchGenerator setupPatchGenerator =
                new EavAttributeSetupPatchGenerator(productEntityData, project);
        final PsiFile dataPatchFile = setupPatchGenerator.generate("test");

        final String filePatch = this.getFixturePath("AddTestAttribute.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(expectedFile, "src/app/code/Foo/Bar/Setup/Patch/Data", dataPatchFile);
    }
}
