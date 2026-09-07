/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
public class PluginDeclarationInspectionTest extends InspectionXmlFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(PluginDeclarationInspection.class);
    }

    private String getAreaPath(final String area) {
        return Package.moduleBaseAreaDir
                + File.separator
                + Areas.getAreaByString(area)
                + File.separator
                + ModuleDiXml.FILE_NAME;
    }

    /**
     * Tests the plugin name duplication warning won't show in the di.xml file
     */
    public void testPluginNameDuplicationWarningWontShow() {
        myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests warning for disabling of non-existing plugin.
     */
    public void testDisabledNonExistingPlugin() {
        myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests whenever the duplication warning shows when the plugin name already
     * defined in the same di.xml file
     */
    public void testPluginNameUsedInSameFile() {
        myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests whenever the duplication warning occurs for a
     * plugin name declared in the frontend area
     * that is already defined in the frontend module
     * area (Vendor/Module/etc/frontend/di.xml)
     */
    public void testPluginNameAlreadyUsedInFrontendArea() {
        myFixture.configureByFile(getFixturePath(getAreaPath("frontend")));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests whenever the duplication warning occurs for
     * a plugin name declared in the adminhtml area
     * that is already defined in the adminhtml module area
     * (Vendor/Module/etc/adminhtml/di.xml)
     */
    public void testPluginNameAlreadyUsedInAdminhtmlArea() {
        myFixture.configureByFile(getFixturePath(getAreaPath("adminhtml")));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests whenever the duplication warning occurs for a plugin name
     * that is already defined in the global module area (Vendor/Module/etc/di.xml)
     */
    public void testPluginNameAlreadyUsedInGlobalArea() {
        myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests whenever the duplication warning occurs for
     * a plugin name declared in the webapi_rest area
     * that is already defined in the webapi_rest module
     * area (Vendor/Module/etc/webapi_rest/di.xml)
     */
    public void testPluginNameAlreadyUsedInWebApiRestArea() {
        myFixture.configureByFile(getFixturePath(this.getAreaPath("webapi_rest")));
        myFixture.testHighlighting(true, false, false);
    }

    /**
     * Tests whenever the duplication warning occurs for
     * a plugin name declared in the webapi_soap area
     * that is already defined in the webapi_soap
     * module area (Vendor/Module/etc/webapi_soap/di.xml)
     */
    public void testPluginNameAlreadyUsedInWebApiSoapArea() {
        myFixture.configureByFile(getFixturePath(getAreaPath("webapi_soap")));
        myFixture.testHighlighting(true, false, false);
    }
    
    /**
     * Tests whenever the duplication warning occurs for
     * a plugin name declared in the graphql area
     * that is already defined in the graphql module
     * area (Vendor/Module/etc/graphql/di.xml)
     */
    public void testPluginNameAlreadyUsedInGraphqlArea() {
        myFixture.configureByFile(getFixturePath(getAreaPath("graphql")));
        myFixture.testHighlighting(true, false, false);
    }
    
    /**
     * Tests whenever the duplication warning occurs for
     * a plugin name declared in the crontab area
     * that is already defined in the crontab module
     * area (Vendor/Module/etc/crontab/di.xml)
     */
    public void testPluginNameAlreadyUsedInCrontabArea() {
        myFixture.configureByFile(getFixturePath(getAreaPath("crontab")));
        myFixture.testHighlighting(true, false, false);
    }
}
