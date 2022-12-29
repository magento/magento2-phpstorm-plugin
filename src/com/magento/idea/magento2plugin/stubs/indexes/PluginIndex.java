/*
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
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
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
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public DataIndexer<String, Set<PluginData>, FileContent> getIndexer() {
        return new DataIndexer<>() {
            @SuppressWarnings("checkstyle:LineLength")
            @NotNull
            @Override
            public Map<String, Set<PluginData>> map(final @NotNull FileContent fileContent) {
                final Map<String, Set<PluginData>> map = new HashMap<>();

                final PsiFile psiFile = fileContent.getPsiFile();
                if (!Settings.isEnabled(psiFile.getProject())) {
                    return map;
                }

                if (!(psiFile instanceof XmlFile)) {
                    return map;
                }
                final XmlDocument document = ((XmlFile) psiFile).getDocument();
                if (document == null) {
                    return map;
                }

                final XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
                if (xmlTags == null) {
                    return map;
                }

                for (final XmlTag xmlTag: xmlTags) {
                    if (xmlTag.getName().equals("config")) {
                        for (final XmlTag typeNode: xmlTag.findSubTags("type")) {
                            final String typeName = typeNode.getAttributeValue("name");
                            final Set<PluginData> plugins = getPluginsForType(typeNode);

                            if (typeName == null || plugins.isEmpty()) {
                                continue;
                            }

                            map.put(PhpLangUtil.toPresentableFQN(typeName), plugins);
                        }
                    }
                }

                return map;
            }

            @SuppressWarnings("checkstyle:LineLength")
            private Set<PluginData> getPluginsForType(final XmlTag typeNode) {
                final Set<PluginData> results = new HashSet<>();

                for (final XmlTag pluginTag: typeNode.findSubTags(ModuleDiXml.PLUGIN_TAG_NAME)) {
                    final String pluginType = pluginTag.getAttributeValue(ModuleDiXml.TYPE_ATTR);
                    final String pluginSortOrder = pluginTag.getAttributeValue(ModuleDiXml.SORT_ORDER_ATTR);

                    if (pluginType != null && !pluginType.isEmpty()) {
                        final PluginData pluginData = getPluginDataObject(pluginType, getIntegerOrZeroValue(pluginSortOrder));
                        results.add(pluginData);
                    }
                }

                return results;
            }

            private Integer getIntegerOrZeroValue(final String sortOrder) {
                if (sortOrder == null || sortOrder.isEmpty()) {
                    return 0;
                }

                try {
                    return Integer.parseInt(sortOrder);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }

            private PluginData getPluginDataObject(
                    final String pluginType,
                    final Integer sortOrder
            ) {
                return new PluginData(pluginType, sortOrder);
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
    @SuppressWarnings("checkstyle:LineLength")
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType().equals(XmlFileType.INSTANCE) && "di".equals(virtualFile.getNameWithoutExtension());
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
