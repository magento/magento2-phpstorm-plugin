package com.magento.idea.magento2plugin.xml.layout;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/22/15.
 */
public class LayoutUtility {
    public static boolean isLayoutFile(VirtualFile virtualFile) {
        VirtualFile parent = virtualFile.getParent();
        return virtualFile.getFileType() == XmlFileType.INSTANCE && parent.isDirectory() && parent.getName().endsWith("layout");
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
            if (LayoutUtility.isLayoutFile(xmlFile)) {
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
}
