/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class GraphQlResolverUsageLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {
    public void testResolverClassShouldHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("Resolver.php", "php"));

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to schema", "/icons/graphqlFile.svg");
    }

    public void testRegularClassShouldNotHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("ClassNotConfiguredInSchema.php", "php"));

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to schema", "/icons/graphqlFile.svg");
    }
}
