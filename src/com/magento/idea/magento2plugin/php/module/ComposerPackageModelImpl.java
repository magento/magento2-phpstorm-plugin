package com.magento.idea.magento2plugin.php.module;

import com.intellij.json.psi.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dkvashnin on 12/5/15.
 */
public class ComposerPackageModelImpl implements ComposerPackageModel {
    private JsonObject sourceComposerJson;

    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String VERSION = "version";
    public static final String AUTOLOAD = "autoload";
    public static final String PSR4 = "psr4";
    public static final String FILES = "file";

    public ComposerPackageModelImpl(@NotNull JsonObject sourceComposerJson) {
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
        String nameProperty = getStringPropertyValue(NAME);
        if (nameProperty != null) {
            String[] vendorAndPackage = nameProperty.split("/");
            if (vendorAndPackage.length == 2) {
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
        JsonObject autoloadObject = getPropertyValueOfType(AUTOLOAD, JsonObject.class);
        if (autoloadObject != null) {
            JsonArray jsonArray = getPropertyValueOfType(FILES, JsonArray.class);
            if (jsonArray != null) {
                List<String> files = new ArrayList<>();
                for(JsonValue value: jsonArray.getValueList()) {
                    if (value instanceof JsonStringLiteral) {
                        files.add(StringUtils.strip(value.getText(), "\""));
                    }
                }
                return files.size() > 0 ? files.toArray(new String[files.size()]) : null;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Map<String, String> getAutoloadPsr4() {
        JsonObject autoloadObject = getPropertyValueOfType(AUTOLOAD, JsonObject.class);
        if (autoloadObject != null) {
            JsonObject jsonObject = getPropertyValueOfType(PSR4, JsonObject.class);
            if (jsonObject != null) {
                Map <String, String> map = new HashMap<String, String>();
                for (JsonProperty property: jsonObject.getPropertyList()) {
                    JsonValue value = property.getValue();

                    if (value != null && value instanceof JsonStringLiteral) {
                        map.put(property.getName(), StringUtils.strip(value.getText(), "\""));
                    }
                }

                return map.size() > 0 ? map : null;
            }
        }

        return null;
    }

    @Nullable
    public <T extends JsonValue> T getPropertyValueOfType(String propertyName, @NotNull Class<T> aClass) {
        JsonProperty property = sourceComposerJson.findProperty(propertyName);
        if (property == null) {
            return null;
        }
        JsonValue value = property.getValue();
        if (value != null && aClass.isInstance(value)) {
            return aClass.cast(value);
        }

        return null;
    }

    @Nullable
    private String getStringPropertyValue(String propertyName) {
        JsonStringLiteral stringLiteral = getPropertyValueOfType(propertyName, JsonStringLiteral.class);

        if (stringLiteral != null) {
            return StringUtils.strip(stringLiteral.getText(), "\"");
        }

        return null;
    }
}
