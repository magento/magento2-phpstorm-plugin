/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.js.RequireJsIndex;
import com.magento.idea.magento2plugin.reference.provider.util.GetAllSubFilesOfVirtualFileUtil;
import com.magento.idea.magento2plugin.reference.provider.util.GetFilePathUtil;
import com.magento.idea.magento2plugin.reference.provider.util.GetModuleNameUtil;
import com.magento.idea.magento2plugin.reference.provider.util.GetModuleSourceFilesUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsIndex {

    private static JsIndex INSTANCE;

    private JsIndex() {
    }

    public static JsIndex getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new JsIndex();
        }
        return INSTANCE;
    }

    public PsiReference[] getRequireJsPreferences(@NotNull PsiElement element, final GlobalSearchScope scope) {
        String originalValue = element.getText();
        String jsKey = originalValue
                .replace("'", "")
                .replace("\"", "");

        Collection<String> values =
                FileBasedIndex.getInstance().getValues(RequireJsIndex.KEY, jsKey, scope);
        PsiManager psiManager = PsiManager.getInstance(element.getProject());

        List<PsiElement> targets = new ArrayList<>();
        if (values.isEmpty()) {
            Collection<VirtualFile> files = LibJsIndex.getInstance().getLibJsFiles(jsKey, element.getProject());
            convertVirtualFilesToPsiElements(psiManager, targets, jsKey, files);
        } else {
            for (String value : values) {
                String filePath = GetFilePathUtil.getInstance().execute(value);
                if (null == filePath) {
                    continue;
                }
                Collection<VirtualFile> files = getJsFilesByPath(value, element.getProject());

                convertVirtualFilesToPsiElements(psiManager, targets, filePath, files);
            }
        }

        if (targets.isEmpty()) {
            return PsiReference.EMPTY_ARRAY;
        }

        return new PsiReference[] {
                new PolyVariantReferenceBase(element, targets)
        };
    }

    private void convertVirtualFilesToPsiElements(PsiManager psiManager, List<PsiElement> targets, String filePath, Collection<VirtualFile> files) {
        for (VirtualFile file : files) {
            String fileUrl = file.getUrl();
            if (!fileUrl.contains(filePath)) {
                return;
            }

            PsiElement psiElement = psiManager.findFile(file);
            if (null != psiElement) {
                targets.add(psiElement);
            }
        }
    }

    private Collection<VirtualFile> getJsFilesByPath(@NotNull String filePath, @NotNull Project project)
    {
        Collection<VirtualFile> virtualFiles = new ArrayList<>();
        Collection<VirtualFile> files = new ArrayList<>();

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1).concat(".js").replace("'", "");
        String moduleName = GetModuleNameUtil.getInstance().execute(filePath);
        if (moduleName == null) {
            virtualFiles.addAll(LibJsIndex.getInstance().getLibJsFiles(filePath, project));
        } else {
            virtualFiles.addAll(GetModuleSourceFilesUtil.getInstance().execute(filePath, project));
        }
        if (!virtualFiles.isEmpty()) {
            for (VirtualFile virtualFile : virtualFiles) {
                Collection<VirtualFile> vfChildren = GetAllSubFilesOfVirtualFileUtil.
                        getInstance().execute(virtualFile);
                if (null != vfChildren) {
                    vfChildren.removeIf(file -> {
                        if (!file.isDirectory()) {
                            String name = file.getName();
                            return !name.equals(fileName);
                        }
                        return true;
                    });
                    files.addAll(vfChildren);
                }
            }
        }
        return files;
    }
}
