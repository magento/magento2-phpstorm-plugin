/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.magento.files.ModuleAclXml;
import com.magento.idea.magento2plugin.stubs.indexes.xml.AclResourceIndex;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AclResourceXmlInspection extends XmlSuppressableInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {
            private final InspectionBundle inspectionBundle = new InspectionBundle();

            @Override
            public void visitXmlTag(final XmlTag xmlTag) {
                final PsiFile file = xmlTag.getContainingFile();
                final String filename = file.getName();
                if (!filename.equals(ModuleAclXml.FILE_NAME)) {
                    return;
                }

                if (!xmlTag.getName().equals(ModuleAclXml.XML_TAG_RESOURCE)) {
                    return;
                }

                final XmlAttribute identifier = xmlTag.getAttribute(ModuleAclXml.XML_ATTR_ID);
                if (identifier == null) {
                    //should be handled by schema
                    return;
                }

                final String idValue = identifier.getValue();
                if (idValue == null || idValue.isEmpty()) {
                    problemsHolder.registerProblem(
                            identifier,
                            inspectionBundle.message(
                                "inspection.error.idAttributeCanNotBeEmpty",
                                "id"
                            ),
                            ProblemHighlightType.WARNING
                    );
                    return;
                }

                final XmlAttribute title = xmlTag.getAttribute(ModuleAclXml.XML_ATTR_TITLE);
                if (title != null && title.getValue() != null) {
                    return;
                }

                final List<String> titles =
                        FileBasedIndex.getInstance().getValues(
                            AclResourceIndex.KEY,
                            idValue,
                            GlobalSearchScope.allScope(file.getProject()
                            )
                        );

                if (titles.isEmpty()) {
                    problemsHolder.registerProblem(
                            identifier,
                            inspectionBundle.message(
                                "inspection.error.missingAttribute",
                                "title"
                            ),
                            ProblemHighlightType.WARNING
                    );
                }
            }
        };
    }
}
