/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;

public final class MagentoPathUrlUtil {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private MagentoPathUrlUtil() {
    }

    /**
     * Constructs a file URL for the Magento packages root, based on the project settings.
     *
     * @param project the project instance
     * @return the constructed file URL
     */
    public static String execute(final Project project) {
        final String magentoPath = Settings.getMagentoPath(project);
        if (magentoPath != null) {
            return  VirtualFileManager.constructUrl(
                    "file",
                    magentoPath
                            + File.separator
                            + Package.packagesRoot
            );
        }

        return null;
    }

    /**
     * Constructs a file URL for the Magento packages root, based on the project settings.
     *
     * @param project the project instance
     * @return the constructed file URL
     */
    public static String getDesignPath(final Project project) {
        final String magentoPath = Settings.getMagentoPath(project);
        if (magentoPath != null) {
            return  VirtualFileManager.constructUrl(
                    "file",
                    magentoPath
                            + File.separator
                            + Package.packagesDesignRoot
            );
        }

        return null;
    }
}
