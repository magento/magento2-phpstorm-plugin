/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.ui.data;

import com.intellij.util.io.DataExternalizer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UiComponentNavigationDataSetExternalizer
        implements DataExternalizer<Set<UiComponentNavigationData>> {
    public static final UiComponentNavigationDataSetExternalizer INSTANCE =
            new UiComponentNavigationDataSetExternalizer();

    @Override
    public void save(
            final @NotNull DataOutput out,
            final Set<UiComponentNavigationData> value
    ) throws IOException {
        out.writeInt(value.size());

        for (final UiComponentNavigationData data : value) {
            out.writeUTF(data.getFileUrl());
            out.writeUTF(data.getKind());
            out.writeUTF(data.getValue());
            writeNullableString(out, data.getComponentName());
            writeNullableString(out, data.getParentComponentName());
            writeNullableString(out, data.getComponentJsPath());
            writeNullableString(out, data.getParentComponentJsPath());
            writeNullableString(out, data.getTemplateKey());
            out.writeInt(data.getValueOffset());
        }
    }

    @Override
    public Set<UiComponentNavigationData> read(final @NotNull DataInput input) throws IOException {
        final int size = input.readInt();
        final Set<UiComponentNavigationData> result = new LinkedHashSet<>(size);

        for (int i = 0; i < size; i++) {
            result.add(new UiComponentNavigationData(
                    input.readUTF(),
                    input.readUTF(),
                    input.readUTF(),
                    readNullableString(input),
                    readNullableString(input),
                    readNullableString(input),
                    readNullableString(input),
                    readNullableString(input),
                    input.readInt()
            ));
        }

        return result;
    }

    private void writeNullableString(
            final @NotNull DataOutput out,
            final @Nullable String value
    ) throws IOException {
        out.writeBoolean(value != null);

        if (value != null) {
            out.writeUTF(value);
        }
    }

    private @Nullable String readNullableString(final @NotNull DataInput input) throws IOException {
        return input.readBoolean() ? input.readUTF() : null;
    }
}
