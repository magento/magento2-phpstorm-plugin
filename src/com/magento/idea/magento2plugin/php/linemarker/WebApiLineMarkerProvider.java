/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.php.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.WebApiTypeIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Line marker for methods and classes which are exposed as web APIs.
 * <p/>
 * Allows to open related web API config.
 * Tooltip displays a list of related REST routes.
 */
public class WebApiLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> collection) {
        if (psiElements.size() > 0) {
            if (!Settings.isEnabled(psiElements.get(0).getProject())) {
                return;
            }
        }
        for (PsiElement psiElement: psiElements) {
            WebApiRoutesCollector collector = new WebApiRoutesCollector();
            List<XmlTag> results = new ArrayList<>();
            if (psiElement instanceof Method) {
                results = collector.getRoutes((Method) psiElement);
            } else if (psiElement instanceof PhpClass) {
                results = collector.getRoutes((PhpClass) psiElement);
            }

            if (!(results.size() > 0)) {
                continue;
            }

            StringBuilder tooltipText = new StringBuilder("Navigate to Web API configuration:<pre>");
            for (XmlTag routeTag : results) {
                tooltipText.append(routeTag.getName()).append("\n");
            }
            tooltipText.append("</pre>");
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                    .create(MagentoIcons.WEB_API)
                    .setTargets(results)
                    .setTooltipText(tooltipText.toString());
            collection.add(builder.createLineMarkerInfo(psiElement));
        }
    }

    /**
     * Web API config nodes collector for service methods and classes. Has built in caching.
     */
    private static class WebApiRoutesCollector {

        private HashMap<String, List<XmlTag>> routesCache = new HashMap<>();

        private static final Map<String, Integer> HTTP_METHODS_SORT_ORDER = new HashMap<String, Integer>() {{
            put("GET", 1);
            put("PUT", 2);
            put("POS", 3);
            put("DEL", 4);
        }};

        /**
         * Get sorted list of Web API routes related to the specified class.
         */
        List<XmlTag> getRoutes(@NotNull PhpClass phpClass) {
            List<XmlTag> routesForClass = new ArrayList<>();
            for (Method method : phpClass.getMethods()) {
                routesForClass.addAll(getRoutes(method));
            }
            sortRoutes(routesForClass);
            return routesForClass;
        }

        /**
         * Get list of Web API routes related to the specified method.
         * <p/>
         * Results are cached.
         */
        List<XmlTag> getRoutes(@NotNull Method method) {
            String methodFqn = method.getFQN();
            if (!routesCache.containsKey(methodFqn)) {
                List<XmlTag> routesForMethod = extractRoutesForMethod(method);
                sortRoutes(routesForMethod);
                routesCache.put(methodFqn, routesForMethod);
            }
            return routesCache.get(methodFqn);
        }

        /**
         * Get list of Web API routes related to the specified method.
         * <p/>
         * Web API declarations for parent classes are taken into account.
         * Results are not cached.
         */
        List<XmlTag> extractRoutesForMethod(@NotNull Method method) {
            List<XmlTag> routesForMethod = WebApiTypeIndex.getWebApiRoutes(method);
            PhpClass phpClass = method.getContainingClass();
            if (phpClass == null) {
                return routesForMethod;
            }
            for (PhpClass parent : method.getContainingClass().getSupers()) {
                for (Method parentMethod : parent.getMethods()) {
                    if (parentMethod.getName().equals(method.getName())) {
                        routesForMethod.addAll(extractRoutesForMethod(parentMethod));
                    }
                }
            }
            return routesForMethod;
        }

        /**
         * Make sure that routes are sorted as follows: GET, PUT, POST, DELETE. Then by path.
         */
        private void sortRoutes(List<XmlTag> routes) {
            routes.sort(
                (firstTag, secondTag) -> {
                    String substring = firstTag.getName().substring(2, 5);
                    Integer firstSortOrder = HTTP_METHODS_SORT_ORDER.get(substring);
                    Integer secondSortOrder = HTTP_METHODS_SORT_ORDER.get(secondTag.getName().substring(2, 5));
                    if (firstSortOrder.compareTo(secondSortOrder) == 0) {
                        // Sort by route if HTTP methods are equal
                        return firstTag.getName().compareTo(secondTag.getName());
                    }
                    return firstSortOrder.compareTo(secondSortOrder);
                }
            );
        }
    }
}
