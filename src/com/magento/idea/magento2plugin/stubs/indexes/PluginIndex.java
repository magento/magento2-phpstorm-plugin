/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
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
 * Created by dkvashnin on 11/10/15.
 */
public class PluginIndex extends FileBasedIndexExtension<String, Set<String>> {
    public static final ID<String, Set<String>> KEY
            = ID.create("com.magento.idea.magento2plugin.stubs.indexes.plugin_to_type");
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
                                Set<String> plugins = getPluginsForType(typeNode);
                                if (plugins.size() > 0) {
                                    map.put(PhpLangUtil.toPresentableFQN(typeName), plugins);
                                }
                            }
                        }
                    }
                }

                return map;
            }

            private Set<String> getPluginsForType(XmlTag typeNode) {
                Set<String> results = new HashSet<String>();

                for (XmlTag pluginTag: typeNode.findSubTags("plugin")) {
                    String pluginType = pluginTag.getAttributeValue("type");
                    if (pluginType != null) {
                        results.add(PhpLangUtil.toPresentableFQN(pluginType));
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
    public DataExternalizer<Set<String>> getValueExternalizer() {
        return new StringSetDataExternalizer();
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
