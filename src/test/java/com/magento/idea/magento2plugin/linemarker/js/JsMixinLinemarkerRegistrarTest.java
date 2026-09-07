/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

public class JsMixinLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {
    /**
     * Target JS files should navigate to declared mixins.
     */
    public void testTargetJsShouldHaveMixinLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file.js"
        );

        assertLinemarkerCountWithTooltip("Navigate to JS mixins", 1);
        assertFirstAnchorLinemarkerCount(1);
    }

    /**
     * Mixin JS files should navigate back to target JS files.
     */
    public void testMixinJsShouldHaveTargetLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file-mixin.js"
        );

        assertLinemarkerCountWithTooltip("Navigate to target JS", 1);
        assertFirstAnchorLinemarkerCount(0);
        assertLinemarkerCountAtText("define", 1);
        assertNoMergeableLinemarkersWithTooltip("Navigate to target JS");
    }

    /**
     * Target JS methods wrapped by mixins should navigate back to the mixin override.
     */
    public void testTargetMethodShouldHaveMixinOverrideLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to JS mixin override", "");
        assertFirstAnchorLinemarkerCount(1);
        assertNoMergeableLinemarkersWithTooltip("Navigate to JS mixin override");
    }

    /**
     * Mixin wrapper assignments should navigate to matching target methods.
     */
    public void testMixinMethodShouldHaveTargetMethodLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file-mixin.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to target method", "");
        assertNoMergeableLinemarkersWithTooltip("Navigate to target method");
    }

    /**
     * UI component mixin object methods should navigate to matching target methods.
     */
    public void testObjectMixinMethodShouldHaveTargetMethodLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/object-mixin.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to target method", "");
    }

    /**
     * jQuery widget mixin object methods should navigate to matching target methods.
     */
    public void testWidgetMixinMethodShouldHaveTargetMethodLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/widget-mixin.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to target method", "");
    }

    /**
     * Function mixins should navigate to the wrapped target function file.
     */
    public void testFunctionMixinShouldHaveTargetFunctionLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/function-mixin.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to target method", "");
    }

    /**
     * Target files should navigate back to object, widget, and function mixin overrides.
     */
    public void testTargetShouldHaveDocumentedMixinOverrideLinemarkers() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/file2.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to JS mixin override", "");
    }

    /**
     * RequireJS config already has references; duplicate mixin gutter markers should not be emitted.
     */
    public void testRequireJsConfigShouldNotHaveMixinLinemarkers() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/requirejs-config.js"
        );

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to JS mixins", "");
        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to target JS", "");
    }

}
