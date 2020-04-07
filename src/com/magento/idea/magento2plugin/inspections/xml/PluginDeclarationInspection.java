/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.magento.idea.magento2plugin.indexes.PluginIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleXml;
import com.magento.idea.magento2plugin.magento.packages.Package;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class PluginDeclarationInspection extends PhpInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {
        return new XmlElementVisitor() {
            private final String moduleXmlFileName = ModuleXml.getInstance().getFileName();
            private static final String pluginsXmlFileName = "di.xml";
            private static final String duplicatedObserverNameSameFileProblemDescription = "The plugin name already used in this file. For more details see Inspection Description.";
            private static final String duplicatedObserverNameProblemDescription =
                    "The plugin name \"%s\" for targeted \"%s\" class is already used in the module \"%s\" (%s scope). For more details see Inspection Description.";
            private HashMap<String, VirtualFile> loadedFileHash = new HashMap<>();
            private final ProblemHighlightType errorSeverity = ProblemHighlightType.WARNING;

            @Override
            public void visitFile(PsiFile file) {
                if (!file.getName().equals(pluginsXmlFileName)) {
                    return;
                }

                XmlTag[] xmlTags = getFileXmlTags(file);
                PluginIndex pluginIndex = PluginIndex.getInstance(file.getProject());

                if (xmlTags == null) {
                    return;
                }

                HashMap<String, XmlTag> targetObserversHash = new HashMap<>();

                for (XmlTag pluginXmlTag: xmlTags) {
                    HashMap<String, XmlTag> pluginProblems = new HashMap<>();
                    if (!pluginXmlTag.getName().equals("type")) {
                        continue;
                    }

                    XmlAttribute pluginNameAttribute = pluginXmlTag.getAttribute("name");

                    String pluginNameAttributeValue = pluginNameAttribute.getValue();
                    if (pluginNameAttributeValue == null) {
                        continue;
                    }

                    List<XmlTag> targetObservers = fetchObserverTagsFromPluginTag(pluginXmlTag);

                    for (XmlTag pluginTypeXmlTag: targetObservers) {
                        XmlAttribute pluginTypeNameAttribute = pluginTypeXmlTag.getAttribute("name");
                        XmlAttribute pluginTypeDisabledAttribute = pluginTypeXmlTag.getAttribute("disabled");

                        if (pluginTypeNameAttribute == null || (pluginTypeDisabledAttribute != null && pluginTypeDisabledAttribute.getValue().equals("true"))) {
                            continue;
                        }

                        String pluginTypeName = pluginTypeNameAttribute.getValue();
                        String pluginTypeKey = pluginNameAttributeValue.concat("_").concat(pluginTypeName);
                        if (targetObserversHash.containsKey(pluginTypeKey)) {
                            problemsHolder.registerProblem(
                                pluginTypeNameAttribute.getValueElement(),
                                duplicatedObserverNameSameFileProblemDescription,
                                errorSeverity
                            );
                        }
                        targetObserversHash.put(pluginTypeKey, pluginTypeXmlTag);

                        List<HashMap<String, String>> modulesWithSameObserverName = fetchModuleNamesWhereSamePluginNameUsed(pluginNameAttributeValue, pluginTypeName, pluginIndex, file);
                        for (HashMap<String, String> moduleEntry: modulesWithSameObserverName) {
                            Map.Entry<String, String> module = moduleEntry.entrySet().iterator().next();
                            String moduleName = module.getKey();
                            String scope = module.getValue();
                            String problemKey = pluginTypeKey.concat("_").concat(moduleName).concat("_").concat(scope);
                            if (!pluginProblems.containsKey(problemKey)){
                                problemsHolder.registerProblem(
                                    pluginTypeNameAttribute.getValueElement(),
                                    String.format(
                                        duplicatedObserverNameProblemDescription,
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

            private List<HashMap<String, String>> fetchModuleNamesWhereSamePluginNameUsed(String pluginNameAttributeValue, String pluginTypeName, PluginIndex pluginIndex, PsiFile file) {
                List<HashMap<String, String>> modulesName = new ArrayList<>();
                String currentFileDirectory = file.getContainingDirectory().toString();
                String currentFileFullPath = currentFileDirectory.concat("/").concat(file.getName());

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
                    String indexedFileFullPath = indexedFileDirectory.concat("/").concat(indexedAttributeParent.getName());
                    if (indexedFileFullPath.equals(currentFileFullPath)) {
                        continue;
                    }

                    String scope = getAreaFromFileDirectory(indexedAttributeParent);

                    List<XmlTag> indexObserversTags = fetchObserverTagsFromPluginTag((XmlTag) indexedPlugin.getParent().getParent());
                    for (XmlTag indexObserversTag: indexObserversTags) {
                        XmlAttribute indexedObserverNameAttribute = indexObserversTag.getAttribute("name");
                        if (indexedObserverNameAttribute == null) {
                            continue;
                        }
                        if (!pluginTypeName.equals(indexedObserverNameAttribute.getValue())){
                            continue;
                        }
                        addModuleNameWhereSamePluginUsed(modulesName, indexedAttributeParent, scope);
                    }
                }

                return modulesName;
            }

            private List<XmlTag> fetchObserverTagsFromPluginTag(XmlTag pluginXmlTag) {
                List<XmlTag> result = new ArrayList<>();
                XmlTag[] pluginTypeXmlTags = PsiTreeUtil.getChildrenOfType(pluginXmlTag, XmlTag.class);
                if (pluginTypeXmlTags == null) {
                    return result;
                }

                for (XmlTag pluginTypeXmlTag: pluginTypeXmlTags) {
                    if (!pluginTypeXmlTag.getName().equals("plugin")) {
                        continue;
                    }

                    result.add(pluginTypeXmlTag);
                }

                return result;
            }

            private void addModuleNameWhereSamePluginUsed(List<HashMap<String, String>> modulesName, PsiFile indexedFile, String scope) {
                XmlTag moduleDeclarationTag = getModuleDeclarationTagByConfigFile(indexedFile);
                if (moduleDeclarationTag == null) return;

                if (!moduleDeclarationTag.getName().equals("module")) {
                    return;
                }
                XmlAttribute moduleNameAttribute = moduleDeclarationTag.getAttribute("name");
                if (moduleNameAttribute == null) {
                    return;
                }

                HashMap<String, String> moduleEntry = new HashMap<>();

                moduleEntry.put(moduleNameAttribute.getValue(), scope);
                modulesName.add(moduleEntry);
            }

            @Nullable
            private XmlTag getModuleDeclarationTagByConfigFile(PsiFile file) {
                String fileDirectory = file.getContainingDirectory().toString();
                String fileArea = file.getContainingDirectory().getName();
                String moduleXmlFilePath = getModuleXmlFilePathByConfigFileDirectory(fileDirectory, fileArea);

                VirtualFile virtualFile = getFileByPath(moduleXmlFilePath);
                if (virtualFile == null) return null;

                PsiFile moduleDeclarationFile = PsiManager.getInstance(file.getProject()).findFile(virtualFile);
                XmlTag[] moduleDeclarationTags = getFileXmlTags(moduleDeclarationFile);
                if (moduleDeclarationTags == null) {
                    return null;
                }
                return moduleDeclarationTags[0];
            }

            @Nullable
            private VirtualFile getFileByPath(String moduleXmlFilePath) {
                if (loadedFileHash.containsKey(moduleXmlFilePath)) {
                    return loadedFileHash.get(moduleXmlFilePath);
                }
                VirtualFile virtualFile;
                try {
                    virtualFile = VfsUtil.findFileByURL(new URL(moduleXmlFilePath));
                } catch (MalformedURLException e) {
                    return null;
                }
                if (virtualFile == null) {
                    return null;
                }
                loadedFileHash.put(moduleXmlFilePath, virtualFile);
                return virtualFile;
            }

            private String getModuleXmlFilePathByConfigFileDirectory(String fileDirectory, String fileArea) {
                String moduleXmlFile = fileDirectory.replace(fileArea, "").concat(moduleXmlFileName);
                if (fileDirectory.endsWith("etc")) {
                    moduleXmlFile = fileDirectory.concat("/").concat(moduleXmlFileName);
                }
                return moduleXmlFile.replace("PsiDirectory:", "file:");
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

                if (areaFromFileDirectory.equals(Package.MODULE_BASE_AREA_DIR)) {
                    return Package.Areas.base.toString();
                }

                for (Package.Areas area: Package.Areas.values()) {
                    if (area.toString().equals(areaFromFileDirectory)) {
                        return area.toString();
                    }
                }

                return "";
            }
        };
    }
}
