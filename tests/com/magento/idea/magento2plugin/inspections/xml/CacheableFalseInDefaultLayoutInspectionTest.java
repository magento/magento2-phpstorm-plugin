/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.io.File;

//tested referenceBlock only as block tag is Special markup for the tests
//@see https://www.jetbrains.org/intellij/sdk/docs/basics/testing_plugins/test_project_and_testdata_directories.html
public class CacheableFalseInDefaultLayoutInspectionTest extends BasePlatformTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(CacheableFalseInDefaultLayoutInspection.class);
    }

    @Override
    protected String getTestDataPath() {
        return new File("testData/inspections/xml/"
                + getClass().getSimpleName().replace("Test", "")).getAbsolutePath();
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testWithCacheableFalseBlock() throws Exception {
        myFixture.configureByFile(getTestName(true) + "/" + LayoutXml.DEFAULT_FILENAME);
        myFixture.testHighlighting(true, false, false);
    }

    public void testWithoutCacheableFalseBlock() throws Exception {
        myFixture.configureByFile(getTestName(true) + "/" + LayoutXml.DEFAULT_FILENAME);
        myFixture.testHighlighting(true, true, true);
    }

    public void testWithCacheableFalseBlockNotDefaultLayout() throws Exception {
        myFixture.configureByFile(getTestName(true) + "/" + "some_layout_index.xml");
        myFixture.testHighlighting(true, true, true);
    }
}