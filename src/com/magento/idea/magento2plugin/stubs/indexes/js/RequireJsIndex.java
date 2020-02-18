/*
 * @author Atwix Team
 * @copyright Copyright (c) 2020 Atwix (https://www.atwix.com/)
 */

package com.magento.idea.magento2plugin.stubs.indexes.js;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class RequireJsIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.require_js");

    @NotNull
    @Override
    public ID<String, String> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            Map<String, String> map = new HashMap<>();
            JSFile jsFile = (JSFile)inputData.getPsiFile();

            JSVarStatement jsVarStatement = PsiTreeUtil.getChildOfType(jsFile, JSVarStatement.class);
            if (jsVarStatement == null) {
                return map;
            }
            JSVariable[] jsVariableList = jsVarStatement.getVariables();
            for (JSVariable jsVariable : jsVariableList) {
                String name = jsVariable.getName();
                if (name.equals("config")) {
                    JSObjectLiteralExpression config = PsiTreeUtil.getChildOfType(jsVariable, JSObjectLiteralExpression.class);
                    if (config == null) {
                        return map;
                    }
                    parseConfigMap(map, config);

                    JSProperty pathsMap = config.findProperty("paths");
                    if (pathsMap == null) {
                        return map;
                    }
                    JSObjectLiteralExpression[] pathGroupsWrappers = PsiTreeUtil.getChildrenOfType(pathsMap, JSObjectLiteralExpression.class);
                    for (JSObjectLiteralExpression pathGroupsWrapper : pathGroupsWrappers) {
                        JSProperty[] allConfigs = pathGroupsWrapper.getProperties();
                        for (JSProperty mapping : allConfigs) {
                            String nameConfig = mapping.getName();
                            JSExpression value = mapping.getValue();
                            if (value == null) {
                                continue;
                            }
                            String valueConfig = value.getText();
                            map.put(nameConfig, valueConfig);
                        }
                    }
                }
            }

            return map;
        };
    }

    private void parseConfigMap(Map<String, String> map, JSObjectLiteralExpression config) {
        JSProperty configMap = config.findProperty("map");
        if (configMap == null) {
            return;
        }

        JSObjectLiteralExpression[] configGroupsWrappers = PsiTreeUtil.getChildrenOfType(configMap, JSObjectLiteralExpression.class);
        for (JSObjectLiteralExpression configGroupsWrapper : configGroupsWrappers) {
            PsiElement[] configGroups = configGroupsWrapper.getChildren();

            for (PsiElement configGroup : configGroups) {
                JSObjectLiteralExpression mappingWrapper = PsiTreeUtil.getChildOfType(configGroup, JSObjectLiteralExpression.class);
                JSProperty[] allConfigs = mappingWrapper.getProperties();

                for (JSProperty mapping : allConfigs) {
                    String nameConfig = mapping.getName();
                    JSExpression value = mapping.getValue();
                    if (value == null) {
                        continue;
                    }
                    String valueConfig = value.getText();
                    map.put(nameConfig, valueConfig);
                }
            }
        }
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> (
                virtualFile.getFileType().equals(JavaScriptFileType.INSTANCE) && virtualFile.getName().equals("requirejs-config.js")
        );
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @NotNull
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }
}
