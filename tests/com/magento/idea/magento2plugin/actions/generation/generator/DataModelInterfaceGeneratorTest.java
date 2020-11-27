/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewDataModelAction;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelInterfaceData;

public class DataModelInterfaceGeneratorTest extends BaseGeneratorTestCase {
    /**
     * Tests for generation of a Magento 2 Data Model Interface.
     */
    public void testGenerateDataModelInterface() {
        final Project project = myFixture.getProject();
        final DataModelInterfaceData interfaceData = new DataModelInterfaceData(
                "Foo\\Bar\\Api\\Data",
                "SampleInterface",
                "Foo_Bar",
                "Foo\\Bar\\Api\\Data\\SampleInterface",
                "SAMPLE_PROPERTY;sample_property;string;SampleProperty;sampleProperty"
        );
        final DataModelInterfaceGenerator generator = new DataModelInterfaceGenerator(
                project, interfaceData
        );
        final PsiFile interfaceFile = generator.generate(NewDataModelAction.ACTION_NAME);
        final PsiFile expectedFile
                = myFixture.configureByFile(this.getFixturePath("SampleInterface.php"));

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Api/Data",
                interfaceFile
        );
    }
}
