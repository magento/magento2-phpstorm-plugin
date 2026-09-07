/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.packages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class SupportedVersion {

    private final String version;

    private SupportedVersion(final String version) {
        this.version = version;
    }

    public @NotNull String getVersion() {
        return version;
    }

    private static final AtomicReference<List<SupportedVersion>> cachedVersions = new AtomicReference<>();

    public static @NotNull List<SupportedVersion> getSupportedVersions() {
        List<SupportedVersion> versions = cachedVersions.get();

        if (versions == null) {
            versions = new ArrayList<>();

            try {
                for (final String versionStr : fetchRemoteSupportedVersions()) {
                    versions.add(new SupportedVersion(versionStr));
                }
            } catch (Exception ignored) {
            }

            cachedVersions.set(versions);
        }

        return versions;
    }

    public static List<String> getSupportedVersionStrings() {
        final List<String> versions = new ArrayList<>();

        for (final SupportedVersion version : getSupportedVersions()) {
            versions.add(version.getVersion());
        }

        return versions;
    }

    public static @Nullable SupportedVersion getVersion(final @NotNull String versionCandidate) {
        for (final SupportedVersion version : getSupportedVersions()) {
            if (version.getVersion().equals(versionCandidate)) {
                return version;
            }
        }

        return null;
    }

    public static List<SupportedVersion> getPriorVersions(final SupportedVersion version) {
        final List<SupportedVersion> previousVersions = new ArrayList<>();

        for (final SupportedVersion supportedVersion : getSupportedVersions()) {
            if (supportedVersion.getVersion().compareTo(version.toString()) < 0) {
                previousVersions.add(supportedVersion);
            }
        }

        return previousVersions;
    }

    /**
     * Fetch supported versions dynamically from Packagist
     * This method performs an HTTP GET request to fetch version data in JSON format
     * from a predefined URL and parses it into a list of version strings.
     *
     * @return List[String] containing supported version strings
     */
    private static List<String> fetchRemoteSupportedVersions() {
        final String url = "https://repo.packagist.org/p2/magento/community-edition.json";
        final List<String> versions = new ArrayList<>();

        HttpURLConnection connection = null;

        try {
            // Establish HTTP connection
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != 200) {
                throw new IOException(//NOPMD - suppressed AvoidThrowingRawExceptionTypes
                        "Failed to fetch data, HTTP response code: " + connection.getResponseCode()
                );
            }

            // Read JSON response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))
            ) {
                final StringBuilder response = new StringBuilder();
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    response.append(line);
                }

                // Parse JSON for version data
                final JSONObject jsonResponse = new JSONObject(response.toString());
                final JSONArray packageObject = jsonResponse
                        .getJSONObject("packages")
                        .getJSONArray("magento/community-edition");

                for (final Object o : packageObject) {
                    final JSONObject version = (JSONObject) o;
                    if (version == null) {
                        continue;
                    }
                    final String versionstring = version.getString("version");
                    if (versionstring == null) {
                        continue;
                    }
                    versions.add(versionstring);
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return versions;
    }

}
