/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.WebApiTypeIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Line marker for methods and classes which are exposed as web APIs.
 * <p/>
 * Allows to open related web API config.
 * Tooltip displays a list of related REST routes.
 */
public class WebApiLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        return null;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> psiElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (psiElements.isEmpty()) {
            return;
        }

        if (!Settings.isEnabled(psiElements.get(0).getProject())) {
            return;
        }
        final WebApiRoutesCollector collector = new WebApiRoutesCollector();

        for (final PsiElement psiElement : psiElements) {
            List<XmlTag> results = new ArrayList<>();

            if (psiElement instanceof Method) {
                results = collector.getRoutes((Method) psiElement);
            } else if (psiElement instanceof PhpClass) {
                results = collector.getRoutes((PhpClass) psiElement);
            }

            if (results.isEmpty()) {
                continue;
            }

            final StringBuilder tooltipText = new StringBuilder(
                    "Navigate to Web API configuration:<pre>"
            );
            for (final XmlTag routeTag : results) {
                tooltipText.append(routeTag.getName()).append('\n');
            }
            tooltipText.append("</pre>");
            final NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                    .create(MagentoIcons.WEB_API)
                    .setTargets(results)
                    .setTooltipText(tooltipText.toString());
            collection.add(builder.createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(psiElement)));
        }
    }

    /**
     * Web API config nodes collector for service methods and classes. Has built in caching.
     */
    private static class WebApiRoutesCollector {

        private final Map<String, List<XmlTag>> routesCache = new HashMap<>();
        private final Map<String, Integer> httpMethodsSortOrder;

        public WebApiRoutesCollector() {
            httpMethodsSortOrder = new HashMap<>();
            httpMethodsSortOrder.put("GET", 1);
            httpMethodsSortOrder.put("PUT", 2);
            httpMethodsSortOrder.put("POS", 3);
            httpMethodsSortOrder.put("DEL", 4);
        }

        /**
         * Get sorted list of Web API routes related to the specified class.
         */
        public List<XmlTag> getRoutes(final @NotNull PhpClass phpClass) {
            final List<XmlTag> routesForClass = new ArrayList<>();

            for (final Method method : phpClass.getMethods()) {
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
        public List<XmlTag> getRoutes(final @NotNull Method method) {
            final String methodFqn = method.getFQN();

            if (!routesCache.containsKey(methodFqn)) {
                final List<XmlTag> routesForMethod = extractRoutesForMethod(method);
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
        public List<XmlTag> extractRoutesForMethod(final @NotNull Method method) {
            final List<XmlTag> routesForMethod = new ArrayList<>();
            final Map<String, List<XmlTag>> routesForMethodMap = new HashMap<>();

            for (final Map.Entry<String, List<XmlTag>> entry
                    : extractRoutesForMethodRecursively(method, routesForMethodMap).entrySet()) {
                routesForMethod.addAll(entry.getValue());
            }

            return routesForMethod;
        }

        private Map<String, List<XmlTag>> extractRoutesForMethodRecursively(
                final @NotNull Method method,
                final Map<String, List<XmlTag>> routesForMethod
        ) {
            routesForMethod.put(method.getFQN(), WebApiTypeIndex.getWebApiRoutes(method));
            final PhpClass phpClass = method.getContainingClass();

            if (phpClass == null) {
                return routesForMethod;
            }

            for (final PhpClass parent : method.getContainingClass().getSupers()) {
                for (final Method parentMethod : parent.getMethods()) {
                    if (parentMethod.getName().equals(method.getName())) {
                        if (routesForMethod.containsKey(parentMethod.getFQN())) {
                            continue;
                        }
                        routesForMethod.putAll(
                                extractRoutesForMethodRecursively(
                                        parentMethod,
                                        routesForMethod
                                )
                        );
                    }
                }
            }

            return routesForMethod;
        }

        /**
         * Make sure that routes are sorted as follows: GET, PUT, POST, DELETE. Then by path.
         */
        private void sortRoutes(final List<XmlTag> routes) {
            routes.sort(
                    (tag1, tag2) -> {
                        final String firstUrl = GetTagAttributeValueUtil.getValue(tag1, "url");
                        final String secondUrl = GetTagAttributeValueUtil.getValue(tag2, "url");
                        final String method1 = GetTagAttributeValueUtil.getValue(tag1, "method");
                        final String method2 = GetTagAttributeValueUtil.getValue(tag2, "method");

                        if (method1.isEmpty() || method2.isEmpty()) {
                            return firstUrl.compareTo(secondUrl);
                        }

                        if (!httpMethodsSortOrder.containsKey(method1)
                                || !httpMethodsSortOrder.containsKey(method2)) {
                            return firstUrl.compareTo(secondUrl);
                        }
                        final Integer firstSortOrder = httpMethodsSortOrder.get(method1);
                        final Integer secondSortOrder = httpMethodsSortOrder.get(method2);

                        if (firstSortOrder.compareTo(secondSortOrder) == 0) {
                            // Sort by route if HTTP methods are equal
                            return firstUrl.compareTo(secondUrl);
                        }

                        return firstSortOrder.compareTo(secondSortOrder);
                    }
            );
        }
    }

    @SuppressWarnings("PMD.UseUtilityClass")
    private static class GetTagAttributeValueUtil {

        public static @NotNull String getValue(
                final XmlTag tag,
                final @NotNull String attributeName
        ) {
            final XmlAttribute attribute = tag.getAttribute(attributeName);

            if (attribute == null) {
                return "";
            }
            final String value = attribute.getValue();

            if (value == null) {
                return "";
            }

            return value.trim();
        }
    }
}
