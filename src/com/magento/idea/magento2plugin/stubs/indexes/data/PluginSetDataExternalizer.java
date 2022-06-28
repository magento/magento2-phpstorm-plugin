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

    public PluginSetDataExternalizer() {
    }

    @Override
    public synchronized void save(@NotNull DataOutput out, Set<PluginData> value) throws IOException {
        out.writeInt(value.size());

        for (PluginData plugin : value) {
            out.writeUTF(plugin.getType());
            out.writeInt(plugin.getSortOrder());
        }
    }

    @Override
    public synchronized Set<PluginData> read(@NotNull DataInput in) throws IOException {
        int size = in.readInt();
        HashSet<PluginData> result = new HashSet<>(size);

        for (int r = size; r > 0; --r) {
            PluginData pluginData = new PluginData(in.readUTF(), in.readInt());
            result.add(pluginData);
        }

        return result;
    }
}
