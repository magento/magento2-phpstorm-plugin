/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentDataProviderData;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;

public class QueryGeneratorTest extends BaseGeneratorTestCase {

    public void testGenerateFileUsingLegacyTemplateAttributes() {
        final FileTemplate template = FileTemplateManager.getInstance(getProject())
                .getInternalTemplate(UiComponentDataProviderFile.TEMPLATE);
        final String originalText = template.getText();
        final boolean originalReformat = template.isReformatCode();

        try {
            template.setText("<?php\nnamespace ${NAMESPACE};\nclass ${CLASS_NAME} {}\n");
            template.setReformatCode(false);
            final QueryGenerator generator = new QueryGenerator(
                    new UiComponentDataProviderData("LegacyDataProvider", "Ui/DataProvider"),
                    "Foo_Bar",
                    getProject()
            );

            final PsiFile generated = generator.generate("Create legacy data provider");

            assertNotNull(generated);
            assertEquals("LegacyDataProvider.php", generated.getName());
            assertTrue(generated.getVirtualFile().getPath().endsWith(
                    "src/app/code/Foo/Bar/Ui/DataProvider/LegacyDataProvider.php"
            ));
            assertEquals(
                    "<?php\nnamespace Foo\\Bar\\Ui\\DataProvider;\nclass LegacyDataProvider {}\n",
                    generated.getText()
            );
        } finally {
            template.setText(originalText);
            template.setReformatCode(originalReformat);
        }
    }
}
