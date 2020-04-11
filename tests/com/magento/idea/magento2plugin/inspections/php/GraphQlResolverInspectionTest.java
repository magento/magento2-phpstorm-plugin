/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.io.File;

public class GraphQlResolverInspectionTest extends BasePlatformTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(GraphQlResolverInspection.class);
    }

    @Override
    protected String getTestDataPath() {
        return new File("testData/inspections/php/"
                + getClass().getSimpleName().replace("Test", "")).getAbsolutePath();
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testWithInvalidResolverInterface() throws Exception {
        myFixture.addFileToProject(getTestDataPath() + "/withInvalidResolverInterface/" + "schema.graphqls", "type Query {" +
                "InvalidResolverTest: InvalidResolver @resolver(class: \"\\\\Magento\\\\Test\\\\InvalidResolverTest\")" +
                "}");
        myFixture.configureByFile(getTestName(true) + "/" + "InvalidResolverTest.php");
        myFixture.checkHighlighting();
    }

    public void testWithValidResolverInterface() throws Exception {
        myFixture.addFileToProject(getTestDataPath() + "/withValidResolverInterface/" + "schema.graphqls", "type mutation {" +
                "    ValidResolverTest(): ValidResolver @resolver(class: \"\\\\Magento\\\\Test\\\\ValidResolverTest\")" +
                "}");
        myFixture.configureByFile(getTestName(true) + "/" + "ValidResolverTest.php");
        myFixture.checkHighlighting();
    }
}
