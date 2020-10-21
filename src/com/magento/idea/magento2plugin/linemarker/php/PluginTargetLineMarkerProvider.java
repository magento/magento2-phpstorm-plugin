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
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            @NotNull List<PsiElement> psiElements,
            @NotNull Collection<LineMarkerInfo> collection
    ) {
        if (psiElements.size() > 0) {
            if (!Settings.isEnabled(psiElements.get(0).getProject())) {
                return;
            }
        }
        PluginClassCache pluginClassCache = new PluginClassCache();
        TargetClassesCollector targetClassesCollector =
                new TargetClassesCollector(pluginClassCache);
        TargetMethodsCollector targetMethodsCollector =
                new TargetMethodsCollector(pluginClassCache);

        for (PsiElement psiElement : psiElements) {
            if (psiElement instanceof PhpClass || psiElement instanceof Method) {
                List<? extends PsiElement> results;

                if (psiElement instanceof PhpClass) {
                    results = targetClassesCollector.collect((PhpClass) psiElement);
                    if (results.size() > 0) {
                        collection.add(NavigationGutterIconBuilder
                                .create(AllIcons.Nodes.Class)
                                .setTargets(results)
                                .setTooltipText("Navigate to target class")
                                .createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(psiElement))
                        );
                    }
                } else {
                    results = targetMethodsCollector.collect((Method) psiElement);
                    if (results.size() > 0) {
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
        private HashMap<String, List<PhpClass>> pluginClassesMap =
                new HashMap<String, List<PhpClass>>();

        List<PhpClass> getTargetClassesForPlugin(
                @NotNull PhpClass phpClass,
                @NotNull String classFQN
        ) {
            List<PhpClass> results = new ArrayList<>();

            if (pluginClassesMap.containsKey(classFQN)) {
                return pluginClassesMap.get(classFQN);
            }

            GetTargetClassNamesByPluginClassName targetClassesService =
                    GetTargetClassNamesByPluginClassName.getInstance(phpClass.getProject());
            ArrayList<String> targetClassNames = targetClassesService.execute(classFQN);

            if (targetClassNames.size() == 0) {
                pluginClassesMap.put(classFQN, results);
                return results;
            }

            PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());

            for (String targetClassName : targetClassNames) {
                Collection<PhpClass> targets = phpIndex.getClassesByFQN(targetClassName);

                if (targets.isEmpty()) {
                    targets = phpIndex.getInterfacesByFQN(targetClassName);
                }

                results.addAll(targets);
            }

            return results;
        }

        List<PhpClass> getTargetClassesForPlugin(@NotNull PhpClass phpClass) {
            List<PhpClass> classesForPlugin = getTargetClassesForPlugin(
                    phpClass, phpClass.getPresentableFQN()
            );
            for (PhpClass parent: phpClass.getSupers()) {
                classesForPlugin.addAll(getTargetClassesForPlugin(parent));
            }

            return classesForPlugin;
        }
    }

    private static class TargetClassesCollector implements Collector<PhpClass, PhpClass> {
        private PluginTargetLineMarkerProvider.PluginClassCache pluginClassCache;

        TargetClassesCollector(PluginTargetLineMarkerProvider.PluginClassCache pluginClassCache) {
            this.pluginClassCache = pluginClassCache;
        }

        @Override
        public List<PhpClass> collect(@NotNull PhpClass psiElement) {
            return pluginClassCache.getTargetClassesForPlugin(psiElement);
        }
    }

    private static class TargetMethodsCollector implements Collector<Method, Method> {
        private PluginTargetLineMarkerProvider.PluginClassCache pluginClassCache;

        TargetMethodsCollector(PluginTargetLineMarkerProvider.PluginClassCache pluginClassCache) {
            this.pluginClassCache = pluginClassCache;
        }

        @Override
        public List<Method> collect(@NotNull Method pluginMethod) {
            List<Method> results = new ArrayList<>();

            /* Check if the method is a plugin */
            if (null == getPluginPrefix(pluginMethod)) {
                return results;
            }

            PhpClass pluginClass = pluginMethod.getContainingClass();
            if (pluginClass == null) {
                return results;
            }

            List<PhpClass> targetClasses = pluginClassCache.getTargetClassesForPlugin(pluginClass);
            if (targetClasses.size() == 0) {
                return results;
            }

            for (PhpClass targetClass: targetClasses) {
                String pluginPrefix = getPluginPrefix(pluginMethod);
                String targetClassMethodName = getTargetMethodName(pluginMethod, pluginPrefix);
                if (targetClassMethodName == null) {
                    continue;
                }

                Method targetMethod = targetClass.findMethodByName(targetClassMethodName);
                if (targetMethod == null) {
                    continue;
                }

                results.add(targetMethod);
            }

            return results;
        }

        private String getTargetMethodName(Method pluginMethod, String pluginPrefix) {
            String pluginMethodName = pluginMethod.getName();
            String targetClassMethodName = pluginMethodName.replace(pluginPrefix, "");
            if (targetClassMethodName.isEmpty()) {
                return null;
            }
            char firstCharOfTargetName = targetClassMethodName.charAt(0);
            int charType = Character.getType(firstCharOfTargetName);
            if (charType == Character.LOWERCASE_LETTER) {
                return null;
            }
            return Character.toLowerCase(firstCharOfTargetName)
                    + targetClassMethodName.substring(1);
        }

        private String getPluginPrefix(Method pluginMethod) {
            String pluginMethodName = pluginMethod.getName();
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
