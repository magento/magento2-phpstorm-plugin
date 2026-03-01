/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.graphqls;

import org.junit.jupiter.api.Test;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class GraphQlResolverClassLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    /**
     * Tests linemarkers in the schema.graphqls file for PHP resolver classes.
     */
    @Test
    public void testWithValidSchemaResolver() {
        myFixture.configureByFile(this.getFixturePath("schema.graphqls", "graphqls"));

        assertHasLinemarkerWithTooltipAndIcon("Navigate to class", "Class");
    }
}
