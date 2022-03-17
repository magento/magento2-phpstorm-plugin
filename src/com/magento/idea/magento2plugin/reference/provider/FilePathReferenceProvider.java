/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.reference.provider.util.GetAllSubFilesOfVirtualFileUtil;
import com.magento.idea.magento2plugin.reference.provider.util.GetFilePathUtil;
import com.magento.idea.magento2plugin.reference.provider.util.GetModuleNameUtil;
import com.magento.idea.magento2plugin.reference.provider.util.GetModuleSourceFilesUtil;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import gnu.trove.THashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class FilePathReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            @NotNull PsiElement element,
            @NotNull ProcessingContext context
    ) {

        List<PsiReference> psiReferences = new ArrayList<>();

        String origValue = element.getText();

        String filePath = GetFilePathUtil.getInstance().execute(origValue);
        if (null == filePath) {
            return PsiReference.EMPTY_ARRAY;
        }

        // Find all files based on provided path
        Collection<VirtualFile> files = getFiles(element);
        if (!(files.size() > 0)) {
            return PsiReference.EMPTY_ARRAY;
        }

        PsiManager psiManager = PsiManager.getInstance(element.getProject());

        String currentPath = "";
        String[] pathParts = filePath.split("/");
        for (int i = 0; i < pathParts.length; i++) {
            String pathPart = pathParts[i];
            Boolean currentPathIsBuilt = false;

            Map<TextRange, List<PsiElement>> psiPathElements = new THashMap<>();

            for (VirtualFile file : files) {
                String fileUrl = file.getUrl();
                if (!fileUrl.contains(filePath)) {
                    continue;
                }
                String rootPathUrl = fileUrl.substring(0, fileUrl.indexOf(filePath));
                String[] relativePathParts
                        = fileUrl.substring(fileUrl.indexOf(filePath)).split("/");

                if (!currentPathIsBuilt) {
                    currentPath = currentPath.isEmpty()
                            ? currentPath.concat(relativePathParts[i])
                            : currentPath.concat("/").concat(relativePathParts[i]);
                    currentPathIsBuilt = true;
                }

                VirtualFile currentVf = VirtualFileManager.getInstance()
                        .findFileByUrl(rootPathUrl.concat(currentPath));

                if (null != currentVf) {
                    PsiElement psiElement = currentVf.isDirectory()
                            ? psiManager.findDirectory(currentVf)
                            : psiManager.findFile(currentVf);
                    if (null != psiElement) {
                        final int currentPathIndex = currentPath.lastIndexOf("/") == -1
                                ? 0 : currentPath.lastIndexOf("/") + 1;

                        TextRange pathRange = new TextRange(
                                origValue.indexOf(filePath)
                                    + currentPathIndex,
                                origValue.indexOf(filePath)
                                    + currentPathIndex
                                    + pathPart.length()
                        );

                        if (!psiPathElements.containsKey(pathRange)) {
                            List<PsiElement> list = new ArrayList<>();
                            list.add(psiElement);
                            psiPathElements.put(pathRange, list);
                        } else {
                            psiPathElements.get(pathRange).add(psiElement);
                        }
                    }
                }
            }

            if (psiPathElements.size() > 0) {
                psiPathElements.forEach((textRange, psiElements) ->
                        psiReferences.add(
                                new PolyVariantReferenceBase(element, textRange, psiElements)
                        )
                );
            }
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }

    private Collection<VirtualFile> getFiles(@NotNull PsiElement element) {
        Collection<VirtualFile> files = new ArrayList<>();

        String filePath = GetFilePathUtil.getInstance().execute(element.getText());
        if (null == filePath) {
            return files;
        }

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        if (fileName.matches(".*\\.\\w+$")) {
            // extension presents
            files = FilenameIndex.getVirtualFilesByName(
                    fileName,
                    GlobalSearchScope.allScope(element.getProject())
            );
            files.removeIf(f -> !f.getPath().endsWith(filePath));

            // filter by module
            Collection<VirtualFile> vfs = GetModuleSourceFilesUtil.getInstance()
                    .execute(element.getText(), element.getProject());
            if (null != vfs) {
                files.removeIf(f -> {
                    for (VirtualFile vf : vfs) {
                        if (f.getPath().startsWith(vf.getPath().concat("/"))) {
                            return false;
                        }
                    }
                    return true;
                });
            }
        } else if (isModuleNamePresent(element)) {
            // extension absent
            Collection<VirtualFile> vfs = GetModuleSourceFilesUtil.getInstance()
                    .execute(element.getText(), element.getProject());
            if (null != vfs) {
                for (VirtualFile vf : vfs) {
                    Collection<VirtualFile> vfChildren = GetAllSubFilesOfVirtualFileUtil
                            .getInstance().execute(vf);
                    if (null != vfChildren) {
                        vfChildren.removeIf(f -> {
                            if (!f.isDirectory()) {
                                String ext = f.getExtension();
                                if (null != ext) {
                                    return !f.getPath().endsWith(filePath.concat(".").concat(ext));
                                }
                            }
                            return true;
                        });
                        files.addAll(vfChildren);
                    }
                }
            }
        }

        return files;
    }

    private boolean isModuleNamePresent(@NotNull PsiElement element) {
        return GetModuleNameUtil.getInstance().execute(element.getText()) != null;
    }
}
