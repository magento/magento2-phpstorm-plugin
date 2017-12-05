package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.EventNameIndex;
import com.magento.idea.magento2plugin.xml.XmlPsiTreeUtil;

import java.util.ArrayList;
import java.util.Collection;

public class EventIndex {

    private static EventIndex INSTANCE;

    private Project project;

    private EventIndex() {
    }

    public static EventIndex getInstance(final Project project) {
        if (null == INSTANCE) {
            INSTANCE = new EventIndex();
        }
        INSTANCE.project = project;

        return INSTANCE;
    }

    public Collection<PsiElement> getEventElements(final String name, final GlobalSearchScope scope) {
        Collection<PsiElement> result = new ArrayList<>();

        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(EventNameIndex.KEY, name, scope);

        for (VirtualFile virtualFile : virtualFiles) {
            XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(virtualFile);
            Collection<XmlAttributeValue> valueElements = XmlPsiTreeUtil
                    .findAttributeValueElements(xmlFile, "event", "name", name);
            result.addAll(valueElements);
        }
        return result;
    }
}
