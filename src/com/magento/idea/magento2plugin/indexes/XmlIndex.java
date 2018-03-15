package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.stubs.indexes.xml.PhpClassNameIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XmlIndex {

    private static XmlIndex INSTANCE;

    private Project project;

    private XmlIndex() {
    }

    public static XmlIndex getInstance(final Project project) {
        if (null == INSTANCE) {
            INSTANCE = new XmlIndex();
        }
        INSTANCE.project = project;
        return INSTANCE;
    }

    public static List<XmlTag> getPhpClassDeclarations(PhpClass phpClass) {

        List<XmlTag> result = new ArrayList<>();

        String fqn = phpClass.getPresentableFQN();

        PsiManager psiManager = PsiManager.getInstance(phpClass.getProject());

        Collection<VirtualFile> vfs = FileBasedIndex.getInstance()
            .getContainingFiles(PhpClassNameIndex.KEY, fqn, GlobalSearchScope.allScope(phpClass.getProject()));

        for (VirtualFile vf : vfs) {
            XmlFile xmlFile = (XmlFile)psiManager.findFile(vf);
            if (xmlFile == null) {
                continue;
            }

            XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(xmlFile.getFirstChild(), XmlTag.class);
            if (xmlTags == null) {
                continue;
            }

            for (XmlTag xmlTag: xmlTags) {
                fillList(xmlTag, fqn, result);
            }
        }

        return result;

    }

    private static void fillList(XmlTag parentTag, String fqn, List<XmlTag> list) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            for (XmlAttribute xmlAttribute: childTag.getAttributes()) {
                String xmlAttributeValue = xmlAttribute.getValue();
                if (xmlAttributeValue != null) {
                    xmlAttributeValue = xmlAttributeValue.startsWith("\\")
                            ? xmlAttributeValue.substring(1) : xmlAttributeValue;
                    if (xmlAttributeValue.startsWith(fqn)) {
                        list.add(childTag);
                    }
                }
            }
            XmlTagValue childTagValue = childTag.getValue();
            String tagValue = childTagValue.getTrimmedText();
            tagValue = tagValue.startsWith("\\") ? tagValue.substring(1) : tagValue;
            if (!tagValue.isEmpty() && tagValue.startsWith(fqn)) {
                list.add(childTag);
            }


            fillList(childTag, fqn, list);
        }
    }
}
