/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkvashnin on 11/5/15.
 */
public class EventNameIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY = ID.create("com.magento.idea.magento2plugin.stubs.indexes.event_name");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> map = new HashMap<>();
            PsiFile psiFile = inputData.getPsiFile();

            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (psiFile instanceof XmlFile) {
                grabEventNamesFromXmlFile((XmlFile) psiFile, map);
            } else {
                for (final EventNameIndexProvider provider : EventNameIndexProvider.EP_NAME.getExtensionList()) {
                    provider.collectEventNames(psiFile, map);
                }
            }

            return map;
        };
    }

    private void grabEventNamesFromXmlFile(XmlFile file, Map<String, Void> map) {
        XmlDocument xmlDocument = file.getDocument();
        if (xmlDocument != null) {
            XmlTag xmlRootTag = xmlDocument.getRootTag();
            if (xmlRootTag != null) {
                for (XmlTag eventTag : xmlRootTag.findSubTags("event")) {
                    String name = eventTag.getAttributeValue("name");
                    if (name != null && !name.isEmpty()) {
                        map.put(name, null);
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            if (file.getFileType() == XmlFileType.INSTANCE) {
                return "events.xml".equals(file.getName());
            }
            for (final EventNameIndexProvider provider : EventNameIndexProvider.EP_NAME.getExtensionList()) {
                if (provider.accepts(file)) {
                    return true;
                }
            }
            return false;
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        // Changing optional language support must rebuild the index across IDE restarts.
        return 3 + EventNameIndexProvider.EP_NAME.getExtensionList().size();
    }
}
