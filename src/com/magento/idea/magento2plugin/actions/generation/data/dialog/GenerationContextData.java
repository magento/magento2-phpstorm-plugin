/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.dialog;

import com.intellij.openapi.project.Project;

/**
 * This interface used to provide dialogues context DTOs to @GeneratorPoolHandler type.
 */
public interface GenerationContextData {

    /**
     * Get project instance.
     *
     * @return Project
     */
    Project getProject();

    /**
     * Get module name.
     *
     * @return String
     */
    String getModuleName();

    /**
     * Get current action name.
     *
     * @return String
     */
    String getActionName();

    /**
     * Check if files should be opened after generation.
     *
     * @return boolean
     */
    boolean checkIfHasOpenFileFlag();
}
