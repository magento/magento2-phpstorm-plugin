/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewDataModelAction;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelInterfaceData;

public class DataModelInterfaceGeneratorTest extends BaseGeneratorTestCase {

    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Api/Data";

    /**
     * Tests for generation of a Magento 2 Data Model Interface.
     */
    public void testGenerateDataModelInterface() {
        final DataModelInterfaceGenerator generator = new DataModelInterfaceGenerator(
                new DataModelInterfaceData(
                        "SampleInterface",
                        "Foo_Bar",
                        "ID_PROPERTY;id_property;int;IdProperty;idProperty,"
                                + "SAMPLE_PROPERTY;sample_property;string;"
                                + "SampleProperty;sampleProperty"
                ),
                myFixture.getProject()
        );
        final PsiFile interfaceFile = generator.generate(NewDataModelAction.ACTION_NAME);
        final PsiFile expectedFile
                = myFixture.configureByFile(this.getFixturePath("SampleInterface.php"));

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, interfaceFile);
    }
}
