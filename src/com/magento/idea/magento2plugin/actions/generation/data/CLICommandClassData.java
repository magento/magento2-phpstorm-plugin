/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

public class CLICommandClassData {
    private final String className;
    private final String parentDirectory;
    private final String commandName;
    private final String commandDescription;
    private final String namespace;
    private final String moduleName;

    public CLICommandClassData(
        String cliClassName,
        String cliParentDirectory,
        String cliCommandName,
        String cliCommandDescription,
        String namespace,
        String moduleName
    ) {
        this.className = cliClassName;
        this.parentDirectory = cliParentDirectory;
        this.commandName = cliCommandName;
        this.commandDescription = cliCommandDescription;
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
        return commandDescription;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getModuleName() {
        return moduleName;
    }
}
