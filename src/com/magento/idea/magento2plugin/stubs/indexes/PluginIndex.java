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
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.data.PluginData;
import com.magento.idea.magento2plugin.stubs.indexes.data.PluginSetDataExternalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class PluginIndex extends FileBasedIndexExtension<String, Set<PluginData>> {
    public static final ID<String, Set<PluginData>> KEY
            = ID.create("com.magento.idea.magento2plugin.stubs.indexes.plugin_to_type");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Set<PluginData>> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Set<PluginData>, FileContent> getIndexer() {
        return new DataIndexer<>() {
            @NotNull
            @Override
            public Map<String, Set<PluginData>> map(@NotNull FileContent fileContent) {
                Map<String, Set<PluginData>> map = new HashMap<>();

                PsiFile psiFile = fileContent.getPsiFile();
                if (!Settings.isEnabled(psiFile.getProject())) {
                    return map;
                }

                if (!(psiFile instanceof XmlFile)) {
                    return map;
                }
                XmlDocument document = ((XmlFile) psiFile).getDocument();
                if(document == null) {
                    return map;
                }

                XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
                if (xmlTags == null) {
                    return map;
                }

                for (XmlTag xmlTag: xmlTags) {
                    if (xmlTag.getName().equals("config")) {
                        for (XmlTag typeNode: xmlTag.findSubTags("type")) {
                            String typeName = typeNode.getAttributeValue("name");
                            if (typeName != null) {
                                Set<PluginData> plugins = getPluginsForType(typeNode);
                                if (plugins.size() > 0) {
                                    map.put(PhpLangUtil.toPresentableFQN(typeName), plugins);
                                }
                            }
                        }
                    }
                }

                return map;
            }

            private Set<PluginData> getPluginsForType(XmlTag typeNode) {
                Set<PluginData> results = new HashSet<>();

                for (XmlTag pluginTag: typeNode.findSubTags(ModuleDiXml.PLUGIN_TAG_NAME)) {
                    String pluginType = pluginTag.getAttributeValue(ModuleDiXml.TYPE_ATTR);
                    String pluginSortOrder = pluginTag.getAttributeValue(ModuleDiXml.SORT_ORDER_ATTR);

                    if (pluginType != null) {
                        pluginSortOrder = pluginSortOrder == null ? "0" : pluginSortOrder;
                        PluginData pluginData = new PluginData(pluginType,  Integer.parseInt(pluginSortOrder));
                        results.add(pluginData);
                    }
                }

                return results;
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
    public DataExternalizer<Set<PluginData>> getValueExternalizer() {
        return PluginSetDataExternalizer.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> (virtualFile.getFileType() == XmlFileType.INSTANCE
                && virtualFile.getNameWithoutExtension().equals("di"));
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
