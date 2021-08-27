/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning;

import java.util.LinkedList;
import java.util.List;

public enum SupportedVersion {

    V2_3_0("2.3.0"),
    V2_3_1("2.3.1"),
    V2_3_2("2.3.2"),
    V2_3_2_p2("2.3.2-p2"),
    V2_3_3("2.3.3"),
    V2_3_3_p1("2.3.3-p1"),
    V2_3_4("2.3.4"),
    V2_3_4_p1("2.3.4-p1"),
    V2_3_4_p2("2.3.4-p2"),
    V2_3_5("2.3.5"),
    V2_3_5_p1("2.3.5-p1"),
    V2_3_5_p2("2.3.5-p2"),
    V2_3_6("2.3.6"),
    V2_3_6_p1("2.3.6-p1"),
    V2_3_7("2.3.7"),
    V2_3_7_p1("2.3.7-p1"),
    V2_4_0("2.4.0"),
    V2_4_0_p1("2.4.0-p1"),
    V2_4_1("2.4.1"),
    V2_4_1_p1("2.4.1-p1"),
    V2_4_2("2.4.2"),
    V2_4_2_p1("2.4.2-p1"),
    V2_4_2_p2("2.4.2-p2"),
    V2_4_3("2.4.3");

    private final String version;

    SupportedVersion(final String version) {
        this.version = version;
    }

    /**
     * Get version.
     *
     * @return String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get supported versions.
     *
     * @return List[String]
     */
    public static List<String> getSupportedVersions() {
        final List<String> versions = new LinkedList<>();

        for (final SupportedVersion version : SupportedVersion.values()) {
            versions.add(version.getVersion());
        }

        return versions;
    }
}
