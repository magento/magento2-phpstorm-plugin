package com.magento.idea.magento2plugin.xml.webapi.index;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.Settings;
import com.magento.idea.magento2plugin.xml.index.LineMarkerXmlTagDecorator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Indexer for classes/interfaces which have methods exposed via Web API.
 */
public class WebApiTypesFileBasedIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> NAME = ID.create("com.magento.idea.magento2plugin.xml.webapi.index.webapi_types");

    private final KeyDescriptor<String> keyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {

            @NotNull
            @Override
            public Map<String, Void> map(@NotNull FileContent fileContent) {
                Map<String, Void> map = new HashMap<>();

                PsiFile psiFile = fileContent.getPsiFile();
                if (!Settings.isEnabled(psiFile.getProject())) {
                    return map;
                }

                XmlDocumentImpl document = PsiTreeUtil.getChildOfType(psiFile, XmlDocumentImpl.class);
                if (document == null) {
                    return map;
                }

                XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
                if (xmlTags == null) {
                    return map;
                }

                for (XmlTag xmlTag : xmlTags) {
                    if (xmlTag.getName().equals("routes")) {
                        for (XmlTag routeNode : xmlTag.findSubTags("route")) {
                            for (XmlTag serviceNode : routeNode.findSubTags("service")) {
                                String typeName = serviceNode.getAttributeValue("class");
                                if (typeName != null) {
                                    map.put(PhpLangUtil.toPresentableFQN(typeName), null);
                                }
                            }
                        }
                    }
                }
                return map;
            }
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return keyDescriptor;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return file.getFileType() == XmlFileType.INSTANCE && file.getNameWithoutExtension().equals("webapi")
                    && !file.getPath().contains("testsuite") && !file.getPath().contains("_files");
            }
        };
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
     *
     * Parent classes are not taken into account.
     */
    public static List<XmlTag> getWebApiRoutes(Method method) {
        List<XmlTag> tags = new ArrayList<>();
        if (!method.getAccess().isPublic()) {
            return tags;
        }
        PhpClass phpClass = method.getContainingClass();
        String methodFqn = method.getName();
        if (phpClass == null) {
            return tags;
        }
        String classFqn = phpClass.getPresentableFQN();
        if (classFqn == null) {
            return tags;
        }
        Collection<VirtualFile> containingFiles = FileBasedIndex
            .getInstance()
            .getContainingFiles(
                NAME,
                classFqn,
                GlobalSearchScope.allScope(phpClass.getProject())
            );

        PsiManager psiManager = PsiManager.getInstance(phpClass.getProject());
        for (VirtualFile virtualFile : containingFiles) {
            XmlFile file = (XmlFile) psiManager.findFile(virtualFile);
            if (file == null) {
                continue;
            }
            XmlTag rootTag = file.getRootTag();
            fillRelatedTags(classFqn, methodFqn, rootTag, tags);
        }
        return tags;
    }

    /**
     * Find routes related to the specified method within single webapi.xml
     */
    private static void fillRelatedTags(String classFqn, String methodFqn, XmlTag parentTag, List<XmlTag> tagsReferences) {
        for (XmlTag routeNode : parentTag.findSubTags("route")) {
            for (XmlTag serviceNode : routeNode.findSubTags("service")) {
                String typeName = serviceNode.getAttributeValue("class");
                String methodName = serviceNode.getAttributeValue("method");
                if (typeName != null && typeName.equals(classFqn)
                    && methodName != null && methodName.equals(methodFqn)
                ) {
                    tagsReferences.add(new WebApiLineMarkerXmlTagDecorator(routeNode));
                }
            }
        }
    }
}

/**
 * Decorator for XmlTag, which allows to render REST routes in web API line marker.
 */
class WebApiLineMarkerXmlTagDecorator extends LineMarkerXmlTagDecorator {

    public WebApiLineMarkerXmlTagDecorator(XmlTag xmlTag) {
        super(xmlTag);
    }

    @NotNull
    @Override
    public String getDescription() {
        return "";
    }

    @Override
    @NotNull
    @NonNls
    public String getName() {
        String httpMethod = this.xmlTag.getAttributeValue("method");
        String route = this.xmlTag.getAttributeValue("url");
        if (httpMethod != null && route != null) {
            return String.format("  %-7s %s", httpMethod, route);
        }
        return xmlTag.getName();
    }
}
