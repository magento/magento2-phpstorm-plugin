/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data.code;

import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Method;

public class PluginMethodData {
    private final PhpDocComment docComment;
    private final Method method;
    private final Method targetMethod;

    public PluginMethodData(Method targetMethod, PhpDocComment docComment, Method method) {
        super();
        this.docComment = docComment;
        this.method = method;
        this.targetMethod = targetMethod;
    }

    public PhpDocComment getDocComment() {
        return this.docComment;
    }

    public Method getMethod() {
        return this.method;
    }

    public Method getTargetMethod() {
        return this.targetMethod;
    }
}
