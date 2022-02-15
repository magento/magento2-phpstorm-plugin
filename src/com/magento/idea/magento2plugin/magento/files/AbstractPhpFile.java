/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPhpFile implements ModuleFileInterface {

    public static final String FILE_EXTENSION = "php";

    public static final String PUBLIC_ACCESS = "public";

    private final String moduleName;
    private final String className;
    private NamespaceBuilder namespaceBuilder;

    /**
     * Abstract php file constructor.
     *
     * @param moduleName String
     * @param className String
     */
    public AbstractPhpFile(
            final @NotNull String moduleName,
            final @NotNull String className
    ) {
        this.moduleName = moduleName;
        this.className = className;
    }

    /**
     * Get namespace.
     *
     * @return String
     */
    public @NotNull String getNamespace() {
        return getNamespaceBuilder().getNamespace();
    }

    /**
     * Get class FQN.
     *
     * @return String
     */
    public @NotNull String getClassFqn() {
        return getNamespaceBuilder().getClassFqn();
    }

    /**
     * Get namespace builder for file.
     *
     * @return String
     */
    public @NotNull NamespaceBuilder getNamespaceBuilder() {
        if (namespaceBuilder == null) {
            namespaceBuilder = new NamespaceBuilder(
                    moduleName,
                    className,
                    getDirectory()
            );
        }

        return namespaceBuilder;
    }

    /**
     * Get directory for file.
     *
     * @return String
     */
    public abstract String getDirectory();

    /**
     * Get file module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get class name.
     *
     * @return String
     */
    public String getClassName() {
        return className;
    }

    /**
     * Get readable name. Should be overridden to use.
     *
     * @return String
     */
    public abstract String getHumanReadableName();

    @Override
    public String getFileName() {
        return className.concat(".").concat(FILE_EXTENSION);
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
