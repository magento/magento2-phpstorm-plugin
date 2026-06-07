/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.ui;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationDataSetExternalizer;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UiComponentTemplateDeclarationIndex
        extends FileBasedIndexExtension<String, Set<UiComponentNavigationData>> {
    private static final String TEMPLATE_PATH_PREFIX = "templatePath:";
    private static final String COMPONENT_NAME_PREFIX = "componentName:";
    private static final String COMPONENT_JS_PATH_PREFIX = "componentJsPath:";

    public static final ID<String, Set<UiComponentNavigationData>> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.ui_component_template_declarations"
    );

    public static @NotNull String templatePathKey(final @NotNull String templatePath) {
        return TEMPLATE_PATH_PREFIX + templatePath;
    }

    public static @NotNull String componentNameKey(final @NotNull String componentName) {
        return COMPONENT_NAME_PREFIX + componentName;
    }

    public static @NotNull String componentJsPathKey(final @NotNull String componentJsPath) {
        return COMPONENT_JS_PATH_PREFIX + componentJsPath;
    }

    @Override
    public @NotNull ID<String, Set<UiComponentNavigationData>> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, Set<UiComponentNavigationData>, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Set<UiComponentNavigationData>> map = new HashMap<>();

            if (!Settings.isEnabled(inputData.getProject())) {
                return map;
            }
            for (final UiComponentNavigationData declaration : UiComponentScopeResolver.getInstance()
                    .collectComponentDeclarations(inputData.getPsiFile())) {
                if (!isTemplateDeclaration(declaration)) {
                    continue;
                }
                add(map, templatePathKey(declaration.getValue()), declaration);
                addNullable(map, declaration.getComponentName(), declaration, UiComponentTemplateDeclarationIndex::componentNameKey);
                addNullable(map, declaration.getComponentJsPath(), declaration, UiComponentTemplateDeclarationIndex::componentJsPathKey);
            }

            return map;
        };
    }

    private boolean isTemplateDeclaration(final @NotNull UiComponentNavigationData declaration) {
        return UiComponentNavigationData.KIND_TEMPLATE.equals(declaration.getKind())
                || UiComponentNavigationData.KIND_CHILD_TEMPLATE.equals(declaration.getKind())
                || UiComponentNavigationData.KIND_TEMPLATES.equals(declaration.getKind());
    }

    private void addNullable(
            final @NotNull Map<String, Set<UiComponentNavigationData>> map,
            final @Nullable String value,
            final @NotNull UiComponentNavigationData declaration,
            final @NotNull KeyBuilder keyBuilder
    ) {
        if (value == null || value.isBlank()) {
            return;
        }
        add(map, keyBuilder.build(value), declaration);
    }

    private void add(
            final @NotNull Map<String, Set<UiComponentNavigationData>> map,
            final @NotNull String key,
            final @NotNull UiComponentNavigationData declaration
    ) {
        map.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(declaration);
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull DataExternalizer<Set<UiComponentNavigationData>> getValueExternalizer() {
        return UiComponentNavigationDataSetExternalizer.INSTANCE;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType().equals(JavaScriptFileType.INSTANCE)
                || virtualFile.getFileType().equals(XmlFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    private interface KeyBuilder {
        @NotNull String build(@NotNull String value);
    }
}
