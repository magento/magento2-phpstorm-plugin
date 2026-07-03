/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;
import java.util.List;

public class BlockTemplateLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {
    /**
     * Layout block classes should navigate to their declared regular templates.
     */
    public void testBlockClassShouldHaveTemplateLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/Block/Widget/CustomerCreations.php"
        );
        final PhpClass phpClass = PsiTreeUtil.findChildOfType(myFixture.getFile(), PhpClass.class);

        assertProviderHasLinemarker(phpClass, "Navigate to block template");
    }

    /**
     * Block-to-template navigation popups should show template names, not PHP file content.
     */
    public void testBlockTemplateTargetsUseTemplateNames() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/Block/Widget/CustomerCreations.php"
        );
        final PhpClass phpClass = PsiTreeUtil.findChildOfType(myFixture.getFile(), PhpClass.class);
        final List<PsiElement> targets = new BlockTemplateLineMarkerProvider()
                .collectTemplates(phpClass);

        assertEquals(1, targets.size());
        assertEquals(
                "Foo_Bar::widget/customer_creations.phtml",
                BlockTemplateLineMarkerProvider.getPresentableTemplateTargetName(targets.get(0))
        );
    }

    /**
     * Layout referenceBlock classes should navigate to their declared regular templates.
     */
    public void testReferenceBlockClassShouldHaveTemplateLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/Block/Category/ListProduct.php"
        );
        final PhpClass phpClass = PsiTreeUtil.findChildOfType(myFixture.getFile(), PhpClass.class);

        assertProviderHasLinemarker(phpClass, "Navigate to block template");
    }

    /**
     * Regular templates should navigate back to layout block classes that declare them.
     */
    public void testTemplateShouldHaveBlockLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/templates/widget/customer_creations.phtml"
        );
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());

        assertProviderHasLinemarker(anchor, "Navigate to template block");
    }

    /**
     * Template-to-block navigation popups should show block class names, not PHP file content.
     */
    public void testTemplateBlockTargetsUseBlockClassNames() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/templates/widget/customer_creations.phtml"
        );
        final List<PsiElement> targets = new BlockTemplateLineMarkerProvider()
                .collectBlocks(myFixture.getFile());

        assertEquals(1, targets.size());
        assertEquals(
                "Foo\\Bar\\Block\\Widget\\TestMethod",
                BlockTemplateLineMarkerProvider.getPresentableBlockTargetName(targets.get(0))
        );
    }

    /**
     * Regular templates should navigate back to layout referenceBlock classes that declare them.
     */
    public void testReferenceBlockTemplateShouldHaveBlockLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/templates/category/list.phtml"
        );
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());

        assertProviderHasLinemarker(anchor, "Navigate to template block");
    }

    private void assertProviderHasLinemarker(final PsiElement element, final String tooltip) {
        final LineMarkerInfo<?> lineMarker = new BlockTemplateLineMarkerProvider()
                .getLineMarkerInfo(element);

        assertNotNull("No line marker returned by provider", lineMarker);
        assertEquals(tooltip, lineMarker.getLineMarkerTooltip());
    }
}
