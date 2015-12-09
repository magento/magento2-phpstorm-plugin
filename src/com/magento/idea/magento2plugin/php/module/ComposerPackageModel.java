package com.magento.idea.magento2plugin.php.module;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by dkvashnin on 12/5/15.
 */
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
