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
import com.magento.idea.magento2plugin.actions.generation.data.MagentoPluginMethodData;
import com.magento.idea.magento2plugin.actions.generation.generator.MagentoPluginMethodsGenerator;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Key;

public class MagentoGenerateBeforeMethodAction extends CodeInsightAction {
    private final MagentoGeneratePluginMethodHandlerBase myHandler = new MagentoGeneratePluginMethodHandlerBase(Plugin.PluginType.before) {
        protected MagentoPluginMethodData[] createPluginMethods(PhpClass currentClass, Method method, Key<Object> targetClassKey) {
            return (new MagentoPluginMethodsGenerator(currentClass, method, targetClassKey)
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
