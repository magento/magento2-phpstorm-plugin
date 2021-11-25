/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;//NOPMD

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.indexes.EventIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;
import com.magento.idea.magento2plugin.magento.files.ModuleXml;
import com.magento.idea.magento2plugin.magento.files.Observer;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
public class ObserverDeclarationInspection extends PhpInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {
            private final String moduleXmlFileName = ModuleXml.getInstance().getFileName();
            private final HashMap<String, VirtualFile> loadedFileHash = new HashMap<>();//NOPMD
            private final InspectionBundle inspectionBundle = new InspectionBundle();
            private final ProblemHighlightType errorSeverity = ProblemHighlightType.WARNING;

            @Override
            public void visitFile(final PsiFile file) {
                if (!file.getName().equals(ModuleEventsXml.FILE_NAME)) {
                    return;
                }

                final XmlTag[] xmlTags = getFileXmlTags(file);

                if (xmlTags == null) {
                    return;
                }

                final EventIndex eventIndex = EventIndex.getInstance(file.getProject());
                final HashMap<String, XmlTag> targetObserversHash = new HashMap<>();

                for (final XmlTag eventXmlTag: xmlTags) {
                    final HashMap<String, XmlTag> eventProblems = new HashMap<>();
                    if (!eventXmlTag.getName().equals("event")) {
                        continue;
                    }

                    final XmlAttribute eventNameAttribute =
                            eventXmlTag.getAttribute(Observer.NAME_ATTRIBUTE);

                    final String eventNameAttributeValue = eventNameAttribute.getValue();
                    if (eventNameAttributeValue == null) {
                        continue;
                    }

                    final List<XmlTag> targetObservers = fetchObserverTagsFromEventTag(eventXmlTag);
                    if (targetObservers.isEmpty()) {
                        continue;
                    }

                    for (final XmlTag observerXmlTag: targetObservers) {
                        final XmlAttribute observerNameAttribute =
                                observerXmlTag.getAttribute(Observer.NAME_ATTRIBUTE);
                        final XmlAttribute observerDisabledAttribute =
                                observerXmlTag.getAttribute("disabled");

                        if (observerNameAttribute == null) {
                            continue;
                        }

                        final String observerName = observerNameAttribute.getValue();
                        final String observerKey = eventNameAttributeValue.concat("_")
                                .concat(observerName);
                        if (targetObserversHash.containsKey(observerKey)) {
                            problemsHolder.registerProblem(
                                    observerNameAttribute.getValueElement(),
                                    inspectionBundle.message(
                                            "inspection.observer.duplicateInSameFile"
                                    ),
                                    errorSeverity
                            );
                        }
                        targetObserversHash.put(observerKey, observerXmlTag);

                        final List<HashMap<String, String>> modulesWithSameObserverName =
                                fetchModuleNamesWhereSameObserverNameUsed(
                                        eventNameAttributeValue,
                                        observerName,
                                        eventIndex,
                                        file
                                );

                        if (observerDisabledAttribute != null
                                && observerDisabledAttribute.getValue() != null
                                && observerDisabledAttribute.getValue().equals("true")
                                && modulesWithSameObserverName.isEmpty()
                        ) {
                            problemsHolder.registerProblem(
                                    observerNameAttribute.getValueElement(),
                                    inspectionBundle.message(
                                            "inspection.observer.disabledObserverDoesNotExist"
                                    ),
                                    errorSeverity
                            );
                        }

                        for (final HashMap<String, String> moduleEntry:
                                modulesWithSameObserverName) {
                            final Map.Entry<String, String> module = moduleEntry
                                    .entrySet().iterator().next();
                            final String moduleName = module.getKey();
                            final String scope = module.getValue();
                            final String problemKey = observerKey.concat("_")
                                    .concat(moduleName)
                                    .concat("_")
                                    .concat(scope);
                            if (!eventProblems.containsKey(problemKey)) {
                                problemsHolder.registerProblem(
                                        observerNameAttribute.getValueElement(),
                                        inspectionBundle.message(
                                            "inspection.observer.duplicateInOtherPlaces",
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

            private List<HashMap<String, String>> fetchModuleNamesWhereSameObserverNameUsed(
                    final String eventNameAttributeValue,
                    final String observerName,
                    final EventIndex eventIndex,
                    final PsiFile file
            ) {
                final List<HashMap<String, String>> modulesName = new ArrayList<>();
                final String currentFileDirectory = file.getContainingDirectory().toString();
                final String currentFileFullPath = currentFileDirectory
                        .concat("/").concat(file.getName());

                final Collection<PsiElement> indexedEvents = eventIndex.getEventElements(
                        eventNameAttributeValue,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(file.getProject()),
                        XmlFileType.INSTANCE
                ));

                for (final PsiElement indexedEvent: indexedEvents) {
                    final PsiFile indexedAttributeParent =
                            PsiTreeUtil.getTopmostParentOfType(indexedEvent, PsiFile.class);
                    if (indexedAttributeParent == null) {
                        continue;
                    }

                    final String indexedEventAttributeValue =
                            ((XmlAttributeValue) indexedEvent).getValue();
                    if (!indexedEventAttributeValue.equals(eventNameAttributeValue)) {
                        continue;
                    }

                    final String indexedFileDirectory = indexedAttributeParent
                            .getContainingDirectory().toString();
                    final String indexedFileFullPath = indexedFileDirectory.concat("/")
                            .concat(indexedAttributeParent.getName());
                    if (indexedFileFullPath.equals(currentFileFullPath)) {
                        continue;
                    }

                    final String scope = getAreaFromFileDirectory(indexedAttributeParent);

                    final List<XmlTag> indexObserversTags =
                            fetchObserverTagsFromEventTag((XmlTag) indexedEvent
                                    .getParent().getParent());
                    for (final XmlTag indexObserversTag: indexObserversTags) {
                        final XmlAttribute indexedObserverNameAttribute =
                                indexObserversTag.getAttribute("name");
                        if (indexedObserverNameAttribute == null) {
                            continue;
                        }
                        if (!observerName.equals(indexedObserverNameAttribute.getValue())) {
                            continue;
                        }
                        addModuleNameWhereSameObserverUsed(
                                modulesName,
                                indexedAttributeParent,
                                scope
                        );
                    }
                }

                return modulesName;
            }

            private List<XmlTag> fetchObserverTagsFromEventTag(final XmlTag eventXmlTag) {
                final List<XmlTag> result = new ArrayList<>();
                final XmlTag[] observerXmlTags =
                        PsiTreeUtil.getChildrenOfType(eventXmlTag, XmlTag.class);
                if (observerXmlTags == null) {
                    return result;
                }

                for (final XmlTag observerXmlTag: observerXmlTags) {
                    if (!observerXmlTag.getName().equals("observer")) {
                        continue;
                    }

                    result.add(observerXmlTag);
                }

                return result;
            }

            private void addModuleNameWhereSameObserverUsed(
                    final List<HashMap<String, String>> modulesName,
                    final PsiFile indexedFile,
                    final String scope
            ) {
                final XmlTag moduleDeclarationTag =
                        getModuleDeclarationTagByConfigFile(indexedFile);
                if (moduleDeclarationTag == null) {
                    return;
                }

                if (!moduleDeclarationTag.getName().equals("module")) {
                    return;
                }
                final XmlAttribute moduleNameAttribute = moduleDeclarationTag.getAttribute("name");
                if (moduleNameAttribute == null) {
                    return;
                }

                final HashMap<String, String> moduleEntry = new HashMap<>();

                moduleEntry.put(moduleNameAttribute.getValue(), scope);
                modulesName.add(moduleEntry);
            }

            @Nullable
            private XmlTag getModuleDeclarationTagByConfigFile(final PsiFile file) {
                final String fileDirectory = file.getContainingDirectory().toString();
                final String fileArea = file.getContainingDirectory().getName();
                final String moduleXmlFilePath =
                        getModuleXmlFilePathByConfigFileDirectory(fileDirectory, fileArea);

                final VirtualFile virtualFile = getFileByPath(moduleXmlFilePath);
                if (virtualFile == null) {
                    return null;
                }

                final PsiFile moduleDeclarationFile =
                        PsiManager.getInstance(file.getProject()).findFile(virtualFile);
                final XmlTag[] moduleDeclarationTags = getFileXmlTags(moduleDeclarationFile);
                if (moduleDeclarationTags == null) {
                    return null;
                }
                return moduleDeclarationTags[0];
            }

            @Nullable
            private VirtualFile getFileByPath(final String moduleXmlFilePath) {
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

            private String getModuleXmlFilePathByConfigFileDirectory(
                    final String fileDirectory,
                    final String fileArea
            ) {
                String moduleXmlFile = fileDirectory.replace(fileArea, "")
                        .concat(moduleXmlFileName);
                if (fileDirectory.endsWith("etc")) {
                    moduleXmlFile = fileDirectory.concat("/").concat(moduleXmlFileName);
                }
                return moduleXmlFile.replace("PsiDirectory:", "file:");
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
