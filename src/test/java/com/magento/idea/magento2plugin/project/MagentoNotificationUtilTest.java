/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableGroup;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ex.ConfigurableExtensionPointUtil;
import com.intellij.openapi.options.ex.ConfigurableVisitor;
import com.intellij.openapi.options.ex.ConfigurableWrapper;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.frameworks.PhpFrameworksCommonConfigurable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MagentoNotificationUtilTest extends BasePlatformTestCase {

    public void testSelectsPhpFrameworkSettingsWhenMagentoSupportIsEnabled() {
        assertSelectsPhpFrameworkSettings(true);
    }

    public void testSelectsPhpFrameworkSettingsWhenMagentoSupportIsDisabled() {
        assertSelectsPhpFrameworkSettings(false);
    }

    private void assertSelectsPhpFrameworkSettings(final boolean enabled) {
        Settings.getInstance(getProject()).pluginEnabled = enabled;
        final ConfigurableGroup group = ConfigurableExtensionPointUtil.getConfigurableGroup(
                getProject(), true
        );

        try {
            final Configurable selected = ConfigurableVisitor.find(
                    MagentoNotificationUtil::isMagentoSettings, List.of(group)
            );

            assertNotNull("Magento settings must be reachable from the real PhpStorm settings tree", selected);
            assertInstanceOf(selected, SearchableConfigurable.class);
            assertEquals("settings.php.frameworks", ((SearchableConfigurable) selected).getId());

            final PhpFrameworksCommonConfigurable frameworks = ConfigurableWrapper.cast(
                    PhpFrameworksCommonConfigurable.class, selected
            );
            assertNotNull(frameworks);
            assertTrue("The selected Frameworks page must contain Magento settings",
                    frameworks.getConfigurables().stream().anyMatch(PhpFrameworkSettingsForm.class::isInstance));
        } finally {
            final List<Configurable> configurables = new ArrayList<>();
            ConfigurableVisitor.collect(configurables::add, group.getConfigurables());
            Collections.reverse(configurables);
            configurables.forEach(Configurable::disposeUIResources);
        }
    }
}
