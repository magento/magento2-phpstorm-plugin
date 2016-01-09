package com.magento.idea.magento2plugin.php.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.Magento2Icons;
import com.magento.idea.magento2plugin.Settings;
import com.magento.idea.magento2plugin.xml.di.index.TypeConfigurationFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.util.LayoutIndexUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by dkvashnin on 11/15/15.
 */
public class ClassConfigurationLineMarkerProvider implements LineMarkerProvider {
    public static List<Collector<PhpClass, XmlTag>> COLLECTORS = new ArrayList<Collector<PhpClass, XmlTag>>() {{
        add(new DiConfigurationCollector());
        add(new LayoutConfigurationCollector());
    }};

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
                for (Collector<PhpClass, XmlTag> collector: COLLECTORS) {
                    results.addAll(collector.collect((PhpClass) psiElement));
                }

                if (results.size() == 0) {
                    continue;
                }
                Collections.sort(
                    results,
                    new Comparator<XmlTag>() {
                        @Override
                        public int compare(XmlTag firstTag, XmlTag secondTag) {
                            return firstTag.getName().compareTo(secondTag.getName());
                        }
                    }
                );
                String tooltipText = "Navigate to configuration:";
                for (XmlTag resultItem: results) {
                    tooltipText += "\n  " + resultItem.getName();
                }
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.
                    create(Magento2Icons.CONFIGURATION).
                    setTargets(results).
                    setTooltipText(tooltipText);

                collection.add(builder.createLineMarkerInfo(psiElement));
            }
        }
    }
}

class DiConfigurationCollector implements Collector<PhpClass, XmlTag> {
    @Override
    public List<XmlTag> collect(@NotNull PhpClass psiElement) {
        return TypeConfigurationFileBasedIndex
            .getClassConfigurations(psiElement);
    }
}

class LayoutConfigurationCollector implements Collector<PhpClass, XmlTag> {
    @Override
    public List<XmlTag> collect(@NotNull PhpClass psiElement) {
        return LayoutIndexUtility.getBlockClassDeclarations(psiElement, psiElement.getProject());
    }
}