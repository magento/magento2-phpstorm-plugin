package com.magento.idea.magento2plugin.xml.layout.index;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/18/15.
 */
public abstract class AbstractComponentNameFileBasedIndex extends ScalarIndexExtension<String> {
    private EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new LayoutDataIndexer(getComponentName());
    }

    protected abstract String getComponentName();

    public static List<XmlTag> getComponentDeclarations(String componentName, String componentType, ID<String, Void> id, Project project) {
        List<XmlTag> results = new ArrayList<XmlTag>();
        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
            .getContainingFiles(
                id,
                componentName,
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
            collectComponentDeclarations(rootTag, results, componentName, componentType);
        }


        return results;
    }

    public static Collection<String> getAllKeys(ID<String, Void> id, Project project) {
        return FileBasedIndex.getInstance().getAllKeys(id, project);
    }

    public static void collectComponentDeclarations(XmlTag parentTag, List<XmlTag> results, String componentName, String componentType) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            if (componentType.equals(childTag.getName()) && componentName.equals(childTag.getAttributeValue("name"))) {
                results.add(childTag);
            } else if(childTag.getSubTags().length > 0 ) {
                collectComponentDeclarations(childTag, results, componentName, componentType);
            }
        }
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
            public boolean acceptInput(@NotNull VirtualFile virtualFile) {
                VirtualFile parent = virtualFile.getParent();
                return virtualFile.getFileType() == XmlFileType.INSTANCE && parent.isDirectory() && parent.getName().endsWith("layout");
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
