/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.Nullable;

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
    public static @Nullable String execute(final Project project) {
        return rootUrl(Settings.getMagentoPath(project), Package.packagesRoot);
    }

    /**
     * Constructs a file URL for the Magento design root, based on the project settings.
     *
     * @param project the project instance
     * @return the constructed file URL
     */
    public static @Nullable String getDesignPath(final Project project) {
        return rootUrl(Settings.getMagentoPath(project), Package.packagesDesignRoot);
    }

    /**
     * Constructs a file URL for the Magento lib/web root, based on the project settings.
     *
     * @param project the project instance
     * @return the constructed file URL
     */
    public static @Nullable String getLibWebPath(final Project project) {
        return rootUrl(Settings.getMagentoPath(project), Package.libWebRoot);
    }

    /**
     * Checks a URL against the roots that are editable without being marked.
     *
     * @param project the project instance
     * @param url the URL to check
     * @return true for the packages root and the design root
     */
    public static boolean isDefaultEditableRoot(final Project project, final String url) {
        return url.equals(execute(project)) || url.equals(getDesignPath(project));
    }

    /**
     * Constructs a file URL for a root under the given Magento path.
     *
     * @param magentoPath the Magento path, can be null when it is not configured
     * @param relativeRoot the root relative to the Magento path
     * @return the constructed file URL
     */
    public static @Nullable String rootUrl(
            final @Nullable String magentoPath,
            final String relativeRoot
    ) {
        if (magentoPath == null) {
            return null;
        }

        return VirtualFileManager.constructUrl(
                "file",
                FileUtil.toCanonicalPath(magentoPath + File.separator + relativeRoot)
        );
    }
}
