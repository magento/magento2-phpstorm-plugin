package com.magento.idea.magento2plugin.php.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.Magento2Icons;
import com.magento.idea.magento2plugin.xml.di.index.TypeConfigurationFileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

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
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
        for (PsiElement psiElement: list) {
            if (psiElement instanceof PhpClass) {
                List<XmlTag> results = TypeConfigurationFileBasedIndex
                    .getClassConfigurations((PhpClass) psiElement);
                if (results == null || results.size() == 0) {
                    continue;
                }

                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.
                    create(Magento2Icons.CONFIGURATION).
                    setTargets(results).
                    setTooltipText("Navigate to configuration");

                collection.add(builder.createLineMarkerInfo(psiElement));
            }
        }
    }
}
