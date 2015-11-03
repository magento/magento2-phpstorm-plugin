package com.magento.idea.magento2plugin.xml.observer.index;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dkvashnin on 11/2/15.
 */
public class EventObserverFileBasedIndex extends FileBasedIndexExtension<String,String[]> {
    public static final ID<String, String[]> NAME = ID.create("com.magento.idea.magento2plugin.xml.observer.index.event_observer");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, String[]> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, String[], FileContent> getIndexer() {
        return new DataIndexer<String, String[], FileContent>() {
            @NotNull
            @Override
            public Map<String, String[]> map(@NotNull FileContent fileContent) {
                Map<String, String[]> map = new HashMap<>();

                PsiFile psiFile = fileContent.getPsiFile();
                XmlDocumentImpl document = PsiTreeUtil.getChildOfType(psiFile, XmlDocumentImpl.class);
                if(document == null) {
                    return map;
                }

                XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
                if(xmlTags == null) {
                    return map;
                }

                for(XmlTag xmlTag: xmlTags) {
                    if(xmlTag.getName().equals("config")) {
                        for(XmlTag eventNode: xmlTag.findSubTags("event")) {
                            if (eventNode.getAttributeValue("name") != null) {
                                map.put(
                                    eventNode.getAttributeValue("name"),
                                    getObserversForEvent(eventNode)
                                );
                            }
                        }
                    }
                }

                return map;
            }

            private String[] getObserversForEvent(XmlTag eventNode) {
                List<String> observerNames = new ArrayList<String>();

                for (XmlTag observerTag: eventNode.findSubTags("observer")) {
                    String name = observerTag.getAttributeValue("instance");
                    if (name != null) {
                        observerNames.add(name);
                    }
                }

                return observerNames.toArray(new String[observerNames.size()]);
            }
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<String[]> getValueExternalizer() {
        return new MySetDataExternalizer();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return file.getFileType() == XmlFileType.INSTANCE && file.getNameWithoutExtension().equals("events");
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    public static class MySetDataExternalizer implements DataExternalizer<String[]> {

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
}
