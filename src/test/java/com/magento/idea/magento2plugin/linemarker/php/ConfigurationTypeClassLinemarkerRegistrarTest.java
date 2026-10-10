/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer;
import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ConfigurationTypeClassLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {

    /**
     * Tests linemarkers in the configured class.
     */
    public void testTypeNameClassShouldHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("Topmenu.php", "php"));

        assertHasLinemarkerWithTooltipAndIcon("Navigate to configuration", "fileTypes/xml.svg");
    }

    /**
     * Tests linemarkers in the non-configured class.
     */
    public void testRegularPhpClassShouldNotHaveLinemarker() {
        myFixture.configureByFile(this.getFixturePath("ClassNotConfiguredInDiXml.php", "php"));

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to configuration", "fileTypes/xml.svg");
    }

    /**
     * Tests that configuration targets show the path of their XML file in the popup.
     */
    public void testTypeNameClassLinemarkerTargetsShouldShowFilePath() throws Exception {
        myFixture.configureByFile(this.getFixturePath("Topmenu.php", "php"));
        myFixture.doHighlighting();

        final List<String> containerTexts = new ArrayList<>();

        for (final LineMarkerInfo<?> lineMarkerInfo : DaemonCodeAnalyzerImpl.getLineMarkers(
                myFixture.getEditor().getDocument(),
                getProject()
        )) {
            if (!"Navigate to configuration".equals(lineMarkerInfo.getLineMarkerTooltip())) {
                continue;
            }
            final NavigationGutterIconRenderer gutterIconRenderer
                    = (NavigationGutterIconRenderer) lineMarkerInfo.getNavigationHandler();
            final PsiTargetPresentationRenderer<PsiElement> targetRenderer
                    = getTargetRenderer(gutterIconRenderer);

            for (final PsiElement target : gutterIconRenderer.getTargetElements()) {
                containerTexts.add(targetRenderer.getPresentation(target).getContainerText());
            }
        }

        assertContainsElements(containerTexts, "vendor/magento/module-catalog/etc/di.xml");
    }

    /**
     * Returns the renderer the popup uses for targets, the platform default when none is set.
     */
    @SuppressWarnings("unchecked")
    private PsiTargetPresentationRenderer<PsiElement> getTargetRenderer(
            final NavigationGutterIconRenderer gutterIconRenderer
    ) throws ReflectiveOperationException {
        final Field field = NavigationGutterIconRenderer.class.getDeclaredField("myTargetRenderer");
        field.setAccessible(true);
        final Supplier<PsiTargetPresentationRenderer<PsiElement>> supplier
                = (Supplier<PsiTargetPresentationRenderer<PsiElement>>) field.get(gutterIconRenderer);

        return supplier == null ? new PsiTargetPresentationRenderer<>() : supplier.get();
    }
}
