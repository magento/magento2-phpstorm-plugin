/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.util;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpReturnType;
import com.magento.idea.magento2plugin.actions.generation.ImportReferences.PhpClassReferenceResolver;
import com.magento.idea.magento2plugin.actions.generation.data.MagentoPluginMethodData;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class FillTextBufferWithPluginMethods {
    private static FillTextBufferWithPluginMethods INSTANCE = null;

    public static FillTextBufferWithPluginMethods getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new FillTextBufferWithPluginMethods();
        }
        return INSTANCE;
    }

    public void execute(@NotNull Key<Object> targetClassKey, Set<CharSequence> insertedMethodsNames, @NotNull PhpClassReferenceResolver resolver, @NotNull StringBuffer textBuf, @NotNull MagentoPluginMethodData[] pluginMethods) {
        for (MagentoPluginMethodData pluginMethod : pluginMethods) {
            insertedMethodsNames.add(pluginMethod.getMethod().getName());
            PhpDocComment comment = pluginMethod.getDocComment();
            if (comment != null) {
                textBuf.append(comment.getText());
            }
            Method targetMethod = pluginMethod.getTargetMethod();
            Parameter[] parameters = targetMethod.getParameters();
            Collection<PsiElement> processElements = new ArrayList<>(Arrays.asList(parameters));
            resolver.processElements(processElements);
            PsiElement targetClass = (PsiElement) pluginMethod.getTargetMethod().getUserData(targetClassKey);
            resolver.processElement(targetClass);
            PhpReturnType returnType = targetMethod.getReturnType();
            if (returnType != null) {
                resolver.processElement(returnType);
            }

            textBuf.append('\n');
            textBuf.append(pluginMethod.getMethod().getText());
        }
    }
}
