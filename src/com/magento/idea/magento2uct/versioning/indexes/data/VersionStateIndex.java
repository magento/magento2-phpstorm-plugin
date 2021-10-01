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
     * Load index data for specified versions.
     *
     * @param versions List[SupportedVersion]
     */
    void load(final @NotNull List<SupportedVersion> versions);
}
