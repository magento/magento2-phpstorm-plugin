/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.php.SearchResultsData;
import com.magento.idea.magento2plugin.actions.generation.generator.php.SearchResultsGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.php.SearchResultsInterfaceGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.files.SearchResultsFile;
import com.magento.idea.magento2plugin.magento.files.SearchResultsInterfaceFile;
import com.magento.idea.magento2plugin.magento.packages.Package;
import java.util.Objects;

public class SearchResultsFilesGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String MODULE_ROOT_DIR = "src/app/code/Foo/Bar/";
    private static final String ENTITY_NAME = "Book";
    private static final String ENTITY_DTO_TYPE = "Foo\\Bar\\Model\\Data\\BookData";
    private static final String INTERFACE_EXPECTED_DIRECTORY = MODULE_ROOT_DIR + "Api/Data";
    private static final String IMPL_EXPECTED_DIRECTORY = MODULE_ROOT_DIR + "Model";
    private PsiFile interfaceFile;
    private PsiFile classFile;
    private PsiFile preferenceFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        final SearchResultsData searchResultsData = new SearchResultsData(
                MODULE_NAME,
                ENTITY_NAME,
                ENTITY_DTO_TYPE
        );
        classFile = new SearchResultsGenerator(
                searchResultsData,
                myFixture.getProject(),
                false
        ).generate("test");

        final SearchResultsInterfaceGenerator interfaceGenerator =
                new SearchResultsInterfaceGenerator(
                searchResultsData,
                myFixture.getProject(),
                false
        );
        interfaceFile = interfaceGenerator.generate("test");
        preferenceFile = interfaceGenerator.getPreferenceFile();
    }

    /**
     * Test generation of search results interface for entity.
     */
    public void testGenerateSearchResultsInterfaceFile() {
        Objects.requireNonNull(interfaceFile);

        final String filePath = this.getFixturePath(
                new SearchResultsInterfaceFile(MODULE_NAME, ENTITY_NAME).getFileName()
        );

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                INTERFACE_EXPECTED_DIRECTORY,
                interfaceFile
        );
    }

    /**
     * Test generation of search results class for entity.
     */
    public void testGenerateSearchResultsFile() {
        Objects.requireNonNull(classFile);

        final String filePath = this.getFixturePath(
                new SearchResultsFile(MODULE_NAME, ENTITY_NAME).getFileName()
        );

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                IMPL_EXPECTED_DIRECTORY,
                classFile
        );
    }

    /**
     * Test generation of search results preference for entity.
     */
    public void testGenerateSearchResultsDiPreference() {
        Objects.requireNonNull(interfaceFile);
        Objects.requireNonNull(preferenceFile);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(this.getFixturePath(ModuleDiXml.FILE_NAME)),
                Package.moduleBaseAreaDir,
                preferenceFile
        );
    }
}
