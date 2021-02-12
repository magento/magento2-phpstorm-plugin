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
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.plugin.GetTargetClassNamesByPluginClassName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginTargetLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> psiElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (!psiElements.isEmpty() && !Settings.isEnabled(psiElements.get(0).getProject())) {
            return;
        }
        final PluginClassCache pluginClassCache = new PluginClassCache();
        final TargetClassesCollector targetClassesCollector =
                new TargetClassesCollector(pluginClassCache);
        final TargetMethodsCollector targetMethodsCollector =
                new TargetMethodsCollector(pluginClassCache);

        for (final PsiElement psiElement : psiElements) {
            if (psiElement instanceof PhpClass || psiElement instanceof Method) {
                List<? extends PsiElement> results;

                if (psiElement instanceof PhpClass) {
                    results = targetClassesCollector.collect((PhpClass) psiElement);
                    if (!results.isEmpty()) {
                        collection.add(NavigationGutterIconBuilder
                                .create(AllIcons.Nodes.Class)
                                .setTargets(results)
                                .setTooltipText("Navigate to target class")
                                .createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(psiElement))
                        );
                    }
                } else {
                    results = targetMethodsCollector.collect((Method) psiElement);
                    if (!results.isEmpty()) {
                        collection.add(NavigationGutterIconBuilder
                                .create(AllIcons.Nodes.Method)
                                .setTargets(results)
                                .setTooltipText("Navigate to target method")
                                .createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(psiElement))
                        );
                    }
                }

            }
        }
    }

    private static class PluginClassCache {
        private final HashMap<String, List<PhpClass>> pluginClassesMap = // NOPMD
                new HashMap<>();

        private List<PhpClass> getTargetClassesForPlugin(
                @NotNull final PhpClass phpClass,
                @NotNull final String classFQN
        ) {

            if (pluginClassesMap.containsKey(classFQN)) {
                return pluginClassesMap.get(classFQN);
            }

            final GetTargetClassNamesByPluginClassName targetClassesService =
                    GetTargetClassNamesByPluginClassName.getInstance(phpClass.getProject());
            final ArrayList<String> targetClassNames = targetClassesService.execute(classFQN);

            final List<PhpClass> results = new ArrayList<>();

            if (targetClassNames.isEmpty()) {
                pluginClassesMap.put(classFQN, results);
                return results;
            }

            final PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());

            for (final String targetClassName : targetClassNames) {
                Collection<PhpClass> targets = phpIndex.getInterfacesByFQN(targetClassName);

                if (targets.isEmpty()) {
                    targets = phpIndex.getClassesByFQN(targetClassName);
                }

                results.addAll(targets);
            }

            return results;
        }

        protected List<PhpClass> getTargetClassesForPlugin(@NotNull final PhpClass phpClass) {
            final List<PhpClass> classesForPlugin = getTargetClassesForPlugin(
                    phpClass, phpClass.getPresentableFQN()
            );
            for (final PhpClass parent: phpClass.getSupers()) {
                classesForPlugin.addAll(getTargetClassesForPlugin(parent));
            }

            return classesForPlugin;
        }
    }

    private static class TargetClassesCollector implements Collector<PhpClass, PhpClass> {
        private final PluginTargetLineMarkerProvider.PluginClassCache pluginClassCache;

        TargetClassesCollector(// NOPMD
                final PluginTargetLineMarkerProvider.PluginClassCache pluginClassCache
        ) {
            this.pluginClassCache = pluginClassCache;
        }

        @Override
        public List<PhpClass> collect(@NotNull final PhpClass psiElement) {
            return pluginClassCache.getTargetClassesForPlugin(psiElement);
        }
    }

    private static class TargetMethodsCollector implements Collector<Method, Method> {
        private final PluginTargetLineMarkerProvider.PluginClassCache pluginClassCache;

        TargetMethodsCollector(// NOPMD
                final PluginTargetLineMarkerProvider.PluginClassCache pluginClassCache
        ) {
            this.pluginClassCache = pluginClassCache;
        }

        @Override
        public List<Method> collect(@NotNull final Method pluginMethod) {
            final List<Method> results = new ArrayList<>();

            /* Check if the method is a plugin */
            if (null == getPluginPrefix(pluginMethod)) {
                return results;
            }

            final PhpClass pluginClass = pluginMethod.getContainingClass();
            if (pluginClass == null) {
                return results;
            }

            final List<PhpClass> targetClasses
                    = pluginClassCache.getTargetClassesForPlugin(pluginClass);
            if (targetClasses.isEmpty()) {
                return results;
            }

            for (final PhpClass targetClass: targetClasses) {
                final String pluginPrefix = getPluginPrefix(pluginMethod);
                final String targetClassMethodName = getTargetMethodName(
                        pluginMethod, pluginPrefix
                );
                if (targetClassMethodName == null) {
                    continue;
                }

                final Method targetMethod = targetClass.findMethodByName(targetClassMethodName);
                if (targetMethod == null) {
                    continue;
                }

                results.add(targetMethod);
            }

            return results;
        }

        private String getTargetMethodName(final Method pluginMethod, final String pluginPrefix) {
            final String targetClassMethodName = pluginMethod.getName().replace(pluginPrefix, "");
            if (targetClassMethodName.isEmpty()) {
                return null;
            }
            final char firstCharOfTargetName = targetClassMethodName.charAt(0);
            if (Character.getType(firstCharOfTargetName) == Character.LOWERCASE_LETTER) {
                return null;
            }
            return Character.toLowerCase(firstCharOfTargetName)
                    + targetClassMethodName.substring(1);
        }

        private String getPluginPrefix(final Method pluginMethod) {
            final String pluginMethodName = pluginMethod.getName();
            if (pluginMethodName.startsWith(Plugin.PluginType.around.toString())) {
                return Plugin.PluginType.around.toString();
            }
            if (pluginMethodName.startsWith(Plugin.PluginType.before.toString())) {
                return Plugin.PluginType.before.toString();
            }
            if (pluginMethodName.startsWith(Plugin.PluginType.after.toString())) {
                return Plugin.PluginType.after.toString();
            }

            return null;
        }
    }

    private interface Collector<T extends PsiElement, K> {
        List<K> collect(@NotNull T psiElement);
    }
}
