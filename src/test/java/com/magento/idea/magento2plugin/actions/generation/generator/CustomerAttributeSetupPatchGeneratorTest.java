/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CustomerEntityData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeType;
import java.util.HashMap;
import java.util.Map;

public class CustomerAttributeSetupPatchGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";

    /**
     * Test generating the customer attribute data patch.
     */
    public void testGenerateMultiselectAttributeDataPatch() {
        final Project project = myFixture.getProject();

        final CustomerEntityData customerEntityData = new CustomerEntityData();
        customerEntityData.setCode("multiselect_test");
        customerEntityData.setLabel("Multiselect Test");
        customerEntityData.setVisible(true);
        customerEntityData.setSource(AttributeSourceModel.TABLE.getSource());
        customerEntityData.setType(AttributeType.VARCHAR.getType());
        customerEntityData.setInput(AttributeInput.MULTISELECT.getInput());
        customerEntityData.setUserDefined(true);
        customerEntityData.setSortOrder(10);
        customerEntityData.setUseInAdminhtmlCustomerForm(true);

        final Map<Integer, String> options = new HashMap<>();
        options.put(0, "option1");
        options.put(1, "option2");
        options.put(2, "option3");
        customerEntityData.setOptions(options);

        customerEntityData.setDataPatchName("AddMultiselectTestCustomerAttribute");
        customerEntityData.setModuleName(MODULE_NAME);


        final CustomerEavAttributePatchGenerator setupPatchGenerator =
                new CustomerEavAttributePatchGenerator(customerEntityData, project);
        final PsiFile dataPatchFile = setupPatchGenerator.generate(
                "testGenerateMultiselectAttributeDataPatch"
        );

        final String filePatch = this.getFixturePath("AddMultiselectTestCustomerAttribute.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePatch);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Setup/Patch/Data",
                dataPatchFile
        );
    }
}
