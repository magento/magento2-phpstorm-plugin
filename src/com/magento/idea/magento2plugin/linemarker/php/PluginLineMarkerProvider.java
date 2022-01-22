/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.linemarker.SearchGutterIconNavigationHandler;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.PluginIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginLineMarkerProvider implements LineMarkerProvider {

    private static final String TOOLTIP_TEXT = "Navigate to plugins";
    private static final int MIN_PLUGIN_METHOD_NAME_LENGTH = 6;

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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
        final PluginClassCache pluginClassCache = new PluginClassCache();
        final ClassPluginCollector classPluginCollector = new ClassPluginCollector(
                pluginClassCache
        );
        final MethodPluginCollector methodPluginCollector = new MethodPluginCollector(
                pluginClassCache
        );

        for (final PsiElement psiElement : psiElements) {
            if (psiElement instanceof PhpClass || psiElement instanceof Method) {
                final List<? extends PsiElement> results;

                if (psiElement instanceof PhpClass) {
                    results = classPluginCollector.collect((PhpClass) psiElement);
                } else {
                    results = methodPluginCollector.collect((Method) psiElement);
                }

                if (!results.isEmpty()) {
                    final GutterIconNavigationHandler<PsiElement> navigationHandler =
                            new SearchGutterIconNavigationHandler<>(
                                    (Collection<? extends NavigatablePsiElement>) results,
                                    TOOLTIP_TEXT
                            );

                    collection.add(
                            NavigationGutterIconBuilder
                                    .create(AllIcons.Nodes.Plugin)
                                    .setTargets(results)
                                    .setTooltipText(TOOLTIP_TEXT)
                                    .createLineMarkerInfo(
                                            PsiTreeUtil.getDeepestFirst(psiElement),
                                            navigationHandler
                                    )
                    );
                }
            }
        }
    }

    private static class PluginClassCache {

        private final Map<String, List<PhpClass>> classPluginsMap = new HashMap<>();

        public List<PhpClass> getPluginsForClass(final @NotNull PhpClass phpClass) {
            final List<PhpClass> pluginsForClass = getPluginsForClass(
                    phpClass,
                    phpClass.getPresentableFQN()
            );

            for (final PhpClass parent : phpClass.getSupers()) {
                if (classPluginsMap.containsKey(parent.getFQN().substring(1))) {
                    continue;
                }
                pluginsForClass.addAll(getPluginsForClass(parent));
            }

            return pluginsForClass;
        }

        public List<PhpClass> getPluginsForClass(
                final @NotNull PhpClass phpClass,
                final @NotNull String classFQN
        ) {
            if (classPluginsMap.containsKey(classFQN)) {
                return classPluginsMap.get(classFQN);
            }

            final List<Set<String>> plugins = FileBasedIndex.getInstance()
                    .getValues(
                            PluginIndex.KEY,
                            classFQN,
                            GlobalSearchScope.allScope(phpClass.getProject())
                    );
            final List<PhpClass> results = new ArrayList<>();

            if (plugins.isEmpty()) {
                classPluginsMap.put(classFQN, results);

                return results;
            }
            final PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());

            for (final Set<String> pluginClassNames : plugins) {
                for (final String pluginClassName: pluginClassNames) {
                    results.addAll(phpIndex.getClassesByFQN(pluginClassName));
                }
            }
            classPluginsMap.put(classFQN, results);

            return results;
        }

        public List<Method> getPluginMethods(final List<PhpClass> plugins) {
            final List<Method> methodList = new ArrayList<>();

            for (final PhpClass plugin: plugins) {
                methodList.addAll(getPluginMethods(plugin));
            }

            return methodList;
        }

        public List<Method> getPluginMethods(final @NotNull PhpClass pluginClass) {
            final List<Method> methodList = new ArrayList<>();

            for (final Method method : pluginClass.getMethods()) {
                if (method.getAccess().isPublic()) {
                    final String pluginMethodName = method.getName();

                    if (pluginMethodName.length() > MIN_PLUGIN_METHOD_NAME_LENGTH) {
                        methodList.add(method);
                    }
                }
            }

            return methodList;
        }
    }

    private static class ClassPluginCollector implements Collector<PhpClass, PhpClass> {

        private final PluginLineMarkerProvider.PluginClassCache pluginClassCache;

        public ClassPluginCollector(
                final PluginLineMarkerProvider.PluginClassCache pluginClassCache
        ) {
            this.pluginClassCache = pluginClassCache;
        }

        @Override
        public List<PhpClass> collect(final @NotNull PhpClass psiElement) {
            return pluginClassCache.getPluginsForClass(psiElement);
        }
    }

    private static class MethodPluginCollector implements Collector<Method, Method> {

        private final PluginLineMarkerProvider.PluginClassCache pluginClassCache;

        public MethodPluginCollector(
                final PluginLineMarkerProvider.PluginClassCache pluginClassCache
        ) {
            this.pluginClassCache = pluginClassCache;
        }

        @Override
        public List<Method> collect(final @NotNull Method psiElement) {
            final List<Method> results = new ArrayList<>();

            final PhpClass methodClass = psiElement.getContainingClass();

            if (methodClass == null) {
                return results;
            }
            final List<PhpClass> pluginsList = pluginClassCache.getPluginsForClass(methodClass);
            final List<Method> pluginMethods = pluginClassCache.getPluginMethods(pluginsList);

            final String classMethodName = WordUtils.capitalize(psiElement.getName());

            for (final Method pluginMethod: pluginMethods) {
                if (isPluginMethodName(pluginMethod.getName(), classMethodName)) {
                    results.add(pluginMethod);
                }
            }

            return results;
        }

        private boolean isPluginMethodName(
                final String pluginMethodName,
                final String classMethodName
        ) {
            return pluginMethodName.substring(5).equals(classMethodName)
                    || pluginMethodName.substring(6).equals(classMethodName);
        }
    }

    private interface Collector<T extends PsiElement, K> {
        List<K> collect(@NotNull T psiElement);
    }
}
