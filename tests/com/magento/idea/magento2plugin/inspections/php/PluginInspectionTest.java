/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.jetbrains.php.PhpBundle;

public class PluginInspectionTest extends InspectionPhpFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(PluginInspection.class);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    /**
     * Inspection highlights error in parameter type.
     */
    public void testWithWrongParameterType() {
        myFixture.configureByFile(getFixturePath("Plugin.php"));

        final String wrongParameterError =  PhpBundle.message(
                "inspection.wrong_param_type",
                "\\Magento\\Catalog\\Block\\Navigation",
                "\\Magento\\Theme\\Block\\Html\\Topmenu"
        );

        assertHasHighlighting(wrongParameterError);
    }

    /**
     * Inspection highlights error in callable parameter type.
     */
    public void testWithWrongCallableType() {
        myFixture.configureByFile(getFixturePath("Plugin.php"));

        final String wrongParameterError =  PhpBundle.message(
                "inspection.wrong_param_type",
                "\\Magento\\Theme\\Block\\Html\\Topmenu",
                "callable"
        );

        assertHasHighlighting(wrongParameterError);
    }
}
