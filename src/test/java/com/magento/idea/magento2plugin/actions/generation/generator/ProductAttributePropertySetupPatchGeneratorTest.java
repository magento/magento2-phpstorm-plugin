/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ProductEntityData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeScope;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import java.util.HashMap;
import java.util.Map;

public class ProductAttributePropertySetupPatchGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String LABEL = "Test Label";
    private static final String TYPE = "static";
    private static final int SORT_ORDER = 10;
    private static final String GROUP = "General";
    private static final String FILE_PATH = "src/app/code/Foo/Bar/Setup/Patch/Data";

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
        productEntityData.setScope(AttributeScope.GLOBAL.getScope());
        productEntityData.setLabel(LABEL);
        productEntityData.setType(TYPE);
        productEntityData.setUsedInGrid(false);
        productEntityData.setRequired(false);
        productEntityData.setInput("text");
        productEntityData.setFilterableInGrid(false);
        productEntityData.setSortOrder(SORT_ORDER);
        productEntityData.setGroup(GROUP);

        productEntityData.setDataPatchName("AddTestAttribute");
        productEntityData.setModuleName(MODULE_NAME);

        final EavAttributeSetupPatchGenerator setupPatchGenerator =
                new EavAttributeSetupPatchGenerator(productEntityData, project);
        final PsiFile dataPatchFile = setupPatchGenerator.generate("testGenerateFile");

        final String filePatch = this.getFixturePath("AddTestAttribute.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(expectedFile, FILE_PATH, dataPatchFile);
    }

    /**
     * Tests the generated file with the boolean source model.
     */
    public void testGenerateFileWithBooleanSourceModel() {
        final Project project = myFixture.getProject();

        final ProductEntityData productEntityData = new ProductEntityData();
        productEntityData.setCode("boolean_input_attribute");
        productEntityData.setVisibleInGrid(false);
        productEntityData.setHtmlAllowedOnFront(false);
        productEntityData.setVisibleOnFront(false);
        productEntityData.setVisible(true);
        productEntityData.setScope(AttributeScope.GLOBAL.getScope());
        productEntityData.setLabel(LABEL);
        productEntityData.setType(TYPE);
        productEntityData.setUsedInGrid(false);
        productEntityData.setRequired(false);
        productEntityData.setInput("boolean");
        productEntityData.setSource(AttributeSourceModel.BOOLEAN.getSource());
        productEntityData.setFilterableInGrid(false);
        productEntityData.setSortOrder(SORT_ORDER);
        productEntityData.setGroup(GROUP);

        productEntityData.setDataPatchName("AddBooleanInputAttributeAttribute");
        productEntityData.setModuleName(MODULE_NAME);

        final EavAttributeSetupPatchGenerator setupPatchGenerator =
                new EavAttributeSetupPatchGenerator(productEntityData, project);
        final PsiFile dataPatchFile = setupPatchGenerator.generate(
                "testGenerateFileWithBooleanSourceModel"
        );

        final String filePatch = this.getFixturePath("AddBooleanInputAttributeAttribute.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(expectedFile, FILE_PATH, dataPatchFile);
    }

    /**
     * Tests the generated file with the source model.
     */
    public void testGenerateFileWithGeneratedSourceModel() {
        final Project project = myFixture.getProject();

        final ProductEntityData productEntityData = new ProductEntityData();
        productEntityData.setCode("attribute_with_custom_source");
        productEntityData.setVisibleInGrid(false);
        productEntityData.setHtmlAllowedOnFront(false);
        productEntityData.setVisibleOnFront(false);
        productEntityData.setVisible(true);
        productEntityData.setScope(AttributeScope.GLOBAL.getScope());
        productEntityData.setLabel("Test Label");
        productEntityData.setType("static");
        productEntityData.setUsedInGrid(false);
        productEntityData.setRequired(false);
        productEntityData.setInput("text");
        productEntityData.setSource("\\Foo\\Bar\\Model\\Source\\AttributeWithCustomSource");
        productEntityData.setFilterableInGrid(false);
        productEntityData.setSortOrder(10);
        productEntityData.setGroup(GROUP);

        productEntityData.setDataPatchName("AddAttributeWithCustomSourceAttribute");
        productEntityData.setModuleName(MODULE_NAME);

        final EavAttributeSetupPatchGenerator setupPatchGenerator =
                new EavAttributeSetupPatchGenerator(productEntityData, project);
        final PsiFile dataPatchFile = setupPatchGenerator.generate(
                "testGenerateFileWithBooleanSourceModel"
        );

        final String filePatch = this.getFixturePath("AddAttributeWithCustomSourceAttribute.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(expectedFile, FILE_PATH, dataPatchFile);
    }

    /**
     * Tests file with the `apply to` attribute.
     */
    public void testGenerateFileWithApplyToAttribute() {
        final Project project = myFixture.getProject();

        final ProductEntityData productEntityData = new ProductEntityData();
        productEntityData.setCode("applied_to_attribute");
        productEntityData.setVisibleInGrid(false);
        productEntityData.setHtmlAllowedOnFront(false);
        productEntityData.setVisibleOnFront(false);
        productEntityData.setVisible(true);
        productEntityData.setScope(AttributeScope.GLOBAL.getScope());
        productEntityData.setLabel("Test Label");
        productEntityData.setType("static");
        productEntityData.setUsedInGrid(false);
        productEntityData.setRequired(false);
        productEntityData.setInput("text");
        productEntityData.setFilterableInGrid(false);
        productEntityData.setSortOrder(10);
        productEntityData.setGroup(GROUP);
        productEntityData.setApplyTo("configurable,simple");

        productEntityData.setDataPatchName("AddAppliedToAttribute");
        productEntityData.setModuleName(MODULE_NAME);

        final EavAttributeSetupPatchGenerator setupPatchGenerator =
                new EavAttributeSetupPatchGenerator(productEntityData, project);
        final PsiFile dataPatchFile = setupPatchGenerator.generate(
                "testGenerateFileWithApplyToAttribute"
        );

        final String filePatch = this.getFixturePath("AddAppliedToAttribute.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(expectedFile, FILE_PATH, dataPatchFile);
    }

    /**
     * Tests file with options.
     */
    public void testGenerateFileWithOptions() {
        final Project project = myFixture.getProject();

        final ProductEntityData productEntityData = new ProductEntityData();
        productEntityData.setVisibleInGrid(false);
        productEntityData.setHtmlAllowedOnFront(false);
        productEntityData.setVisibleOnFront(false);
        productEntityData.setVisible(true);
        productEntityData.setScope(AttributeScope.GLOBAL.getScope());
        productEntityData.setCode("attribute_with_options");
        productEntityData.setLabel("Attribute With Options");
        productEntityData.setType("varchar");
        productEntityData.setUsedInGrid(false);
        productEntityData.setRequired(false);
        productEntityData.setInput("multiselect");
        productEntityData.setSource(AttributeSourceModel.NULLABLE_SOURCE.getSource());
        productEntityData.setFilterableInGrid(false);
        productEntityData.setSortOrder(10);
        productEntityData.setGroup(GROUP);

        final Map<Integer, String> options = new HashMap<>();
        options.put(0, "option1");
        options.put(1, "option2");
        options.put(2, "option3");

        productEntityData.setOptions(options);

        productEntityData.setDataPatchName("AddAttributeWithOptionsAttribute");
        productEntityData.setModuleName(MODULE_NAME);

        final EavAttributeSetupPatchGenerator setupPatchGenerator =
                new EavAttributeSetupPatchGenerator(productEntityData, project);
        final PsiFile dataPatchFile = setupPatchGenerator.generate("testGenerateFileWithOptions");

        final String filePatch = this.getFixturePath("AddAttributeWithOptionsAttribute.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(expectedFile, FILE_PATH, dataPatchFile);
    }
}
