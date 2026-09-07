/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.data;

import com.magento.idea.magento2uct.packages.SupportedVersion;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface VersionStateIndex {

    String FILE_NAME_PATTERN = "indexes.v%version.%key.idc";

    /**
     * Used to store indexes for all versions in the single file.
     */
    String SINGLE_FILE_NAME_PATTERN = "indexes.%key.idc";

    /**
     * Load index data for specified versions from the resource.
     *
     * @param versions List[SupportedVersion]
     */
    void load(final @NotNull List<SupportedVersion> versions);

    /**
     * Load index data for specified versions from the filesystem.
     *
     * @param versions List[SupportedVersion]
     */
    void loadFromFile(final @NotNull List<SupportedVersion> versions);
}
