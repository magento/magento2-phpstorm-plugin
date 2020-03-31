/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.packages;

import java.util.ArrayList;

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

    public static enum License {
        OSL("Open Software License (OSL)"),
        MPL("Mozilla Public License (MPL)"),
        MITL("Massachusetts Institute of Technology License (MITL)"),
        LGPL("GNU Lesser General Public License (LGPL)"),
        GPL("GNU General Public License (GPL)"),
        BSDL("Berkeley Software Distribution License (BSDL)"),
        ASL("Apache Software License (ASL)"),
        AFL("Academic Free License (AFL)");

        private String licenseName;

        License(String name) {
            this.licenseName = name;
        }

        public String getLicenseName() {
            return licenseName;
        }
    }

    public static Package.Areas getAreaByString(String string)
    {
        for (Package.Areas areas: Package.Areas.values()) {
            if (!areas.toString().equals(string))
            {
                continue;
            }
            return areas;
        }
        return null;
    }
}
