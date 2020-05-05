/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.CommentSize", "PMD.DataClass"})
public class CLICommandClassData {
    private final String className;
    private final String parentDirectory;
    private final String commandName;
    private final String description;
    private final String namespace;
    private final String moduleName;

    /**
     * CLI Command PHP class data class.
     *
     * @param cliClassName PHP class name
     * @param parentDirectory Prent directory of a PHP class
     * @param cliCommandName CLI Command name
     * @param description CLI Command description
     * @param namespace namespace of a PHP class
     * @param moduleName CLI Command module name
     */
    public CLICommandClassData(
            final String cliClassName,
            final String parentDirectory,
            final String cliCommandName,
            final String description,
            final String namespace,
            final String moduleName
    ) {
        this.className = cliClassName;
        this.parentDirectory = parentDirectory;
        this.commandName = cliCommandName;
        this.description = description;
        this.namespace = namespace;
        this.moduleName = moduleName;
    }

    public String getClassName() {
        return className;
    }

    public String getParentDirectory() {
        return parentDirectory;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getCommandDescription() {
        return description;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getModuleName() {
        return moduleName;
    }
}
