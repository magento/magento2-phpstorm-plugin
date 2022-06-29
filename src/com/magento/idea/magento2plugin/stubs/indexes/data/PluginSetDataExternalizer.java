/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.data;

import com.intellij.util.io.DataExternalizer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class PluginSetDataExternalizer  implements DataExternalizer<Set<PluginData>> {
    public static final PluginSetDataExternalizer INSTANCE = new PluginSetDataExternalizer();

    @SuppressWarnings("checkstyle:LineLength")
    @Override
    public void save(final @NotNull DataOutput out, final Set<PluginData> value) throws IOException {
        out.writeInt(value.size());

        for (final PluginData plugin : value) {
            out.writeUTF(plugin.getType());
            out.writeInt(plugin.getSortOrder());
        }
    }

    @Override
    public Set<PluginData> read(final @NotNull DataInput income) throws IOException {
        final int size = income.readInt();
        final HashSet<PluginData> result = new HashSet<>(size);

        for (int r = size; r > 0; --r) {
            result.add(getPluginDataObject(income.readUTF(), income.readInt()));
        }

        return result;
    }

    private PluginData getPluginDataObject(final String pluginType, final Integer sortOrder) {
        return new PluginData(pluginType,  sortOrder);
    }
}
