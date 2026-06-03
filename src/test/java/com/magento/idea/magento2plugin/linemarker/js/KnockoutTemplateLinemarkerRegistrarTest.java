/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class KnockoutTemplateLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {
    /**
     * Knockout component JS files should navigate to their declared templates.
     */
    public void testComponentJsShouldHaveTemplateLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/knockout-component.js"
        );

        assertProviderHasLinemarker("Navigate to Knockout template");
    }

    /**
     * Knockout template files should navigate back to declaring component JS files.
     */
    public void testTemplateShouldHaveComponentLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/template.html"
        );

        assertProviderHasLinemarker("Navigate to Knockout component");
    }

    private void assertProviderHasLinemarker(final String tooltip) {
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());
        final LineMarkerInfo<?> lineMarker = new KnockoutTemplateLineMarkerProvider()
                .getLineMarkerInfo(anchor);

        assertNotNull("No line marker returned by provider", lineMarker);
        assertEquals(tooltip, lineMarker.getLineMarkerTooltip());
    }
}
