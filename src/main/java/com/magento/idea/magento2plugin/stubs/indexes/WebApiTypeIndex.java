/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.linemarker.xml.LineMarkerXmlTagDecorator;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Indexer for classes/interfaces which have methods exposed via Web API.
 */
public class WebApiTypeIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.webapi_type"
    );
    private final KeyDescriptor<String> keyDescriptor = new EnumeratorStringDescriptor();

    @Override
    public @NotNull ID<String, Void> getName() {
        return KEY;
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    @Override
    public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new HashMap<>();
            final PsiFile psiFile = inputData.getPsiFile();

            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }
            final XmlDocument document = ((XmlFile) psiFile).getDocument();

            if (document == null) {
                return map;
            }
            final XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(
                    psiFile.getFirstChild(),
                    XmlTag.class
            );

            if (xmlTags == null) {
                return map;
            }

            for (final XmlTag xmlTag : xmlTags) {
                if ("routes".equals(xmlTag.getName())) {
                    for (final XmlTag routeNode : xmlTag.findSubTags("route")) {
                        for (final XmlTag serviceNode : routeNode.findSubTags("service")) {
                            final String typeName = serviceNode.getAttributeValue("class");

                            if (typeName != null) {
                                map.put(PhpLangUtil.toPresentableFQN(typeName), null);
                            }
                        }
                    }
                }
            }

            return map;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return keyDescriptor;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getFileType() == XmlFileType.INSTANCE
                && "webapi".equals(file.getNameWithoutExtension())
                && !file.getPath().contains("testsuite") && !file.getPath().contains("_files");
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    /**
     * Get list of Web API routes associated with the provided method.
     * Parent classes are not taken into account.
     *
     * @param method Method
     *
     * @return List[XmlTag]
     */
    public static List<XmlTag> getWebApiRoutes(final Method method) {
        final List<XmlTag> tags = new ArrayList<>();

        if (!method.getAccess().isPublic()) {
            return tags;
        }
        final PhpClass phpClass = method.getContainingClass();

        if (phpClass == null) {
            return tags;
        }
        final String classFqn = phpClass.getPresentableFQN();
        final Collection<VirtualFile> containingFiles = FileBasedIndex
                .getInstance()
                .getContainingFiles(
                        KEY,
                        classFqn,
                        GlobalSearchScope.allScope(phpClass.getProject())
                );

        final PsiManager psiManager = PsiManager.getInstance(phpClass.getProject());
        final String methodFqn = method.getName();

        for (final VirtualFile virtualFile : containingFiles) {
            if (virtualFile.getFileType() != XmlFileType.INSTANCE) {
                continue;
            }
            final XmlFile file = (XmlFile) psiManager.findFile(virtualFile);

            if (file == null) {
                continue;
            }
            final XmlTag rootTag = file.getRootTag();

            if (rootTag == null) {
                continue;
            }
            fillRelatedTags(classFqn, methodFqn, rootTag, tags);
        }

        return tags;
    }

    /**
     * Find routes related to the specified method within single webapi.xml.
     *
     * @param classFqn String
     * @param methodFqn String
     * @param parentTag XmlTag
     * @param tagsReferences List[XmlTag]
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static void fillRelatedTags(
            final String classFqn,
            final String methodFqn,
            final XmlTag parentTag,
            final List<XmlTag> tagsReferences
    ) {
        for (final XmlTag routeNode : parentTag.findSubTags("route")) {
            for (final XmlTag serviceNode : routeNode.findSubTags("service")) {
                final String typeName = serviceNode.getAttributeValue("class");
                final String methodName = serviceNode.getAttributeValue("method");

                if (typeName != null && typeName.equals(classFqn)
                        && methodName != null && methodName.equals(methodFqn)
                ) {
                    tagsReferences.add(new WebApiLineMarkerXmlTagDecorator(routeNode));
                }
            }
        }
    }

    /**
     * Decorator for XmlTag, which allows to render REST routes in web API line marker.
     */
    private static class WebApiLineMarkerXmlTagDecorator extends LineMarkerXmlTagDecorator {

        public WebApiLineMarkerXmlTagDecorator(final XmlTag xmlTag) {
            super(xmlTag);
        }

        @Override
        public @NotNull String getDescription() {
            return "";
        }

        @Override
        public @NonNls @NotNull String getName() {
            final String httpMethod = this.xmlTag.getAttributeValue("method");
            final String route = this.xmlTag.getAttributeValue("url");

            if (httpMethod != null && route != null) {
                return String.format("  %-7s %s", httpMethod, route);
            }

            return xmlTag.getName();
        }
    }
}
