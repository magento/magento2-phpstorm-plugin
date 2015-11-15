package com.magento.idea.magento2plugin.xml.di.index;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import com.magento.idea.magento2plugin.xml.index.StringSetDataExternalizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by dkvashnin on 11/15/15.
 */
public class TypeConfigurationFileBasedIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> NAME = ID.create("com.magento.idea.magento2plugin.xml.di.index.type_configuration");
    private final EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();

    public static final Map<String, String> TAG_ATTRIBUTE_RELATION = new HashMap<String, String>() {{
        put(XmlHelper.TYPE_TAG, XmlHelper.NAME_ATTRIBUTE);
        put(XmlHelper.PREFERENCE_TAG, XmlHelper.FOR_ATTRIBUTE);
        put(XmlHelper.PLUGIN_TAG, XmlHelper.TYPE_ATTRIBUTE);
        put(XmlHelper.VIRTUAL_TYPE_TAG, XmlHelper.TYPE_ATTRIBUTE);
    }};

    @Nullable
    public static List<XmlTag> getClassConfigurations(PhpClass phpClass) {
        String classFqn = phpClass.getPresentableFQN();
        if (classFqn == null) {
            return null;
        }

        Collection<VirtualFile> containingFiles = FileBasedIndex
            .getInstance()
            .getContainingFiles(
                NAME,
                classFqn,
                GlobalSearchScope.allScope(phpClass.getProject())
            );

        PsiManager psiManager = PsiManager.getInstance(phpClass.getProject());

        List<XmlTag> tags = new ArrayList<XmlTag>();

        for (VirtualFile virtualFile: containingFiles) {
            XmlFile file = (XmlFile)psiManager.findFile(virtualFile);

            if (file == null) {
                continue;
            }

            XmlTag rootTag = file.getRootTag();
            fillRelatedTags(classFqn, rootTag, tags);
        }

        return tags;
    }

    private static void fillRelatedTags(String classFqn, XmlTag parentTag, List<XmlTag> tagsReferences) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            String tagName = childTag.getName();
            String attribute = TAG_ATTRIBUTE_RELATION.get(tagName);
            if (attribute == null) {
                continue;
            }

            String className = childTag.getAttributeValue(attribute);
            if (className != null && PhpLangUtil.toPresentableFQN(className).equals(classFqn)) {
                tagsReferences.add(childTag);
            }

            // type tag has plugin tags
            if (tagName.equals(XmlHelper.TYPE_TAG)) {
                fillRelatedTags(classFqn, childTag, tagsReferences);
            }
        }
    }

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {
            @NotNull
            @Override
            public Map<String, Void> map(@NotNull FileContent fileContent) {
                Map<String, Void> map = new HashMap<>();

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
                        processConfigurationTags(xmlTag, map);
                    }
                }

                return map;
            }

            private void processConfigurationTags(XmlTag parentTag, Map<String, Void> results) {
                for (XmlTag childTag: parentTag.getSubTags()) {
                    String tagName = childTag.getName();

                    String attribute = TAG_ATTRIBUTE_RELATION.get(tagName);
                    if (attribute == null) {
                        continue;
                    }

                    String className = childTag.getAttributeValue(attribute);
                    if (className != null) {
                        results.put(PhpLangUtil.toPresentableFQN(className), null);
                    }

                    // type tag has plugin tags
                    if (tagName.equals(XmlHelper.TYPE_TAG)) {
                        processConfigurationTags(childTag, results);
                    }
                }
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
        return 0;
    }
}
