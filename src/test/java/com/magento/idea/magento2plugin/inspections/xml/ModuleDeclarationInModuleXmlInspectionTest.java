/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleXml;
import com.magento.idea.magento2plugin.project.Settings;

public class ModuleDeclarationInModuleXmlInspectionTest
        extends InspectionXmlFixtureTestCase {
    private static final String MESSAGE_ID =
            "inspection.moduleDeclaration.warning.wrongModuleName";
    private static final String WRONG_MODULE_NAME = "Wrong_ModuleName";
    private static final String SETUP_VERSION_ATTRIBUTE_VALUE = "1.0.0";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(ModuleDeclarationInModuleXmlInspection.class);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    /**
     * Inspection highlights warning in editable module.
     */
    public void testWrongDeclarationInEditableModule() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath =
                "/src/xml/ModuleDeclarationInModuleXmlInspection/"
                        + "wrongDeclarationInEditableModule";
        myFixture.configureByFile(
                getFixturePath("app/code/Test/TestModule/etc/" + ModuleXml.FILE_NAME)
        );

        final String errorMessage =  inspectionBundle.message(
                MESSAGE_ID,
                WRONG_MODULE_NAME,
                "Test_TestModule"
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Inspection do not highlight wrong module name warning for setup version attribute.
     */
    public void testSetupVersionNotErrorMessageInEditableModule() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath =
                "/src/xml/ModuleDeclarationInModuleXmlInspection/"
                        + "setupVersionNotErrorMessageInEditableModule";
        myFixture.configureByFile(
                getFixturePath("app/code/Test/TestModule/etc/" + ModuleXml.FILE_NAME)
        );

        final String errorMessage =  inspectionBundle.message(
                MESSAGE_ID,
                SETUP_VERSION_ATTRIBUTE_VALUE,
                "Test_TestModule"
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Inspection skips sub tags.
     */
    public void testSubTagShouldNotBeHighlightedInEditableModule() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath =
            "/src/xml/ModuleDeclarationInModuleXmlInspection/"
                + "wrongDeclarationInEditableModule";
        myFixture.configureByFile(
                getFixturePath("app/code/Test/TestModule/etc/" + ModuleXml.FILE_NAME)
        );

        final String errorMessage =  inspectionBundle.message(
                MESSAGE_ID,
                WRONG_MODULE_NAME,
                "Test_TestModule"
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Inspection skips warning in root.
     */
    public void testWrongDeclarationInRoot() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath =
            "/src/xml/ModuleDeclarationInModuleXmlInspection/wrongDeclarationInRoot";
        myFixture.configureByFile(
                getFixturePath("etc/" + ModuleXml.FILE_NAME)
        );

        final String errorMessage =  inspectionBundle.message(
                MESSAGE_ID,
                WRONG_MODULE_NAME,
                "WrongDeclarationInRoot_etc"
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Inspection skips warning in vendor.
     */
    public void testWrongDeclarationInVendor() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath =
            "/src/xml/ModuleDeclarationInModuleXmlInspection/wrongDeclarationInVendor";
        myFixture.configureByFile(
                getFixturePath("vendor/magento/module-catalog/etc/" + ModuleXml.FILE_NAME)
        );

        final String errorMessage =  inspectionBundle.message(
                MESSAGE_ID,
                WRONG_MODULE_NAME,
                "module-catalog_etc"
        );

        assertHasNoHighlighting(errorMessage);
    }
}
