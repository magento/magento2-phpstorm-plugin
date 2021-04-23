/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
public class PreferenceDeclarationInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {
            private final InspectionBundle inspectionBundle = new InspectionBundle();

            @Override
            public void visitFile(final @NotNull PsiFile file) {
                if (!file.getName().equals(ModuleDiXml.FILE_NAME)) {
                    return;
                }

                final XmlTag[] xmlTags = getFileXmlTags(file);

                if (xmlTags == null) {
                    return;
                }

                for (final XmlTag preferenceXmlTag : xmlTags) {
                    if (!preferenceXmlTag.getName().equals(ModuleDiXml.PREFERENCE_TAG_NAME)) {
                        continue;
                    }

                    final XmlAttribute preferenceForAttribute =
                            preferenceXmlTag.getAttribute(ModuleDiXml.PREFERENCE_ATTR_FOR);
                    final XmlAttribute preferenceTypeAttribute =
                            preferenceXmlTag.getAttribute(ModuleDiXml.TYPE_ATTR);

                    if (preferenceForAttribute == null || preferenceTypeAttribute == null) {
                        continue;
                    }

                    final Boolean isPreferenceForClassExists =
                            isXmlAttributeValueClassExists(preferenceForAttribute);

                    if (isPreferenceForClassExists != null && !isPreferenceForClassExists) {
                        reportClassDoesntExistsProblem(preferenceForAttribute);
                    }

                    final Boolean isPreferenceTypeClassExists =
                            isXmlAttributeValueClassExists(preferenceTypeAttribute);

                    if (isPreferenceTypeClassExists != null && !isPreferenceTypeClassExists) {
                        reportClassDoesntExistsProblem(preferenceTypeAttribute);
                    }
                }
            }

            /**
             * Output a warning in the xml class.
             *
             * @param xmlAttribute XmlAttribute
             */
            private void reportClassDoesntExistsProblem(final @NotNull XmlAttribute xmlAttribute) {
                if (xmlAttribute.getValueElement() == null) {
                    return;
                }
                problemsHolder.registerProblem(
                        xmlAttribute.getValueElement(),
                        inspectionBundle.message("inspection.preference.error.notExist"),
                        ProblemHighlightType.WARNING
                );
            }

            /**
             * Checks the existence of the php class in the specified directory.
             *
             * @param xmlAttribute XmlAttribute
             *
             * @return attributeValueClass
             */
            private @Nullable Boolean isXmlAttributeValueClassExists(
                    final @NotNull XmlAttribute xmlAttribute
            ) {
                final String attributeValue = xmlAttribute.getValue();

                if (attributeValue == null) {
                    return null;
                }

                final PhpClass attributeValueClass =
                        GetPhpClassByFQN.getInstance(xmlAttribute.getProject())
                                .execute(attributeValue);

                return attributeValueClass != null;
            }

            /**
             * Get all children xml tags for root xml tag of file.
             *
             * @param file PsiFile
             *
             * @return XmlTag[]
             */
            private @Nullable XmlTag[] getFileXmlTags(final @NotNull PsiFile file) {
                final XmlDocument xmlDocument = PsiTreeUtil.getChildOfType(file, XmlDocument.class);
                final XmlTag xmlRootTag = PsiTreeUtil.getChildOfType(xmlDocument, XmlTag.class);

                return PsiTreeUtil.getChildrenOfType(xmlRootTag, XmlTag.class);
            }
        };
    }
}
