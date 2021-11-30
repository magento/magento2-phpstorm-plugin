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
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class RequireJsIndex extends FileBasedIndexExtension<String, String> {

    public static final ID<String, String> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.require_js"
    );

    @Override
    public @NotNull ID<String, String> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, String> map = new HashMap<>();
            final JSFile jsFile = (JSFile) inputData.getPsiFile();

            final JSVarStatement jsVarStatement = PsiTreeUtil.getChildOfType(
                    jsFile,
                    JSVarStatement.class
            );

            if (jsVarStatement == null) {
                return map;
            }
            final JSVariable[] jsVariableList = jsVarStatement.getVariables();

            for (final JSVariable jsVariable : jsVariableList) {
                final String name = jsVariable.getName();

                if ("config".equals(name)) {
                    final JSObjectLiteralExpression config = PsiTreeUtil.getChildOfType(
                            jsVariable,
                            JSObjectLiteralExpression.class
                    );

                    if (config == null) {
                        return map;
                    }
                    parseConfigMap(map, config);
                    parseConfigPaths(map, config);
                }
            }

            return map;
        };
    }

    private void parseConfigMap(
            final Map<String, String> map,
            final JSObjectLiteralExpression config
    ) {
        final JSProperty configMap = config.findProperty("map");

        if (configMap == null) {
            return;
        }
        final JSObjectLiteralExpression[] configGroupsWrappers = PsiTreeUtil.getChildrenOfType(
                configMap,
                JSObjectLiteralExpression.class
        );
        if (configGroupsWrappers == null) {
            return;
        }

        for (final JSObjectLiteralExpression configGroupsWrapper : configGroupsWrappers) {
            final PsiElement[] configGroups = configGroupsWrapper.getChildren();

            for (final PsiElement configGroup : configGroups) {
                final JSObjectLiteralExpression mappingWrapper = PsiTreeUtil.getChildOfType(
                        configGroup,
                        JSObjectLiteralExpression.class
                );

                if (mappingWrapper == null) {
                    continue;
                }
                processObjectProperties(map, mappingWrapper.getProperties());
            }
        }
    }

    private void parseConfigPaths(
            final Map<String, String> map,
            final JSObjectLiteralExpression config
    ) {
        final JSProperty pathsMap = config.findProperty("paths");

        if (pathsMap == null) {
            return;
        }
        final JSObjectLiteralExpression[] pathGroupsWrappers = PsiTreeUtil
                .getChildrenOfType(pathsMap, JSObjectLiteralExpression.class);

        if (pathGroupsWrappers == null) {
            return;
        }

        for (final JSObjectLiteralExpression pathGroupsWrapper : pathGroupsWrappers) {
            processObjectProperties(map, pathGroupsWrapper.getProperties());
        }
    }

    private void processObjectProperties(
            final Map<String, String> map,
            final JSProperty... allConfigs
    ) {
        for (final JSProperty mapping : allConfigs) {
            final String nameConfig = mapping.getName();
            final JSExpression value = mapping.getValue();

            if (value == null) {
                continue;
            }
            final String valueConfig = value.getText();
            map.put(nameConfig, valueConfig);
        }
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile ->
                virtualFile.getFileType().equals(JavaScriptFileType.INSTANCE)
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

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }
}
