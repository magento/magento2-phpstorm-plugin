/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import com.magento.idea.magento2plugin.project.Settings;

public class ModuleDeclarationInRegistrationPhpInspectionTest
        extends InspectionPhpFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(ModuleDeclarationInRegistrationPhpInspection.class);
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
                "/src/php/ModuleDeclarationInRegistrationPhpInspection/"
                        + "wrongDeclarationInEditableModule";
        myFixture.configureByFile(
                getFixturePath("app/code/Test/TestModule/" + RegistrationPhp.FILE_NAME)
        );

        final String errorMessage =  inspectionBundle.message(
                "inspection.moduleDeclaration.warning.wrongModuleName",
                "Wrong_ModuleName",
                "Test_TestModule"
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Inspection skips warning in root.
     */
    public void testWrongDeclarationInRoot() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath =
            "/src/php/ModuleDeclarationInRegistrationPhpInspection/wrongDeclarationInRoot";
        myFixture.configureByFile(
                getFixturePath(RegistrationPhp.FILE_NAME)
        );

        final String errorMessage =  inspectionBundle.message(
                "inspection.moduleDeclaration.warning.wrongModuleName",
                "Wrong_ModuleName",
                "ModuleDeclarationInRegistrationPhpInspection_WrongDeclarationInRoot"
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Inspection skips warning in vendor.
     */
    public void testWrongDeclarationInVendor() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath =
            "/src/php/ModuleDeclarationInRegistrationPhpInspection/wrongDeclarationInVendor";
        myFixture.configureByFile(
                getFixturePath("vendor/magento/module-catalog/" + RegistrationPhp.FILE_NAME)
        );

        final String errorMessage =  inspectionBundle.message(
                "inspection.moduleDeclaration.warning.wrongModuleName",
                "Wrong_ModuleName",
                "magento_module-catalog"
        );

        assertHasNoHighlighting(errorMessage);
    }
}
