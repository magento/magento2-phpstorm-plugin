/*
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

    @SuppressWarnings({
            "PMD.CognitiveComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.NPathComplexity",
            "PMD.AvoidInstantiatingObjectsInLoops",
            "PMD.AvoidDeeplyNestedIfStmts"
    })
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final String origValue = element.getText();

        final String filePath = GetFilePathUtil.getInstance().execute(origValue);
        if (null == filePath) {
            return PsiReference.EMPTY_ARRAY;
        }

        // Find all files based on provided path
        final Collection<VirtualFile> files = getFiles(element);

        if (files.isEmpty()) {
            return PsiReference.EMPTY_ARRAY;
        }
        final PsiManager psiManager = PsiManager.getInstance(element.getProject());

        final List<PsiReference> psiReferences = new ArrayList<>();

        String currentPath = "";
        final String[] pathParts = filePath.split("/");
        for (int i = 0; i < pathParts.length; i++) {
            final String pathPart = pathParts[i];
            Boolean currentPathIsBuilt = false;

            final Map<TextRange, List<PsiElement>> psiPathElements = new THashMap<>();

            for (final VirtualFile file : files) {
                final String fileUrl = file.getUrl();
                if (!fileUrl.contains(filePath)) {
                    continue;
                }
                final String rootPathUrl = fileUrl.substring(0, fileUrl.indexOf(filePath));
                final String[] relativePathParts
                        = fileUrl.substring(fileUrl.indexOf(filePath)).split("/");

                if (!currentPathIsBuilt) {
                    currentPath = currentPath.isEmpty()
                            ? currentPath.concat(relativePathParts[i])
                            : currentPath.concat("/").concat(relativePathParts[i]);
                    currentPathIsBuilt = true;
                }

                final VirtualFile currentVf = VirtualFileManager.getInstance()
                        .findFileByUrl(rootPathUrl.concat(currentPath));

                if (null != currentVf) {
                    final PsiElement psiElement = currentVf.isDirectory()
                            ? psiManager.findDirectory(currentVf)
                            : psiManager.findFile(currentVf);
                    if (null != psiElement) {
                        final int currentPathIndex = currentPath.lastIndexOf('/') == -1
                                ? 0 : currentPath.lastIndexOf('/') + 1;
                        final int startOffset = origValue.indexOf(filePath) + currentPathIndex;
                        final int endOffset = startOffset + pathPart.length();

                        if (!isProperRange(startOffset, endOffset)) {
                            continue;
                        }
                        final TextRange pathRange = new TextRange(startOffset, endOffset);

                        if (psiPathElements.containsKey(pathRange)) {
                            psiPathElements.get(pathRange).add(psiElement);
                        } else {
                            final List<PsiElement> list = new ArrayList<>();
                            list.add(psiElement);
                            psiPathElements.put(pathRange, list);
                        }
                    }
                }
            }

            if (!psiPathElements.isEmpty()) {
                psiPathElements.forEach((textRange, psiElements) ->
                        psiReferences.add(
                                new PolyVariantReferenceBase(element, textRange, psiElements)
                        )
                );
            }
        }

        return psiReferences.toArray(new PsiReference[0]);
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    private Collection<VirtualFile> getFiles(final @NotNull PsiElement element) {
        Collection<VirtualFile> files = new ArrayList<>();

        final String filePath = GetFilePathUtil.getInstance().execute(element.getText());
        if (null == filePath) {
            return files;
        }

        final String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

        if (fileName.matches(".*\\.\\w+$")) {
            // extension presents
            files = FilenameIndex.getVirtualFilesByName(
                    fileName,
                    GlobalSearchScope.allScope(element.getProject())
            );
            files.removeIf(f -> !f.getPath().endsWith(filePath));

            // filter by module
            final Collection<VirtualFile> vfs = GetModuleSourceFilesUtil.getInstance()
                    .execute(element.getText(), element.getProject());
            if (null != vfs) {
                files.removeIf(f -> {
                    for (final VirtualFile vf : vfs) {
                        if (f.getPath().startsWith(vf.getPath().concat("/"))) {
                            return false;
                        }
                    }
                    return true;
                });
            }
        } else if (isModuleNamePresent(element)) {
            // extension absent
            final Collection<VirtualFile> vfs = GetModuleSourceFilesUtil.getInstance()
                    .execute(element.getText(), element.getProject());
            if (null != vfs) {
                for (final VirtualFile vf : vfs) {
                    final Collection<VirtualFile> vfChildren = GetAllSubFilesOfVirtualFileUtil
                            .getInstance().execute(vf);
                    if (null != vfChildren) {
                        vfChildren.removeIf(f -> {
                            if (!f.isDirectory()) { //NOPMD
                                final String ext = f.getExtension();
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

    private boolean isModuleNamePresent(final @NotNull PsiElement element) {
        return GetModuleNameUtil.getInstance().execute(element.getText()) != null;
    }

    private boolean isProperRange(final int startOffset, final int endOffset) {
        return startOffset <= endOffset && startOffset >= 0;
    }
}
