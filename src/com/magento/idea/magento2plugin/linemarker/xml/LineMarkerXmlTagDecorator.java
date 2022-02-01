/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.xml;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
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
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoModule;
import java.util.Map;
import javax.swing.Icon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Decorator for XmlTag, which allows user-friendly line markers rendering.
 */
@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ExcessiveImports",
        "PMD.CouplingBetweenObjects",
        "PMD.ExcessivePublicCount",
        "PMD.CyclomaticComplexity",
        "PMD.TooManyMethods",
        "PMD.AvoidUncheckedExceptionsInSignatures"
})
public abstract class LineMarkerXmlTagDecorator implements XmlTag {

    protected XmlTag xmlTag;

    protected Project project;

    public LineMarkerXmlTagDecorator(final XmlTag xmlTag) {
        this.xmlTag = xmlTag;
        this.project = xmlTag.getProject();
    }

    @NotNull
    protected String getAreaName() {
        final VirtualFile containingDirectory = xmlTag
                .getContainingFile().getVirtualFile().getParent();
        final String configDirectory = containingDirectory.getName();

        if ("etc".equals(configDirectory)) {
            final VirtualFile moduleDirectory = containingDirectory.getParent();

            if ("app".equals(moduleDirectory.getName())) {
                return "primary";
            }
            return "global";
        }

        return configDirectory;
    }

    @NotNull
    private String getComponentName() {
        final MagentoComponentManager moduleManager = MagentoComponentManager.getInstance(project);
        final MagentoModule module = moduleManager.getComponentOfTypeForFile(
                xmlTag.getContainingFile(), MagentoModule.class
        );

        if (module == null) {
            return "";
        }

        return module.getMagentoName();
    }

    @NotNull
    public abstract String getDescription();

    /**
     * Get line marker text.
     * This method should be overridden to generate user-friendly XmlTag presentation.
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
    public @Nullable XmlAttribute getAttribute(
            final @NonNls String name,
            final @NonNls String namespace
    ) {
        return xmlTag.getAttribute(name, namespace);
    }

    @Override
    public @Nullable XmlAttribute getAttribute(final @NonNls String qname) {
        return xmlTag.getAttribute(qname);
    }

    @Override
    public @Nullable String getAttributeValue(
            final @NonNls String name,
            final @NonNls String namespace
    ) {
        return xmlTag.getAttributeValue(name, namespace);
    }

    @Override
    public @Nullable String getAttributeValue(final @NonNls String qname) {
        return xmlTag.getAttributeValue(qname);
    }

    @Override
    public XmlAttribute setAttribute(
            final @NonNls String name,
            final @NonNls String namespace,
            final @NonNls String value
    ) throws IncorrectOperationException {
        return xmlTag.setAttribute(name, namespace, value);
    }

    @Override
    public XmlAttribute setAttribute(
            final @NonNls String qname,
            final @NonNls String value
    ) throws IncorrectOperationException {
        return xmlTag.setAttribute(qname, value);
    }

    @Override
    public XmlTag createChildTag(
            final @NonNls String localName,
            final @NonNls String namespace,
            final @Nullable @NonNls String bodyText,
            final boolean enforceNamespacesDeep
    ) {
        return xmlTag.createChildTag(localName, namespace, bodyText, enforceNamespacesDeep);
    }

    @Override
    public XmlTag addSubTag(final XmlTag subTag, final boolean first) {
        return this.xmlTag.addSubTag(subTag, first);
    }

    @Override
    public @NotNull XmlTag[] getSubTags() {
        return xmlTag.getSubTags();
    }

    @Override
    public @NotNull XmlTag[] findSubTags(final @NonNls String qname) {
        return xmlTag.findSubTags(qname);
    }

    @Override
    public @NotNull XmlTag[] findSubTags(
            final @NonNls String localName,
            final @Nullable String namespace
    ) {
        return xmlTag.findSubTags(localName, namespace);
    }

    @Override
    public @Nullable XmlTag findFirstSubTag(final @NonNls String qname) {
        return xmlTag.findFirstSubTag(qname);
    }

    @Override
    public @NotNull @NonNls String getNamespacePrefix() {
        return xmlTag.getNamespacePrefix();
    }

    @Override
    public @NotNull @NonNls String getNamespaceByPrefix(final @NonNls String prefix) {
        return xmlTag.getNamespaceByPrefix(prefix);
    }

    @Override
    public @Nullable String getPrefixByNamespace(final @NonNls String namespace) {
        return xmlTag.getPrefixByNamespace(namespace);
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
    public @Nullable XmlNSDescriptor getNSDescriptor(
            final @NonNls String namespace,
            final boolean strict
    ) {
        return xmlTag.getNSDescriptor(namespace, strict);
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
    public @Nullable @NonNls String getSubTagText(final @NonNls String qname) {
        return xmlTag.getSubTagText(qname);
    }

    @Override
    public boolean processElements(
            final PsiElementProcessor psiElementProcessor,
            final PsiElement psiElement
    ) {
        return xmlTag.processElements(psiElementProcessor, psiElement);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Project getProject() throws PsiInvalidElementAccessException {
        return xmlTag.getProject();
    }

    @Override
    @Contract(pure = true)
    public @NotNull Language getLanguage() {
        return xmlTag.getLanguage();
    }

    @Override
    @Contract(pure = true)
    public PsiManager getManager() {
        return xmlTag.getManager();
    }

    @Override
    @Contract(pure = true)
    public @NotNull PsiElement[] getChildren() {
        return xmlTag.getChildren();
    }

    @Override
    @Contract(pure = true)
    public PsiElement getParent() {
        return xmlTag.getParent();
    }

    @Override
    @Contract(pure = true)
    public PsiElement getFirstChild() {
        return xmlTag.getFirstChild();
    }

    @Override
    @Contract(pure = true)
    public PsiElement getLastChild() {
        return xmlTag.getLastChild();
    }

    @Override
    @Contract(pure = true)
    public PsiElement getNextSibling() {
        return xmlTag.getNextSibling();
    }

    @Override
    @Contract(pure = true)
    public PsiElement getPrevSibling() {
        return xmlTag.getPrevSibling();
    }

    @Override
    @Contract(pure = true)
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return xmlTag.getContainingFile();
    }

    @Override
    @Contract(pure = true)
    public TextRange getTextRange() {
        return xmlTag.getTextRange();
    }

    @Override
    @Contract(pure = true)
    public int getStartOffsetInParent() {
        return xmlTag.getStartOffsetInParent();
    }

    @Override
    @Contract(pure = true)
    public int getTextLength() {
        return xmlTag.getTextLength();
    }

    @Override
    @Contract(pure = true)
    public @Nullable PsiElement findElementAt(final int offset) {
        return xmlTag.findElementAt(offset);
    }

    @Override
    @Contract(pure = true)
    public @Nullable PsiReference findReferenceAt(final int offset) {
        return xmlTag.findReferenceAt(offset);
    }

    @Override
    @Contract(pure = true)
    public int getTextOffset() {
        return xmlTag.getTextOffset();
    }

    @Override
    @Contract(pure = true)
    public @NonNls String getText() {
        return xmlTag.getText();
    }

    @Override
    @Contract(pure = true)
    public @NotNull char[] textToCharArray() {
        return xmlTag.textToCharArray();
    }

    @Override
    @Contract(pure = true)
    public PsiElement getNavigationElement() {
        return xmlTag.getNavigationElement();
    }

    @Override
    @Contract(pure = true)
    public PsiElement getOriginalElement() {
        return xmlTag.getOriginalElement();
    }

    @Override
    @Contract(pure = true)
    public boolean textMatches(final @NotNull @NonNls CharSequence charSequence) {
        return xmlTag.textMatches(charSequence);
    }

    @Override
    @Contract(pure = true)
    public boolean textMatches(final @NotNull PsiElement psiElement) {
        return xmlTag.textMatches(psiElement);
    }

    @Override
    @Contract(pure = true)
    public boolean textContains(final char character) {
        return xmlTag.textContains(character);
    }

    @Override
    public void accept(final @NotNull PsiElementVisitor psiElementVisitor) {
        xmlTag.accept(psiElementVisitor);
    }

    @Override
    public void acceptChildren(final @NotNull PsiElementVisitor psiElementVisitor) {
        xmlTag.acceptChildren(psiElementVisitor);
    }

    @Override
    public PsiElement copy() {
        return xmlTag.copy();
    }

    @Override
    public PsiElement add(
            final @NotNull PsiElement psiElement
    ) throws IncorrectOperationException {
        return xmlTag.add(psiElement);
    }

    @Override
    public PsiElement addBefore(
            final @NotNull PsiElement psiElement,
            final @Nullable PsiElement psiElement1
    ) throws IncorrectOperationException {
        return xmlTag.addBefore(psiElement, psiElement1);
    }

    @Override
    public PsiElement addAfter(
            final @NotNull PsiElement psiElement,
            final @Nullable PsiElement psiElement1
    ) throws IncorrectOperationException {
        return xmlTag.addAfter(psiElement, psiElement1);
    }

    @Override
    public void checkAdd(final @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (xmlTag instanceof XmlTagImpl) {
            ((XmlTagImpl) xmlTag).checkAdd(psiElement);
        }
        throw new IncorrectOperationException(getClass().getName());
    }

    @Override
    public PsiElement addRange(
            final PsiElement psiElement,
            final PsiElement psiElement1
    ) throws IncorrectOperationException {
        return xmlTag.addRange(psiElement, psiElement1);
    }

    @Override
    public PsiElement addRangeBefore(
            final @NotNull PsiElement psiElement,
            final @NotNull PsiElement psiElement1,
            final PsiElement psiElement2
    ) throws IncorrectOperationException {
        return xmlTag.addRangeBefore(psiElement, psiElement1, psiElement2);
    }

    @Override
    public PsiElement addRangeAfter(
            final PsiElement psiElement,
            final PsiElement psiElement1,
            final PsiElement psiElement2
    ) throws IncorrectOperationException {
        return xmlTag.addRangeAfter(psiElement, psiElement1, psiElement2);
    }

    @Override
    public void delete() throws IncorrectOperationException {
        xmlTag.delete();
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
        if (xmlTag instanceof XmlTagImpl) {
            ((XmlTagImpl) xmlTag).checkDelete();
        }
        throw new IncorrectOperationException(getClass().getName());
    }

    @Override
    public void deleteChildRange(
            final PsiElement psiElement,
            final PsiElement psiElement1
    ) throws IncorrectOperationException {
        xmlTag.deleteChildRange(psiElement, psiElement1);
    }

    @Override
    public PsiElement replace(
            final @NotNull PsiElement psiElement
    ) throws IncorrectOperationException {
        return xmlTag.replace(psiElement);
    }

    @Override
    @Contract(pure = true)
    public boolean isValid() {
        return xmlTag.isValid();
    }

    @Override
    @Contract(pure = true)
    public boolean isWritable() {
        return xmlTag.isWritable();
    }

    @Override
    @Contract(pure = true)
    public @Nullable PsiReference getReference() {
        return xmlTag.getReference();
    }

    @Override
    @Contract(pure = true)
    public @NotNull PsiReference[] getReferences() {
        return xmlTag.getReferences();
    }

    @Override
    @Contract(pure = true)
    public @Nullable <T> T getCopyableUserData(final Key<T> key) {
        return xmlTag.getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(final Key<T> key, final @Nullable T value) {
        xmlTag.putCopyableUserData(key, value);
    }

    @Override
    public boolean processDeclarations(
            final @NotNull PsiScopeProcessor psiScopeProcessor,
            final @NotNull ResolveState resolveState,
            final @Nullable PsiElement psiElement,
            final @NotNull PsiElement psiElement1
    ) {
        return xmlTag.processDeclarations(psiScopeProcessor, resolveState, psiElement, psiElement1);
    }

    @Override
    @Contract(pure = true)
    public @Nullable PsiElement getContext() {
        return xmlTag.getContext();
    }

    @Override
    @Contract(pure = true)
    public boolean isPhysical() {
        return xmlTag.isPhysical();
    }

    @Override
    @Contract(pure = true)
    public @NotNull GlobalSearchScope getResolveScope() {
        return xmlTag.getResolveScope();
    }

    @Override
    @Contract(pure = true)
    public @NotNull SearchScope getUseScope() {
        return xmlTag.getUseScope();
    }

    @Override
    @Contract(pure = true)
    public ASTNode getNode() {
        return xmlTag.getNode();
    }

    @Override
    @Contract(pure = true)
    public @NonNls String toString() {
        return xmlTag.toString();
    }

    @Override
    @Contract(pure = true)
    public boolean isEquivalentTo(final PsiElement psiElement) {
        return xmlTag.isEquivalentTo(psiElement);
    }

    @Override
    public @Nullable <T> T getUserData(final @NotNull Key<T> key) {
        return xmlTag.getUserData(key);
    }

    @Override
    public <T> void putUserData(final @NotNull Key<T> key, final @Nullable T value) {
        xmlTag.putUserData(key, value);
    }

    @Override
    public Icon getIcon(final @IconFlags int flags) {
        return xmlTag.getIcon(flags);
    }

    @Override
    public PsiElement setName(
            final @NonNls @NotNull String name
    ) throws IncorrectOperationException {
        return xmlTag.setName(name);
    }

    @Override
    public @Nullable PsiMetaData getMetaData() {
        return xmlTag.getMetaData();
    }

    @Override
    public @Nullable XmlTag getParentTag() {
        return xmlTag.getParentTag();
    }

    @Override
    public @Nullable XmlTagChild getNextSiblingInTag() {
        return xmlTag.getNextSiblingInTag();
    }

    @Override
    public @Nullable XmlTagChild getPrevSiblingInTag() {
        return xmlTag.getPrevSiblingInTag();
    }
}
