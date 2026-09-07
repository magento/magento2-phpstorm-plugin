/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.indexes.PluginIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
public class PluginDeclarationInspection extends PhpInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {
            private InspectionBundle inspectionBundle = new InspectionBundle();
            private final ProblemHighlightType errorSeverity = ProblemHighlightType.WARNING;

            @Override
            public void visitFile(final PsiFile file) {
                if (!file.getName().equals(ModuleDiXml.FILE_NAME)) {
                    return;
                }

                final XmlTag[] xmlTags = getFileXmlTags(file);

                if (xmlTags == null) {
                    return;
                }

                final HashMap<String, XmlTag> targetPluginHash = new HashMap<>();
                final PluginIndex pluginIndex = PluginIndex.getInstance(file.getProject());
                final HashMap<String, XmlTag> pluginProblems = new HashMap<>();

                for (final XmlTag pluginXmlTag: xmlTags) {
                    pluginProblems.clear();
                    if (!pluginXmlTag.getName().equals(ModuleDiXml.TYPE_TAG)) {
                        continue;
                    }

                    final XmlAttribute pluginNameAttribute =
                            pluginXmlTag.getAttribute(ModuleDiXml.NAME_ATTR);
                    if (pluginNameAttribute == null) {
                        continue;
                    }

                    final String pluginNameAttributeValue = pluginNameAttribute.getValue();
                    if (pluginNameAttributeValue == null) {
                        continue;
                    }

                    final List<XmlTag> targetPlugin = fetchPluginTagsFromPluginTag(pluginXmlTag);

                    for (final XmlTag pluginTypeXmlTag: targetPlugin) {
                        final XmlAttribute pluginTypeNameAttribute
                                = pluginTypeXmlTag.getAttribute(ModuleDiXml.NAME_ATTR);
                        final XmlAttribute pluginTypeDisabledAttribute
                                = pluginTypeXmlTag.getAttribute(ModuleDiXml.DISABLED_ATTR_NAME);

                        if (pluginTypeNameAttribute == null) {
                            continue;
                        }

                        final String pluginTypeName = pluginTypeNameAttribute.getValue();
                        if (pluginTypeName == null) {
                            continue;
                        }

                        final String pluginTypeKey = pluginNameAttributeValue.concat(
                                Package.vendorModuleNameSeparator
                        ).concat(pluginTypeName);
                        if (targetPluginHash.containsKey(pluginTypeKey)) {
                            problemsHolder.registerProblem(
                                    pluginTypeNameAttribute.getValueElement(),
                                    inspectionBundle.message(
                                            "inspection.plugin.duplicateInSameFile"
                                    ),
                                    errorSeverity
                            );
                        }
                        targetPluginHash.put(pluginTypeKey, pluginTypeXmlTag);

                        final List<Pair<String, String>> modulesWithSamePluginName =
                                fetchModuleNamesWhereSamePluginNameUsed(
                                    pluginNameAttributeValue,
                                    pluginTypeName,
                                    pluginIndex,
                                    file
                        );
                        final XmlAttribute pluginTypeAttribute =
                                pluginTypeXmlTag.getAttribute(ModuleDiXml.TYPE_ATTR);

                        if (pluginTypeDisabledAttribute != null
                                && pluginTypeAttribute == null
                                && pluginTypeDisabledAttribute.getValue() != null
                                && pluginTypeDisabledAttribute.getValue().equals("true")
                                && !pluginTypeName.isEmpty()
                        ) {
                            @Nullable final XmlAttributeValue valueElement
                                    = pluginTypeNameAttribute.getValueElement();
                            if (modulesWithSamePluginName.isEmpty() && valueElement != null) {
                                problemsHolder.registerProblem(
                                            valueElement,
                                            inspectionBundle.message(
                                                "inspection.plugin.disabledPluginDoesNotExist"
                                            ),
                                            errorSeverity
                                );
                            } else {
                                continue;
                            }
                        }

                        for (final Pair<String, String> moduleEntry: modulesWithSamePluginName) {
                            final String scope = moduleEntry.getFirst();
                            final String moduleName = moduleEntry.getSecond();
                            if (scope == null || moduleName == null) {
                                continue;
                            }
                            final String problemKey = pluginTypeKey.concat(
                                    Package.vendorModuleNameSeparator
                            ).concat(moduleName)
                                    .concat(Package.vendorModuleNameSeparator).concat(scope);
                            if (!pluginProblems.containsKey(problemKey)) {
                                problemsHolder.registerProblem(
                                        pluginTypeNameAttribute.getValueElement(),
                                        inspectionBundle.message(
                                            "inspection.plugin.duplicateInOtherPlaces",
                                             pluginTypeName,
                                            pluginNameAttributeValue,
                                            moduleName,
                                            scope
                                         ),
                                        errorSeverity
                                );
                                pluginProblems.put(problemKey, pluginTypeXmlTag);
                            }
                        }
                    }
                }
            }

            private List<Pair<String, String>> fetchModuleNamesWhereSamePluginNameUsed(
                    final String pluginNameAttributeValue,
                    final String pluginTypeName,
                    final PluginIndex pluginIndex,
                    final PsiFile file
            ) {
                final List<Pair<String, String>> modulesName = new ArrayList<>();
                final PsiDirectory parentDirectory = file.getContainingDirectory();

                if (parentDirectory == null) {
                    return modulesName;
                }
                final String currentFileDirectory = file.getContainingDirectory().toString();
                final String currentFileFullPath =
                        currentFileDirectory.concat(File.separator).concat(file.getName());

                final Collection<PsiElement> indexedPlugins =
                        pluginIndex.getPluginElements(
                                pluginNameAttributeValue,
                                GlobalSearchScope.getScopeRestrictedByFileTypes(
                                        GlobalSearchScope.allScope(file.getProject()),
                                        XmlFileType.INSTANCE
                ));

                for (final PsiElement indexedPlugin: indexedPlugins) {
                    final PsiFile indexedAttributeParent =
                            PsiTreeUtil.getTopmostParentOfType(indexedPlugin, PsiFile.class);
                    if (indexedAttributeParent == null) {
                        continue;
                    }

                    final String indexedPluginAttributeValue =
                            ((XmlAttributeValue) indexedPlugin).getValue();
                    if (!indexedPluginAttributeValue.equals(pluginNameAttributeValue)) {
                        continue;
                    }

                    final String indexedFileDirectory =
                            indexedAttributeParent.getContainingDirectory().toString();
                    final String indexedFileFullPath =
                            indexedFileDirectory.concat(File.separator)
                                    .concat(indexedAttributeParent.getName());
                    if (indexedFileFullPath.equals(currentFileFullPath)) {
                        continue;
                    }

                    final String scope = getAreaFromFileDirectory(indexedAttributeParent);

                    final List<XmlTag> indexPluginTags =
                            fetchPluginTagsFromPluginTag((XmlTag) indexedPlugin
                                    .getParent().getParent());
                    for (final XmlTag indexPluginTag: indexPluginTags) {
                        final XmlAttribute indexedPluginNameAttribute =
                                indexPluginTag.getAttribute(ModuleDiXml.NAME_ATTR);
                        if (indexedPluginNameAttribute == null) {
                            continue;
                        }
                        if (!pluginTypeName.equals(indexedPluginNameAttribute.getValue())) {
                            continue;
                        }
                        addModuleNameWhereSamePluginUsed(
                                modulesName,
                                indexedAttributeParent,
                                scope
                        );
                    }
                }

                return modulesName;
            }

            private List<XmlTag> fetchPluginTagsFromPluginTag(final XmlTag pluginXmlTag) {
                final List<XmlTag> result = new ArrayList<>();
                final XmlTag[] pluginTypeXmlTags =
                        PsiTreeUtil.getChildrenOfType(pluginXmlTag, XmlTag.class);
                if (pluginTypeXmlTags == null) {
                    return result;
                }

                for (final XmlTag pluginTypeXmlTag: pluginTypeXmlTags) {
                    if (!pluginTypeXmlTag.getName().equals(ModuleDiXml.PLUGIN_TAG_NAME)) {
                        continue;
                    }

                    result.add(pluginTypeXmlTag);
                }

                return result;
            }

            private void addModuleNameWhereSamePluginUsed(
                    final List<Pair<String, String>> modulesName,
                    final PsiFile indexedFile,
                    final String scope
            ) {
                final String moduleName = GetModuleNameByDirectoryUtil
                        .execute(
                                indexedFile.getContainingDirectory(),
                                problemsHolder.getProject()
                        );

                modulesName.add(Pair.create(scope, moduleName));
            }

            @Nullable
            private XmlTag[] getFileXmlTags(final PsiFile file) {
                final XmlDocument xmlDocument = PsiTreeUtil.getChildOfType(file, XmlDocument.class);
                final XmlTag xmlRootTag = PsiTreeUtil.getChildOfType(xmlDocument, XmlTag.class);
                return PsiTreeUtil.getChildrenOfType(xmlRootTag, XmlTag.class);
            }

            private String getAreaFromFileDirectory(final @NotNull PsiFile file) {
                if (file.getParent() == null) {
                    return "";
                }

                final String areaFromFileDirectory = file.getParent().getName();

                if (areaFromFileDirectory.equals(Package.moduleBaseAreaDir)) {
                    return Areas.base.toString();
                }

                for (final Areas area: Areas.values()) {
                    if (area.toString().equals(areaFromFileDirectory)) {
                        return area.toString();
                    }
                }

                return "";
            }
        };
    }
}
