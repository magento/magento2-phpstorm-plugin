/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.ui;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.files.UiComponentXml;
import com.magento.idea.magento2plugin.stubs.indexes.ui.DisplayAreaIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ui.GetRegionUsageIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ui.UiComponentComponentDeclarationIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.util.magento.js.JsAst;
import com.magento.idea.magento2plugin.util.magento.js.JsObject;
import com.magento.idea.magento2plugin.util.magento.js.JsProperty;
import com.magento.idea.magento2plugin.util.magento.js.JsPsiSupportProvider;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutRegionResolver;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutTemplatePathResolver;
import com.magento.idea.magento2plugin.util.magento.js.MagentoJsParser;
import com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.CouplingBetweenObjects",
        "PMD.TooManyMethods",
        "PMD.ExcessiveClassLength"
})
public class UiComponentScopeResolver {
    private static final Set<String> XML_CONTAINER_ITEM_NAMES = Set.of(
            "components",
            "children",
            "config",
            "data",
            "deps",
            "imports",
            "exports",
            "links",
            "listens",
            "tracks",
            "templates",
            "jsLayout"
    );
    private static final Set<String> XML_VALUE_ITEM_NAMES = Set.of(
            "component",
            "displayArea",
            "template",
            "childTemplate",
            "elementTmpl"
    );
    private static final Set<String> JS_CONTAINER_NAMES = Set.of(
            "components",
            "children",
            "defaults",
            "config",
            "templates"
    );
    private static final Set<String> JS_VALUE_PROPERTY_NAMES = Set.of(
            "component",
            "displayArea",
            "template",
            "childTemplate",
            "elementTmpl"
    );
    private static UiComponentScopeResolver INSTANCE;

    private UiComponentScopeResolver() {
    }

    public static UiComponentScopeResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UiComponentScopeResolver();
        }

        return INSTANCE;
    }

    public @NotNull List<UiComponentNavigationData> collectComponentDeclarations(
            final @NotNull com.intellij.util.indexing.FileContent content
    ) {
        final VirtualFile virtualFile = content.getFile();
        final String ext = virtualFile.getExtension();

        if ("js".equalsIgnoreCase(ext)) {
            final JsPsiSupportProvider jsProvider = JsPsiSupportProvider.getInstance();
            if (jsProvider != null) {
                final List<UiComponentNavigationData> provided =
                        jsProvider.collectUiComponentDeclarations(content.getPsiFile());
                if (provided != null) {
                    return provided;
                }
            }
            return collectJsComponentDeclarations(virtualFile, MagentoVfsUtil.getContentAsText(content));
        }
        if ("php".equalsIgnoreCase(ext)) {
            return collectPhpComponentDeclarations(virtualFile, MagentoVfsUtil.getContentAsText(content));
        }
        if (content.getFileType() instanceof com.intellij.openapi.fileTypes.LanguageFileType) {
            return collectComponentDeclarations(content.getPsiFile());
        }

        return Collections.emptyList();
    }

    public @NotNull List<UiComponentNavigationData> collectComponentDeclarations(
            final @NotNull PsiFile psiFile
    ) {
        if (isJsFile(psiFile) || isJavaScriptLanguage(psiFile.getLanguage())) {
            final JsPsiSupportProvider jsProvider = JsPsiSupportProvider.getInstance();
            if (jsProvider != null) {
                final List<UiComponentNavigationData> provided =
                        jsProvider.collectUiComponentDeclarations(psiFile);
                if (provided != null) {
                    return provided;
                }
            }
            return collectJsComponentDeclarations(psiFile);
        }
        if (psiFile instanceof XmlFile) {
            return collectXmlComponentDeclarations((XmlFile) psiFile);
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile != null && "php".equals(virtualFile.getExtension())) {
            return collectPhpComponentDeclarations(psiFile);
        }

        return Collections.emptyList();
    }

    public @NotNull List<UiComponentNavigationData> collectGetRegionUsages(
            final @NotNull PsiFile psiFile
    ) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null) {
            return Collections.emptyList();
        }
        return collectGetRegionUsages(virtualFile, psiFile.getText());
    }

    public @NotNull List<UiComponentNavigationData> collectGetRegionUsages(
            final @NotNull VirtualFile virtualFile,
            final @NotNull CharSequence text
    ) {
        if (!"html".equalsIgnoreCase(virtualFile.getExtension())) {
            return Collections.emptyList();
        }
        final List<UiComponentNavigationData> results = new ArrayList<>();

        for (final KnockoutRegionResolver.RegionMatch match : KnockoutRegionResolver.getInstance()
                .collectGetRegionMatches(text.toString())) {
            results.add(new UiComponentNavigationData(
                    virtualFile.getUrl(),
                    UiComponentNavigationData.KIND_GET_REGION,
                    match.getRegionName(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    match.getStartOffset()
            ));
        }

        return results;
    }

    public @Nullable UiComponentNavigationData findDisplayAreaDeclaration(
            final @NotNull PsiElement element
    ) {
        final PsiFile psiFile = element.getContainingFile();
        if (psiFile == null) {
            return null;
        }
        final int startOffset = element.getTextRange().getStartOffset();
        final int endOffset = element.getTextRange().getEndOffset();

        for (final UiComponentNavigationData declaration : collectComponentDeclarations(psiFile)) {
            if (!UiComponentNavigationData.KIND_DISPLAY_AREA.equals(declaration.getKind())) {
                continue;
            }
            if (overlapsValue(startOffset, endOffset, declaration)) {
                return declaration;
            }
        }

        if (!isJsFile(psiFile) && isJavaScriptLanguage(element.getLanguage())) {
            final PsiElement jsScope = findEmbeddedJsScope(element, psiFile);
            final VirtualFile virtualFile = psiFile.getVirtualFile() != null
                    ? psiFile.getVirtualFile()
                    : psiFile.getViewProvider().getVirtualFile();
            if (jsScope != null && virtualFile != null) {
                final JsPsiSupportProvider jsProvider = JsPsiSupportProvider.getInstance();
                final List<UiComponentNavigationData> declarations;
                if (jsProvider != null) {
                    final List<UiComponentNavigationData> provided =
                            jsProvider.collectEmbeddedUiComponentDeclarations(jsScope, virtualFile);
                    declarations = provided != null
                            ? provided
                            : collectJsComponentDeclarations(
                                    virtualFile,
                                    jsScope.getText(),
                                    jsScope == psiFile ? 0 : jsScope.getTextRange().getStartOffset()
                            );
                } else {
                    final int baseOffset = jsScope == psiFile ? 0 : jsScope.getTextRange().getStartOffset();
                    declarations = collectJsComponentDeclarations(virtualFile, jsScope.getText(), baseOffset);
                }
                for (final UiComponentNavigationData declaration : declarations) {
                    if (UiComponentNavigationData.KIND_DISPLAY_AREA.equals(declaration.getKind())
                            && overlapsValue(startOffset, endOffset, declaration)) {
                        return declaration;
                    }
                }
            }
        }

        return null;
    }

    private static @Nullable PsiElement findEmbeddedJsScope(
            final @NotNull PsiElement element,
            final @NotNull PsiFile psiFile
    ) {
        PsiElement current = element;
        PsiElement jsRoot = null;
        while (current != null) {
            if (isJavaScriptLanguage(current.getLanguage())) {
                jsRoot = current;
            } else if (jsRoot != null) {
                break;
            }
            if (current == psiFile) {
                break;
            }
            current = current.getParent();
        }
        return jsRoot;
    }

    private static boolean isJavaScriptLanguage(final @NotNull com.intellij.lang.Language language) {
        final com.intellij.lang.Language jsLanguage = com.intellij.lang.Language.findLanguageByID("JavaScript");
        if (jsLanguage != null && language.isKindOf(jsLanguage)) {
            return true;
        }
        com.intellij.lang.Language current = language;
        while (current != null) {
            final String id = current.getID();
            if ("JavaScript".equals(id) || "ECMAScript 6".equals(id)) {
                return true;
            }
            current = current.getBaseLanguage();
        }
        return false;
    }

    public @Nullable String getDisplayAreaValue(final @NotNull PsiElement element) {
        final UiComponentNavigationData declaration = findDisplayAreaDeclaration(element);

        return declaration == null ? null : declaration.getValue();
    }

    public @NotNull List<PsiElement> resolveGetRegionTargetsForDisplayArea(
            final @NotNull PsiElement element,
            final @NotNull String displayArea
    ) {
        final UiComponentNavigationData source = findDisplayAreaDeclaration(element);
        final Set<PsiElement> targets = new LinkedHashSet<>();

        if (source != null) {
            final List<PsiElement> scopedTargets = resolveScopedGetRegionTargets(element.getProject(), source);

            if (!scopedTargets.isEmpty()) {
                return scopedTargets;
            }
            final List<PsiElement> inheritedScopeTargets = resolveGetRegionTargetsFromComponentDeclarations(
                    element.getProject(),
                    source
            );

            if (!inheritedScopeTargets.isEmpty()) {
                return inheritedScopeTargets;
            }
        }
        for (final UiComponentNavigationData usage : getRegionUsages(element.getProject(), displayArea)) {
            addTargetElement(element.getProject(), targets, usage);
        }

        return new ArrayList<>(targets);
    }

    public @NotNull List<PsiElement> resolveDisplayAreaTargetsForGetRegion(
            final @NotNull PsiFile templateFile,
            final @NotNull String regionName
    ) {
        final Project project = templateFile.getProject();

        if (DumbService.isDumb(project)) {
            final List<PsiElement> fallbackTargets = resolveDisplayAreaTargetsFromProjectFilesInDumbMode(
                    templateFile,
                    regionName
            );
            return fallbackTargets;
        }
        final List<UiComponentNavigationData> owners =
                UiComponentTemplateResolver.getInstance().resolveTemplateOwnerDeclarations(templateFile);
        final Set<PsiElement> targets = new LinkedHashSet<>();
        final List<UiComponentNavigationData> indexedDisplayAreas = displayAreaDeclarations(project, regionName);

        if (!owners.isEmpty()) {
            for (final UiComponentNavigationData displayArea : indexedDisplayAreas) {
                if (!isDisplayAreaOwnedByTemplateOwner(displayArea, owners)) {
                    continue;
                }
                addTargetElement(project, targets, displayArea);
            }

            if (!targets.isEmpty()) {
                return new ArrayList<>(targets);
            }

            return resolveDisplayAreaTargetsFromProjectFiles(
                    project,
                    regionName,
                    owners,
                    templateFile
            );
        }
        for (final UiComponentNavigationData displayArea : indexedDisplayAreas) {
            addTargetElement(project, targets, displayArea);
        }
        if (targets.isEmpty()) {
            return resolveDisplayAreaTargetsFromProjectFiles(
                    project,
                    regionName,
                    owners,
                    templateFile
            );
        }

        return new ArrayList<>(targets);
    }

    public @NotNull List<PsiElement> resolveTemplateUsageDeclarations(
            final @NotNull PsiFile templateFile,
            final @NotNull String declarationKind,
            final @Nullable String templateKey
    ) {
        final Project project = templateFile.getProject();
        final List<UiComponentNavigationData> owners =
                UiComponentTemplateResolver.getInstance().resolveTemplateOwnerDeclarations(templateFile);
        final Set<PsiElement> targets = new LinkedHashSet<>();

        for (final UiComponentNavigationData owner : owners) {
            final List<UiComponentNavigationData> declarations =
                    UiComponentTemplateResolver.getInstance().resolveTemplateDeclarationsForOwner(project, owner);

            for (final UiComponentNavigationData declaration : declarations) {
                if (!declarationKind.equals(declaration.getKind())) {
                    continue;
                }
                if (templateKey != null && !templateKey.equals(declaration.getTemplateKey())) {
                    continue;
                }
                addTargetElement(project, targets, declaration);
            }
        }

        return new ArrayList<>(targets);
    }

    public @NotNull List<PsiElement> resolveTemplateFilesForDisplayAreaTargets(
            final @NotNull Project project,
            final @NotNull Collection<PsiElement> displayAreaTargets
    ) {
        final Set<PsiElement> targets = new LinkedHashSet<>();

        for (final PsiElement displayAreaTarget : displayAreaTargets) {
            final UiComponentNavigationData displayArea = findDisplayAreaDeclarationForTarget(displayAreaTarget);

            if (displayArea == null) {
                continue;
            }
            for (final UiComponentNavigationData declaration : resolveTemplateDeclarationsForComponent(
                    project,
                    displayArea
            )) {
                targets.addAll(UiComponentTemplateResolver.getInstance().resolveTemplateFiles(
                        project,
                        declaration.getValue()
                ));
            }
        }

        return new ArrayList<>(targets);
    }

    public @NotNull List<PsiElement> resolveNavigationTargets(
            final @NotNull Project project,
            final @NotNull Collection<UiComponentNavigationData> declarations
    ) {
        final Set<PsiElement> targets = new LinkedHashSet<>();

        for (final UiComponentNavigationData declaration : declarations) {
            addTargetElement(project, targets, declaration);
        }

        return new ArrayList<>(targets);
    }

    public @NotNull List<UiComponentNavigationData> componentDeclarationsByJsPath(
            final @NotNull Project project,
            final @NotNull String componentJsPath
    ) {
        final List<UiComponentNavigationData> results = new ArrayList<>();

        for (final Set<UiComponentNavigationData> declarations : FileBasedIndex.getInstance().getValues(
                UiComponentComponentDeclarationIndex.KEY,
                UiComponentComponentDeclarationIndex.componentJsPathKey(componentJsPath),
                GlobalSearchScope.allScope(project)
        )) {
            results.addAll(declarations);
        }

        return results;
    }

    public @Nullable String getRequireJsPath(final @NotNull PsiFile psiFile) {
        if (!isJsFile(psiFile)) {
            return null;
        }

        return getRequireJsPathFromFilePath(psiFile);
    }

    public @NotNull List<UiComponentNavigationData> getRegionUsages(
            final @NotNull Project project,
            final @NotNull String regionName
    ) {
        final List<UiComponentNavigationData> results = new ArrayList<>();

        for (final Set<UiComponentNavigationData> usages : FileBasedIndex.getInstance().getValues(
                GetRegionUsageIndex.KEY,
                regionName,
                GlobalSearchScope.allScope(project)
        )) {
            results.addAll(usages);
        }

        return results;
    }

    private @Nullable UiComponentNavigationData findDisplayAreaDeclarationForTarget(
            final @NotNull PsiElement target
    ) {
        final PsiFile psiFile = target.getContainingFile();

        if (psiFile == null || psiFile.getVirtualFile() == null) {
            return null;
        }
        final int targetStartOffset = target.getTextRange().getStartOffset();
        final int targetEndOffset = target.getTextRange().getEndOffset();

        for (final UiComponentNavigationData declaration : collectComponentDeclarations(psiFile)) {
            if (!UiComponentNavigationData.KIND_DISPLAY_AREA.equals(declaration.getKind())) {
                continue;
            }
            if (overlapsValue(targetStartOffset, targetEndOffset, declaration)) {
                return declaration;
            }
        }

        return null;
    }

    private @NotNull List<UiComponentNavigationData> resolveTemplateDeclarationsForComponent(
            final @NotNull Project project,
            final @NotNull UiComponentNavigationData componentDeclaration
    ) {
        final Set<UiComponentNavigationData> results = new LinkedHashSet<>();
        final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(componentDeclaration.getFileUrl());

        if (file != null && !file.isDirectory()) {
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

            if (psiFile != null) {
                for (final UiComponentNavigationData declaration : collectComponentDeclarations(psiFile)) {
                    if (isTemplateDeclaration(declaration)
                            && (isSameComponent(componentDeclaration, declaration)
                            || isDescendantComponentDeclaration(componentDeclaration, declaration))) {
                        results.add(declaration);
                    }
                }
            }
        }
        for (final UiComponentNavigationData declaration : UiComponentTemplateResolver.getInstance()
                .resolveTemplateDeclarationsForOwner(project, componentDeclaration)) {
            if (isSameComponent(componentDeclaration, declaration)) {
                results.add(declaration);
            }
        }

        return new ArrayList<>(results);
    }

    private boolean isDescendantComponentDeclaration(
            final @NotNull UiComponentNavigationData parent,
            final @NotNull UiComponentNavigationData child
    ) {
        return isNestedUnder(child.getComponentName(), parent.getComponentName(), ".")
                || isNestedUnder(child.getComponentJsPath(), parent.getComponentJsPath(), "/");
    }

    private boolean isSameComponent(
            final @NotNull UiComponentNavigationData first,
            final @NotNull UiComponentNavigationData second
    ) {
        if (hasValue(first.getComponentName()) && hasValue(second.getComponentName())) {
            return first.getComponentName().equals(second.getComponentName())
                    || matchesJsDefaultDeclaration(first, second);
        }

        return matchesNonBlank(first.getComponentJsPath(), second.getComponentJsPath());
    }

    private boolean matchesJsDefaultDeclaration(
            final @NotNull UiComponentNavigationData first,
            final @NotNull UiComponentNavigationData second
    ) {
        return matchesNonBlank(first.getComponentJsPath(), second.getComponentJsPath())
                && (isJsDefaultDeclaration(first) || isJsDefaultDeclaration(second));
    }

    private boolean isJsDefaultDeclaration(final @NotNull UiComponentNavigationData declaration) {
        return hasValue(declaration.getComponentName())
                && declaration.getComponentName().equals(declaration.getComponentJsPath());
    }

    private boolean hasValue(final @Nullable String value) {
        return value != null && !value.isBlank();
    }

    public @NotNull List<UiComponentNavigationData> displayAreaDeclarations(
            final @NotNull Project project,
            final @NotNull String displayArea
    ) {
        final List<UiComponentNavigationData> results = new ArrayList<>();

        for (final Set<UiComponentNavigationData> declarations : FileBasedIndex.getInstance().getValues(
                DisplayAreaIndex.KEY,
                displayArea,
                GlobalSearchScope.allScope(project)
        )) {
            results.addAll(declarations);
        }

        return results;
    }

    private @NotNull List<PsiElement> resolveScopedGetRegionTargets(
            final @NotNull Project project,
            final @NotNull UiComponentNavigationData displayArea
    ) {
        final Set<String> parentTemplateUrls = new LinkedHashSet<>();

        for (final UiComponentNavigationData templateDeclaration : collectParentTemplateDeclarationsFromSourceFile(
                project,
                displayArea
        )) {
            parentTemplateUrls.addAll(resolveTemplateFileUrls(project, templateDeclaration.getValue()));
        }
        for (final UiComponentNavigationData templateDeclaration : UiComponentTemplateResolver.getInstance()
                .resolveTemplateDeclarationsForParent(project, displayArea)) {
            parentTemplateUrls.addAll(resolveTemplateFileUrls(project, templateDeclaration.getValue()));
        }

        if (parentTemplateUrls.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<PsiElement> targets = new LinkedHashSet<>();

        for (final UiComponentNavigationData usage : getRegionUsages(project, displayArea.getValue())) {
            if (!parentTemplateUrls.contains(usage.getFileUrl())) {
                continue;
            }
            addTargetElement(project, targets, usage);
        }

        return new ArrayList<>(targets);
    }

    private @NotNull List<UiComponentNavigationData> collectParentTemplateDeclarationsFromSourceFile(
            final @NotNull Project project,
            final @NotNull UiComponentNavigationData displayArea
    ) {
        final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(displayArea.getFileUrl());

        if (file == null || file.isDirectory()) {
            return Collections.emptyList();
        }
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        if (psiFile == null) {
            return Collections.emptyList();
        }
        final List<UiComponentNavigationData> results = new ArrayList<>();

        for (final UiComponentNavigationData declaration : collectComponentDeclarations(psiFile)) {
            if (!isTemplateDeclaration(declaration)) {
                continue;
            }
            if (matchesNonBlank(displayArea.getParentComponentName(), declaration.getComponentName())
                    || matchesNonBlank(displayArea.getParentComponentJsPath(), declaration.getComponentJsPath())) {
                results.add(declaration);
            }
        }

        return results;
    }

    private boolean isTemplateDeclaration(final @NotNull UiComponentNavigationData declaration) {
        return UiComponentNavigationData.KIND_TEMPLATE.equals(declaration.getKind())
                || UiComponentNavigationData.KIND_CHILD_TEMPLATE.equals(declaration.getKind())
                || UiComponentNavigationData.KIND_TEMPLATES.equals(declaration.getKind())
                || UiComponentNavigationData.KIND_ELEMENT_TEMPLATE.equals(declaration.getKind());
    }

    private @NotNull List<PsiElement> resolveGetRegionTargetsFromComponentDeclarations(
            final @NotNull Project project,
            final @NotNull UiComponentNavigationData source
    ) {
        if (source.getComponentJsPath() == null || source.getParentComponentName() != null) {
            return Collections.emptyList();
        }
        final Set<PsiElement> results = new LinkedHashSet<>();

        for (final UiComponentNavigationData componentDeclaration : componentDeclarationsByJsPath(
                project,
                source.getComponentJsPath()
        )) {
            if (componentDeclaration.getParentComponentName() == null
                    && componentDeclaration.getParentComponentJsPath() == null) {
                continue;
            }
            results.addAll(resolveScopedGetRegionTargets(project, new UiComponentNavigationData(
                    source.getFileUrl(),
                    UiComponentNavigationData.KIND_DISPLAY_AREA,
                    source.getValue(),
                    componentDeclaration.getComponentName(),
                    componentDeclaration.getParentComponentName(),
                    componentDeclaration.getComponentJsPath(),
                    componentDeclaration.getParentComponentJsPath(),
                    null,
                    source.getValueOffset()
            )));
        }

        return new ArrayList<>(results);
    }

    private @NotNull Set<String> resolveTemplateFileUrls(
            final @NotNull Project project,
            final @NotNull String templatePath
    ) {
        final Set<String> result = new LinkedHashSet<>();

        for (final PsiElement templateFile : KnockoutTemplatePathResolver.getInstance()
                .resolveTemplateFiles(project, templatePath)) {
            if (templateFile instanceof PsiFile && ((PsiFile) templateFile).getVirtualFile() != null) {
                result.add(((PsiFile) templateFile).getVirtualFile().getUrl());
            }
        }

        return result;
    }

    private boolean isDisplayAreaOwnedByTemplateOwner(
            final @NotNull UiComponentNavigationData displayArea,
            final @NotNull Collection<UiComponentNavigationData> owners
    ) {
        for (final UiComponentNavigationData owner : owners) {
            if (matchesNonBlank(displayArea.getParentComponentName(), owner.getComponentName())
                    || matchesNonBlank(displayArea.getParentComponentJsPath(), owner.getComponentJsPath())
                    || matchesNonBlank(displayArea.getParentComponentJsPath(), owner.getComponentName())
                    || isNestedUnder(displayArea.getComponentName(), owner.getComponentName(), ".")
                    || isNestedUnder(displayArea.getComponentJsPath(), owner.getComponentJsPath(), "/")) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesNonBlank(
            final @Nullable String first,
            final @Nullable String second
    ) {
        return first != null && !first.isBlank() && first.equals(second);
    }

    private boolean isNestedUnder(
            final @Nullable String child,
            final @Nullable String parent,
            final @NotNull String separator
    ) {
        return child != null
                && parent != null
                && !child.isBlank()
                && !parent.isBlank()
                && child.startsWith(parent + separator);
    }

    private @NotNull List<PsiElement> resolveDisplayAreaTargetsFromProjectFiles(
            final @NotNull Project project,
            final @NotNull String regionName,
            final @NotNull Collection<UiComponentNavigationData> owners,
            final @NotNull PsiFile templateFile
    ) {
        final Set<PsiElement> targets = new LinkedHashSet<>();
        final List<UiComponentNavigationData> fallbackDeclarations = collectDisplayAreaDeclarationsFromProjectFiles(
                project,
                regionName
        );

        for (final UiComponentNavigationData displayArea : fallbackDeclarations) {
            if (!owners.isEmpty() && !isDisplayAreaOwnedByTemplateOwner(displayArea, owners)) {
                continue;
            }
            addTargetElement(project, targets, displayArea);
        }

        return new ArrayList<>(targets);
    }

    private @NotNull List<PsiElement> resolveDisplayAreaTargetsFromProjectFilesInDumbMode(
            final @NotNull PsiFile templateFile,
            final @NotNull String regionName
    ) {
        final Project project = templateFile.getProject();
        final List<UiComponentNavigationData> owners = collectTemplateOwnerDeclarationsFromProjectFiles(templateFile);
        final Set<PsiElement> targets = new LinkedHashSet<>();

        for (final UiComponentNavigationData displayArea : collectDisplayAreaDeclarationsFromOwnerFiles(
                project,
                regionName,
                owners
        )) {
            if (isDisplayAreaOwnedByTemplateOwner(displayArea, owners)) {
                addTargetElement(project, targets, displayArea);
            }
        }

        return new ArrayList<>(targets);
    }

    private @NotNull List<UiComponentNavigationData> collectTemplateOwnerDeclarationsFromProjectFiles(
            final @NotNull PsiFile templateFile
    ) {
        final Set<String> templatePaths = getTemplateRequireJsPathsFromFilePath(templateFile);

        if (templatePaths.isEmpty()) {
            return Collections.emptyList();
        }
        final Project project = templateFile.getProject();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final List<UiComponentNavigationData> results = new ArrayList<>();

        for (final VirtualFile file : MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "xml".equals(virtualFile.getExtension())
                        || "js".equals(virtualFile.getExtension())
        )) {
            if (file.isDirectory() || !fileContainsAnyText(file, templatePaths)) {
                continue;
            }
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile == null) {
                continue;
            }
            for (final UiComponentNavigationData declaration : collectComponentDeclarations(psiFile)) {
                if (isTemplateDeclaration(declaration) && templatePaths.contains(declaration.getValue())) {
                    results.add(declaration);
                }
            }
        }

        return results;
    }

    private @NotNull List<UiComponentNavigationData> collectDisplayAreaDeclarationsFromOwnerFiles(
            final @NotNull Project project,
            final @NotNull String regionName,
            final @NotNull Collection<UiComponentNavigationData> owners
    ) {
        if (owners.isEmpty()) {
            return Collections.emptyList();
        }
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Set<String> scannedFileUrls = new LinkedHashSet<>();
        final List<UiComponentNavigationData> results = new ArrayList<>();

        for (final UiComponentNavigationData owner : owners) {
            if (!scannedFileUrls.add(owner.getFileUrl())) {
                continue;
            }
            final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(owner.getFileUrl());

            if (file == null || file.isDirectory() || !fileContainsText(file, regionName)) {
                continue;
            }
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile == null) {
                continue;
            }
            for (final UiComponentNavigationData declaration : collectComponentDeclarations(psiFile)) {
                if (UiComponentNavigationData.KIND_DISPLAY_AREA.equals(declaration.getKind())
                        && regionName.equals(declaration.getValue())) {
                    results.add(declaration);
                }
            }
        }

        return results;
    }

    private @NotNull List<UiComponentNavigationData> collectDisplayAreaDeclarationsFromProjectFiles(
            final @NotNull Project project,
            final @NotNull String regionName
    ) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Set<VirtualFile> files = new LinkedHashSet<>();
        final List<UiComponentNavigationData> results = new ArrayList<>();

        files.addAll(FileTypeIndex.getFiles(XmlFileType.INSTANCE, GlobalSearchScope.allScope(project)));
        files.addAll(FilenameIndex.getAllFilesByExt(project, "js"));
        files.addAll(MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "xml".equals(virtualFile.getExtension())
                        || "js".equals(virtualFile.getExtension())
        ));

        for (final VirtualFile file : files) {
            if (file.isDirectory() || !fileContainsText(file, regionName)) {
                continue;
            }
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile == null) {
                continue;
            }
            for (final UiComponentNavigationData declaration : collectComponentDeclarations(psiFile)) {
                if (UiComponentNavigationData.KIND_DISPLAY_AREA.equals(declaration.getKind())
                        && regionName.equals(declaration.getValue())) {
                    results.add(declaration);
                }
            }
        }

        return results;
    }

    private @NotNull Set<String> getTemplateRequireJsPathsFromFilePath(final @NotNull PsiFile psiFile) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        final Set<String> result = new LinkedHashSet<>();

        if (virtualFile == null || !"html".equals(virtualFile.getExtension())) {
            return result;
        }
        final String filePath = virtualFile.getPath();

        addAppCodeTemplateRequireJsPaths(result, filePath);
        addVendorTemplateRequireJsPaths(result, filePath);

        return result;
    }

    private void addAppCodeTemplateRequireJsPaths(
            final @NotNull Set<String> result,
            final @NotNull String filePath
    ) {
        final String marker = "/app/code/";
        final int markerIndex = filePath.indexOf(marker);

        if (markerIndex < 0) {
            return;
        }
        final String relativePath = filePath.substring(markerIndex + marker.length());
        final String[] parts = relativePath.split("/");

        if (parts.length < 7 || !"view".equals(parts[2]) || !"web".equals(parts[4])) {
            return;
        }
        addTemplateRequireJsPathAliases(
                result,
                parts[0] + "_" + parts[1],
                stripHtmlExtension(joinPath(parts, 5))
        );
    }

    private void addVendorTemplateRequireJsPaths(
            final @NotNull Set<String> result,
            final @NotNull String filePath
    ) {
        final String marker = "/vendor/";
        final int markerIndex = filePath.indexOf(marker);

        if (markerIndex < 0) {
            return;
        }
        final String relativePath = filePath.substring(markerIndex + marker.length());
        final String[] parts = relativePath.split("/");

        if (parts.length < 7 || !"view".equals(parts[2]) || !"web".equals(parts[4])) {
            return;
        }
        addTemplateRequireJsPathAliases(
                result,
                composerPackageToModuleName(parts[0], parts[1]),
                stripHtmlExtension(joinPath(parts, 5))
        );
    }

    private void addTemplateRequireJsPathAliases(
            final @NotNull Set<String> result,
            final @NotNull String moduleName,
            final @NotNull String relativePath
    ) {
        result.add(moduleName + "/" + relativePath);
        addTemplateDirectoryAlias(result, moduleName, relativePath, "template");
        addTemplateDirectoryAlias(result, moduleName, relativePath, "templates");
    }

    private void addTemplateDirectoryAlias(
            final @NotNull Set<String> result,
            final @NotNull String moduleName,
            final @NotNull String relativePath,
            final @NotNull String directory
    ) {
        final String prefix = directory + "/";

        if (relativePath.startsWith(prefix)) {
            result.add(moduleName + "/" + relativePath.substring(prefix.length()));
        }
    }

    private @NotNull String joinPath(
            final @NotNull String[] parts,
            final int startIndex
    ) {
        final StringBuilder result = new StringBuilder();

        for (int index = startIndex; index < parts.length; index++) {
            if (result.length() > 0) {
                result.append('/');
            }
            result.append(parts[index]);
        }

        return result.toString();
    }

    private @NotNull String stripHtmlExtension(final @NotNull String path) {
        return path.endsWith(".html")
                ? path.substring(0, path.length() - ".html".length())
                : path;
    }

    private boolean fileContainsAnyText(
            final @NotNull VirtualFile file,
            final @NotNull Collection<String> texts
    ) {
        try {
            final String fileText = VfsUtilCore.loadText(file);

            for (final String text : texts) {
                if (fileText.contains(text)) {
                    return true;
                }
            }
        } catch (IOException exception) {
            return false;
        }

        return false;
    }

    private boolean fileContainsText(
            final @NotNull VirtualFile file,
            final @NotNull String text
    ) {
        try {
            return VfsUtilCore.loadText(file).contains(text);
        } catch (IOException exception) {
            return false;
        }
    }

    private void addTargetElement(
            final @NotNull Project project,
            final @NotNull Collection<PsiElement> targets,
            final @NotNull UiComponentNavigationData declaration
    ) {
        final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(declaration.getFileUrl());

        if (file == null || file.isDirectory()) {
            return;
        }
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        if (psiFile == null) {
            return;
        }
        final PsiElement target = declaration.getValueOffset() >= 0
                ? psiFile.findElementAt(declaration.getValueOffset())
                : psiFile;

        targets.add(normalizeNavigationTarget(target == null ? psiFile : target));
    }

    private @NotNull PsiElement normalizeNavigationTarget(final @NotNull PsiElement target) {
        final PsiComment comment = PsiTreeUtil.getParentOfType(target, PsiComment.class, false);

        if (comment != null) {
            return comment;
        }
        final PsiElement jsProperty = findJsPropertyAncestor(target);

        if (jsProperty != null) {
            return jsProperty;
        }
        final XmlAttributeValue attributeValue = PsiTreeUtil.getParentOfType(target, XmlAttributeValue.class, false);

        if (attributeValue != null) {
            return attributeValue;
        }
        final XmlTag xmlTag = PsiTreeUtil.getParentOfType(target, XmlTag.class, false);

        return xmlTag == null ? target : xmlTag;
    }

    private static @Nullable PsiElement findJsPropertyAncestor(final @NotNull PsiElement target) {
        PsiElement current = target;

        while (current != null && !(current instanceof PsiFile)) {
            if (implementsClassOrInterface(current.getClass(), "com.intellij.lang.javascript.psi.JSProperty")) {
                return current;
            }
            current = current.getParent();
        }

        return null;
    }

    private static boolean implementsClassOrInterface(
            final @Nullable Class<?> type,
            final @NotNull String expectedName
    ) {
        if (type == null) {
            return false;
        }
        if (expectedName.equals(type.getName())) {
            return true;
        }
        for (final Class<?> iface : type.getInterfaces()) {
            if (implementsClassOrInterface(iface, expectedName)) {
                return true;
            }
        }

        return implementsClassOrInterface(type.getSuperclass(), expectedName);
    }

    private boolean overlapsValue(
            final int startOffset,
            final int endOffset,
            final @NotNull UiComponentNavigationData declaration
    ) {
        final int valueStartOffset = declaration.getValueOffset();
        final int valueEndOffset = valueStartOffset + declaration.getValue().length();

        return declaration.getValueOffset() >= 0
                && startOffset <= valueEndOffset
                && endOffset >= valueStartOffset;
    }

    private @NotNull List<UiComponentNavigationData> collectJsComponentDeclarations(
            final @NotNull PsiFile jsFile
    ) {
        final VirtualFile virtualFile = jsFile.getVirtualFile() != null
                ? jsFile.getVirtualFile()
                : jsFile.getViewProvider().getVirtualFile();
        return collectJsComponentDeclarations(virtualFile, jsFile.getText());
    }

    private @NotNull List<UiComponentNavigationData> collectJsComponentDeclarations(
            final @NotNull VirtualFile virtualFile,
            final @NotNull CharSequence text
    ) {
        return collectJsComponentDeclarations(virtualFile, text, 0);
    }

    private @NotNull List<UiComponentNavigationData> collectJsComponentDeclarations(
            final @NotNull VirtualFile virtualFile,
            final @NotNull CharSequence text,
            final int baseOffset
    ) {
        final Set<UiComponentNavigationData> results = new LinkedHashSet<>();
        final String fileUrl = virtualFile.getUrl();
        final String fileComponentPath = getRequireJsPathFromFilePath(virtualFile);
        final JsAst ast = MagentoJsParser.parse(text);

        collectJsObjectComponentDeclarations(ast, fileUrl, fileComponentPath, baseOffset, results);
        collectDynamicJsLayoutChildDeclarations(ast, fileUrl, fileComponentPath, baseOffset, results);
        collectJsFileDefaultDeclarations(ast, fileUrl, fileComponentPath, baseOffset, results);

        return new ArrayList<>(results);
    }

    private void collectJsObjectComponentDeclarations(
            final @NotNull JsAst ast,
            final @NotNull String fileUrl,
            final @Nullable String fileComponentPath,
            final int baseOffset,
            final @NotNull Set<UiComponentNavigationData> results
    ) {
        for (final JsProperty property : ast.getProperties()) {
            if (property.getValueObject() == null || !isJsComponentObject(property)) {
                continue;
            }
            final String componentName = buildJsComponentName(property);

            if (componentName == null) {
                continue;
            }
            final String parentComponentName = getParentComponentName(componentName);
            final JsObject objectLiteral = property.getValueObject();
            final String componentJsPath = getDirectJsStringProperty(objectLiteral, "component");
            final String parentComponentJsPath = getParentJsComponentPath(property);
            results.add(new UiComponentNavigationData(
                    fileUrl,
                    UiComponentNavigationData.KIND_COMPONENT,
                    componentName,
                    componentName,
                    parentComponentName,
                    componentJsPath == null ? fileComponentPath : componentJsPath,
                    parentComponentJsPath,
                    null,
                    baseOffset + property.getKeyStartOffset()
            ));

            addJsComponentValueDeclarations(
                    results,
                    objectLiteral,
                    fileUrl,
                    baseOffset,
                    componentName,
                    parentComponentName,
                    componentJsPath == null ? fileComponentPath : componentJsPath,
                    parentComponentJsPath
            );
        }
    }

    private void collectJsFileDefaultDeclarations(
            final @NotNull JsAst ast,
            final @NotNull String fileUrl,
            final @Nullable String fileComponentPath,
            final int baseOffset,
            final @NotNull Set<UiComponentNavigationData> results
    ) {
        for (final JsProperty property : ast.getProperties()) {
            if (isInsideJsComponentObject(property)) {
                continue;
            }
            if (isInsideDynamicJsLayoutChildObject(property)) {
                continue;
            }
            final String propertyName = property.getName();

            if ("displayArea".equals(propertyName)) {
                addJsStringDeclaration(
                        results,
                        property,
                        fileUrl,
                        baseOffset,
                        UiComponentNavigationData.KIND_DISPLAY_AREA,
                        fileComponentPath,
                        null,
                        fileComponentPath,
                        null,
                        null
                );
            } else if ("template".equals(propertyName)) {
                addJsStringDeclaration(
                        results,
                        property,
                        fileUrl,
                        baseOffset,
                        UiComponentNavigationData.KIND_TEMPLATE,
                        fileComponentPath,
                        null,
                        fileComponentPath,
                        null,
                        null
                );
            } else if ("childTemplate".equals(propertyName)) {
                addJsStringDeclaration(
                        results,
                        property,
                        fileUrl,
                        baseOffset,
                        UiComponentNavigationData.KIND_CHILD_TEMPLATE,
                        fileComponentPath,
                        null,
                        fileComponentPath,
                        null,
                        null
                );
            } else if ("elementTmpl".equals(propertyName)) {
                addJsStringDeclaration(
                        results,
                        property,
                        fileUrl,
                        baseOffset,
                        UiComponentNavigationData.KIND_ELEMENT_TEMPLATE,
                        fileComponentPath,
                        null,
                        fileComponentPath,
                        null,
                        null
                );
            } else if ("templates".equals(propertyName)) {
                addJsTemplatesCollectionDeclarations(
                        results,
                        property,
                        fileUrl,
                        baseOffset,
                        fileComponentPath,
                        null,
                        fileComponentPath,
                        null
                );
            }
        }
    }

    private void collectDynamicJsLayoutChildDeclarations(
            final @NotNull JsAst ast,
            final @NotNull String fileUrl,
            final @Nullable String fileComponentPath,
            final int baseOffset,
            final @NotNull Set<UiComponentNavigationData> results
    ) {
        for (final JsObject objectLiteral : ast.getObjects()) {
            if (!isDynamicJsLayoutChildObject(objectLiteral)) {
                continue;
            }
            final String scopeComponentPath = fileComponentPath != null
                    ? fileComponentPath
                    : getDirectJsStringProperty(objectLiteral, "component");
            final String componentName = buildDynamicJsLayoutComponentName(
                    objectLiteral,
                    scopeComponentPath != null ? scopeComponentPath : "dynamic",
                    baseOffset
            );
            final String componentJsPath = getDirectJsStringProperty(objectLiteral, "component");

            results.add(new UiComponentNavigationData(
                    fileUrl,
                    UiComponentNavigationData.KIND_COMPONENT,
                    componentName,
                    componentName,
                    fileComponentPath,
                    componentJsPath,
                    fileComponentPath,
                    null,
                    baseOffset + objectLiteral.getStartOffset()
            ));
            addJsComponentValueDeclarations(
                    results,
                    objectLiteral,
                    fileUrl,
                    baseOffset,
                    componentName,
                    fileComponentPath,
                    componentJsPath,
                    fileComponentPath
            );
        }
    }

    private void addJsComponentValueDeclarations(
            final @NotNull Set<UiComponentNavigationData> results,
            final @NotNull JsObject objectLiteral,
            final @NotNull String fileUrl,
            final int baseOffset,
            final @NotNull String componentName,
            final @Nullable String parentComponentName,
            final @Nullable String componentJsPath,
            final @Nullable String parentComponentJsPath
    ) {
        final JsProperty displayArea = objectLiteral.findProperty("displayArea");
        final JsProperty template = findJsProperty(objectLiteral, "template");
        final JsProperty childTemplate = findJsProperty(objectLiteral, "childTemplate");
        final JsProperty elementTemplate = findJsProperty(objectLiteral, "elementTmpl");
        final JsProperty templates = findJsProperty(objectLiteral, "templates");

        addJsStringDeclaration(
                results,
                displayArea,
                fileUrl,
                baseOffset,
                UiComponentNavigationData.KIND_DISPLAY_AREA,
                componentName,
                parentComponentName,
                componentJsPath,
                parentComponentJsPath,
                null
        );
        addJsStringDeclaration(
                results,
                template,
                fileUrl,
                baseOffset,
                UiComponentNavigationData.KIND_TEMPLATE,
                componentName,
                parentComponentName,
                componentJsPath,
                parentComponentJsPath,
                null
        );
        addJsStringDeclaration(
                results,
                childTemplate,
                fileUrl,
                baseOffset,
                UiComponentNavigationData.KIND_CHILD_TEMPLATE,
                componentName,
                parentComponentName,
                componentJsPath,
                parentComponentJsPath,
                null
        );
        addJsStringDeclaration(
                results,
                elementTemplate,
                fileUrl,
                baseOffset,
                UiComponentNavigationData.KIND_ELEMENT_TEMPLATE,
                componentName,
                parentComponentName,
                componentJsPath,
                parentComponentJsPath,
                null
        );
        addJsTemplatesCollectionDeclarations(
                results,
                templates,
                fileUrl,
                baseOffset,
                componentName,
                parentComponentName,
                componentJsPath,
                parentComponentJsPath
        );
    }

    private void addJsStringDeclaration(
            final @NotNull Set<UiComponentNavigationData> results,
            final @Nullable JsProperty property,
            final @NotNull String fileUrl,
            final int baseOffset,
            final @NotNull String kind,
            final @Nullable String componentName,
            final @Nullable String parentComponentName,
            final @Nullable String componentJsPath,
            final @Nullable String parentComponentJsPath,
            final @Nullable String templateKey
    ) {
        if (property == null || property.getValueObject() != null) {
            return;
        }
        final String value = property.getStringValue();

        if (value == null) {
            return;
        }
        final String normalizedValue = normalizeTemplateValueIfNeeded(kind, value);

        if (normalizedValue == null || normalizedValue.isBlank()) {
            return;
        }
        results.add(new UiComponentNavigationData(
                fileUrl,
                kind,
                normalizedValue,
                componentName,
                parentComponentName,
                componentJsPath,
                parentComponentJsPath,
                templateKey,
                baseOffset + property.getValueStartOffset() + 1
        ));
    }

    private void addJsTemplatesCollectionDeclarations(
            final @NotNull Set<UiComponentNavigationData> results,
            final @Nullable JsProperty templatesProperty,
            final @NotNull String fileUrl,
            final int baseOffset,
            final @Nullable String componentName,
            final @Nullable String parentComponentName,
            final @Nullable String componentJsPath,
            final @Nullable String parentComponentJsPath
    ) {
        if (templatesProperty == null || templatesProperty.getValueObject() == null) {
            return;
        }
        final JsObject templatesObject = templatesProperty.getValueObject();

        for (final JsProperty templateEntry : templatesObject.getProperties()) {
            addJsStringDeclaration(
                    results,
                    templateEntry,
                    fileUrl,
                    baseOffset,
                    UiComponentNavigationData.KIND_TEMPLATES,
                    componentName,
                    parentComponentName,
                    componentJsPath,
                    parentComponentJsPath,
                    templateEntry.getName()
            );
        }
    }

    private @Nullable JsProperty findJsProperty(
            final @NotNull JsObject objectLiteral,
            final @NotNull String propertyName
    ) {
        final JsProperty directProperty = objectLiteral.findProperty(propertyName);

        if (directProperty != null) {
            return directProperty;
        }
        final JsProperty configProperty = objectLiteral.findProperty("config");

        if (configProperty == null || configProperty.getValueObject() == null) {
            return null;
        }

        return configProperty.getValueObject().findProperty(propertyName);
    }

    private @Nullable String getDirectJsStringProperty(
            final @NotNull JsObject objectLiteral,
            final @NotNull String propertyName
    ) {
        final JsProperty property = objectLiteral.findProperty(propertyName);

        return property == null ? null : property.getStringValue();
    }

    private @Nullable String getDirectJsPropertyText(
            final @NotNull JsObject objectLiteral,
            final @NotNull String propertyName
    ) {
        final JsProperty property = objectLiteral.findProperty(propertyName);

        if (property == null || property.getRawValueText() == null) {
            return null;
        }

        return property.getRawValueText().trim();
    }

    private boolean isJsComponentObject(final @NotNull JsProperty property) {
        final String name = property.getName();

        if (name == null || JS_CONTAINER_NAMES.contains(name) || JS_VALUE_PROPERTY_NAMES.contains(name)) {
            return false;
        }
        final JsObject objectLiteral = property.getValueObject();

        if (objectLiteral == null) {
            return false;
        }
        if (objectLiteral.findProperty("children") != null
                || objectLiteral.findProperty("components") != null
                || objectLiteral.findProperty("component") != null
                || objectLiteral.findProperty("displayArea") != null
                || objectLiteral.findProperty("childTemplate") != null
                || objectLiteral.findProperty("elementTmpl") != null) {
            return true;
        }

        return isInsideJsUiComponentContainer(property)
                && (hasStaticJsTemplateDeclaration(objectLiteral)
                || objectLiteral.findProperty("config") != null);
    }

    private boolean isInsideJsComponentObject(final @NotNull JsProperty property) {
        JsProperty current = property.getParentProperty();

        while (current != null) {
            if (current.getValueObject() != null && isJsComponentObject(current)) {
                return true;
            }
            current = current.getParentProperty();
        }

        return false;
    }

    private boolean isInsideDynamicJsLayoutChildObject(final @NotNull JsProperty property) {
        JsObject current = property.getContainingObject();

        while (current != null) {
            if (isDynamicJsLayoutChildObject(current)) {
                return true;
            }
            final JsProperty parentProperty = current.getParentProperty();
            current = parentProperty == null ? null : parentProperty.getContainingObject();
        }

        return false;
    }

    private boolean isDynamicJsLayoutChildObject(final @NotNull JsObject objectLiteral) {
        return objectLiteral.findProperty("displayArea") != null
                && objectLiteral.findProperty("parent") != null
                && "this.name".equals(getDirectJsPropertyText(objectLiteral, "parent"));
    }

    private @NotNull String buildDynamicJsLayoutComponentName(
            final @NotNull JsObject objectLiteral,
            final @NotNull String fileComponentPath,
            final int baseOffset
    ) {
        final String name = getDirectJsStringProperty(objectLiteral, "name");

        if (name != null && !name.isBlank()) {
            return fileComponentPath + "." + name;
        }

        return fileComponentPath + ".dynamic." + (baseOffset + objectLiteral.getStartOffset());
    }

    private boolean isInsideJsUiComponentContainer(final @NotNull JsProperty property) {
        JsProperty current = property.getParentProperty();

        while (current != null) {
            final String name = current.getName();

            if ("components".equals(name) || "children".equals(name)) {
                return true;
            }
            current = current.getParentProperty();
        }

        return false;
    }

    private boolean hasStaticJsTemplateDeclaration(final @NotNull JsObject objectLiteral) {
        final JsProperty template = findJsProperty(objectLiteral, "template");
        final JsProperty elementTemplate = findJsProperty(objectLiteral, "elementTmpl");

        return isStaticJsTemplateDeclaration(template, UiComponentNavigationData.KIND_TEMPLATE)
                || isStaticJsTemplateDeclaration(elementTemplate, UiComponentNavigationData.KIND_ELEMENT_TEMPLATE);
    }

    private boolean isStaticJsTemplateDeclaration(
            final @Nullable JsProperty property,
            final @NotNull String kind
    ) {
        if (property == null || property.getValueObject() != null) {
            return false;
        }
        final String rawValue = property.getStringValue();

        return normalizeTemplateValueIfNeeded(kind, rawValue) != null;
    }

    private @Nullable String buildJsComponentName(final @NotNull JsProperty property) {
        final List<String> segments = new ArrayList<>();
        JsProperty current = property;

        while (current != null) {
            final String name = current.getName();

            if (name != null && !JS_CONTAINER_NAMES.contains(name) && !JS_VALUE_PROPERTY_NAMES.contains(name)) {
                segments.add(name);
            }
            current = current.getParentProperty();
        }
        Collections.reverse(segments);

        return segments.isEmpty() ? null : String.join(".", segments);
    }

    private @Nullable String getParentJsComponentPath(final @NotNull JsProperty property) {
        JsProperty current = property.getParentProperty();

        while (current != null) {
            if (current.getValueObject() != null) {
                final String componentPath = getDirectJsStringProperty(
                        current.getValueObject(),
                        "component"
                );

                if (componentPath != null) {
                    return componentPath;
                }
            }
            current = current.getParentProperty();
        }

        return null;
    }

    private @NotNull List<UiComponentNavigationData> collectXmlComponentDeclarations(
            final @NotNull XmlFile xmlFile
    ) {
        if (!isXmlNavigationCandidate(xmlFile)) {
            return Collections.emptyList();
        }
        final String fileUrl = getFileUrl(xmlFile);
        final List<XmlComponentNode> nodes = collectXmlComponentNodes(xmlFile);
        final Map<String, XmlComponentNode> nodesByName = new LinkedHashMap<>();
        final Set<UiComponentNavigationData> results = new LinkedHashSet<>();

        for (final XmlComponentNode node : nodes) {
            nodesByName.put(node.getComponentName(), node);
        }
        for (final XmlComponentNode node : nodes) {
            final XmlComponentNode parent = node.getParentComponentName() == null
                    ? null
                    : nodesByName.get(node.getParentComponentName());
            final String parentJsPath = parent == null ? null : parent.getComponentJsPath();

            addXmlNodeDeclarations(results, fileUrl, node, parentJsPath);
        }

        return new ArrayList<>(results);
    }

    private @NotNull List<UiComponentNavigationData> collectPhpComponentDeclarations(
            final @NotNull PsiFile psiFile
    ) {
        final VirtualFile virtualFile = psiFile.getVirtualFile() != null
                ? psiFile.getVirtualFile()
                : psiFile.getViewProvider().getVirtualFile();
        return collectPhpComponentDeclarations(virtualFile, psiFile.getText());
    }

    private @NotNull List<UiComponentNavigationData> collectPhpComponentDeclarations(
            final @NotNull VirtualFile virtualFile,
            final @NotNull CharSequence text
    ) {
        final Set<UiComponentNavigationData> results = new LinkedHashSet<>();
        final String fileUrl = virtualFile.getUrl();

        for (final KnockoutRegionResolver.LayoutComponentDeclaration declaration
                : KnockoutRegionResolver.getInstance().collectPhpLayoutComponentDeclarations(null, text.toString())) {
            final String componentPath = declaration.getComponentPath();
            final String templatePath = KnockoutTemplatePathResolver.getInstance()
                    .normalizeTemplatePath(declaration.getTemplatePath());

            if (templatePath == null || declaration.getTemplateOffset() < 0) {
                continue;
            }
            results.add(new UiComponentNavigationData(
                    fileUrl,
                    UiComponentNavigationData.KIND_TEMPLATE,
                    templatePath,
                    componentPath,
                    null,
                    componentPath,
                    null,
                    null,
                    declaration.getTemplateOffset()
            ));
        }

        return new ArrayList<>(results);
    }

    private boolean isXmlNavigationCandidate(final @NotNull XmlFile xmlFile) {
        final VirtualFile virtualFile = xmlFile.getVirtualFile();

        if (virtualFile == null || virtualFile.getFileType() != XmlFileType.INSTANCE) {
            return false;
        }
        final String path = virtualFile.getPath();

        return path.contains("/layout/")
                || path.contains("/ui_component/")
                || xmlFile.getText().contains("jsLayout")
                || xmlFile.getText().contains("displayArea")
                || xmlFile.getText().contains("childTemplate");
    }

    private @NotNull List<XmlComponentNode> collectXmlComponentNodes(final @NotNull XmlFile xmlFile) {
        final List<XmlComponentNode> nodes = new ArrayList<>();

        for (final XmlTag tag : PsiTreeUtil.findChildrenOfType(xmlFile, XmlTag.class)) {
            if (!isXmlComponentTag(tag)) {
                continue;
            }
            final String componentName = buildXmlComponentName(tag);

            if (componentName == null) {
                continue;
            }
            nodes.add(new XmlComponentNode(
                    tag,
                    componentName,
                    getParentComponentName(componentName),
                    getXmlValue(tag, "component")
            ));
        }

        return nodes;
    }

    private void addXmlNodeDeclarations(
            final @NotNull Set<UiComponentNavigationData> results,
            final @NotNull String fileUrl,
            final @NotNull XmlComponentNode node,
            final @Nullable String parentComponentJsPath
    ) {
        results.add(new UiComponentNavigationData(
                fileUrl,
                UiComponentNavigationData.KIND_COMPONENT,
                node.getComponentJsPath() == null ? node.getComponentName() : node.getComponentJsPath(),
                node.getComponentName(),
                node.getParentComponentName(),
                node.getComponentJsPath(),
                parentComponentJsPath,
                null,
                node.getComponentJsPath() == null
                        ? node.getTag().getTextRange().getStartOffset()
                        : getXmlValueOffset(node.getTag(), "component")
        ));
        addXmlValueDeclaration(
                results,
                fileUrl,
                UiComponentNavigationData.KIND_DISPLAY_AREA,
                node,
                parentComponentJsPath,
                null,
                getXmlValue(node.getTag(), "displayArea"),
                getXmlValueOffset(node.getTag(), "displayArea")
        );
        addXmlValueDeclaration(
                results,
                fileUrl,
                UiComponentNavigationData.KIND_TEMPLATE,
                node,
                parentComponentJsPath,
                null,
                getXmlTemplateValue(node.getTag(), "template"),
                getXmlTemplateValueOffset(node.getTag(), "template")
        );
        addXmlValueDeclaration(
                results,
                fileUrl,
                UiComponentNavigationData.KIND_CHILD_TEMPLATE,
                node,
                parentComponentJsPath,
                null,
                getXmlTemplateValue(node.getTag(), "childTemplate"),
                getXmlTemplateValueOffset(node.getTag(), "childTemplate")
        );
        addXmlValueDeclaration(
                results,
                fileUrl,
                UiComponentNavigationData.KIND_ELEMENT_TEMPLATE,
                node,
                parentComponentJsPath,
                null,
                getXmlTemplateValue(node.getTag(), "elementTmpl"),
                getXmlTemplateValueOffset(node.getTag(), "elementTmpl")
        );
        addXmlTemplatesCollectionDeclarations(results, fileUrl, node, parentComponentJsPath);
    }

    private void addXmlTemplatesCollectionDeclarations(
            final @NotNull Set<UiComponentNavigationData> results,
            final @NotNull String fileUrl,
            final @NotNull XmlComponentNode node,
            final @Nullable String parentComponentJsPath
    ) {
        final XmlTag templatesTag = getXmlTemplateTag(node.getTag(), "templates");

        if (templatesTag == null) {
            return;
        }
        for (final XmlTag templateItem : templatesTag.getSubTags()) {
            if (!UiComponentXml.XML_TAG_ITEM.equals(templateItem.getName())) {
                continue;
            }
            addXmlValueDeclaration(
                    results,
                    fileUrl,
                    UiComponentNavigationData.KIND_TEMPLATES,
                    node,
                    parentComponentJsPath,
                    templateItem.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME),
                    getTagValue(templateItem),
                    getTagValueOffset(templateItem)
            );
        }
    }

    private void addXmlValueDeclaration(
            final @NotNull Set<UiComponentNavigationData> results,
            final @NotNull String fileUrl,
            final @NotNull String kind,
            final @NotNull XmlComponentNode node,
            final @Nullable String parentComponentJsPath,
            final @Nullable String templateKey,
            final @Nullable String rawValue,
            final int valueOffset
    ) {
        final String value = normalizeTemplateValueIfNeeded(kind, rawValue);

        if (value == null || valueOffset < 0) {
            return;
        }
        results.add(new UiComponentNavigationData(
                fileUrl,
                kind,
                value,
                node.getComponentName(),
                node.getParentComponentName(),
                node.getComponentJsPath(),
                parentComponentJsPath,
                templateKey,
                valueOffset
        ));
    }

    private boolean isXmlComponentTag(final @NotNull XmlTag tag) {
        final String segment = getXmlComponentSegment(tag);

        if (segment == null) {
            return false;
        }
        if (hasNamedItemAncestor(tag, "templates") || hasNamedItemAncestor(tag, "config")) {
            return false;
        }

        return getXmlValue(tag, "component") != null
                || getXmlValue(tag, "displayArea") != null
                || getXmlTemplateValue(tag, "template") != null
                || getXmlTemplateValue(tag, "childTemplate") != null
                || getXmlTemplateValue(tag, "elementTmpl") != null
                || getXmlTemplateTag(tag, "templates") != null
                || getDirectChildItem(tag, "children") != null
                || getDirectChildItem(tag, "components") != null;
    }

    private @Nullable String buildXmlComponentName(final @NotNull XmlTag tag) {
        final List<String> segments = new ArrayList<>();
        PsiElement current = tag;

        while (current instanceof XmlTag) {
            final String segment = getXmlComponentSegment((XmlTag) current);

            if (segment != null) {
                segments.add(segment);
            }
            current = current.getParent();
        }
        Collections.reverse(segments);

        return segments.isEmpty() ? null : String.join(".", segments);
    }

    private @Nullable String getXmlComponentSegment(final @NotNull XmlTag tag) {
        if (UiComponentXml.XML_TAG_ITEM.equals(tag.getName())) {
            final String name = tag.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME);

            if (name == null || XML_CONTAINER_ITEM_NAMES.contains(name) || XML_VALUE_ITEM_NAMES.contains(name)) {
                return null;
            }

            return name;
        }
        final String nameAttribute = tag.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME);

        if (nameAttribute == null || nameAttribute.isBlank()) {
            return null;
        }

        return nameAttribute;
    }

    private @Nullable String getParentComponentName(final @NotNull String componentName) {
        final int dotIndex = componentName.lastIndexOf('.');

        return dotIndex < 0 ? null : componentName.substring(0, dotIndex);
    }

    private @Nullable String getXmlTemplateValue(
            final @NotNull XmlTag tag,
            final @NotNull String itemName
    ) {
        final String attributeValue = tag.getAttributeValue(itemName);

        if (attributeValue != null) {
            return attributeValue;
        }
        final XmlTag templateTag = getXmlTemplateTag(tag, itemName);

        return templateTag == null ? null : getTagValue(templateTag);
    }

    private int getXmlTemplateValueOffset(
            final @NotNull XmlTag tag,
            final @NotNull String itemName
    ) {
        final XmlAttribute attribute = tag.getAttribute(itemName);

        if (attribute != null && attribute.getValueElement() != null) {
            return attribute.getValueElement().getTextRange().getStartOffset() + 1;
        }
        final XmlTag templateTag = getXmlTemplateTag(tag, itemName);

        return templateTag == null ? -1 : getTagValueOffset(templateTag);
    }

    private @Nullable XmlTag getXmlTemplateTag(
            final @NotNull XmlTag tag,
            final @NotNull String itemName
    ) {
        final XmlTag directValueTag = getDirectChildTag(tag, itemName);

        if (directValueTag != null) {
            return directValueTag;
        }
        final XmlTag directTag = getDirectChildItem(tag, itemName);

        if (directTag != null) {
            return directTag;
        }
        final XmlTag configTag = getDirectChildItem(tag, "config");

        if (configTag != null) {
            final XmlTag configItemTag = getDirectChildItem(configTag, itemName);

            if (configItemTag != null) {
                return configItemTag;
            }
            final XmlTag configValueTag = getDirectChildTag(configTag, itemName);

            if (configValueTag != null) {
                return configValueTag;
            }
        }
        final XmlTag settingsTag = getDirectChildTag(tag, "settings");

        return settingsTag == null ? null : getDirectChildTag(settingsTag, itemName);
    }

    private @Nullable String getXmlValue(
            final @NotNull XmlTag tag,
            final @NotNull String key
    ) {
        final String attributeValue = tag.getAttributeValue(key);

        if (attributeValue != null) {
            return attributeValue;
        }
        final String directItemValue = getDirectChildItemValue(tag, key);

        if (directItemValue != null) {
            return directItemValue;
        }
        final XmlTag directValueTag = getDirectChildTag(tag, key);

        if (directValueTag != null) {
            return getTagValue(directValueTag);
        }
        final XmlTag configTag = getDirectChildItem(tag, "config");

        if (configTag != null) {
            final String configItemValue = getDirectChildItemValue(configTag, key);

            if (configItemValue != null) {
                return configItemValue;
            }
            final XmlTag configValueTag = getDirectChildTag(configTag, key);

            if (configValueTag != null) {
                return getTagValue(configValueTag);
            }
        }
        final XmlTag settingsTag = getDirectChildTag(tag, "settings");

        if (settingsTag != null) {
            final XmlTag settingsValueTag = getDirectChildTag(settingsTag, key);

            if (settingsValueTag != null) {
                return getTagValue(settingsValueTag);
            }
        }

        return null;
    }

    private int getXmlValueOffset(
            final @NotNull XmlTag tag,
            final @NotNull String key
    ) {
        final XmlAttribute attribute = tag.getAttribute(key);

        if (attribute != null && attribute.getValueElement() != null) {
            return attribute.getValueElement().getTextRange().getStartOffset() + 1;
        }
        final XmlTag child = getDirectChildItem(tag, key);

        if (child != null) {
            return getTagValueOffset(child);
        }
        final XmlTag directValueTag = getDirectChildTag(tag, key);

        if (directValueTag != null) {
            return getTagValueOffset(directValueTag);
        }
        final XmlTag configTag = getDirectChildItem(tag, "config");

        if (configTag != null) {
            final XmlTag configItemTag = getDirectChildItem(configTag, key);

            if (configItemTag != null) {
                return getTagValueOffset(configItemTag);
            }
            final XmlTag configValueTag = getDirectChildTag(configTag, key);

            if (configValueTag != null) {
                return getTagValueOffset(configValueTag);
            }
        }
        final XmlTag settingsTag = getDirectChildTag(tag, "settings");

        if (settingsTag != null) {
            final XmlTag settingsValueTag = getDirectChildTag(settingsTag, key);

            if (settingsValueTag != null) {
                return getTagValueOffset(settingsValueTag);
            }
        }

        return -1;
    }

    private @Nullable XmlTag getDirectChildTag(
            final @NotNull XmlTag parent,
            final @NotNull String tagName
    ) {
        for (final XmlTag child : parent.getSubTags()) {
            if (tagName.equals(child.getName())) {
                return child;
            }
        }

        return null;
    }

    private @Nullable XmlTag getDirectChildItem(
            final @NotNull XmlTag parent,
            final @NotNull String itemName
    ) {
        for (final XmlTag child : parent.getSubTags()) {
            if (UiComponentXml.XML_TAG_ITEM.equals(child.getName())
                    && itemName.equals(child.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME))) {
                return child;
            }
        }

        return null;
    }

    private @Nullable String getDirectChildItemValue(
            final @NotNull XmlTag parent,
            final @NotNull String itemName
    ) {
        final XmlTag item = getDirectChildItem(parent, itemName);

        return item == null ? null : getTagValue(item);
    }

    private @Nullable String getTagValue(final @NotNull XmlTag tag) {
        final String value = tag.getValue().getTrimmedText();

        return value.isBlank() ? null : value;
    }

    private int getTagValueOffset(final @NotNull XmlTag tag) {
        final String valueText = tag.getValue().getText();
        final String trimmedValue = tag.getValue().getTrimmedText();

        if (trimmedValue.isBlank()) {
            return -1;
        }
        final int valueStartOffset = tag.getValue().getTextRange().getStartOffset();
        final int trimOffset = valueText.indexOf(trimmedValue);

        return valueStartOffset + Math.max(trimOffset, 0);
    }

    private boolean hasNamedItemAncestor(
            final @NotNull XmlTag tag,
            final @NotNull String itemName
    ) {
        XmlTag parent = tag.getParentTag();

        while (parent != null) {
            if (UiComponentXml.XML_TAG_ITEM.equals(parent.getName())
                    && itemName.equals(parent.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME))) {
                return true;
            }
            parent = parent.getParentTag();
        }

        return false;
    }

    private @NotNull String getFileUrl(final @NotNull PsiFile psiFile) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        return virtualFile == null ? psiFile.getViewProvider().getVirtualFile().getUrl() : virtualFile.getUrl();
    }

    private @Nullable String getRequireJsPathFromFilePath(final @NotNull PsiFile jsFile) {
        return getRequireJsPathFromFilePath(jsFile.getVirtualFile());
    }

    public @Nullable String getRequireJsPathFromFilePath(final @Nullable VirtualFile virtualFile) {
        if (virtualFile == null || !"js".equalsIgnoreCase(virtualFile.getExtension())) {
            return null;
        }
        final String filePath = virtualFile.getPath();
        final String appCodePath = getAppCodeRequireJsPath(filePath);

        if (appCodePath != null) {
            return appCodePath;
        }
        final String themePath = getThemeRequireJsPath(filePath);

        if (themePath != null) {
            return themePath;
        }

        return getVendorRequireJsPath(filePath);
    }

    private static boolean isJsFile(final @Nullable PsiFile psiFile) {
        if (psiFile == null) {
            return false;
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile != null) {
            return "js".equalsIgnoreCase(virtualFile.getExtension());
        }

        return psiFile.getName().toLowerCase().endsWith(".js");
    }

    private @Nullable String getAppCodeRequireJsPath(final @NotNull String filePath) {
        final String marker = "/app/code/";
        final int markerIndex = filePath.indexOf(marker);

        if (markerIndex < 0) {
            return null;
        }
        final String relativePath = filePath.substring(markerIndex + marker.length());
        final String[] parts = relativePath.split("/");

        if (parts.length < 7 || !"view".equals(parts[2]) || !"web".equals(parts[4])) {
            return null;
        }

        return parts[0] + "_" + parts[1] + "/" + stripJsExtension(joinParts(parts, 5));
    }

    private @Nullable String getVendorRequireJsPath(final @NotNull String filePath) {
        final String marker = "/vendor/";
        final int markerIndex = filePath.indexOf(marker);

        if (markerIndex < 0) {
            return null;
        }
        final String relativePath = filePath.substring(markerIndex + marker.length());
        final String[] parts = relativePath.split("/");

        if (parts.length < 7 || !"view".equals(parts[2]) || !"web".equals(parts[4])) {
            return null;
        }
        final String moduleName = composerPackageToModuleName(parts[0], parts[1]);

        return moduleName + "/" + stripJsExtension(joinParts(parts, 5));
    }

    private @Nullable String getThemeRequireJsPath(final @NotNull String filePath) {
        final String marker = "/app/design/";
        final int markerIndex = filePath.indexOf(marker);

        if (markerIndex < 0) {
            return null;
        }
        final String relativePath = filePath.substring(markerIndex + marker.length());
        final String[] parts = relativePath.split("/");

        if (parts.length < 7 || !"web".equals(parts[4])) {
            return null;
        }

        return parts[3] + "/" + stripJsExtension(joinParts(parts, 5));
    }

    private @NotNull String composerPackageToModuleName(
            final @NotNull String vendor,
            final @NotNull String packageName
    ) {
        final String modulePart = packageName.startsWith("module-")
                ? packageName.substring("module-".length())
                : packageName;

        return kebabToPascal(vendor) + "_" + kebabToPascal(modulePart);
    }

    private @NotNull String kebabToPascal(final @NotNull String value) {
        final StringBuilder result = new StringBuilder();

        for (final String part : value.split("-")) {
            if (part.isBlank()) {
                continue;
            }
            result.append(Character.toUpperCase(part.charAt(0)));

            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }

        return result.toString();
    }

    private @NotNull String joinParts(
            final @NotNull String[] parts,
            final int startIndex
    ) {
        final List<String> result = new ArrayList<>();

        for (int i = startIndex; i < parts.length; i++) {
            result.add(parts[i]);
        }

        return String.join("/", result);
    }

    private @NotNull String stripJsExtension(final @NotNull String path) {
        return path.endsWith(".js") ? path.substring(0, path.length() - ".js".length()) : path;
    }

    private @Nullable String normalizeValue(final @Nullable String rawValue) {
        if (rawValue == null) {
            return null;
        }
        final String trimmed = rawValue.trim();

        if (trimmed.length() < 2 || !isQuotedText(trimmed)) {
            return null;
        }

        return trimmed.substring(1, trimmed.length() - 1);
    }

    public @Nullable String normalizeValueForKind(
            final @NotNull String kind,
            final @Nullable String rawValue
    ) {
        return normalizeTemplateValueIfNeeded(kind, rawValue);
    }

    private @Nullable String normalizeTemplateValueIfNeeded(
            final @NotNull String kind,
            final @Nullable String rawValue
    ) {
        if (rawValue == null) {
            return null;
        }
        if (UiComponentNavigationData.KIND_TEMPLATE.equals(kind)
                || UiComponentNavigationData.KIND_CHILD_TEMPLATE.equals(kind)
                || UiComponentNavigationData.KIND_TEMPLATES.equals(kind)
                || UiComponentNavigationData.KIND_ELEMENT_TEMPLATE.equals(kind)) {
            return KnockoutTemplatePathResolver.getInstance().normalizeTemplatePath(rawValue);
        }

        return rawValue;
    }

    private boolean isQuotedText(final @NotNull String text) {
        final String trimmed = text.trim();

        return (trimmed.startsWith("'") && trimmed.endsWith("'"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\""));
    }

    private static class XmlComponentNode {
        private final XmlTag tag;
        private final String componentName;
        private final String parentComponentName;
        private final String componentJsPath;

        XmlComponentNode(
                final @NotNull XmlTag tag,
                final @NotNull String componentName,
                final @Nullable String parentComponentName,
                final @Nullable String componentJsPath
        ) {
            this.tag = tag;
            this.componentName = componentName;
            this.parentComponentName = parentComponentName;
            this.componentJsPath = componentJsPath;
        }

        @NotNull XmlTag getTag() {
            return tag;
        }

        @NotNull String getComponentName() {
            return componentName;
        }

        @Nullable String getParentComponentName() {
            return parentComponentName;
        }

        @Nullable String getComponentJsPath() {
            return componentJsPath;
        }
    }
}
