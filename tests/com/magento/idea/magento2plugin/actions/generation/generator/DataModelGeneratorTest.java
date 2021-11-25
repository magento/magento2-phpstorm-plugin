/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewDataModelAction;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelData;

public class DataModelGeneratorTest extends BaseGeneratorTestCase {
    /**
     * Tests for generation of a Magento 2 Data Model.
     */
    public void testGenerateDataModel() {
        final Project project = myFixture.getProject();
        final DataModelData modelData = new DataModelData(
                "Foo\\Bar\\Model\\Data",
                "Sample",
                "Foo_Bar",
                "Foo\\Bar\\Model\\Data\\Sample",
                "Foo\\Bar\\Api\\Data\\SampleInterface",
                "SAMPLE_PROPERTY;sample_property;string;SampleProperty;sampleProperty"
        );
        final DataModelGenerator generator = new DataModelGenerator(
                project, modelData
        );
        final PsiFile modelFile = generator.generate(NewDataModelAction.ACTION_NAME);
        final PsiFile expectedFile
                = myFixture.configureByFile(this.getFixturePath("Sample.php"));

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model/Data",
                modelFile
        );
    }
}
