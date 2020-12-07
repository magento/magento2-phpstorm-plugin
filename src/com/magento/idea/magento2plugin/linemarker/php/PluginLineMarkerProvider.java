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
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.PluginIndex;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PluginLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> psiElements, @NotNull Collection<? super LineMarkerInfo<?>> collection) {
        if (psiElements.size() > 0) {
            if (!Settings.isEnabled(psiElements.get(0).getProject())) {
                return;
            }
        }
        PluginClassCache pluginClassCache = new PluginClassCache();
        ClassPluginCollector classPluginCollector = new ClassPluginCollector(pluginClassCache);
        MethodPluginCollector methodPluginCollector = new MethodPluginCollector(pluginClassCache);

        for (PsiElement psiElement : psiElements) {
            if (psiElement instanceof PhpClass || psiElement instanceof Method) {
                List<? extends PsiElement> results;

                if (psiElement instanceof PhpClass) {
                    results = classPluginCollector.collect((PhpClass) psiElement);
                } else {
                    results = methodPluginCollector.collect((Method) psiElement);
                }

                if (results.size() > 0 ) {
                    collection.add(NavigationGutterIconBuilder
                            .create(AllIcons.Nodes.Plugin)
                            .setTargets(results)
                            .setTooltipText("Navigate to plugins")
                            .createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(psiElement))
                    );
                }
            }
        }
    }

    private static class PluginClassCache {
        private HashMap<String, List<PhpClass>> classPluginsMap = new HashMap<String, List<PhpClass>>();

        List<PhpClass> getPluginsForClass(@NotNull PhpClass phpClass, @NotNull String classFQN) {
            List<PhpClass> results = new ArrayList<>();

            if (classPluginsMap.containsKey(classFQN)) {
                return classPluginsMap.get(classFQN);
            }

            List<Set<String>> plugins = FileBasedIndex.getInstance()
                .getValues(PluginIndex.KEY, classFQN, GlobalSearchScope.allScope(phpClass.getProject()));

            if (plugins.size() == 0) {
                classPluginsMap.put(classFQN, results);
                return results;
            }

            PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());

            for (Set<String> pluginClassNames: plugins) {
                for (String pluginClassName: pluginClassNames) {
                    results.addAll(phpIndex.getClassesByFQN(pluginClassName));
                }
            }
            classPluginsMap.put(classFQN, results);
            return results;
        }

        List<PhpClass> getPluginsForClass(@NotNull PhpClass phpClass)
        {
            List<PhpClass> pluginsForClass = getPluginsForClass(phpClass, phpClass.getPresentableFQN());
            for (PhpClass parent: phpClass.getSupers()) {
                pluginsForClass.addAll(getPluginsForClass(parent));
            }

            return pluginsForClass;
        }

        List<Method> getPluginMethods(@NotNull PhpClass plugin) {
            List<Method> methodList = new ArrayList<Method>();
            for (Method method : plugin.getMethods()) {
                if (method.getAccess().isPublic()) {
                    String pluginMethodName = method.getName();
                    if (pluginMethodName.length() > 6) {
                        methodList.add(method);
                    }
                }
            }
            return methodList;
        }

        List<Method> getPluginMethods(List<PhpClass> plugins) {
            List<Method> methodList = new ArrayList<Method>();
            for (PhpClass plugin: plugins) {
                methodList.addAll(getPluginMethods(plugin));
            }
            return methodList;
        }
    }

    private static class ClassPluginCollector implements Collector<PhpClass, PhpClass> {
        private PluginLineMarkerProvider.PluginClassCache pluginClassCache;

        ClassPluginCollector(PluginLineMarkerProvider.PluginClassCache pluginClassCache) {
            this.pluginClassCache = pluginClassCache;
        }

        @Override
        public List<PhpClass> collect(@NotNull PhpClass psiElement) {
            return pluginClassCache.getPluginsForClass(psiElement);
        }
    }

    private static class MethodPluginCollector implements Collector<Method, Method> {
        private PluginLineMarkerProvider.PluginClassCache pluginClassCache;

        MethodPluginCollector(PluginLineMarkerProvider.PluginClassCache pluginClassCache) {
            this.pluginClassCache = pluginClassCache;
        }

        @Override
        public List<Method> collect(@NotNull Method psiElement) {
            List<Method> results = new ArrayList<>();

            PhpClass methodClass = psiElement.getContainingClass();
            if (methodClass == null) {
                return results;
            }

            List<PhpClass> pluginsList = pluginClassCache.getPluginsForClass(methodClass);
            List<Method> pluginMethods = pluginClassCache.getPluginMethods(pluginsList);

            String classMethodName = WordUtils.capitalize(psiElement.getName());
            for (Method pluginMethod: pluginMethods) {
                if (isPluginMethodName(pluginMethod.getName(), classMethodName)) {
                    results.add(pluginMethod);
                }
            }
            return results;
        }

        private boolean isPluginMethodName(String pluginMethodName, String classMethodName) {
            return pluginMethodName.substring(5).equals(classMethodName) || pluginMethodName.substring(6).equals(classMethodName);
        }
    }

    private interface Collector<T extends PsiElement, K> {
        List<K> collect(@NotNull T psiElement);
    }
}
