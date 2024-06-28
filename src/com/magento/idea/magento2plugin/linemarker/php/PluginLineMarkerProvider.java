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
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.linemarker.SearchGutterIconNavigationHandler;
import com.magento.idea.magento2plugin.linemarker.php.data.PluginMethodData;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.PluginIndex;
import com.magento.idea.magento2plugin.stubs.indexes.data.PluginData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
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

    @SuppressWarnings("checkstyle:LineLength")
    private static class PluginClassCache {

        private final Map<String, List<PluginData>> classPluginsMap = new HashMap<>();

        public List<PluginData> getPluginsForClass(final @NotNull PhpClass phpClass) {
            final List<PluginData> pluginsForClass = getPluginsForClass(
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

        public List<PluginData> getPluginsForClass(
                final @NotNull PhpClass phpClass,
                final @NotNull String classFQN
        ) {
            if (classPluginsMap.containsKey(classFQN)) {
                return classPluginsMap.get(classFQN);
            }

            final List<Set<PluginData>> plugins = FileBasedIndex.getInstance()
                    .getValues(
                            PluginIndex.KEY,
                            classFQN,
                            GlobalSearchScope.allScope(phpClass.getProject())
                    );
            final List<PluginData> results = new ArrayList<>();

            if (plugins.isEmpty()) {
                classPluginsMap.put(classFQN, results);

                return results;
            }

            for (final Set<PluginData> pluginDataList : plugins) {
                for (final PluginData pluginData: pluginDataList) {
                    pluginData.setPhpClass(phpClass);
                    results.add(pluginData);
                }
            }
            classPluginsMap.put(classFQN, results);

            return results;
        }

        public List<PluginMethodData> getPluginMethods(final List<PluginData> pluginDataList) {
            final List<PluginMethodData> result = new ArrayList<>();

            for (final PluginData pluginData: pluginDataList) {
                for (final PhpClass plugin: pluginData.getPhpClassCollection()) {
                    //@todo add module sequence ID if sortOrder equal zero. It should be negative value.
                    result.addAll(getPluginMethods(plugin, pluginData.getSortOrder()));
                }
            }

            return result;
        }

        public List<PluginMethodData> getPluginMethods(final @NotNull PhpClass pluginClass, final int sortOrder) {
            final List<PluginMethodData> methodList = new ArrayList<>();

            for (final Method method : pluginClass.getMethods()) {
                if (method.getAccess().isPublic()) {
                    final String pluginMethodName = method.getName();

                    if (pluginMethodName.length() > MIN_PLUGIN_METHOD_NAME_LENGTH) {
                        //@todo module sequence value should be set here instead of zero.
                        methodList.add(getPluginMethodDataObject(method, sortOrder, 0));
                    }
                }
            }

            return methodList;
        }

        @NotNull
        private PluginMethodData getPluginMethodDataObject(final Method method, final int sortOrder, final int moduleSequence) {
            return new PluginMethodData(method, sortOrder, moduleSequence);
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
            final List<PluginData> pluginDataList = pluginClassCache.getPluginsForClass(psiElement);
            final List<PhpClass> phpClassList =  new ArrayList<>();

            for (final PluginData pluginData: pluginDataList) {
                phpClassList.addAll(pluginData.getPhpClassCollection());
            }

            return phpClassList;
        }
    }

    private static class MethodPluginCollector implements Collector<Method, Method> {

        private final PluginLineMarkerProvider.PluginClassCache pluginClassCache;
        private final Map<String, Integer> pluginMethodsSortOrder;
        private static final String AFTER_PLUGIN_PREFIX = "after";

        public MethodPluginCollector(
                final PluginLineMarkerProvider.PluginClassCache pluginClassCache
        ) {
            this.pluginClassCache = pluginClassCache;
            pluginMethodsSortOrder = new HashMap<>();
            pluginMethodsSortOrder.put("before", 1);
            pluginMethodsSortOrder.put("around", 2);
            pluginMethodsSortOrder.put(AFTER_PLUGIN_PREFIX, 3);
        }

        @SuppressWarnings("checkstyle:LineLength")
        @Override
        public List<Method> collect(final @NotNull Method psiElement) {
            final List<Method> results = new ArrayList<>();
            final PhpClass methodClass = psiElement.getContainingClass();

            if (methodClass == null) {
                return results;
            }

            final List<PluginData> pluginDataList = pluginClassCache.getPluginsForClass(methodClass);
            final List<PluginMethodData> pluginMethods = pluginClassCache.getPluginMethods(pluginDataList);
            final String classMethodName = StringUtils.capitalize(psiElement.getName());

            pluginMethods.removeIf(pluginMethod -> !isPluginMethodName(pluginMethod.getMethodName(), classMethodName));
            sortMethods(pluginMethods, results);

            return results;
        }

        @SuppressWarnings({"checkstyle:Indentation", "checkstyle:OperatorWrap", "checkstyle:LineLength", "PMD.NPathComplexity"})
        private void sortMethods(final @NotNull List<PluginMethodData> methodDataList, final List<Method> results) {
            final List<Integer> bufferSortOrderList = new ArrayList<>();
            int biggestSortOrder = 0;

            for (final PluginMethodData pluginMethodData: methodDataList) {
                final String methodName = pluginMethodData.getMethodName();

                if (methodName.startsWith("around")) {
                    bufferSortOrderList.add(pluginMethodData.getSortOrder());
                }

                if (pluginMethodData.getSortOrder() > biggestSortOrder) {
                    biggestSortOrder = pluginMethodData.getSortOrder();
                }
            }

            final int biggestSortOrderValue = biggestSortOrder;

            methodDataList.sort(
                (PluginMethodData method1, PluginMethodData method2) -> {
                    final String firstMethodName =  method1.getMethodName();
                    final String secondMethodName =  method2.getMethodName();
                    final int firstIndexEnd = firstMethodName.startsWith(AFTER_PLUGIN_PREFIX) ? 5 : 6;
                    final int secondIndexEnd = secondMethodName.startsWith(AFTER_PLUGIN_PREFIX) ? 5 : 6;
                    final String firstMethodPrefix =  firstMethodName.substring(0,firstIndexEnd);
                    final String secondMethodPrefix =  secondMethodName.substring(0,secondIndexEnd);

                    if (!pluginMethodsSortOrder.containsKey(firstMethodPrefix)
                        || !pluginMethodsSortOrder.containsKey(secondMethodPrefix)) {
                        return firstMethodName.compareTo(secondMethodName);
                    }

                    final Integer firstNameSortOrder = pluginMethodsSortOrder.get(firstMethodPrefix);
                    final Integer secondNameSortOrder = pluginMethodsSortOrder.get(secondMethodPrefix);

                    if (firstNameSortOrder.compareTo(secondNameSortOrder) != 0) {
                        return firstNameSortOrder.compareTo(secondNameSortOrder);
                    }

                    Integer firstBuffer = 0;
                    Integer secondBuffer = 0;
                    final Integer firstModuleSequence = (method1.getModuleSequence() + biggestSortOrderValue) * -1;
                    final Integer secondModuleSequence = (method2.getModuleSequence() + biggestSortOrderValue) * -1;
                    final Integer firstSortOrder = method1.getSortOrder() == 0 ?
                        firstModuleSequence :
                        method1.getSortOrder();
                    final Integer secondSortOrder = method2.getSortOrder() == 0 ?
                        secondModuleSequence :
                        method2.getSortOrder();

                    if (!bufferSortOrderList.isEmpty() && firstMethodPrefix.equals(AFTER_PLUGIN_PREFIX)) {
                        for (final Integer bufferSortOrder : bufferSortOrderList) {
                            if (bufferSortOrder < firstSortOrder && firstBuffer < bufferSortOrder + 1) {
                                firstBuffer = bufferSortOrder + 1;
                            }

                            if (bufferSortOrder < secondSortOrder && secondBuffer < bufferSortOrder + 1) {
                                secondBuffer = bufferSortOrder + 1;
                            }
                        }
                    }

                    firstBuffer = firstBuffer.equals(0) ? firstSortOrder : firstBuffer * -1;
                    secondBuffer = secondBuffer.equals(0) ? secondSortOrder : secondBuffer * -1;

                    if (firstBuffer.compareTo(secondBuffer) == 0 && firstSortOrder.compareTo(secondSortOrder) != 0) {
                        return firstSortOrder.compareTo(secondSortOrder);
                    }

                    if (firstBuffer.compareTo(secondBuffer) == 0 && firstSortOrder.compareTo(secondSortOrder) == 0) {
                        return firstModuleSequence.compareTo(secondModuleSequence);
                    }

                    return firstBuffer.compareTo(secondBuffer);
                }
            );

            for (final PluginMethodData pluginMethodData: methodDataList) {
                results.add(pluginMethodData.getMethod());
            }
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
