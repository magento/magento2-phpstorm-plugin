package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

public class MagentoPluginMethodData {
    private final PhpClass pluginClass;
    private final PhpDocComment docComment;
    private final Method method;
    private final Method targetMethod;
    private final MagentoPluginMethodData.Type pluginType;

    public MagentoPluginMethodData(Method targetMethod, PhpClass pluginClass, PhpDocComment docComment, Method method, @NotNull MagentoPluginMethodData.Type pluginType) {
        super();
        this.pluginClass = pluginClass;
        this.docComment = docComment;
        this.method = method;
        this.targetMethod = targetMethod;
        this.pluginType = pluginType;
    }

    public PhpDocComment getDocComment() {
        return this.docComment;
    }

    public Method getMethod() {
        return this.method;
    }

    public PhpClass getPluginClass() {
        return this.pluginClass;
    }

    public Method getTargetMethod() {
        return this.targetMethod;
    }

    public Type getPluginType() {
        return pluginType;
    }

    public static enum Type {
        AROUND,
        AFTER,
        BEFORE;

        private Type() {
        }
    }
}
