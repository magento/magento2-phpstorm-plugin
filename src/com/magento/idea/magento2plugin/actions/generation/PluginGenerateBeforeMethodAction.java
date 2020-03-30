/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.code.PluginMethodData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.PluginMethodsGenerator;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Key;

public class PluginGenerateBeforeMethodAction extends CodeInsightAction {
    private final PluginGenerateMethodHandlerBase myHandler = new PluginGenerateMethodHandlerBase(Plugin.PluginType.before) {
        protected PluginMethodData[] createPluginMethods(PhpClass currentClass, Method method, Key<Object> targetClassKey) {
            return (new PluginMethodsGenerator(currentClass, method, targetClassKey)
                    .createPluginMethods(Plugin.PluginType.before));
        }
    };

    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return this.myHandler.isValidFor(editor, file);
    }

    @NotNull
    protected CodeInsightActionHandler getHandler() {
        return this.myHandler;
    }
}
