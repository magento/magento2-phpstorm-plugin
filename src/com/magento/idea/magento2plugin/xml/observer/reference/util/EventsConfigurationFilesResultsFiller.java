package com.magento.idea.magento2plugin.xml.observer.reference.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.resolve.PhpResolveResult;
import com.magento.idea.magento2plugin.xml.observer.index.EventObserverFileBasedIndex;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;

import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/3/15.
 */
public class EventsConfigurationFilesResultsFiller implements ReferenceResultsFiller {
    public static final ReferenceResultsFiller INSTANCE = new EventsConfigurationFilesResultsFiller();

    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        Collection<VirtualFile> containingFiles = FileBasedIndex
            .getInstance()
            .getContainingFiles(
                EventObserverFileBasedIndex.NAME,
                typeName,
                GlobalSearchScope.allScope(psiElement.getProject())
            );

        PsiManager psiManager = PsiManager.getInstance(psiElement.getProject());
        for (VirtualFile virtualFile: containingFiles) {
            XmlFile xmlFile = (XmlFile)psiManager.findFile(virtualFile);
            if (xmlFile != null) {
                results.add(new PhpResolveResult(xmlFile));
            }
        }
    }
}
