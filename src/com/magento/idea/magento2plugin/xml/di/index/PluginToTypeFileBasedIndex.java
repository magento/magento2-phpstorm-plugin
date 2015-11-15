package com.magento.idea.magento2plugin.xml.di.index;

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
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.xml.index.StringSetDataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by dkvashnin on 11/10/15.
 */
public class PluginToTypeFileBasedIndex extends FileBasedIndexExtension<String, String[]> {
    public static final ID<String, String[]> NAME = ID.create("com.magento.idea.magento2plugin.xml.di.index.plugin_to_type");
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
                        for(XmlTag typeNode: xmlTag.findSubTags("type")) {
                            String typeName = typeNode.getAttributeValue("name");
                            if (typeName != null) {
                                String[] plugins = getPluginsForType(typeNode);
                                if (plugins.length != 0) {
                                    map.put(
                                        PhpLangUtil.toPresentableFQN(typeName),
                                        getPluginsForType(typeNode)
                                    );
                                }
                            }
                        }
                    }
                }

                return map;
            }

            private String[] getPluginsForType(XmlTag typeNode) {
                List<String> results = new ArrayList<String>();

                for (XmlTag pluginTag: typeNode.findSubTags("plugin")) {
                    String pluginType = pluginTag.getAttributeValue("type");
                    if (pluginType != null) {
                        results.add(PhpLangUtil.toPresentableFQN(pluginType));
                    }
                }
                return results.toArray(new String[results.size()]);
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
        return new StringSetDataExternalizer();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return file.getFileType() == XmlFileType.INSTANCE && file.getNameWithoutExtension().equals("di");
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
}
