/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.jar.Manifest;
import org.jetbrains.annotations.Nullable;

public final class PluginVersionUtil {

    private PluginVersionUtil() {}

    /**
     * Reads the version written by Gradle to the plugin jar manifest.
     *
     * @return the plugin version, or null when no manifest version is available
     */
    public static @Nullable String getVersion() {
        final URL classUrl = PluginVersionUtil.class.getResource("PluginVersionUtil.class");

        if (classUrl == null || !"jar".equals(classUrl.getProtocol())) {
            return null;
        }

        final String classLocation = classUrl.toExternalForm();
        final URI manifestLocation = URI.create(
                classLocation.substring(0, classLocation.lastIndexOf("!/") + 2)
                        + "META-INF/MANIFEST.MF"
        );

        try (InputStream stream = manifestLocation.toURL().openStream()) {
            return new Manifest(stream).getMainAttributes().getValue("Version");
        } catch (IOException exception) {
            return null;
        }
    }
}
