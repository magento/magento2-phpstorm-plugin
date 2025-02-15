/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.php;

public class ConfigPhpModuleReferenceRegistrarTest extends ReferencePhpFixtureTestCase {

    /**
     * Tests for module name reference under array key 'modules' in config.php
     */
    public void testModuleNameMustHaveReference() {
        myFixture.configureByFile(this.getFixturePath("config.php"));

        assertHasReferenceToDirectory("module-catalog");
    }

    /**
     * Tests for no module name reference under a different array key in config.php
     */
    public void testModuleNameMustNotHaveReference() {
        myFixture.configureByFile(this.getFixturePath("config.php"));

        assertHasNoReferenceToDirectory("module-catalog");
    }
}
