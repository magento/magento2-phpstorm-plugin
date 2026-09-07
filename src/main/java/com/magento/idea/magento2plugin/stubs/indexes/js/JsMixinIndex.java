/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.js;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.data.StringSetDataExternalizer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsMixinIndex extends FileBasedIndexExtension<String, Set<String>> {
    public static final ID<String, Set<String>> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.js_mixins"
    );

    @Override
    public @NotNull ID<String, Set<String>> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Set<String>> map = new HashMap<>();

            if (!Settings.isEnabled(inputData.getProject())) {
                return map;
            }

            final JSFile jsFile = (JSFile) inputData.getPsiFile();
            map.putAll(getMixinMap(jsFile));

            return map;
        };
    }

    public static @NotNull Map<String, Set<String>> getMixinMap(final @NotNull JSFile jsFile) {
        final Map<String, Set<String>> map = new HashMap<>();
        final JSVarStatement jsVarStatement = PsiTreeUtil.getChildOfType(
                jsFile,
                JSVarStatement.class
        );

        if (jsVarStatement == null) {
            return map;
        }

        for (final JSVariable jsVariable : jsVarStatement.getVariables()) {
            if (!"config".equals(jsVariable.getName())) {
                continue;
            }
            final JSObjectLiteralExpression config = PsiTreeUtil.getChildOfType(
                    jsVariable,
                    JSObjectLiteralExpression.class
            );

            if (config != null) {
                parseMixins(map, config);
            }
        }

        return map;
    }

    private static void parseMixins(
            final @NotNull Map<String, Set<String>> map,
            final @NotNull JSObjectLiteralExpression rootConfig
    ) {
        final JSObjectLiteralExpression config = getObjectValue(rootConfig.findProperty("config"));

        if (config == null) {
            return;
        }

        final JSObjectLiteralExpression mixins = getObjectValue(config.findProperty("mixins"));

        if (mixins == null) {
            return;
        }

        for (final JSProperty targetProperty : mixins.getProperties()) {
            final String target = normalizePath(targetProperty.getName());
            final JSObjectLiteralExpression mixinDeclarations = getObjectValue(targetProperty);

            if (target == null || mixinDeclarations == null) {
                continue;
            }

            for (final JSProperty mixinProperty : mixinDeclarations.getProperties()) {
                final String mixin = normalizePath(mixinProperty.getName());

                if (mixin != null) {
                    map.computeIfAbsent(target, ignored -> new LinkedHashSet<>()).add(mixin);
                }
            }
        }
    }

    private static @Nullable JSObjectLiteralExpression getObjectValue(final @Nullable JSProperty property) {
        if (property == null) {
            return null;
        }
        final JSExpression value = property.getValue();

        return value instanceof JSObjectLiteralExpression ? (JSObjectLiteralExpression) value : null;
    }

    private static @Nullable String normalizePath(final @Nullable String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        return path.replace("\"", "").replace("'", "");
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull DataExternalizer<Set<String>> getValueExternalizer() {
        return StringSetDataExternalizer.INSTANCE;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType().equals(JavaScriptFileType.INSTANCE)
                && "requirejs-config.js".equals(virtualFile.getName());
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
