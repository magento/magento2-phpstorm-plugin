/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.DataProviderDeclarationData;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class DataProviderDeclarationGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String CLASS_NAME = "MyVirtualClass";
    private static final String COLLECTION = "My/Collection";
    private static final String DATA_SOURCE = "my_grid_data_source";
    private static final String TABLE = "my_table";
    private static final String ACTION_NAME = "test";

    /**
     * Test checks whether menu.xml file generated correctly.
     */
    public void testGenerateDataProviderDeclarationFile() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                generateDiXmlFile()
        );
    }

    /**
     * Generate Menu XML file.
     *
     * @return PsiFile
     */
    private PsiFile generateDiXmlFile() {
        final DataProviderDeclarationGenerator dataProviderDeclaration =
                new DataProviderDeclarationGenerator(
                    new DataProviderDeclarationData(
                            MODULE_NAME,
                            CLASS_NAME,
                            COLLECTION,
                            DATA_SOURCE,
                            TABLE
                    ),
                    myFixture.getProject()
        );
        return dataProviderDeclaration.generate(ACTION_NAME);
    }
}
