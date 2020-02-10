/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePathReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

        List<PsiReference> psiReferences = new ArrayList<>();

        String origValue = element.getText();

        String filePath = getFilePath(element);
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
                String[] relativePathParts = fileUrl.substring(fileUrl.indexOf(filePath)).split("/");

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

                        TextRange pathRange = new TextRange(
                            origValue.indexOf(filePath)
                                + (currentPath.lastIndexOf("/") == -1 ? 0 : currentPath.lastIndexOf("/") + 1),
                            origValue.indexOf(filePath)
                                + (currentPath.lastIndexOf("/") == -1 ? 0 : currentPath.lastIndexOf("/") + 1)
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
                psiPathElements.forEach(((textRange, psiElements) ->
                        psiReferences.add(new PolyVariantReferenceBase(element, textRange, psiElements))
                ));
            }
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }

    private Collection<VirtualFile> getFiles(@NotNull PsiElement element)
    {
        Collection<VirtualFile> files = new ArrayList<>();

        String filePath = getFilePath(element);
        if (null == filePath) {
            return files;
        }

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        if (fileName.matches(".*\\.\\w+$")) {
            // extension presents
            files = FilenameIndex.getVirtualFilesByName(
                    element.getProject(),
                    fileName,
                    GlobalSearchScope.allScope(element.getProject())
            );
            files.removeIf(f -> !f.getPath().endsWith(filePath));

            // filter by module
            Collection<VirtualFile> vfs = getModuleSourceFiles(element);
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
            Collection<VirtualFile> vfs = getModuleSourceFiles(element);
            if (null != vfs) {
                for (VirtualFile vf : vfs) {
                    Collection<VirtualFile> vfChildren = getAllSubFiles(vf);
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

    private Collection<VirtualFile> getModuleFile(@NotNull PsiElement element)
    {
        String moduleName = getModuleName(element);
        if (null == moduleName || moduleName.isEmpty()) {
            return null;
        }
        return FileBasedIndex.getInstance()
                .getContainingFiles(ModuleNameIndex.KEY, moduleName,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(element.getProject()),
                                PhpFileType.INSTANCE
                        )
                );
    }

    private Collection<VirtualFile> getModuleSourceFiles(@NotNull PsiElement element)
    {
        Collection<VirtualFile> virtualFiles = getModuleFile(element);
        if (null == virtualFiles) {
            return null;
        }
        virtualFiles.removeIf(vf -> !(vf != null && vf.getParent() != null));
        Collection<VirtualFile> sourceVfs = new ArrayList<>();
        for (VirtualFile vf : virtualFiles) {
            sourceVfs.add(vf.getParent());
        }
        return sourceVfs;
    }

    private String getFilePath(@NotNull PsiElement element)
    {
        String value = element.getText();
        String moduleName = getModuleName(element);
        if (null != moduleName && value.contains(moduleName))  {
            value = value.replace(moduleName, "");
        }

        Pattern pattern = Pattern.compile("\\W?(([\\w-]+/)*[\\w\\.-]+)");
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    private String getModuleName(@NotNull PsiElement element)
    {
        Pattern pattern = Pattern.compile("(([A-Z][a-zA-Z0-9]+)_([A-Z][a-zA-Z0-9]+))");
        Matcher matcher = pattern.matcher(element.getText());
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isModuleNamePresent(@NotNull PsiElement element)
    {
        return getModuleName(element) != null;
    }

    private Collection<VirtualFile> getAllSubFiles(VirtualFile virtualFile)
    {
        Collection<VirtualFile> list = new ArrayList<>();

        VfsUtilCore.visitChildrenRecursively(virtualFile, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (!file.isDirectory()) {
                    list.add(file);
                }
                return super.visitFile(file);
            }
        });
        return list;
    }
}
