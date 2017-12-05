package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.xml.LineMarkerXmlTagDecorator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by dkvashnin on 11/15/15.
 */
public class TypeConfigurationIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.type_configuration");
    private final EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();

    private static final String TYPE_TAG = "type";
    private static final String PLUGIN_TAG = "plugin";
    private static final String VIRTUAL_TYPE_TAG = "virtualType";
    private static final String PREFERENCE_TAG = "preference";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String FOR_ATTRIBUTE = "for";

    private static final Map<String, String> TAG_ATTRIBUTE_RELATION = new HashMap<String, String>() {{
        put(TYPE_TAG, NAME_ATTRIBUTE);
        put(PREFERENCE_TAG, FOR_ATTRIBUTE);
        put(PLUGIN_TAG, TYPE_ATTRIBUTE);
        put(VIRTUAL_TYPE_TAG, TYPE_ATTRIBUTE);
        put("event", null);
        put("observer", "instance");
    }};

    public static List<XmlTag> getClassConfigurations(PhpClass phpClass) {
        String classFqn = phpClass.getPresentableFQN();

        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
            .getContainingFiles(KEY, classFqn, GlobalSearchScope.allScope(phpClass.getProject())
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
            if (attribute != null) {
                String className = childTag.getAttributeValue(attribute);
                if (className != null && PhpLangUtil.toPresentableFQN(className).equals(classFqn)) {
                    tagsReferences.add(getLineMarkerDecorator(childTag));
                }
            }

            // type tag has plugin tags
            if (tagName.equals(TYPE_TAG)) {
                fillRelatedTags(classFqn, childTag, tagsReferences);
            }

            if (tagName.equals("event")) {
                fillRelatedTags(classFqn, childTag, tagsReferences);
            }
        }
    }

    /**
     * Decorate tag with appropriate line marker decorator.
     */
    @NotNull
    private static XmlTag getLineMarkerDecorator(XmlTag tag) {
        switch (tag.getName()) {
            case PREFERENCE_TAG:
                return new DiPreferenceLineMarkerXmlTagDecorator(tag);
            case TYPE_TAG:
                return new DiTypeLineMarkerXmlTagDecorator(tag);
            case PLUGIN_TAG:
                return new DiPluginLineMarkerXmlTagDecorator(tag);
            case VIRTUAL_TYPE_TAG:
                return new DiVirtualTypeLineMarkerXmlTagDecorator(tag);
            default:
                return tag;
        }
    }

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
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
                        processConfigurationTags(xmlTag, map);
                    }
                }

                return map;
            }

            private void processConfigurationTags(XmlTag parentTag, Map<String, Void> results) {
                for (XmlTag childTag: parentTag.getSubTags()) {
                    String tagName = childTag.getName();

                    String attribute = TAG_ATTRIBUTE_RELATION.get(tagName);
                    if (attribute != null) {
                        String className = childTag.getAttributeValue(attribute);
                        if (className != null) {
                            results.put(PhpLangUtil.toPresentableFQN(className), null);
                        }
                    }

                    // type tag has plugin tags
                    if (tagName.equals(TYPE_TAG)) {
                        processConfigurationTags(childTag, results);
                    }

                    if (tagName.equals("event")) {
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
        return virtualFile -> (virtualFile.getFileType()
                == XmlFileType.INSTANCE
                    && (virtualFile.getNameWithoutExtension().equalsIgnoreCase("di")
                        || virtualFile.getNameWithoutExtension().equalsIgnoreCase("events"))
        );
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    /**
     * Decorator for XmlTag, which improves readability of "preference" node in configuration line marker.
     */
    private static class DiPreferenceLineMarkerXmlTagDecorator extends LineMarkerXmlTagDecorator {

        DiPreferenceLineMarkerXmlTagDecorator(XmlTag xmlTag) {
            super(xmlTag);
        }

        @NotNull
        @Override
        public String getDescription() {
            String preference = xmlTag.getAttributeValue(TYPE_ATTRIBUTE);
            if (preference != null) {
                return String.format("preference %s", preference);
            }
            return xmlTag.getName();
        }

        @Override
        @NotNull
        @NonNls
        public String getName() {
            String preference = xmlTag.getAttributeValue(TYPE_ATTRIBUTE);
            if (preference != null) {
                return String.format("[%s] preference %s", getAreaName(), preference);
            }
            return xmlTag.getName();
        }
    }

    /**
     * Decorator for XmlTag, which improves readability of "type" node in configuration line marker.
     */
    private static class DiTypeLineMarkerXmlTagDecorator extends LineMarkerXmlTagDecorator {

        DiTypeLineMarkerXmlTagDecorator(XmlTag xmlTag) {
            super(xmlTag);
        }

        @NotNull
        @Override
        public String getDescription() {
            return "type declaration";
        }
    }

    /**
     * Decorator for XmlTag, which improves readability of "plugin" node in configuration line marker.
     */
    private static class DiPluginLineMarkerXmlTagDecorator extends LineMarkerXmlTagDecorator {

        DiPluginLineMarkerXmlTagDecorator(XmlTag xmlTag) {
            super(xmlTag);
        }

        @NotNull
        @Override
        public String getDescription() {
            XmlTag typeTag = xmlTag.getParentTag();
            if (typeTag == null) {
                return xmlTag.getName();
            }
            String type = typeTag.getAttributeValue(NAME_ATTRIBUTE);
            if (type == null) {
                return xmlTag.getName();
            }

            return String.format("plugin for %s", type);
        }
    }

    /**
     * Decorator for XmlTag, which improves readability of "virtualType" node in configuration line marker.
     */
    private static class DiVirtualTypeLineMarkerXmlTagDecorator extends LineMarkerXmlTagDecorator {

        DiVirtualTypeLineMarkerXmlTagDecorator(XmlTag xmlTag) {
            super(xmlTag);
        }

        @NotNull
        @Override
        public String getDescription() {
            String type = xmlTag.getAttributeValue(NAME_ATTRIBUTE);
            if (type != null) {
                return String.format("virtual type %s", type);
            }
            return xmlTag.getName();
        }
    }
}
