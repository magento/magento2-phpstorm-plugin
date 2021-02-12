/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class GraphQlResolverUsageLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    /**
     * Tests linemarkers in the resolver class.
     */
    public void testResolverClassShouldHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("Resolver.php", "php"));

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to schema", "icons/graphqlFile.svg");
    }

    /**
     * Tests linemarkers in the regular class.
     */
    public void testRegularClassShouldNotHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("ClassNotConfiguredInSchema.php", "php"));

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to schema", "icons/graphqlFile.svg");
    }
}
