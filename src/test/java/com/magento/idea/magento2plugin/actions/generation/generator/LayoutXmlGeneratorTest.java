/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.magento.packages.Areas;

public class LayoutXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/view/adminhtml/layout";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ROUTE = "customroute";
    private static final String CONTROLLER_NAME = "Entity";
    private static final String ACTION_NAME = "View";
    private static final String FORM_NAME = "my_form";
    private static final String FILE_NAME = "customroute_entity_view.xml";

    /**
     * Test generating layout XML file.
     */
    public void testGenerateLayoutXmlFile() {
        final String filePath = this.getFixturePath(FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final LayoutXmlData layoutXmlData = new LayoutXmlData(
                Areas.adminhtml.toString(),
                ROUTE,
                MODULE_NAME,
                CONTROLLER_NAME,
                ACTION_NAME,
                FORM_NAME
        );
        final LayoutXmlGenerator cronjobClassGenerator = new LayoutXmlGenerator(
                layoutXmlData,
                project
        );

        final PsiFile file = cronjobClassGenerator.generate("test");
        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }
}
