package com.magento.idea.magento2plugin.xml.di.index;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by dkvashnin on 10/13/15.
 */
public class VirtualTypesNamesFileBasedIndex extends FileBasedIndexExtension<String,String> {
    public static final ID<String, String> NAME = ID.create("com.magento.idea.magento2plugin.xml.di.index.virtual_types_names");
    private final EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();
    private final MyDataIndexer myDataIndexer = new MyDataIndexer();

    private static final int SUPER_MAX_NESTING_LEVEL = 3;

    public static String[] getAllVirtualTypesNames(final Project project) {
        final Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(NAME, project);
        return ArrayUtil.toStringArray(allKeys);
    }

    public static XmlAttributeValue[] getVirtualTypesByName(final Project project, final String virtualTypeName, final GlobalSearchScope scope) {
        List<XmlAttributeValue> xmlAttributeList = new ArrayList<XmlAttributeValue>();

        Collection<VirtualFile> virtualFileCollection = FileBasedIndex.getInstance().getContainingFiles(NAME, virtualTypeName, scope);
        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile virtualFile: virtualFileCollection) {
            XmlFile xmlFile = (XmlFile)psiManager.findFile(virtualFile);
            if (xmlFile == null) {
                continue;
            }

            XmlTag rootTag = xmlFile.getRootTag();
            if (rootTag == null) {
                continue;
            }

            for (XmlTag typeTag: rootTag.getSubTags()) {
                if (typeTag.getName().equals("virtualType")) {
                    XmlAttribute nameAttribute = typeTag.getAttribute("name");
                    if (nameAttribute != null) {
                        if (nameAttribute.getValue() != null && nameAttribute.getValue().equals(virtualTypeName))

                        xmlAttributeList.add(nameAttribute.getValueElement());
                    }
                }
            }
        }

        return xmlAttributeList.toArray(new XmlAttributeValue[xmlAttributeList.size()]);
    }

    public static String getParentTypeName(final Project project, String virtualTypeName) {
        List<String> originNames = FileBasedIndex.getInstance().getValues(NAME, virtualTypeName, GlobalSearchScope.allScope(project));

        if (originNames.size() > 0) {
            return originNames.get(0);
        }

        return null;
    }

    public static String getSuperParentTypeName(final Project project, String inputChildTypeName) {
        String superName = null;
        String childTypeName = inputChildTypeName;

        for (int index = 0; index < SUPER_MAX_NESTING_LEVEL; index++) {
            superName = getParentTypeName(project, childTypeName);

            if (superName == null) {
                superName = childTypeName;
                break;
            }

            childTypeName = superName;
        }

        return superName == null
            ? (!inputChildTypeName.equals(childTypeName) ? childTypeName : null)
            : superName;
    }

    public static List<PhpClass> getSuperParentTypes(final Project project, String inputChildTypeName) {
        List<PhpClass> result = new ArrayList<>();

        String superName = getSuperParentTypeName(project, inputChildTypeName);
        if (superName == null) {
            return result;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        result.addAll(phpIndex.getClassesByFQN(superName));
        result.addAll(phpIndex.getInterfacesByFQN(superName));

        return result;
    }

    @NotNull
    @Override
    public ID<String, String> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return myDataIndexer;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
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
        return 2;
    }

    private class MyDataIndexer implements DataIndexer<String, String, FileContent> {

        @NotNull
        @Override
        public Map<String, String> map(@NotNull FileContent fileContent) {
            Map<String, String> map = new HashMap<>();

            PsiFile psiFile = fileContent.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

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
                    for(XmlTag typeNode: xmlTag.findSubTags("virtualType")) {
                        if (typeNode.getAttributeValue("name") != null && typeNode.getAttributeValue("type") != null) {
                            map.put(
                                typeNode.getAttributeValue("name"),
                                PhpLangUtil.toPresentableFQN(typeNode.getAttributeValue("type"))
                                );
                        }
                    }
                }
            }

            return map;
        }
    }
}
