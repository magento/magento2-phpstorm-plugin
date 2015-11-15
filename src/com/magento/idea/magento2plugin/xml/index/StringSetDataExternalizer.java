package com.magento.idea.magento2plugin.xml.index;

import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dkvashnin on 11/10/15.
 */
public class StringSetDataExternalizer implements DataExternalizer<Set<String>> {

    private final EnumeratorStringDescriptor myStringEnumerator = new EnumeratorStringDescriptor();

    public synchronized void save(@NotNull DataOutput out, Set<String> values) throws IOException {

        out.writeInt(values.size());
        for(String value: values) {
            this.myStringEnumerator.save(out, value != null ? value : "");
        }

    }

    public synchronized Set<String> read(@NotNull DataInput in) throws IOException {
        Set<String> set = new HashSet<String>();
        int r = in.readInt();

        while (r > 0) {
            set.add(this.myStringEnumerator.read(in));
            r--;
        }

        return set;
    }
}