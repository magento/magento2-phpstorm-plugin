/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.MagentoPluginMethodData;
import com.magento.idea.magento2plugin.actions.generation.generator.MagentoPluginMethodsGenerator;
import org.jetbrains.annotations.NotNull;

public class MagentoGenerateAroundMethodAction extends CodeInsightAction {
    public static final String MAGENTO_PLUGIN_AROUND_METHOD_TEMPLATE_NAME = "Magento Plugin Around Method";

    private final MagentoGeneratePluginMethodHandlerBase myHandler = new MagentoGeneratePluginMethodHandlerBase(MagentoPluginMethodData.Type.AROUND) {
        protected MagentoPluginMethodData[] createPluginMethods(PhpClass currentClass, Method method, Key<Object> targetClassKey) {
            return (new MagentoPluginMethodsGenerator(currentClass, method, targetClassKey)
                    .createPluginMethods(MAGENTO_PLUGIN_AROUND_METHOD_TEMPLATE_NAME, MagentoPluginMethodData.Type.AROUND));
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
