package com.magento.idea.magento2plugin.xml.index;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagChild;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.magento.idea.magento2plugin.php.module.MagentoComponent;
import com.magento.idea.magento2plugin.php.module.MagentoComponentManager;
import com.magento.idea.magento2plugin.php.module.MagentoModule;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Decorator for XmlTag, which allows to render user-friendly line markers.
 */
abstract public class LineMarkerXmlTagDecorator implements XmlTag {

    protected XmlTag xmlTag;

    protected Project project;

    public LineMarkerXmlTagDecorator(XmlTag xmlTag) {
        this.xmlTag = xmlTag;
        this.project = xmlTag.getProject();
    }

    @NotNull
    protected String getAreaName() {
        VirtualFile containingDirectory = xmlTag.getContainingFile().getVirtualFile().getParent();
        String configDirectory = containingDirectory.getName();

        if (configDirectory.equals("etc")) {
            VirtualFile moduleDirectory = containingDirectory.getParent();
            if (moduleDirectory.getName().equals("app")) {
                return "primary";
            }
            return "global";
        }
        return configDirectory;
    }

    @NotNull
    protected String getComponentName() {
        MagentoComponentManager moduleManager = MagentoComponentManager.getInstance(project);
        MagentoModule module = moduleManager.getComponentOfTypeForFile(xmlTag.getContainingFile(), MagentoModule.class);

        if (module == null) {
            return "";
        }

        return module.getMagentoName();
    }

    @NotNull
    abstract public String getDescription();

    /**
     * Get line marker text. This method should be overridden to generate user-friendly XmlTag presentation.
     */
    @Override
    @NotNull
    @NonNls
    public String getName() {
        return String.format("%s [%s] - %s", getComponentName(), getAreaName(), getDescription());
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
