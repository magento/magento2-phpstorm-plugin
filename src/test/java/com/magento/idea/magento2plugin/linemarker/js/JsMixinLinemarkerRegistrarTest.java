/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class JsMixinLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {
    /**
     * Target JS files should navigate to declared mixins.
     */
    public void testTargetJsShouldHaveMixinLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file.js"
        );

        assertProviderHasLinemarker("Navigate to JS mixins");
    }

    /**
     * Mixin JS files should navigate back to target JS files.
     */
    public void testMixinJsShouldHaveTargetLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file-mixin.js"
        );

        assertProviderHasLinemarker("Navigate to target JS");
    }

    private void assertProviderHasLinemarker(final String tooltip) {
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());
        final LineMarkerInfo<?> lineMarker = new JsMixinLineMarkerProvider()
                .getLineMarkerInfo(anchor);

        assertNotNull("No line marker returned by provider", lineMarker);
        assertEquals(tooltip, lineMarker.getLineMarkerTooltip());
    }
}
