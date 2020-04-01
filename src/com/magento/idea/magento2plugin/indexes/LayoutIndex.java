/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.indexes;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/20/15.
 */
public class LayoutIndex {
    private static List<XmlTag> getComponentDeclarations(String componentValue, String componentType, ID<String, Void> id, Project project, ComponentMatcher componentMatcher) {
        List<XmlTag> results = new ArrayList<XmlTag>();
        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
            .getContainingFiles(
                id,
                componentValue,
                GlobalSearchScope.allScope(project)
            );
        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile virtualFile: containingFiles) {
            XmlFile xmlFile = (XmlFile)psiManager.findFile(virtualFile);
            if (xmlFile == null) {
                continue;
            }

            XmlTag rootTag = xmlFile.getRootTag();
            if (rootTag == null) {
                continue;
            }
            collectComponentDeclarations(rootTag, results, componentValue, componentType, componentMatcher);
        }

        return results;
    }

    public static boolean isLayoutFile(VirtualFile virtualFile) {
        VirtualFile parent = virtualFile.getParent();
        return virtualFile.getFileType() == XmlFileType.INSTANCE && parent.isDirectory()
                && parent.getName().endsWith("layout");
    }

    public static boolean isLayoutFile(PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getOriginalFile().getVirtualFile();
        return isLayoutFile(virtualFile);
    }

    public static List<XmlFile> getLayoutFiles(Project project, @Nullable String fileName) {
        List<XmlFile> results = new ArrayList<XmlFile>();
        Collection<VirtualFile> xmlFiles = FilenameIndex.getAllFilesByExt(project, "xml");

        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile xmlFile: xmlFiles) {
            if (isLayoutFile(xmlFile)) {
                if (fileName != null && !xmlFile.getNameWithoutExtension().equals(fileName)) {
                    continue;
                }

                PsiFile file = psiManager.findFile(xmlFile);
                if (file != null) {
                    results.add((XmlFile)file);
                }
            }
        }

        return results;
    }

    public static List<XmlFile> getLayoutFiles(Project project) {
        return getLayoutFiles(project, null);
    }

    public static Collection<String> getAllKeys(ID<String, Void> id, Project project) {
        return FileBasedIndex.getInstance().getAllKeys(id, project);
    }

    private static void collectComponentDeclarations(XmlTag parentTag, List<XmlTag> results, String componentName, String componentType, ComponentMatcher componentMatcher) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            if (componentType.equals(childTag.getName()) && componentMatcher.matches(componentName, childTag)) {
                results.add(childTag);
            } else if(childTag.getSubTags().length > 0 ) {
                collectComponentDeclarations(childTag, results, componentName, componentType, componentMatcher);
            }
        }
    }

    public static List<XmlTag> getBlockDeclarations(String componentName, Project project) {
        return getComponentDeclarations(componentName, "block", BlockNameIndex.KEY, project, new NameComponentMatcher());
    }

    public static List<XmlTag> getContainerDeclarations(String componentName, Project project) {
        return getComponentDeclarations(componentName, "container", ContainerNameIndex.KEY, project, new NameComponentMatcher());
    }

    private interface ComponentMatcher {
        boolean matches(String value, XmlTag tag);
    }

    private static class NameComponentMatcher implements ComponentMatcher {
        @Override
        public boolean matches(String value, XmlTag tag) {
            return value.equals(tag.getAttributeValue("name"));
        }
    }
}
