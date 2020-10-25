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
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("PMD")
public final class UIComponentIndex {

    private UIComponentIndex() {
        throw new AssertionError("Instantiating utility class...");
    }

    /**
     * Available ui component file.
     *
     * @param virtualFile VirtualFile
     * @return boolean
     */
    public static boolean isUiComponentFile(final VirtualFile virtualFile) {
        final VirtualFile parent = virtualFile.getParent();
        return virtualFile.getFileType() == XmlFileType.INSTANCE && parent.isDirectory()
                && parent.getName().endsWith("ui_component");
    }

    /**
     * Get ui component files.
     *
     * @param project Project
     * @param fileName String
     * @return List
     */
    public static List<XmlFile> getUiComponentFiles(
            final Project project,
            final @Nullable String fileName
    ) {
        final List<XmlFile> results = new ArrayList<XmlFile>();//NOPMD
        final Collection<VirtualFile> xmlFiles = FilenameIndex.getAllFilesByExt(project, "xml");

        final PsiManager psiManager = PsiManager.getInstance(project);
        for (final VirtualFile xmlFile: xmlFiles) {
            if (isUiComponentFile(xmlFile)) {
                if (fileName != null && !xmlFile.getNameWithoutExtension().equals(fileName)) {
                    continue;
                }

                final PsiFile file = psiManager.findFile(xmlFile);
                if (file != null) {
                    results.add((XmlFile)file);
                }
            }
        }

        return results;
    }

    /**
     * Get ui component files.
     *
     * @param project Project
     * @return List
     */
    public static List<XmlFile> getUiComponentFiles(final Project project) {
        return getUiComponentFiles(project, null);
    }

    /**
     * Get All Keys.
     *
     * @param identifier ID
     * @param project Project
     * @return Collection
     */
    public static Collection<String> getAllKeys(
            final ID<String, Void> identifier,
            final Project project
    ) {
        return FileBasedIndex.getInstance().getAllKeys(identifier, project);
    }

    /**
     * Get ui component files.
     *
     * @param project Project
     * @param fileName String
     * @return List
     */
    public static List<XmlFile> getUIComponentFiles(Project project, @Nullable String fileName) {
        List<XmlFile> results = new ArrayList<XmlFile>();
        Collection<VirtualFile> xmlFiles = FilenameIndex.getAllFilesByExt(project, "xml");

        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile xmlFile: xmlFiles) {
            if (isUIComponentFile(xmlFile)) {
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

    /**
     * Available ui component file.
     *
     * @param virtualFile VirtualFile
     * @return boolean
     */
    public static boolean isUIComponentFile(VirtualFile virtualFile) {
        VirtualFile parent = virtualFile.getParent();
        return virtualFile.getFileType() == XmlFileType.INSTANCE && parent.isDirectory()
                && parent.getName().endsWith("ui_component");
    }

}
