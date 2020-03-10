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
import org.jetbrains.annotations.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.Nullable;

public class ObserverDeclarationInspection extends PhpInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {
        return new XmlElementVisitor() {
            private static final String duplicatedObserverNameProblemDescription = "Duplicated observer name!";
            private static final String moduleXmlFileName = "module.xml";
            private static final String eventsXmlFileName = "events.xml";
            private static final String notSequencedModuleProblemDescription =
                    "Observer name \"%s\" for event \"%s\" is already declared in the module \"%s\" for the current area. In case you want to override the implementation make sure the \"%s\" module is added to sequences in the current module \"module.xml\".";
            private HashMap<String, VirtualFile> loadedFileHash = new HashMap<>();

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
                        if (observerNameAttribute == null) {
                            continue;
                        }

                        String observerName = observerNameAttribute.getValue();
                        String observerKey = eventNameAttributeValue.concat("_").concat(observerName);
                        if (targetObserversHash.containsKey(observerKey)) {
                            problemsHolder.registerProblem(observerNameAttribute.getValueElement(), duplicatedObserverNameProblemDescription, ProblemHighlightType.ERROR);
                        }
                        targetObserversHash.put(observerKey, observerXmlTag);

                        List<String> moduleNamesToBeSequencedOn = fetchModuleNamesToBeSequencedOn(eventNameAttributeValue, observerName, eventIndex, file);
                        if (!moduleNamesToBeSequencedOn.isEmpty()) {
                            List<String> sequencedModuleNames = getSequencedModulesByConfigFile(file);
                            for (String moduleNameToBeSequencedOn: moduleNamesToBeSequencedOn) {
                                String problemKey = observerKey.concat("_").concat(moduleNameToBeSequencedOn);
                                if (!sequencedModuleNames.contains(moduleNameToBeSequencedOn) && !eventProblems.containsKey(problemKey)){
                                    problemsHolder.registerProblem(observerNameAttribute.getValueElement(), String.format(notSequencedModuleProblemDescription, observerName, eventNameAttributeValue, moduleNameToBeSequencedOn, moduleNameToBeSequencedOn), ProblemHighlightType.ERROR);
                                    eventProblems.put(problemKey, observerXmlTag);
                                }
                            }
                        }
                    }
                }
            }

            private List<String> fetchModuleNamesToBeSequencedOn(String eventNameAttributeValue, String observerName, EventIndex eventIndex, PsiFile file) {
                List<String> moduleDeclarationToBeSequencedOn = new ArrayList<>();
                String currentFileDirectory = file.getContainingDirectory().toString();
                String currentFileFullPath = currentFileDirectory.concat("/").concat(file.getName());
                String currentFileArea = file.getContainingDirectory().getName();

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
                    String indexedFileArea = indexedAttributeParent.getContainingDirectory().getName();
                    if (indexedFileFullPath.equals(currentFileFullPath)) {
                        continue;
                    }
                    if (!indexedFileArea.equals(currentFileArea)) {
                        continue;
                    }

                    List<XmlTag> indexObserversTags = fetchObserverTagsFromEventTag((XmlTag) indexedEvent.getParent().getParent());
                    for (XmlTag indexObserversTag: indexObserversTags) {
                        XmlAttribute indexedObserverNameAttribute = indexObserversTag.getAttribute("name");
                        if (indexedObserverNameAttribute == null) {
                            continue;
                        }
                        if (!observerName.equals(indexedObserverNameAttribute.getValue())){
                            continue;
                        }
                        addModuleNameToBeSequencedOn(moduleDeclarationToBeSequencedOn, indexedAttributeParent);
                    }
                }

                return moduleDeclarationToBeSequencedOn;
            }

            private List<String> getSequencedModulesByConfigFile(PsiFile file) {
                List<String> sequencedModules = new ArrayList<>();
                XmlTag currentModuleDeclaration = getModuleDeclarationTagByConfigFile(file);
                if (currentModuleDeclaration == null) {
                    return sequencedModules;
                }
                XmlTag[] moduleDeclarationChildren = PsiTreeUtil.getChildrenOfType(currentModuleDeclaration, XmlTag.class);
                if (moduleDeclarationChildren == null) {
                    return sequencedModules;
                }
                for (XmlTag moduleDeclarationChild: moduleDeclarationChildren) {
                    if (!moduleDeclarationChild.getName().equals("sequence")) {
                        continue;
                    }
                    XmlTag[] sequenceDeclarationChildren = PsiTreeUtil.getChildrenOfType(moduleDeclarationChild, XmlTag.class);
                    if (sequenceDeclarationChildren == null) {
                        continue;
                    }
                    for (XmlTag sequenceDeclarationChild: sequenceDeclarationChildren) {
                        if (!sequenceDeclarationChild.getName().equals("module")) {
                            continue;
                        }

                        XmlAttribute sequencedModuleNameAttribute = sequenceDeclarationChild.getAttribute("name");
                        if (sequencedModuleNameAttribute == null) {
                            continue;
                        }

                        sequencedModules.add(sequencedModuleNameAttribute.getValue());
                    }
                }
                return sequencedModules;
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

            private void addModuleNameToBeSequencedOn(List<String> moduleDeclarationToBeSequencedOn, PsiFile indexedFile) {
                XmlTag moduleDeclarationTag = getModuleDeclarationTagByConfigFile(indexedFile);
                if (moduleDeclarationTag == null) return;

                if (!moduleDeclarationTag.getName().equals("module")) {
                    return;
                }
                XmlAttribute moduleNameAttribute = moduleDeclarationTag.getAttribute("name");
                if (moduleNameAttribute == null) {
                    return;
                }
                moduleDeclarationToBeSequencedOn.add(moduleNameAttribute.getValue());
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
        };
    }
}
