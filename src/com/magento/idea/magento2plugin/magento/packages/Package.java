/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

public class Package {
    public static String packagesRoot = "app/code";
    public static String libWebRoot = "lib/web";
    public static String frameworkRootComposer = "vendor/magento/framework";
    public static String frameworkRootGit = "lib/internal/Magento/Framework";
    public static String vendor = "vendor";
    public static String moduleBaseAreaDir = "etc";
    public static String vendorModuleNameSeparator = "_";
    public static String fqnSeparator = "\\";

    /**
     * Casts string to the certain area.
     *
     * @param string String
     * @return Areas
     */
    public static Areas getAreaByString(String string) {
        for (Areas area: Areas.values()) {
            if (!area.toString().equals(string)) {
                continue;
            }

            return area;
        }

        return null;
    }
}
