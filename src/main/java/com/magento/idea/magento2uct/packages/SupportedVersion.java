/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.packages;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public enum SupportedVersion {
    ;
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
     * Get version ENUM by version code.
     *
     * @param versionCandidate String
     *
     * @return SupportedVersion
     */
    public @Nullable static SupportedVersion getVersion(final @NotNull String versionCandidate) {
        for (final SupportedVersion version : SupportedVersion.values()) {
            if (version.getVersion().equals(versionCandidate)) {
                return version;
            }
        }
        return null;
    }

    /**
     * Get supported versions.
     *
     * @return List[String]
     */
    public static List<String> getSupportedVersions() {
        try {
            return fetchSupportedVersions();
        } catch (Exception e) {
            // Return an empty list or log the exception
            return List.of();
        }
    }

    /**
     * Fetch supported versions dynamically from Packagist
     * This method performs an HTTP GET request to fetch version data in JSON format
     * from a predefined URL and parses it into a list of version strings.
     *
     * @return List[String] containing supported version strings
     * @throws Exception if an error occurs during HTTP connection or JSON parsing
     */
    public static List<String> fetchSupportedVersions() throws Exception {
        String url = "https://repo.packagist.org/p2/magento/community-edition.json";
        List<String> versions = new ArrayList<>();

        HttpURLConnection connection = null;
        try {
            // Establish HTTP connection
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != 200) {
                throw new Exception(
                    "Failed to fetch data, HTTP response code: " + connection.getResponseCode()
                );
            }

            // Read JSON response
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))
            ) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse JSON for version data
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray packageObject = jsonResponse
                        .getJSONObject("packages")
                        .getJSONArray("magento/community-edition");

                for (Object o : packageObject) {
                    JSONObject version = (JSONObject) o;
                    if (version == null) {
                        continue;
                    }
                    String versionstring = version.getString("version");
                    if (versionstring == null) {
                        continue;
                    }
                    versions.add(versionstring);
                }
            }
        } catch (Exception e) {
            throw new Exception(
                "Error fetching or parsing supported versions: " + e.getMessage(),
                e
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return versions;
    }

    /**
     * Get previous versions.
     *
     * @param version SupportedVersion
     *
     * @return List[SupportedVersion]
     */
    public static List<SupportedVersion> getPriorVersions(final SupportedVersion version) {
        final List<SupportedVersion> previousVersions = new ArrayList<>();

        for (final SupportedVersion supportedVersion : SupportedVersion.values()) {
            if (supportedVersion.compareTo(version) < 0) {
                previousVersions.add(supportedVersion);
            }
        }

        return previousVersions;
    }
}
