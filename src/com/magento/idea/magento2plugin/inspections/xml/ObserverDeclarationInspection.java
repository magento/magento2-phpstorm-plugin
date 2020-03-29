/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.magento.idea.magento2plugin.indexes.EventIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleXml;
import com.magento.idea.magento2plugin.magento.packages.Package;
import org.jetbrains.annotations.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.Nullable;

public class ObserverDeclarationInspection extends PhpInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {
        return new XmlElementVisitor() {
            private final String moduleXmlFileName = ModuleXml.getInstance().getFileName();
            private static final String eventsXmlFileName = "events.xml";
            private static final String duplicatedObserverNameSameFileProblemDescription = "The observer name already used in this file. For more details see Inspection Description.";
            private static final String duplicatedObserverNameProblemDescription =
                    "The observer name \"%s\" for event \"%s\" is already used in the module \"%s\" (%s scope). For more details see Inspection Description.";
            private HashMap<String, VirtualFile> loadedFileHash = new HashMap<>();
            private final ProblemHighlightType errorSeverity = ProblemHighlightType.WARNING;

            @Override
            public void visitFile(PsiFile file) {
                if (!file.getName().equals(eventsXmlFileName)) {
                    return;
                }

                XmlTag[] xmlTags = getFileXmlTags(file);
                EventIndex eventIndex = EventIndex.getInstance(file.getProject());

                if (xmlTags == null) {
                    return;
                }

                HashMap<String, XmlTag> targetObserversHash = new HashMap<>();

                for (XmlTag eventXmlTag: xmlTags) {
                    HashMap<String, XmlTag> eventProblems = new HashMap<>();
                    if (!eventXmlTag.getName().equals("event")) {
                        continue;
                    }

                    XmlAttribute eventNameAttribute = eventXmlTag.getAttribute("name");

                    String eventNameAttributeValue = eventNameAttribute.getValue();
                    if (eventNameAttributeValue == null) {
                        continue;
                    }

                    List<XmlTag> targetObservers = fetchObserverTagsFromEventTag(eventXmlTag);

                    for (XmlTag observerXmlTag: targetObservers) {
                        XmlAttribute observerNameAttribute = observerXmlTag.getAttribute("name");
                        XmlAttribute observerDisabledAttribute = observerXmlTag.getAttribute("disabled");

                        if (observerNameAttribute == null || (observerDisabledAttribute != null && observerDisabledAttribute.getValue().equals("true"))) {
                            continue;
                        }

                        String observerName = observerNameAttribute.getValue();
                        String observerKey = eventNameAttributeValue.concat("_").concat(observerName);
                        if (targetObserversHash.containsKey(observerKey)) {
                            problemsHolder.registerProblem(
                                observerNameAttribute.getValueElement(),
                                duplicatedObserverNameSameFileProblemDescription,
                                errorSeverity
                            );
                        }
                        targetObserversHash.put(observerKey, observerXmlTag);

                        List<HashMap<String, String>> modulesWithSameObserverName = fetchModuleNamesWhereSameObserverNameUsed(eventNameAttributeValue, observerName, eventIndex, file);
                        for (HashMap<String, String> moduleEntry: modulesWithSameObserverName) {
                            Map.Entry<String, String> module = moduleEntry.entrySet().iterator().next();
                            String moduleName = module.getKey();
                            String scope = module.getValue();
                            String problemKey = observerKey.concat("_").concat(moduleName).concat("_").concat(scope);
                            if (!eventProblems.containsKey(problemKey)){
                                problemsHolder.registerProblem(
                                    observerNameAttribute.getValueElement(),
                                    String.format(
                                        duplicatedObserverNameProblemDescription,
                                        observerName,
                                        eventNameAttributeValue,
                                        moduleName,
                                        scope
                                    ),
                                    errorSeverity
                                );
                                eventProblems.put(problemKey, observerXmlTag);
                            }
                        }
                    }
                }
            }

            private List<HashMap<String, String>> fetchModuleNamesWhereSameObserverNameUsed(String eventNameAttributeValue, String observerName, EventIndex eventIndex, PsiFile file) {
                List<HashMap<String, String>> modulesName = new ArrayList<>();
                String currentFileDirectory = file.getContainingDirectory().toString();
                String currentFileFullPath = currentFileDirectory.concat("/").concat(file.getName());

                Collection<PsiElement> indexedEvents = eventIndex.getEventElements(eventNameAttributeValue, GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(file.getProject()),
                        XmlFileType.INSTANCE
                ));

                for (PsiElement indexedEvent: indexedEvents) {
                    PsiFile indexedAttributeParent = PsiTreeUtil.getTopmostParentOfType(indexedEvent, PsiFile.class);
                    if (indexedAttributeParent == null) {
                        continue;
                    }

                    String indexedEventAttributeValue = ((XmlAttributeValue) indexedEvent).getValue();
                    if (!indexedEventAttributeValue.equals(eventNameAttributeValue)) {
                        continue;
                    }

                    String indexedFileDirectory = indexedAttributeParent.getContainingDirectory().toString();
                    String indexedFileFullPath = indexedFileDirectory.concat("/").concat(indexedAttributeParent.getName());
                    if (indexedFileFullPath.equals(currentFileFullPath)) {
                        continue;
                    }

                    String scope = getAreaFromFileDirectory(indexedAttributeParent);

                    List<XmlTag> indexObserversTags = fetchObserverTagsFromEventTag((XmlTag) indexedEvent.getParent().getParent());
                    for (XmlTag indexObserversTag: indexObserversTags) {
                        XmlAttribute indexedObserverNameAttribute = indexObserversTag.getAttribute("name");
                        if (indexedObserverNameAttribute == null) {
                            continue;
                        }
                        if (!observerName.equals(indexedObserverNameAttribute.getValue())){
                            continue;
                        }
                        addModuleNameWhereSameObserverUsed(modulesName, indexedAttributeParent, scope);
                    }
                }

                return modulesName;
            }

            private List<XmlTag> fetchObserverTagsFromEventTag(XmlTag eventXmlTag) {
                List<XmlTag> result = new ArrayList<>();
                XmlTag[] observerXmlTags = PsiTreeUtil.getChildrenOfType(eventXmlTag, XmlTag.class);
                if (observerXmlTags == null) {
                    return result;
                }

                for (XmlTag observerXmlTag: observerXmlTags) {
                    if (!observerXmlTag.getName().equals("observer")) {
                        continue;
                    }

                    result.add(observerXmlTag);
                }

                return result;
            }

            private void addModuleNameWhereSameObserverUsed(List<HashMap<String, String>> modulesName, PsiFile indexedFile, String scope) {
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
