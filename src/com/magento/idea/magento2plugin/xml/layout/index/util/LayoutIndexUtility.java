package com.magento.idea.magento2plugin.xml.layout.index.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.xml.index.LineMarkerXmlTagDecorator;
import com.magento.idea.magento2plugin.xml.layout.index.BlockClassFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.BlockFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.ContainerFileBasedIndex;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/20/15.
 */
public class LayoutIndexUtility {
    public static List<XmlTag> getComponentDeclarations(String componentValue, String componentType, ID<String, Void> id, Project project, ComponentMatcher componentMatcher) {
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

    public static Collection<String> getAllKeys(ID<String, Void> id, Project project) {
        return FileBasedIndex.getInstance().getAllKeys(id, project);
    }

    public static void collectComponentDeclarations(XmlTag parentTag, List<XmlTag> results, String componentName, String componentType, ComponentMatcher componentMatcher) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            if (componentType.equals(childTag.getName()) && componentMatcher.matches(componentName, childTag)) {
                results.add(childTag);
            } else if(childTag.getSubTags().length > 0 ) {
                collectComponentDeclarations(childTag, results, componentName, componentType, componentMatcher);
            }
        }
    }

    public static List<XmlTag> getBlockDeclarations(String componentName, Project project) {
        return getComponentDeclarations(componentName, "block", BlockFileBasedIndex.NAME, project, new NameComponentMatcher());
    }

    public static List<XmlTag> getContainerDeclarations(String componentName, Project project) {
        return getComponentDeclarations(componentName, "container", ContainerFileBasedIndex.NAME, project, new NameComponentMatcher());
    }

    public static List<XmlTag> getBlockClassDeclarations(PhpClass phpClass, Project project) {
        String className = phpClass.getPresentableFQN();

        List<XmlTag> blockTags = getComponentDeclarations(className, "block", BlockClassFileBasedIndex.NAME, project, new ClassComponentMatcher());
        List<XmlTag> decoratedBlockTags = new ArrayList<>();
        for (XmlTag blockTag: blockTags) {
            decoratedBlockTags.add(new LayoutBlockLineMarkerXmlTagDecorator(blockTag));
        }
        return decoratedBlockTags;
    }
}

interface ComponentMatcher {
    boolean matches(String value, XmlTag tag);
}

class NameComponentMatcher implements ComponentMatcher {
    @Override
    public boolean matches(String value, XmlTag tag) {
        return value.equals(tag.getAttributeValue("name"));
    }
}

class ClassComponentMatcher implements ComponentMatcher {
    @Override
    public boolean matches(String value, XmlTag tag) {
        return value.equals(tag.getAttributeValue("class"));
    }
}

/**
 * Decorator for XmlTag, which improves readability of "block" node in configuration line marker.
 */
class LayoutBlockLineMarkerXmlTagDecorator extends LineMarkerXmlTagDecorator {

    public LayoutBlockLineMarkerXmlTagDecorator(XmlTag xmlTag) {
        super(xmlTag);
    }

    @NotNull
    @Override
    protected String getAreaName() {
        return xmlTag.getContainingFile().getVirtualFile().getParent().getParent().getName();
    }

    @NotNull
    @Override
    public String getDescription() {
        String name = xmlTag.getAttributeValue("name");
        if (name != null) {
            return String.format("block %s", name);
        }
        return xmlTag.getName();
    }
}