/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.*;
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
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.*;
import com.intellij.openapi.util.Pair;

public class PluginDeclarationInspection extends PhpInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {
        return new XmlElementVisitor() {
            private InspectionBundle inspectionBundle = new InspectionBundle();
            private final ProblemHighlightType errorSeverity = ProblemHighlightType.WARNING;

            @Override
            public void visitFile(PsiFile file) {
                if (!file.getName().equals(ModuleDiXml.FILE_NAME)) {
                    return;
                }

                XmlTag[] xmlTags = getFileXmlTags(file);
                PluginIndex pluginIndex = PluginIndex.getInstance(file.getProject());

                if (xmlTags == null) {
                    return;
                }

                HashMap<String, XmlTag> targetPluginHash = new HashMap<>();

                for (XmlTag pluginXmlTag: xmlTags) {
                    HashMap<String, XmlTag> pluginProblems = new HashMap<>();
                    if (!pluginXmlTag.getName().equals(ModuleDiXml.PLUGIN_TYPE_TAG)) {
                        continue;
                    }

                    XmlAttribute pluginNameAttribute = pluginXmlTag.getAttribute(ModuleDiXml.PLUGIN_TYPE_ATTR_NAME);

                    String pluginNameAttributeValue = pluginNameAttribute.getValue();
                    if (pluginNameAttributeValue == null) {
                        continue;
                    }

                    List<XmlTag> targetPlugin = fetchPluginTagsFromPluginTag(pluginXmlTag);

                    for (XmlTag pluginTypeXmlTag: targetPlugin) {
                        XmlAttribute pluginTypeNameAttribute = pluginTypeXmlTag.getAttribute(ModuleDiXml.PLUGIN_TYPE_ATTR_NAME);
                        XmlAttribute pluginTypeDisabledAttribute = pluginTypeXmlTag.getAttribute(ModuleDiXml.DISABLED_ATTR_NAME);

                        if (pluginTypeNameAttribute == null ||
                            (
                                pluginTypeDisabledAttribute != null &&
                                pluginTypeDisabledAttribute.getValue() != null &&
                                pluginTypeDisabledAttribute.getValue().equals("true")
                            )
                        ) {
                            continue;
                        }

                        String pluginTypeName = pluginTypeNameAttribute.getValue();
                        String pluginTypeKey = pluginNameAttributeValue.concat(Package.vendorModuleNameSeparator).concat(pluginTypeName);
                        if (targetPluginHash.containsKey(pluginTypeKey)) {
                            problemsHolder.registerProblem(
                                pluginTypeNameAttribute.getValueElement(),
                                inspectionBundle.message("inspection.plugin.duplicateInSameFile"),
                                errorSeverity
                            );
                        }
                        targetPluginHash.put(pluginTypeKey, pluginTypeXmlTag);

                        List<Pair<String, String>> modulesWithSamePluginName = fetchModuleNamesWhereSamePluginNameUsed(pluginNameAttributeValue, pluginTypeName, pluginIndex, file);
                        for (Pair<String, String> moduleEntry: modulesWithSamePluginName) {
                            String scope = moduleEntry.getFirst();
                            String moduleName = moduleEntry.getSecond();
                            if (scope == null || moduleName == null) {
                                continue;
                            }
                            String problemKey = pluginTypeKey.concat(Package.vendorModuleNameSeparator)
                                    .concat(moduleName).concat(Package.vendorModuleNameSeparator).concat(scope);
                            if (!pluginProblems.containsKey(problemKey)){
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

            private List<Pair<String, String>> fetchModuleNamesWhereSamePluginNameUsed(String pluginNameAttributeValue, String pluginTypeName, PluginIndex pluginIndex, PsiFile file) {
                List<Pair<String, String>> modulesName = new ArrayList<>();
                String currentFileDirectory = file.getContainingDirectory().toString();
                String currentFileFullPath = currentFileDirectory.concat(File.separator).concat(file.getName());

                Collection<PsiElement> indexedPlugins = pluginIndex.getPluginElements(pluginNameAttributeValue, GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(file.getProject()),
                        XmlFileType.INSTANCE
                ));

                for (PsiElement indexedPlugin: indexedPlugins) {
                    PsiFile indexedAttributeParent = PsiTreeUtil.getTopmostParentOfType(indexedPlugin, PsiFile.class);
                    if (indexedAttributeParent == null) {
                        continue;
                    }

                    String indexedPluginAttributeValue = ((XmlAttributeValue) indexedPlugin).getValue();
                    if (!indexedPluginAttributeValue.equals(pluginNameAttributeValue)) {
                        continue;
                    }

                    String indexedFileDirectory = indexedAttributeParent.getContainingDirectory().toString();
                    String indexedFileFullPath = indexedFileDirectory.concat(File.separator).concat(indexedAttributeParent.getName());
                    if (indexedFileFullPath.equals(currentFileFullPath)) {
                        continue;
                    }

                    String scope = getAreaFromFileDirectory(indexedAttributeParent);

                    List<XmlTag> indexPluginTags = fetchPluginTagsFromPluginTag((XmlTag) indexedPlugin.getParent().getParent());
                    for (XmlTag indexPluginTag: indexPluginTags) {
                        XmlAttribute indexedPluginNameAttribute = indexPluginTag.getAttribute(ModuleDiXml.PLUGIN_TYPE_ATTR_NAME);
                        if (indexedPluginNameAttribute == null) {
                            continue;
                        }
                        if (!pluginTypeName.equals(indexedPluginNameAttribute.getValue())){
                            continue;
                        }
                        addModuleNameWhereSamePluginUsed(modulesName, indexedAttributeParent, scope);
                    }
                }

                return modulesName;
            }

            private List<XmlTag> fetchPluginTagsFromPluginTag(XmlTag pluginXmlTag) {
                List<XmlTag> result = new ArrayList<>();
                XmlTag[] pluginTypeXmlTags = PsiTreeUtil.getChildrenOfType(pluginXmlTag, XmlTag.class);
                if (pluginTypeXmlTags == null) {
                    return result;
                }

                for (XmlTag pluginTypeXmlTag: pluginTypeXmlTags) {
                    if (!pluginTypeXmlTag.getName().equals(ModuleDiXml.PLUGIN_TAG_NAME)) {
                        continue;
                    }

                    result.add(pluginTypeXmlTag);
                }

                return result;
            }

            private void addModuleNameWhereSamePluginUsed(
                List<Pair<String, String>> modulesName,
                PsiFile indexedFile,
                String scope
            ) {
                String moduleName = GetModuleNameByDirectory.getInstance(problemsHolder.getProject())
                        .execute(indexedFile.getContainingDirectory());

                modulesName.add(Pair.create(scope, moduleName));
            }

            @Nullable
            private XmlTag[] getFileXmlTags(PsiFile file) {
                XmlDocument xmlDocument = PsiTreeUtil.getChildOfType(file, XmlDocument.class);
                XmlTag xmlRootTag = PsiTreeUtil.getChildOfType(xmlDocument, XmlTag.class);
                return PsiTreeUtil.getChildrenOfType(xmlRootTag, XmlTag.class);
            }

            private String getAreaFromFileDirectory(@NotNull PsiFile file) {
                if (file.getParent() == null) {
                    return "";
                }

                String areaFromFileDirectory = file.getParent().getName();

                if (areaFromFileDirectory.equals(Package.moduleBaseAreaDir)) {
                    return Areas.base.toString();
                }

                for (Areas area: Areas.values()) {
                    if (area.toString().equals(areaFromFileDirectory)) {
                        return area.toString();
                    }
                }

                return "";
            }
        };
    }
}
