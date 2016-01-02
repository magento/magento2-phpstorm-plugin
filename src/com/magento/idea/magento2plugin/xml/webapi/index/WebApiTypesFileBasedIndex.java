package com.magento.idea.magento2plugin.xml.webapi.index;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
                return file.getFileType() == XmlFileType.INSTANCE && file.getNameWithoutExtension().equals("webapi");
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
class WebApiLineMarkerXmlTagDecorator implements XmlTag {

    private XmlTag xmlTag;

    public WebApiLineMarkerXmlTagDecorator(XmlTag xmlTag) {
        this.xmlTag = xmlTag;
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

    @Override
    @NotNull
    @NonNls
    public String getNamespace() {
        return xmlTag.getNamespace();
    }

    @Override
    @NotNull
    @NonNls
    public String getLocalName() {
        return xmlTag.getLocalName();
    }

    @Override
    @Nullable
    public XmlElementDescriptor getDescriptor() {
        return xmlTag.getDescriptor();
    }

    @Override
    @NotNull
    public XmlAttribute[] getAttributes() {
        return xmlTag.getAttributes();
    }

    @Override
    @Nullable
    public XmlAttribute getAttribute(@NonNls String s, @NonNls String s1) {
        return xmlTag.getAttribute(s, s1);
    }

    @Override
    @Nullable
    public XmlAttribute getAttribute(@NonNls String s) {
        return xmlTag.getAttribute(s);
    }

    @Override
    @Nullable
    public String getAttributeValue(@NonNls String s, @NonNls String s1) {
        return xmlTag.getAttributeValue(s, s1);
    }

    @Override
    @Nullable
    public String getAttributeValue(@NonNls String s) {
        return xmlTag.getAttributeValue(s);
    }

    @Override
    public XmlAttribute setAttribute(@NonNls String s, @NonNls String s1, @NonNls String s2) throws IncorrectOperationException {
        return xmlTag.setAttribute(s, s1, s2);
    }

    @Override
    public XmlAttribute setAttribute(@NonNls String s, @NonNls String s1) throws IncorrectOperationException {
        return xmlTag.setAttribute(s, s1);
    }

    @Override
    public XmlTag createChildTag(@NonNls String s, @NonNls String s1, @Nullable @NonNls String s2, boolean b) {
        return xmlTag.createChildTag(s, s1, s2, b);
    }

    @Override
    public XmlTag addSubTag(XmlTag xmlTag, boolean b) {
        return this.xmlTag.addSubTag(xmlTag, b);
    }

    @Override
    @NotNull
    public XmlTag[] getSubTags() {
        return xmlTag.getSubTags();
    }

    @Override
    @NotNull
    public XmlTag[] findSubTags(@NonNls String s) {
        return xmlTag.findSubTags(s);
    }

    @Override
    @NotNull
    public XmlTag[] findSubTags(@NonNls String s, @Nullable String s1) {
        return xmlTag.findSubTags(s, s1);
    }

    @Override
    @Nullable
    public XmlTag findFirstSubTag(@NonNls String s) {
        return xmlTag.findFirstSubTag(s);
    }

    @Override
    @NotNull
    @NonNls
    public String getNamespacePrefix() {
        return xmlTag.getNamespacePrefix();
    }

    @Override
    @NotNull
    @NonNls
    public String getNamespaceByPrefix(@NonNls String s) {
        return xmlTag.getNamespaceByPrefix(s);
    }

    @Override
    @Nullable
    public String getPrefixByNamespace(@NonNls String s) {
        return xmlTag.getPrefixByNamespace(s);
    }

    @Override
    public String[] knownNamespaces() {
        return xmlTag.knownNamespaces();
    }

    @Override
    public boolean hasNamespaceDeclarations() {
        return xmlTag.hasNamespaceDeclarations();
    }

    @Override
    @NotNull
    public Map<String, String> getLocalNamespaceDeclarations() {
        return xmlTag.getLocalNamespaceDeclarations();
    }

    @Override
    @NotNull
    public XmlTagValue getValue() {
        return xmlTag.getValue();
    }

    @Override
    @Nullable
    public XmlNSDescriptor getNSDescriptor(@NonNls String s, boolean b) {
        return xmlTag.getNSDescriptor(s, b);
    }

    @Override
    public boolean isEmpty() {
        return xmlTag.isEmpty();
    }

    @Override
    public void collapseIfEmpty() {
        xmlTag.collapseIfEmpty();
    }

    @Override
    @Nullable
    @NonNls
    public String getSubTagText(@NonNls String s) {
        return xmlTag.getSubTagText(s);
    }

    @Override
    public boolean processElements(PsiElementProcessor psiElementProcessor, PsiElement psiElement) {
        return xmlTag.processElements(psiElementProcessor, psiElement);
    }

    @Override
    @NotNull
    @Contract(
            pure = true
    )
    public Project getProject() throws PsiInvalidElementAccessException {
        return xmlTag.getProject();
    }

    @Override
    @NotNull
    @Contract(
            pure = true
    )
    public Language getLanguage() {
        return xmlTag.getLanguage();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiManager getManager() {
        return xmlTag.getManager();
    }

    @Override
    @NotNull
    @Contract(
            pure = true
    )
    public PsiElement[] getChildren() {
        return xmlTag.getChildren();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiElement getParent() {
        return xmlTag.getParent();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiElement getFirstChild() {
        return xmlTag.getFirstChild();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiElement getLastChild() {
        return xmlTag.getLastChild();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiElement getNextSibling() {
        return xmlTag.getNextSibling();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiElement getPrevSibling() {
        return xmlTag.getPrevSibling();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return xmlTag.getContainingFile();
    }

    @Override
    @Contract(
            pure = true
    )
    public TextRange getTextRange() {
        return xmlTag.getTextRange();
    }

    @Override
    @Contract(
            pure = true
    )
    public int getStartOffsetInParent() {
        return xmlTag.getStartOffsetInParent();
    }

    @Override
    @Contract(
            pure = true
    )
    public int getTextLength() {
        return xmlTag.getTextLength();
    }

    @Override
    @Nullable
    @Contract(
            pure = true
    )
    public PsiElement findElementAt(int i) {
        return xmlTag.findElementAt(i);
    }

    @Override
    @Nullable
    @Contract(
            pure = true
    )
    public PsiReference findReferenceAt(int i) {
        return xmlTag.findReferenceAt(i);
    }

    @Override
    @Contract(
            pure = true
    )
    public int getTextOffset() {
        return xmlTag.getTextOffset();
    }

    @Override
    @NonNls
    @Contract(
            pure = true
    )
    public String getText() {
        return xmlTag.getText();
    }

    @Override
    @NotNull
    @Contract(
            pure = true
    )
    public char[] textToCharArray() {
        return xmlTag.textToCharArray();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiElement getNavigationElement() {
        return xmlTag.getNavigationElement();
    }

    @Override
    @Contract(
            pure = true
    )
    public PsiElement getOriginalElement() {
        return xmlTag.getOriginalElement();
    }

    @Override
    @Contract(
            pure = true
    )
    public boolean textMatches(@NotNull @NonNls CharSequence charSequence) {
        return xmlTag.textMatches(charSequence);
    }

    @Override
    @Contract(
            pure = true
    )
    public boolean textMatches(@NotNull PsiElement psiElement) {
        return xmlTag.textMatches(psiElement);
    }

    @Override
    @Contract(
            pure = true
    )
    public boolean textContains(char c) {
        return xmlTag.textContains(c);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor psiElementVisitor) {
        xmlTag.accept(psiElementVisitor);
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor psiElementVisitor) {
        xmlTag.acceptChildren(psiElementVisitor);
    }

    @Override
    public PsiElement copy() {
        return xmlTag.copy();
    }

    @Override
    public PsiElement add(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        return xmlTag.add(psiElement);
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement psiElement, @Nullable PsiElement psiElement1) throws IncorrectOperationException {
        return xmlTag.addBefore(psiElement, psiElement1);
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement psiElement, @Nullable PsiElement psiElement1) throws IncorrectOperationException {
        return xmlTag.addAfter(psiElement, psiElement1);
    }

    @Override
    public void checkAdd(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        xmlTag.checkAdd(psiElement);
    }

    @Override
    public PsiElement addRange(PsiElement psiElement, PsiElement psiElement1) throws IncorrectOperationException {
        return xmlTag.addRange(psiElement, psiElement1);
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement psiElement, @NotNull PsiElement psiElement1, PsiElement psiElement2) throws IncorrectOperationException {
        return xmlTag.addRangeBefore(psiElement, psiElement1, psiElement2);
    }

    @Override
    public PsiElement addRangeAfter(PsiElement psiElement, PsiElement psiElement1, PsiElement psiElement2) throws IncorrectOperationException {
        return xmlTag.addRangeAfter(psiElement, psiElement1, psiElement2);
    }

    @Override
    public void delete() throws IncorrectOperationException {
        xmlTag.delete();
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
        xmlTag.checkDelete();
    }

    @Override
    public void deleteChildRange(PsiElement psiElement, PsiElement psiElement1) throws IncorrectOperationException {
        xmlTag.deleteChildRange(psiElement, psiElement1);
    }

    @Override
    public PsiElement replace(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        return xmlTag.replace(psiElement);
    }

    @Override
    @Contract(
            pure = true
    )
    public boolean isValid() {
        return xmlTag.isValid();
    }

    @Override
    @Contract(
            pure = true
    )
    public boolean isWritable() {
        return xmlTag.isWritable();
    }

    @Override
    @Nullable
    @Contract(
            pure = true
    )
    public PsiReference getReference() {
        return xmlTag.getReference();
    }

    @Override
    @NotNull
    @Contract(
            pure = true
    )
    public PsiReference[] getReferences() {
        return xmlTag.getReferences();
    }

    @Override
    @Nullable
    @Contract(
            pure = true
    )
    public <T> T getCopyableUserData(Key<T> key) {
        return xmlTag.getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, @Nullable T t) {
        xmlTag.putCopyableUserData(key, t);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor psiScopeProcessor, @NotNull ResolveState resolveState, @Nullable PsiElement psiElement, @NotNull PsiElement psiElement1) {
        return xmlTag.processDeclarations(psiScopeProcessor, resolveState, psiElement, psiElement1);
    }

    @Override
    @Nullable
    @Contract(
            pure = true
    )
    public PsiElement getContext() {
        return xmlTag.getContext();
    }

    @Override
    @Contract(
            pure = true
    )
    public boolean isPhysical() {
        return xmlTag.isPhysical();
    }

    @Override
    @NotNull
    @Contract(
            pure = true
    )
    public GlobalSearchScope getResolveScope() {
        return xmlTag.getResolveScope();
    }

    @Override
    @NotNull
    @Contract(
            pure = true
    )
    public SearchScope getUseScope() {
        return xmlTag.getUseScope();
    }

    @Override
    @Contract(
            pure = true
    )
    public ASTNode getNode() {
        return xmlTag.getNode();
    }

    @Override
    @NonNls
    @Contract(
            pure = true
    )
    public String toString() {
        return xmlTag.toString();
    }

    @Override
    @Contract(
            pure = true
    )
    public boolean isEquivalentTo(PsiElement psiElement) {
        return xmlTag.isEquivalentTo(psiElement);
    }

    @Override
    @Nullable
    public <T> T getUserData(@NotNull Key<T> key) {
        return xmlTag.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T t) {
        xmlTag.putUserData(key, t);
    }

    @Override
    public Icon getIcon(@IconFlags int i) {
        return xmlTag.getIcon(i);
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException {
        return xmlTag.setName(s);
    }

    @Override
    @Nullable
    public PsiMetaData getMetaData() {
        return xmlTag.getMetaData();
    }

    @Override
    @Nullable
    public XmlTag getParentTag() {
        return xmlTag.getParentTag();
    }

    @Override
    @Nullable
    public XmlTagChild getNextSiblingInTag() {
        return xmlTag.getNextSiblingInTag();
    }

    @Override
    @Nullable
    public XmlTagChild getPrevSiblingInTag() {
        return xmlTag.getPrevSiblingInTag();
    }
}
