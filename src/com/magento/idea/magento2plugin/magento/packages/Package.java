/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.packages;

public class Package {
    public static String PACKAGES_ROOT = "app/code";
    public static String VENDOR = "vendor";
    public static String MODULE_BASE_AREA_DIR = "etc";
    public static String VENDOR_MODULE_NAME_SEPARATOR = "_";
    public static String FQN_SEPARATOR = "\\";

    public static enum Areas {
        base,
        adminhtml,
        frontend,
        crontab,
        webapi_rest,
        webapi_soap,
        graphql
    }
}
