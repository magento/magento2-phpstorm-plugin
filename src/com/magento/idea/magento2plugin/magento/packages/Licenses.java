/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

public enum Licenses {
    CUSTOM("Custom License"),
    OSL("Open Software License (OSL)"),
    MPL("Mozilla Public License (MPL)"),
    MITL("Massachusetts Institute of Technology License (MITL)"),
    LGPL("GNU Lesser General Public License (LGPL)"),
    GPL("GNU General Public License (GPL)"),
    BSDL("Berkeley Software Distribution License (BSDL)"),
    ASL("Apache Software License (ASL)"),
    AFL("Academic Free License (AFL)");

    private String licenseName;

    Licenses(final String name) {
        this.licenseName = name;
    }

    public String getLicenseName() {
        return licenseName;
    }
}
