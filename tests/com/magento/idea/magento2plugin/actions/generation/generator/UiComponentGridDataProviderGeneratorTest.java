/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridDataProviderData;
import com.magento.idea.magento2plugin.magento.files.UiComponentGridDataProviderPhp;

public class UiComponentGridDataProviderGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Ui/Component/Listing";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String PROVIDER_CLASS_NAME = "GridDataProvider";
    private static final String PROVIDER_NAMESPACE = "Foo\\Bar\\Ui\\Listing";
    private static final String PROVIDER_PATH = "Ui/Component/Listing";
    private static final String COLLECTION_FQN = "Foo\\Bar\\Model\\Resource\\Entity\\Collection";

    /**
     * Test data provider class file generation with custom type.
     */
    public void testGenerateCustomDataProvider() {
        final PsiFile dataProviderFile = generateDataProvider(
                UiComponentGridDataProviderPhp.CUSTOM_TYPE,
                null
        );
        final String filePath = this.getFixturePath("GridDataProvider.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                dataProviderFile
        );
    }

    /**
     * Test data provider class file generation with collection type.
     */
    public void testGenerateCollectionDataProvider() {
        final PsiFile dataProviderFile = generateDataProvider(
                UiComponentGridDataProviderPhp.COLLECTION_TYPE,
                COLLECTION_FQN
        );
        final String filePath = this.getFixturePath("GridDataProvider.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                EXPECTED_DIRECTORY,
                dataProviderFile
        );
    }

    private PsiFile generateDataProvider(
            final String providerType,
            final String collectionFqn
    ) {
        final Project project = myFixture.getProject();
        final UiComponentGridDataProviderData providerData = new UiComponentGridDataProviderData(
                providerType,
                PROVIDER_CLASS_NAME,
                PROVIDER_NAMESPACE,
                PROVIDER_PATH,
                collectionFqn
        );
        final UiComponentGridDataProviderGenerator generator;
        generator = new UiComponentGridDataProviderGenerator(
                providerData,
                MODULE_NAME,
                project
        );

        return generator.generate("test");
    }
}
