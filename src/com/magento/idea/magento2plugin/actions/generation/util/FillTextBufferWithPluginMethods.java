/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.util;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.magento.idea.magento2plugin.actions.generation.data.code.PluginMethodData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.references.PhpClassReferenceResolver;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class FillTextBufferWithPluginMethods {

    /**
     * Fill text buffer with plugin methods.
     *
     * @param targetClassKey Key[Object]
     * @param insertedMethodsNames Set[CharSequence]
     * @param resolver PhpClassReferenceResolver
     * @param textBuf StringBuffer
     * @param pluginMethods PluginMethodData
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void execute(
            final @NotNull Key<Object> targetClassKey,
            final Set<CharSequence> insertedMethodsNames,
            final @NotNull PhpClassReferenceResolver resolver,
            final @NotNull StringBuffer textBuf,
            final @NotNull PluginMethodData... pluginMethods
    ) {
        for (final PluginMethodData pluginMethod : pluginMethods) {
            insertedMethodsNames.add(pluginMethod.getMethod().getName());
            final PhpDocComment comment = pluginMethod.getDocComment();

            if (comment != null) {
                textBuf.append(comment.getText());
            }
            final Method targetMethod = pluginMethod.getTargetMethod();
            final Parameter[] parameters = targetMethod.getParameters();
            final Collection<PsiElement> processElements =
                    new ArrayList<>(Arrays.asList(parameters));
            resolver.processElements(processElements);
            final PsiElement targetClass = (PsiElement) pluginMethod.getTargetMethod()
                    .getUserData(targetClassKey);
            resolver.processElement(targetClass);

            final String returnTypeCandidate = PhpTypeMetadataParserUtil.getMethodReturnType(
                    targetMethod
            );

            if (returnTypeCandidate != null
                    && PhpClassGeneratorUtil.isValidFqn(returnTypeCandidate)) {
                resolver.processElement(
                        PhpPsiElementFactory.createReturnType(
                                pluginMethod.getTargetMethod().getProject(),
                                returnTypeCandidate
                        )
                );
            }

            textBuf.append('\n');
            textBuf.append(pluginMethod.getMethod().getText());
        }
    }
}
