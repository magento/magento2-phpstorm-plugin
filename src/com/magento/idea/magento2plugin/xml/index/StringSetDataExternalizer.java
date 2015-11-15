package com.magento.idea.magento2plugin.xml.index;

import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkvashnin on 11/10/15.
 */
public class StringSetDataExternalizer implements DataExternalizer<String[]> {

    private final EnumeratorStringDescriptor myStringEnumerator = new EnumeratorStringDescriptor();

    public synchronized void save(@NotNull DataOutput out, String[] values) throws IOException {

        out.writeInt(values.length);
        for(String value: values) {
            this.myStringEnumerator.save(out, value != null ? value : "");
        }

    }

    public synchronized String[] read(@NotNull DataInput in) throws IOException {
        List<String> list = new ArrayList<String>();
        int r = in.readInt();

        while (r > 0) {
            list.add(this.myStringEnumerator.read(in));
            r--;
        }

        return list.toArray(new String[list.size()]);
    }
}