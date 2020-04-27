/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.magento.idea.magento2plugin.MagentoIcons;

public class WebApiLinemarkerRegistrarTest extends LinemarkerPhpFixtureTestCase {

    private static final String expectedClassLineMarkerTooltip =
        "Navigate to Web API configuration:<pre>  PUT     /V1/blog/post\n" +
        "  POST    /V1/blog/update\n" +
        "</pre>";

    private static final String expectedMethodCreateLineMarkerTooltip =
        "Navigate to Web API configuration:<pre>  POST    /V1/blog/update\n" +
        "</pre>";

    private static final String expectedMethodUpdateLineMarkerTooltip =
        "Navigate to Web API configuration:<pre>  PUT     /V1/blog/post\n" +
        "</pre>";

    /**
     * Class configured as WEB API service in web_api.xml should have WEB API line markers
     */
    public void testWebApiServiceShouldHaveLinemarker() {
        String filePath = this.getFixturePath("TestService.php");

        //work around for issue caused by com.magento.idea.magento2plugin.linemarker.xml.LineMarkerXmlTagDecorator
        //in com.intellij.psi.impl.smartPointers.SmartPsiElementPointerImpl.createElementInfo
        boolean isInStressTestCurrent = ApplicationInfoImpl.isInStressTest();
        ApplicationInfoImpl.setInStressTest(true);

        myFixture.configureByFile(filePath);

        //assert class line marker
        assertHasLinemarkerWithTooltipAndIcon(expectedClassLineMarkerTooltip, MagentoIcons.WEB_API.toString());

        //assert methods line markers
        assertHasLinemarkerWithTooltipAndIcon(expectedMethodCreateLineMarkerTooltip, MagentoIcons.WEB_API.toString());
        assertHasLinemarkerWithTooltipAndIcon(expectedMethodUpdateLineMarkerTooltip, MagentoIcons.WEB_API.toString());

        //restore default value
        ApplicationInfoImpl.setInStressTest(isInStressTestCurrent);
    }

    /**
     * Regular class should not have WEB API line markers
     */
    public void testRegularPhpClassShouldNotHaveLinemarker() {
        String filePath = this.getFixturePath("ClassNotConfiguredInWebApiXml.php");
        myFixture.configureByFile(filePath);

        //assert class line marker
        assertHasNoLinemarkerWithTooltipAndIcon(expectedClassLineMarkerTooltip, MagentoIcons.WEB_API.toString());

        //assert methods line markers
        assertHasNoLinemarkerWithTooltipAndIcon(expectedMethodCreateLineMarkerTooltip, MagentoIcons.WEB_API.toString());
        assertHasNoLinemarkerWithTooltipAndIcon(expectedMethodUpdateLineMarkerTooltip, MagentoIcons.WEB_API.toString());
    }
}
