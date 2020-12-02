/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.indexes.XmlIndex;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 11/15/15.
 */
public class ClassConfigurationLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        if (elements.size() > 0) {
            if (!Settings.isEnabled(elements.get(0).getProject())) {
                return;
            }
        }
        for (PsiElement psiElement: elements) {
            if (psiElement instanceof PhpClass) {
                List<XmlTag> results = new ArrayList<XmlTag>();

                results.addAll(XmlIndex.getPhpClassDeclarations((PhpClass) psiElement));

                if (!(results.size() > 0)) {
                    continue;
                }
                results.sort(Comparator.comparing(XmlTag::getName));

                String tooltipText = "Navigate to configuration";
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                        .create(AllIcons.FileTypes.Xml)
                        .setTargets(results)
                        .setTooltipText(tooltipText);

                PsiElement className = ((PhpClass) psiElement).getNameIdentifier();
                if (className != null) {
                    result.add(builder.createLineMarkerInfo(className));
                }
            }
        }
    }
}
