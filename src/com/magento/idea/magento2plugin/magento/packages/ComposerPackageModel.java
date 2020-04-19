/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.packages;

import org.jetbrains.annotations.Nullable;
import java.util.Map;

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
}
