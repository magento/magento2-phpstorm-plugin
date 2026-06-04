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

    /**
     * Target JS methods wrapped by mixins should navigate back to the mixin override.
     */
    public void testTargetMethodShouldHaveMixinOverrideLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("<html>Navigate to JS mixin override</html>", "");
    }

    /**
     * Mixin wrapper assignments should navigate to matching target methods.
     */
    public void testMixinMethodShouldHaveTargetMethodLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file-mixin.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("<html>Navigate to target method</html>", "");
    }

    /**
     * UI component mixin object methods should navigate to matching target methods.
     */
    public void testObjectMixinMethodShouldHaveTargetMethodLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/object-mixin.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("<html>Navigate to target method</html>", "");
    }

    /**
     * jQuery widget mixin object methods should navigate to matching target methods.
     */
    public void testWidgetMixinMethodShouldHaveTargetMethodLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/widget-mixin.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("<html>Navigate to target method</html>", "");
    }

    /**
     * Function mixins should navigate to the wrapped target function file.
     */
    public void testFunctionMixinShouldHaveTargetFunctionLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/function-mixin.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("<html>Navigate to target method</html>", "");
    }

    /**
     * Target files should navigate back to object, widget, and function mixin overrides.
     */
    public void testTargetShouldHaveDocumentedMixinOverrideLinemarkers() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file2.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("<html>Navigate to JS mixin override</html>", "");
    }

    /**
     * RequireJS config already has references; duplicate mixin gutter markers should not be emitted.
     */
    public void testRequireJsConfigShouldNotHaveMixinLinemarkers() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/requirejs-config.js"
        );

        assertHasNoLinemarkerWithTooltipAndIcon("<html>Navigate to JS mixins</html>", "");
        assertHasNoLinemarkerWithTooltipAndIcon("<html>Navigate to target JS</html>", "");
    }

    private void assertProviderHasLinemarker(final String tooltip) {
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());
        final LineMarkerInfo<?> lineMarker = new JsMixinLineMarkerProvider()
                .getLineMarkerInfo(anchor);

        assertNotNull("No line marker returned by provider", lineMarker);
        assertEquals(tooltip, lineMarker.getLineMarkerTooltip());
    }
}
