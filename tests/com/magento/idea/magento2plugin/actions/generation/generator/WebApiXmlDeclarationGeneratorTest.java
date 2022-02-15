/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.xml.WebApiXmlRouteData;
import com.magento.idea.magento2plugin.actions.generation.generator.xml.WebApiDeclarationGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleWebApiXmlFile;

public class WebApiXmlDeclarationGeneratorTest extends BaseGeneratorTestCase {

    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";
    private static final String COULD_NOT_GENERATE_MESSAGE =
            ModuleWebApiXmlFile.DECLARATION_TEMPLATE + " could not be generated!";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String URL = "test/foo/save";
    private static final String HTTP_METHOD = "POST";
    private static final String SERVICE_CLASS = "Foo\\Bar\\Api\\SaveFoo";
    private static final String SERVICE_METHOD = "execute";
    private static final String RESOURCE = "self";

    /**
     * Test generation of Web API xml declaration for a service.
     */
    public void testGenerateWebApiXmlDeclarationForService() {
        final WebApiXmlRouteData data = new WebApiXmlRouteData(
                MODULE_NAME,
                URL,
                HTTP_METHOD,
                SERVICE_CLASS,
                SERVICE_METHOD,
                RESOURCE
        );
        final WebApiDeclarationGenerator generator = new WebApiDeclarationGenerator(
                data,
                myFixture.getProject()
        );
        final PsiFile result = generator.generate("test");

        if (result == null) {
            fail(COULD_NOT_GENERATE_MESSAGE);
        }

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(this.getFixturePath(ModuleWebApiXmlFile.FILE_NAME)),
                EXPECTED_DIRECTORY,
                result
        );
    }
}
