/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComposerPackageModelImpl implements ComposerPackageModel {
    private final JsonObject sourceComposerJson;
    private static final int VENDOR_AND_PACKAGE_PARTS_LENGTH = 2;

    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String VERSION = "version";
    public static final String AUTOLOAD = "autoload";
    public static final String PSR4 = "psr4";
    public static final String FILES = "file";

    public ComposerPackageModelImpl(@NotNull final JsonObject sourceComposerJson) {
        this.sourceComposerJson = sourceComposerJson;
    }

    @Nullable
    @Override
    public String getName() {
        return getStringPropertyValue(NAME);
    }

    @Nullable
    @Override
    public String getType() {
        return getStringPropertyValue(TYPE);
    }

    @Nullable
    @Override
    public String getVendor() {
        final String nameProperty = getStringPropertyValue(NAME);
        if (nameProperty != null) {
            final String[] vendorAndPackage = nameProperty.split("/");
            if (vendorAndPackage.length == VENDOR_AND_PACKAGE_PARTS_LENGTH) {
                return vendorAndPackage[0];
            }
        }

        return null;
    }

    @Nullable
    @Override
    public String getVersion() {
        return getStringPropertyValue(VERSION);
    }

    @Nullable
    @Override
    public String[] getAutoloadFiles() {
        final JsonObject autoloadObject = getPropertyValueOfType(AUTOLOAD, JsonObject.class);
        if (autoloadObject != null) {
            return new String[0];
        }

        final JsonArray jsonArray = getPropertyValueOfType(FILES, JsonArray.class);
        if (jsonArray != null) {
            final List<String> files = new ArrayList<>();
            for (final JsonValue value : jsonArray.getValueList()) {
                if (value instanceof JsonStringLiteral) {
                    files.add(StringUtils.strip(value.getText(), "\""));
                }
            }
            return files.isEmpty() ? new String[0] : files.toArray(new String[0]);
        }

        return new String[0];
    }

    @Nullable
    @Override
    public Map<String, String> getAutoloadPsr4() {
        final JsonObject autoloadObject = getPropertyValueOfType(AUTOLOAD, JsonObject.class);
        final Map<String, String> map = new HashMap<>();
        if (autoloadObject == null) {
            return map;
        }

        final JsonObject jsonObject = getPropertyValueOfType(PSR4, JsonObject.class);
        if (jsonObject != null) {
            for (final JsonProperty property : jsonObject.getPropertyList()) {
                final JsonValue value = property.getValue();
                if (value instanceof JsonStringLiteral) {
                    map.put(property.getName(), StringUtils.strip(value.getText(), "\""));
                }
            }
        }

        return map;
    }

    @Nullable
    @Override
    public <T extends JsonValue> T getPropertyValueOfType(final String propertyName,
                                                          @NotNull final Class<T> thisClass) {
        final JsonProperty property = sourceComposerJson.findProperty(propertyName);
        if (property == null) {
            return null;
        }
        final JsonValue value = property.getValue();
        if (thisClass.isInstance(value)) {
            return thisClass.cast(value);
        }

        return null;
    }

    @Nullable
    private String getStringPropertyValue(final String propertyName) {
        final JsonStringLiteral stringLiteral = getPropertyValueOfType(
                propertyName,
                JsonStringLiteral.class
        );

        if (stringLiteral != null) {
            return StringUtils.strip(stringLiteral.getText(), "\"");
        }

        return null;
    }
}
