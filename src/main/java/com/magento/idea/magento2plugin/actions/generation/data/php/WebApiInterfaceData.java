/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.php;

import com.jetbrains.php.lang.psi.elements.Method;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class WebApiInterfaceData {

    private final String moduleName;
    private final String classFqn;
    private final String name;
    private final String description;
    private final List<Method> methods;

    /**
     * Web API interface DTO constructor.
     *
     * @param moduleName String
     * @param classFqn String
     * @param name String
     * @param description String
     * @param methods List[Method]
     */
    public WebApiInterfaceData(
            final @NotNull String moduleName,
            final @NotNull String classFqn,
            final @NotNull String name,
            final @NotNull String description,
            final @NotNull List<Method> methods
    ) {
        this.moduleName = moduleName;
        this.classFqn = classFqn;
        this.name = name;
        this.description = description;
        this.methods = methods;
    }

    /**
     * Get module name.
     *
     * @return String
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Get class FQN for which api interface to be generated.
     *
     * @return String
     */
    public String getClassFqn() {
        return classFqn;
    }

    /**
     * Get interface name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get description for the api interface class.
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get list of methods for the api interface.
     *
     * @return List[Method]
     */
    public List<Method> getMethods() {
        return methods;
    }
}
