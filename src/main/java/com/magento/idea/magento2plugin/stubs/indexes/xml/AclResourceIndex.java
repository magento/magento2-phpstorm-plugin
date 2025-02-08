/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.impl.DomApplicationComponent;
import com.magento.idea.magento2plugin.magento.files.ModuleAclXml;
import com.magento.idea.magento2plugin.project.Settings;
import gnu.trove.THashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class AclResourceIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.acl_resources");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, String> map = new THashMap<>();//NOPMD
            final PsiFile psiFile = inputData.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (psiFile instanceof XmlFile) {
                final XmlDocument xmlDocument = ((XmlFile) psiFile).getDocument();
                if (xmlDocument != null) {
                    final XmlTag xmlRootTag = xmlDocument.getRootTag();
                    if (xmlRootTag != null) { //NOPMD
                        parseRootTag(map, xmlRootTag);
                    }
                }
            }
            return map;
        };
    }

    protected void parseRootTag(final Map<String, String> map, final XmlTag xmlRootTag) {
        for (final XmlTag aclTag : xmlRootTag.findSubTags(ModuleAclXml.XML_TAG_ACL)) {
            for (final XmlTag resourcesTag : aclTag.findSubTags(ModuleAclXml.XML_TAG_RESOURCES)) {
                parseResourceTag(map, resourcesTag);
            }
        }
    }

    private void parseResourceTag(final Map<String, String> map, final XmlTag resourcesTag) {
        for (final XmlTag resourceTag : resourcesTag.findSubTags(ModuleAclXml.XML_TAG_RESOURCE)) {
            final String identifier = resourceTag.getAttributeValue(ModuleAclXml.XML_ATTR_ID);
            final String title = resourceTag.getAttributeValue(ModuleAclXml.XML_ATTR_TITLE);

            if (identifier != null && title != null && !identifier.isEmpty()
                    && !title.isEmpty()) {
                map.put(identifier, title);
            }

            parseResourceTag(map, resourceTag);
        }
    }

    @NotNull
    @Override
    public ID<String, String> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file ->
                file.getFileType() == XmlFileType.INSTANCE
                    && file.getName().equalsIgnoreCase(ModuleAclXml.FILE_NAME);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return DomApplicationComponent.getInstance().getCumulativeVersion(false);
    }
}
