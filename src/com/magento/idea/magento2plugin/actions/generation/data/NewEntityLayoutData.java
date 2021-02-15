/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class NewEntityLayoutData {

    private final String moduleName;
    private final String newActionPath;
    private final String editActionPath;

    /**
     * New entity layout data.
     *
     * @param moduleName String
     * @param newActionPath String
     * @param editActionPath String
     */
    public NewEntityLayoutData(
            final @NotNull String moduleName,
            final @NotNull String newActionPath,
            final @NotNull String editActionPath
    ) {
        this.moduleName = moduleName;
        this.newActionPath = newActionPath;
        this.editActionPath = editActionPath;
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
     * Get new action path.
     *
     * @return String
     */
    public String getNewActionPath() {
        return newActionPath;
    }

    /**
     * Get edit action path.
     *
     * @return String
     */
    public String getEditActionPath() {
        return editActionPath;
    }
}
