/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import java.util.List;
import java.util.Map;

public final class CommitXmlFileUtil {

    private CommitXmlFileUtil() {}

    /**
     * Adjusts tags to XML file and commit.
     *
     * @param xmlFile XmlFile
     * @param subTags List[XmlTag]
     * @param childParentRelationMap Map
     * @return XmlFile
     */
    public static XmlFile execute(
            final XmlFile xmlFile,
            final List<XmlTag> subTags,
            final Map<XmlTag, XmlTag> childParentRelationMap
    ) {
        WriteCommandAction.runWriteCommandAction(xmlFile.getProject(), () -> {
            for (final XmlTag tag : Lists.reverse(subTags)) {
                if (childParentRelationMap.containsKey(tag)) {
                    final XmlTag parent = childParentRelationMap.get(tag);
                    parent.addSubTag(tag, false);
                }
            }
        });
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(
                xmlFile.getProject()
        );
        final Document document = psiDocumentManager.getDocument(xmlFile);

        if (document != null) {
            psiDocumentManager.commitDocument(document);
        }
        return xmlFile;
    }

    /**
     * Make some XML editing operation in the safe env.
     *
     * @param xmlFile XmlFile
     * @param runnable Runnable
     */
    public static void execute(
            final XmlFile xmlFile,
            final Runnable runnable
    ) {
        WriteCommandAction.runWriteCommandAction(xmlFile.getProject(), runnable);

        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(
                xmlFile.getProject()
        );
        final Document document = psiDocumentManager.getDocument(xmlFile);

        if (document != null) {
            psiDocumentManager.commitDocument(document);
        }
    }
}
