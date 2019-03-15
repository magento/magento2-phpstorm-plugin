package com.magento.idea.magento2plugin.php.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.indexes.XmlIndex;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
        if (list.size() > 0) {
            if (!Settings.isEnabled(list.get(0).getProject())) {
                return;
            }
        }
        for (PsiElement psiElement: list) {
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
                    collection.add(builder.createLineMarkerInfo(className));
                }
            }
        }
    }
}
