/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.php;

public class GraphQlResolverInspectionTest extends InspectionPhpFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(GraphQlResolverInspection.class);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testWithInvalidResolverInterface() throws Exception {
        myFixture.addFileToProject("schema.graphqls", "type Query {" +
                "InvalidResolverTest: InvalidResolver @resolver(class: \"\\\\Magento\\\\Test\\\\InvalidResolverTest\")" +
                "}");
        myFixture.configureByFile(getFixturePath("InvalidResolverTest.php"));
        myFixture.checkHighlighting();
    }

    public void testWithValidResolverInterface() throws Exception {
        myFixture.addFileToProject("schema.graphqls", "type mutation {" +
                "    ValidResolverTest(): ValidResolver @resolver(class: \"\\\\Magento\\\\Test\\\\ValidResolverTest\")" +
                "}");
        myFixture.configureByFile(getFixturePath("ValidResolverTest.php"));
        myFixture.checkHighlighting();
    }
}
