/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.LayoutXml;

//tested referenceBlock only as block tag is Special markup for the tests
//@see https://www.jetbrains.org/intellij/sdk/docs/basics/testing_plugins/test_project_and_testdata_directories.html
public class CacheableFalseInDefaultLayoutInspectionTest extends InspectionXmlFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(CacheableFalseInDefaultLayoutInspection.class);
    }

    public void testWithCacheableFalseBlock() throws Exception {
        myFixture.configureByFile(getFixturePath(LayoutXml.DEFAULT_FILENAME));
        myFixture.testHighlighting(true, false, false);
    }

    public void testWithoutCacheableFalseBlock() throws Exception {
        myFixture.configureByFile(getFixturePath(LayoutXml.DEFAULT_FILENAME));
        myFixture.testHighlighting(true, true, true);
    }

    public void testWithCacheableFalseBlockNotDefaultLayout() throws Exception {
        myFixture.configureByFile(getFixturePath("some_layout_index.xml"));
        myFixture.testHighlighting(true, true, true);
    }
}