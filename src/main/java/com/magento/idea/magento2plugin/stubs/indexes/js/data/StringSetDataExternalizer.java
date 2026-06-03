/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.js.data;

import com.intellij.util.io.DataExternalizer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class StringSetDataExternalizer implements DataExternalizer<Set<String>> {
    public static final StringSetDataExternalizer INSTANCE = new StringSetDataExternalizer();

    @Override
    public void save(final @NotNull DataOutput out, final Set<String> value) throws IOException {
        out.writeInt(value.size());

        for (final String item : value) {
            out.writeUTF(item);
        }
    }

    @Override
    public Set<String> read(final @NotNull DataInput input) throws IOException {
        final int size = input.readInt();
        final Set<String> result = new LinkedHashSet<>(size);

        for (int i = 0; i < size; i++) {
            result.add(input.readUTF());
        }

        return result;
    }
}
