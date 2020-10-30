/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class WebApiLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    private static final String EXPECTED_CLASS_LINE_MARKER_TOOLTIP
            = "Navigate to Web API configuration:<pre>  PUT     /V1/blog/post\n"
            + "  POST    /V1/blog/update\n"
            + "</pre>";

    private static final String EXPECTED_METHOD_CREATE_LINE_MARKER_TOOLTIP
            = "Navigate to Web API configuration:<pre>  POST    /V1/blog/update\n"
            + "</pre>";

    private static final String EXPECTED_METHOD_UPDATE_LINE_MARKER_TOOLTIP
            = "Navigate to Web API configuration:<pre>  PUT     /V1/blog/post\n"
            + "</pre>";

    /**
     * Class configured as WEB API service in web_api.xml should have WEB API line markers.
     */
    public void testWebApiServiceShouldHaveLinemarker() {
        // work around for issue caused by
        // com.magento.idea.magento2plugin.linemarker.xml.LineMarkerXmlTagDecorator
        // in com.intellij.psi.impl.smartPointers.SmartPsiElementPointerImpl.createElementInfo
        final boolean isInStressTestCurrent = ApplicationInfoImpl.isInStressTest();
        ApplicationInfoImpl.setInStressTest(true);

        myFixture.configureByFile(this.getFixturePath("TestService.php", "php"));

        //assert class line marker
        assertHasLinemarkerWithTooltipAndIcon(
                EXPECTED_CLASS_LINE_MARKER_TOOLTIP,
                MagentoIcons.WEB_API.toString()
        );

        //assert methods line markers
        assertHasLinemarkerWithTooltipAndIcon(
                EXPECTED_METHOD_CREATE_LINE_MARKER_TOOLTIP,
                MagentoIcons.WEB_API.toString()
        );
        assertHasLinemarkerWithTooltipAndIcon(
                EXPECTED_METHOD_UPDATE_LINE_MARKER_TOOLTIP,
                MagentoIcons.WEB_API.toString()
        );

        //restore default value
        ApplicationInfoImpl.setInStressTest(isInStressTestCurrent);
    }

    /**
     * Regular class should not have WEB API line markers.
     */
    public void testRegularPhpClassShouldNotHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("ClassNotConfiguredInWebApiXml.php", "php"));

        //assert class line marker
        assertHasNoLinemarkerWithTooltipAndIcon(
                EXPECTED_CLASS_LINE_MARKER_TOOLTIP,
                MagentoIcons.WEB_API.toString()
        );

        //assert methods line markers
        assertHasNoLinemarkerWithTooltipAndIcon(
                EXPECTED_METHOD_CREATE_LINE_MARKER_TOOLTIP,
                MagentoIcons.WEB_API.toString()
        );
        assertHasNoLinemarkerWithTooltipAndIcon(
                EXPECTED_METHOD_UPDATE_LINE_MARKER_TOOLTIP,
                MagentoIcons.WEB_API.toString()
        );
    }
}
