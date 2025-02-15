/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.RoutesXmlData;
import com.magento.idea.magento2plugin.magento.files.RoutesXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;

public class RoutesXmlGeneratorTest extends BaseGeneratorTestCase {

    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc/adminhtml";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ROUTE = "customroute";

    /**
     * Test generating routes XML file.
     */
    public void testGenerateRoutesXmlFile() {
        final String filePath = this.getFixturePath(RoutesXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final RoutesXmlData routesXmlData = new RoutesXmlData(
                Areas.adminhtml.toString(),
                ROUTE,
                MODULE_NAME
        );
        final RoutesXmlGenerator routesXmlGenerator = new RoutesXmlGenerator(
                routesXmlData,
                project
        );

        final PsiFile file = routesXmlGenerator.generate("test");
        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }
}
