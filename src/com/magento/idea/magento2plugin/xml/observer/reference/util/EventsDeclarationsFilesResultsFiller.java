package com.magento.idea.magento2plugin.xml.observer.reference.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.resolve.PhpResolveResult;
import com.magento.idea.magento2plugin.xml.observer.PhpPatternsHelper;
import com.magento.idea.magento2plugin.xml.observer.index.EventsDeclarationsFileBasedIndex;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;

import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/5/15.
 */
public class EventsDeclarationsFilesResultsFiller implements ReferenceResultsFiller {
    public static final ReferenceResultsFiller INSTANCE = new EventsDeclarationsFilesResultsFiller();

    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        Collection<VirtualFile> containingFiles = FileBasedIndex
            .getInstance()
            .getContainingFiles(
                EventsDeclarationsFileBasedIndex.NAME,
                typeName,
                GlobalSearchScope.allScope(psiElement.getProject())
            );

        PsiManager psiManager = PsiManager.getInstance(psiElement.getProject());
        for (VirtualFile virtualFile: containingFiles) {
            PhpFile phpFile = (PhpFile)psiManager.findFile(virtualFile);
            if (phpFile != null) {
                recursiveFill(results, phpFile, typeName);
            }
        }
    }

    private void recursiveFill(List<ResolveResult> results, PsiElement psiElement, String typeName) {
        if (PhpPatternsHelper.STRING_METHOD_ARGUMENT.accepts(psiElement)) {
            if (StringUtil.unquoteString(psiElement.getText()).equals(typeName)) {
                results.add(new PhpResolveResult(psiElement));
            }
            return;
        }

        for(PsiElement child: psiElement.getChildren()) {
            recursiveFill(results, child, typeName);
        }
    }
}