/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

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
import com.magento.idea.magento2plugin.stubs.indexes.EventObserverIndex;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;

public class EventIndex {

    private final Project project;

    /**
     * Constructor.
     */
    public EventIndex(final Project project) {
        this.project = project;
    }

    /**
     * Gets event elements by event name.
     */
    public Collection<PsiElement> getEventElements(
            final String name,
            final GlobalSearchScope scope
    ) {
        final Collection<PsiElement> result = new ArrayList<>();

        final Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(EventNameIndex.KEY, name, scope);

        for (final VirtualFile virtualFile : virtualFiles) {
            final XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(virtualFile);
            final Collection<XmlAttributeValue> valueElements = XmlPsiTreeUtil
                    .findAttributeValueElements(xmlFile, "event", "name", name);
            result.addAll(valueElements);
        }
        return result;
    }

    /**
     * Gets observers by event-observer name combination.
     */
    public Collection<PsiElement> getObservers(
            final String eventName,
            final String observerName,
            final GlobalSearchScope scope
    ) {
        final Collection<PsiElement> result = new ArrayList<>();
        final Collection<VirtualFile> virtualFiles
                = FileBasedIndex.getInstance().getContainingFiles(
                        EventObserverIndex.KEY, eventName, scope
                );

        for (final VirtualFile virtualFile: virtualFiles) {
            final XmlFile eventsXmlFile
                    = (XmlFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (eventsXmlFile != null) {
                result.addAll(
                        XmlPsiTreeUtil.findObserverTags(eventsXmlFile, eventName, observerName)
                );
            }
        }

        return result;
    }
}
