/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import com.intellij.json.psi.JsonValue;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ComposerPackageModel {
    @Nullable
    String getName();

    @Nullable
    String getType();

    @Nullable
    String getVendor();

    @Nullable
    String getVersion();

    @Nullable
    String[] getAutoloadFiles();

    @Nullable
    Map<String, String> getAutoloadPsr4();

    <T extends JsonValue> T getPropertyValueOfType(String propertyName,
                                                   @NotNull Class<T> thisClass);
}
