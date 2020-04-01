/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.stubs.indexes.StringSetDataExternalizer;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by dkvashnin on 11/2/15.
 */
public class EventObserverIndex extends FileBasedIndexExtension<String,Set<String>> {
    public static final ID<String, Set<String>> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.event_observer");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Set<String>> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return new DataIndexer<String, Set<String>, FileContent>() {
            @NotNull
            @Override
            public Map<String, Set<String>> map(@NotNull FileContent fileContent) {
                Map<String, Set<String>> map = new HashMap<>();

                PsiFile psiFile = fileContent.getPsiFile();
                if (!Settings.isEnabled(psiFile.getProject())) {
                    return map;
                }

                if (!(psiFile instanceof XmlFile)) {
                    return map;
                }

                XmlDocument document = ((XmlFile) psiFile).getDocument();
                if (document == null) {
                    return map;
                }

                XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
                if (xmlTags == null) {
                    return map;
                }

                for (XmlTag xmlTag: xmlTags) {
                    if (xmlTag.getName().equals("config")) {
                        for (XmlTag eventNode: xmlTag.findSubTags("event")) {
                            if (eventNode.getAttributeValue("name") != null) {
                                map.put(eventNode.getAttributeValue("name"), getObserversForEvent(eventNode));
                            }
                        }
                    }
                }

                return map;
            }

            private Set<String> getObserversForEvent(XmlTag eventNode) {
                Set<String> observerNames = new HashSet<String>();

                for (XmlTag observerTag: eventNode.findSubTags("observer")) {
                    String name = observerTag.getAttributeValue("instance");
                    if (name != null) {
                        observerNames.add(PhpLangUtil.toPresentableFQN(name));
                    }
                }

                return observerNames;
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
    public DataExternalizer<Set<String>> getValueExternalizer() {
        return new StringSetDataExternalizer();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> (file.getFileType() == XmlFileType.INSTANCE && file.getName().equals("events.xml"));
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
