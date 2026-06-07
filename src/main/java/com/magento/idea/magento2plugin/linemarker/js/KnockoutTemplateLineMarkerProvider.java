/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.KnockoutTemplateIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutRegionResolver;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutTemplatePathResolver;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentTemplateResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnockoutTemplateLineMarkerProvider implements LineMarkerProvider {
    private static final String TEMPLATE_TOOLTIP_TEXT = "Navigate to Knockout template";
    private static final String COMPONENT_TOOLTIP_TEXT = "Navigate to Knockout component";
    private static final String XML_COMPONENT_USAGE_TOOLTIP_TEXT = "Navigate to XML UI component usage";
    private static final String XML_TEMPLATE_USAGE_TOOLTIP_TEXT = "Navigate to XML template usage";
    private static final String MAGENTO_TEMPLATE_TOOLTIP_TEXT = "Navigate to Magento template";
    private static final String MAGENTO_UI_COMPONENT_TOOLTIP_TEXT = "Navigate to Magento UI component";
    private static final String REGION_COMPONENT_TOOLTIP_TEXT = "Navigate to region components";
    private static final String REGION_CHILD_TEMPLATE_TOOLTIP_TEXT = "Navigate to child Knockout templates";
    private static final String REGION_TEMPLATE_TOOLTIP_TEXT = "Navigate to region templates";
    private static final String REGION_DISPLAY_AREA_TOOLTIP_TEXT = "Navigate to displayArea declaration";

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        final PsiFile psiFile = psiElement.getContainingFile();

        if (!isSupportedFile(psiFile)) {
            return null;
        }
        if (!Settings.isEnabled(psiElement.getProject())) {
            return null;
        }
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(psiFile);

        if (!psiElement.equals(anchor)) {
            return null;
        }

        return createLineMarker(anchor, psiFile);
    }

    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> psiElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (psiElements.isEmpty() || !Settings.isEnabled(psiElements.get(0).getProject())) {
            return;
        }
        final Set<PsiFile> processedFiles = new HashSet<>();

        for (final PsiElement psiElement : psiElements) {
            final PsiFile psiFile = psiElement.getContainingFile();

            if (!isSupportedFile(psiFile) || processedFiles.contains(psiFile)) {
                continue;
            }
            processedFiles.add(psiFile);

            addRegionLineMarkers(psiFile, collection);
        }
    }

    private void addRegionLineMarkers(
            final @NotNull PsiFile psiFile,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (psiFile instanceof JSFile) {
            addDisplayAreaLineMarkers((JSFile) psiFile, collection);
            return;
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile != null && "html".equals(virtualFile.getExtension())) {
            addGetRegionLineMarkers(psiFile, collection);
            return;
        }
        if (psiFile instanceof XmlFile) {
            addXmlUiComponentLineMarkers((XmlFile) psiFile, collection);
        }
    }

    private void addXmlUiComponentLineMarkers(
            final @NotNull XmlFile xmlFile,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final Set<String> processedDeclarations = new HashSet<>();

        for (final UiComponentNavigationData declaration : UiComponentScopeResolver.getInstance()
                .collectComponentDeclarations(xmlFile)) {
            if (!processedDeclarations.add(declaration.getKind() + ":" + declaration.getValueOffset())) {
                continue;
            }
            if (UiComponentNavigationData.KIND_COMPONENT.equals(declaration.getKind())) {
                addXmlComponentLineMarker(xmlFile, declaration, collection);
            } else if (isTemplateDeclaration(declaration)) {
                addXmlTemplateLineMarker(xmlFile, declaration, collection);
            }
        }
    }

    private void addXmlComponentLineMarker(
            final @NotNull XmlFile xmlFile,
            final @NotNull UiComponentNavigationData declaration,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (declaration.getComponentJsPath() == null || declaration.getValueOffset() < 0) {
            return;
        }
        final List<PsiElement> targets = RequireJsPathResolver.getInstance()
                .resolveJsFilesOrAlias(xmlFile.getProject(), declaration.getComponentJsPath());

        if (targets.isEmpty()) {
            return;
        }
        final PsiElement anchor = xmlFile.findElementAt(declaration.getValueOffset());

        if (anchor == null) {
            return;
        }
        final List<PsiElement> preparedTargets = LineMarkerTargetPresentationUtil.prepareTargets(targets);

        collection.add(NavigationGutterIconBuilder
                .create(JavaScriptFileType.INSTANCE.getIcon())
                .setTargets(preparedTargets)
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(MAGENTO_UI_COMPONENT_TOOLTIP_TEXT)
                .createLineMarkerInfo(
                        anchor,
                        LineMarkerTargetPresentationUtil.createNavigationHandler(
                                preparedTargets,
                                MAGENTO_UI_COMPONENT_TOOLTIP_TEXT
                        )
                ));
    }

    private void addXmlTemplateLineMarker(
            final @NotNull XmlFile xmlFile,
            final @NotNull UiComponentNavigationData declaration,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (declaration.getValueOffset() < 0) {
            return;
        }
        final List<PsiElement> targets = KnockoutTemplatePathResolver.getInstance()
                .resolveTemplateFiles(xmlFile.getProject(), declaration.getValue());

        if (targets.isEmpty()) {
            return;
        }
        final PsiElement anchor = xmlFile.findElementAt(declaration.getValueOffset());

        if (anchor == null) {
            return;
        }
        final List<PsiElement> preparedTargets = LineMarkerTargetPresentationUtil.prepareTargets(targets);

        collection.add(NavigationGutterIconBuilder
                .create(HtmlFileType.INSTANCE.getIcon())
                .setTargets(preparedTargets)
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(MAGENTO_TEMPLATE_TOOLTIP_TEXT)
                .createLineMarkerInfo(
                        anchor,
                        LineMarkerTargetPresentationUtil.createNavigationHandler(
                                preparedTargets,
                                MAGENTO_TEMPLATE_TOOLTIP_TEXT
                        )
                ));
    }

    private void addLayoutDeclarationLineMarkers(
            final @NotNull PsiFile psiFile,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        for (final KnockoutRegionResolver.LayoutComponentDeclaration declaration
                : KnockoutRegionResolver.getInstance().collectLayoutComponentDeclarations(psiFile)) {
            addLayoutTemplateLineMarker(declaration, collection);
            addLayoutDisplayAreaLineMarker(declaration, collection);
        }
    }

    private void addLayoutTemplateLineMarker(
            final @NotNull KnockoutRegionResolver.LayoutComponentDeclaration declaration,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (declaration.getTemplatePath() == null || declaration.getTemplateOffset() < 0) {
            return;
        }
        final List<PsiElement> targets = KnockoutTemplatePathResolver.getInstance().resolveTemplateFiles(
                declaration.getSourceFile().getProject(),
                declaration.getTemplatePath()
        );

        if (targets.isEmpty()) {
            return;
        }
        final PsiElement anchor = declaration.getSourceFile().findElementAt(declaration.getTemplateOffset());

        if (anchor == null) {
            return;
        }
        collection.add(NavigationGutterIconBuilder
                .create(HtmlFileType.INSTANCE.getIcon())
                .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(targets))
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(TEMPLATE_TOOLTIP_TEXT)
                .createLineMarkerInfo(anchor));
    }

    private void addLayoutDisplayAreaLineMarker(
            final @NotNull KnockoutRegionResolver.LayoutComponentDeclaration declaration,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (declaration.getDisplayArea() == null || declaration.getDisplayAreaOffset() < 0) {
            return;
        }
        final List<PsiElement> targets = KnockoutRegionResolver.getInstance().resolveGetRegionTemplateFiles(
                declaration.getSourceFile().getProject(),
                declaration.getDisplayArea()
        );

        if (targets.isEmpty()) {
            return;
        }
        final PsiElement anchor = declaration.getSourceFile().findElementAt(declaration.getDisplayAreaOffset());

        if (anchor == null) {
            return;
        }
        final List<PsiElement> preparedTargets = LineMarkerTargetPresentationUtil.prepareTargets(targets);

        collection.add(NavigationGutterIconBuilder
                .create(HtmlFileType.INSTANCE.getIcon())
                .setTargets(preparedTargets)
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(REGION_TEMPLATE_TOOLTIP_TEXT)
                .createLineMarkerInfo(
                        anchor,
                        LineMarkerTargetPresentationUtil.createNavigationHandler(
                                preparedTargets,
                                REGION_TEMPLATE_TOOLTIP_TEXT
                        )
                ));
    }

    private void addGetRegionLineMarkers(
            final @NotNull PsiFile psiFile,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (DumbService.isDumb(psiFile.getProject())) {
            return;
        }
        for (final KnockoutRegionResolver.RegionMatch regionMatch : KnockoutRegionResolver.getInstance()
                .collectGetRegionMatches(psiFile.getText())) {
            final List<PsiElement> targets = UiComponentScopeResolver.getInstance()
                    .resolveDisplayAreaTargetsForGetRegion(psiFile, regionMatch.getRegionName());

            if (targets.isEmpty()) {
                if (addLegacyGetRegionLineMarker(psiFile, collection, regionMatch)) {
                    continue;
                }
                continue;
            }
            final List<PsiElement> navigationTargets = new ArrayList<>(targets);
            List<PsiElement> childTemplateTargets = UiComponentScopeResolver.getInstance()
                    .resolveTemplateFilesForDisplayAreaTargets(psiFile.getProject(), targets);

            if (childTemplateTargets.isEmpty()) {
                childTemplateTargets = collectDisplayAreaTemplates(
                        psiFile.getProject(),
                        regionMatch.getRegionName(),
                        KnockoutRegionResolver.getInstance().resolveDisplayAreaComponentFiles(
                                psiFile.getProject(),
                                regionMatch.getRegionName()
                        )
                );
            }
            navigationTargets.addAll(childTemplateTargets);
            final PsiElement anchor = psiFile.findElementAt(regionMatch.getStartOffset());

            if (anchor == null) {
                continue;
            }
            final List<PsiElement> preparedTargets =
                    LineMarkerTargetPresentationUtil.prepareTargets(navigationTargets);

            collection.add(NavigationGutterIconBuilder
                    .create(HtmlFileType.INSTANCE.getIcon())
                    .setTargets(preparedTargets)
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(REGION_DISPLAY_AREA_TOOLTIP_TEXT)
                    .createLineMarkerInfo(
                            anchor,
                            LineMarkerTargetPresentationUtil.createNavigationHandler(
                                    preparedTargets,
                                    REGION_DISPLAY_AREA_TOOLTIP_TEXT
                            )
                    ));
        }
    }

    private boolean addLegacyGetRegionLineMarker(
            final @NotNull PsiFile psiFile,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection,
            final @NotNull KnockoutRegionResolver.RegionMatch regionMatch
    ) {
        final List<PsiElement> componentTargets = KnockoutRegionResolver.getInstance()
                .resolveDisplayAreaComponentFiles(psiFile.getProject(), regionMatch.getRegionName());
        final List<PsiElement> childTemplates = collectDisplayAreaTemplates(
                psiFile.getProject(),
                regionMatch.getRegionName(),
                componentTargets
        );

        if (componentTargets.isEmpty() && childTemplates.isEmpty()) {
            return false;
        }
        final PsiElement anchor = psiFile.findElementAt(regionMatch.getStartOffset());

        if (anchor == null) {
            return false;
        }

        if (!childTemplates.isEmpty()) {
            final List<PsiElement> preparedChildTemplates = prepareFileTargets(childTemplates);

            collection.add(NavigationGutterIconBuilder
                    .create(HtmlFileType.INSTANCE.getIcon())
                    .setTargets(preparedChildTemplates)
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(REGION_CHILD_TEMPLATE_TOOLTIP_TEXT)
                    .createLineMarkerInfo(
                            anchor,
                            LineMarkerTargetPresentationUtil.createNavigationHandler(
                                    preparedChildTemplates,
                                    REGION_CHILD_TEMPLATE_TOOLTIP_TEXT
                            )
                    ));
            return true;
        }
        final List<PsiElement> preparedComponentTargets = LineMarkerTargetPresentationUtil.prepareTargets(componentTargets);

        collection.add(NavigationGutterIconBuilder
                .create(JavaScriptFileType.INSTANCE.getIcon())
                .setTargets(preparedComponentTargets)
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(REGION_COMPONENT_TOOLTIP_TEXT)
                .createLineMarkerInfo(
                        anchor,
                        LineMarkerTargetPresentationUtil.createNavigationHandler(
                                preparedComponentTargets,
                                REGION_COMPONENT_TOOLTIP_TEXT
                        )
                ));

        return true;
    }

    private void addDisplayAreaLineMarkers(
            final @NotNull JSFile jsFile,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final Collection<JSProperty> properties = PsiTreeUtil.findChildrenOfType(jsFile, JSProperty.class);

        for (final JSProperty property : properties) {
            final String displayArea = KnockoutRegionResolver.getInstance().getDisplayArea(property);

            if (displayArea == null) {
                continue;
            }
            final List<PsiElement> targets = KnockoutRegionResolver.getInstance()
                    .resolveGetRegionTemplateFiles(jsFile.getProject(), displayArea);

            if (targets.isEmpty()) {
                continue;
            }
            final ASTNode nameIdentifier = property.findNameIdentifier();
            final PsiElement anchor = nameIdentifier == null ? property : nameIdentifier.getPsi();
            final List<PsiElement> preparedTargets = LineMarkerTargetPresentationUtil.prepareTargets(targets);

            collection.add(NavigationGutterIconBuilder
                    .create(HtmlFileType.INSTANCE.getIcon())
                    .setTargets(preparedTargets)
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(REGION_TEMPLATE_TOOLTIP_TEXT)
                    .createLineMarkerInfo(
                            anchor,
                            LineMarkerTargetPresentationUtil.createNavigationHandler(
                                    preparedTargets,
                                    REGION_TEMPLATE_TOOLTIP_TEXT
                            )
                    ));
        }
    }

    private @NotNull List<PsiElement> collectTemplatesFromComponents(
            final @NotNull Collection<PsiElement> components
    ) {
        final List<PsiElement> results = new ArrayList<>();

        for (final PsiElement component : components) {
            if (component instanceof JSFile) {
                results.addAll(collectTemplates((JSFile) component));
            }
        }

        return results;
    }

    private @NotNull List<PsiElement> collectDisplayAreaTemplates(
            final @NotNull Project project,
            final @NotNull String displayArea,
            final @NotNull Collection<PsiElement> components
    ) {
        final List<PsiElement> results = new ArrayList<>(KnockoutRegionResolver.getInstance()
                .resolveDisplayAreaTemplateFiles(project, displayArea));

        results.addAll(collectTemplatesFromComponents(components));

        return prepareFileTargets(results);
    }

    private @Nullable LineMarkerInfo<?> createLineMarker(
            final @NotNull PsiElement anchor,
            final @NotNull PsiFile psiFile
    ) {
        if (psiFile instanceof JSFile) {
            final ComponentNavigationTargets componentTargets = collectComponentNavigationTargets((JSFile) psiFile);

            if (componentTargets.getTargets().isEmpty()) {
                return null;
            }
            final String tooltip = componentTargets.hasTemplateTargets()
                    ? TEMPLATE_TOOLTIP_TEXT
                    : XML_COMPONENT_USAGE_TOOLTIP_TEXT;
            final List<PsiElement> preparedTargets =
                    LineMarkerTargetPresentationUtil.prepareTargets(componentTargets.getTargets());

            return NavigationGutterIconBuilder
                    .create(componentTargets.hasTemplateTargets()
                            ? HtmlFileType.INSTANCE.getIcon()
                            : XmlFileType.INSTANCE.getIcon())
                    .setTargets(preparedTargets)
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(tooltip)
                    .createLineMarkerInfo(
                            anchor,
                            LineMarkerTargetPresentationUtil.createNavigationHandler(preparedTargets, tooltip)
                    );
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null || !"html".equals(virtualFile.getExtension())) {
            return null;
        }
        final TemplateNavigationTargets templateTargets = collectTemplateNavigationTargets(psiFile);

        if (templateTargets.getTargets().isEmpty()) {
            return null;
        }
        final String tooltip = templateTargets.hasComponentTargets()
                ? COMPONENT_TOOLTIP_TEXT
                : XML_TEMPLATE_USAGE_TOOLTIP_TEXT;
        final List<PsiElement> preparedTargets =
                LineMarkerTargetPresentationUtil.prepareJsFileTargets(templateTargets.getTargets());

        return NavigationGutterIconBuilder
                .create(templateTargets.hasComponentTargets()
                        ? JavaScriptFileType.INSTANCE.getIcon()
                        : XmlFileType.INSTANCE.getIcon())
                .setTargets(preparedTargets)
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(tooltip)
                .createLineMarkerInfo(
                        anchor,
                        LineMarkerTargetPresentationUtil.createNavigationHandler(preparedTargets, tooltip)
                );
    }

    private @NotNull ComponentNavigationTargets collectComponentNavigationTargets(final @NotNull JSFile jsFile) {
        final Set<PsiElement> results = new LinkedHashSet<>();
        boolean hasTemplateTargets = false;

        for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                .collectTemplatePaths(jsFile)) {
            final List<PsiElement> templateFiles = KnockoutTemplatePathResolver.getInstance()
                    .resolveTemplateFiles(jsFile.getProject(), templatePath);

            if (!templateFiles.isEmpty()) {
                hasTemplateTargets = true;
            }
            results.addAll(templateFiles);
        }
        final String componentJsPath = UiComponentScopeResolver.getInstance().getRequireJsPath(jsFile);

        if (componentJsPath != null && !componentJsPath.isBlank()) {
            final List<UiComponentNavigationData> componentDeclarations = UiComponentScopeResolver.getInstance()
                    .componentDeclarationsByJsPath(jsFile.getProject(), componentJsPath);
            addTargetsExcludingSourceFile(
                    jsFile,
                    results,
                    UiComponentScopeResolver.getInstance()
                            .resolveNavigationTargets(jsFile.getProject(), componentDeclarations)
            );

            for (final UiComponentNavigationData templateDeclaration : UiComponentTemplateResolver.getInstance()
                    .resolveTemplateDeclarationsForComponentJsPath(jsFile.getProject(), componentJsPath)) {
                final List<PsiElement> templateFiles = KnockoutTemplatePathResolver.getInstance()
                        .resolveTemplateFiles(jsFile.getProject(), templateDeclaration.getValue());

                if (!templateFiles.isEmpty()) {
                    hasTemplateTargets = true;
                }
                results.addAll(templateFiles);
                addTargetsExcludingSourceFile(
                        jsFile,
                        results,
                        UiComponentScopeResolver.getInstance()
                                .resolveNavigationTargets(jsFile.getProject(), List.of(templateDeclaration))
                );
            }
        }

        return new ComponentNavigationTargets(new ArrayList<>(results), hasTemplateTargets);
    }

    private @NotNull List<PsiElement> collectTemplates(final @NotNull JSFile jsFile) {
        final List<PsiElement> results = new ArrayList<>();

        for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                .collectTemplatePaths(jsFile)) {
            results.addAll(KnockoutTemplatePathResolver.getInstance()
                    .resolveTemplateFiles(jsFile.getProject(), templatePath));
        }

        return results;
    }

    private @NotNull List<PsiElement> collectComponents(final @NotNull PsiFile psiFile) {
        final List<PsiElement> results = new ArrayList<>();
        final Project project = psiFile.getProject();

        for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                .getTemplateRequireJsPaths(psiFile)) {
            final Collection<Set<String>> componentPathSets = FileBasedIndex.getInstance()
                    .getValues(
                            KnockoutTemplateIndex.KEY,
                            templatePath,
                            GlobalSearchScope.allScope(project)
                    );

            for (final Set<String> componentPaths : componentPathSets) {
                for (final String componentUrl : componentPaths) {
                    addComponentIfFound(project, results, componentUrl);
                }
            }
        }

        return results;
    }

    private @NotNull TemplateNavigationTargets collectTemplateNavigationTargets(final @NotNull PsiFile psiFile) {
        final Set<PsiElement> results = new LinkedHashSet<>(collectComponents(psiFile));
        boolean hasComponentTargets = !results.isEmpty();
        final Set<String> templatePaths = KnockoutTemplatePathResolver.getInstance()
                .getTemplateRequireJsPaths(psiFile);

        for (final String templatePath : templatePaths) {
            for (final UiComponentNavigationData declaration : UiComponentTemplateResolver.getInstance()
                    .resolveTemplateDeclarationsForTemplatePath(psiFile.getProject(), templatePath)) {
                addTemplateDeclarationTarget(psiFile, results, declaration);
                final String componentJsPath = declaration.getComponentJsPath();

                if (componentJsPath == null || componentJsPath.isBlank() || "uiComponent".equals(componentJsPath)) {
                    continue;
                }
                final List<PsiElement> componentFiles = RequireJsPathResolver.getInstance()
                        .resolveJsFilesOrAlias(psiFile.getProject(), componentJsPath);

                if (!componentFiles.isEmpty()) {
                    hasComponentTargets = true;
                }
                results.addAll(componentFiles);
            }
        }

        return new TemplateNavigationTargets(normalizeJsFileTargets(results), hasComponentTargets);
    }

    @NotNull List<PsiElement> normalizeJsFileTargets(final @NotNull Collection<PsiElement> targets) {
        final Set<PsiElement> results = new LinkedHashSet<>();

        for (final PsiElement target : targets) {
            final PsiFile containingFile = target instanceof PsiFile
                    ? (PsiFile) target
                    : target.getContainingFile();

            if (containingFile instanceof JSFile) {
                results.add(containingFile);
                continue;
            }
            results.add(target);
        }

        return new ArrayList<>(results);
    }

    private void addTemplateDeclarationTarget(
            final @NotNull PsiFile sourceFile,
            final @NotNull Collection<PsiElement> results,
            final @NotNull UiComponentNavigationData declaration
    ) {
        final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(declaration.getFileUrl());

        if (file == null || file.isDirectory()) {
            return;
        }
        final PsiFile declarationFile = PsiManager.getInstance(sourceFile.getProject()).findFile(file);

        if (declarationFile == null || declarationFile.equals(sourceFile)) {
            return;
        }
        if (declarationFile instanceof JSFile) {
            results.add(declarationFile);
            return;
        }
        addTargetsExcludingSourceFile(
                sourceFile,
                results,
                UiComponentScopeResolver.getInstance()
                        .resolveNavigationTargets(sourceFile.getProject(), List.of(declaration))
        );
    }

    private void addTargetsExcludingSourceFile(
            final @NotNull PsiFile sourceFile,
            final @NotNull Collection<PsiElement> results,
            final @NotNull Collection<PsiElement> targets
    ) {
        for (final PsiElement target : targets) {
            if (target.getContainingFile() != null && target.getContainingFile().equals(sourceFile)) {
                continue;
            }
            results.add(target);
        }
    }

    private void addComponentIfFound(
            final @NotNull Project project,
            final @NotNull Collection<PsiElement> results,
            final @NotNull String componentUrl
    ) {
        final VirtualFile file = VirtualFileManager.getInstance()
                .findFileByUrl(componentUrl);

        if (file == null || file.isDirectory()) {
            return;
        }
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        if (psiFile != null) {
            results.add(psiFile);
        }
    }

    private void addComponentsFromMagentoVfs(
            final @NotNull Project project,
            final @NotNull Collection<PsiElement> results,
            final @NotNull Set<String> templatePaths
    ) {
        if (templatePaths.isEmpty()) {
            return;
        }
        final PsiManager psiManager = PsiManager.getInstance(project);

        for (final VirtualFile file : MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "js".equals(virtualFile.getExtension())
        )) {
            final PsiFile psiFile = psiManager.findFile(file);

            if (!(psiFile instanceof JSFile)) {
                continue;
            }
            final Set<String> componentTemplatePaths = KnockoutTemplatePathResolver.getInstance()
                    .collectTemplatePaths((JSFile) psiFile);

            for (final String templatePath : templatePaths) {
                if (componentTemplatePaths.contains(templatePath)) {
                    results.add(psiFile);
                    break;
                }
            }
        }
    }

    private void addComponentsFromProjectJsFiles(
            final @NotNull Project project,
            final @NotNull Collection<PsiElement> results,
            final @NotNull Set<String> templatePaths
    ) {
        if (templatePaths.isEmpty()) {
            return;
        }
        final PsiManager psiManager = PsiManager.getInstance(project);

        for (final VirtualFile file : FileTypeIndex.getFiles(
                JavaScriptFileType.INSTANCE,
                GlobalSearchScope.allScope(project)
        )) {
            final PsiFile psiFile = psiManager.findFile(file);

            if (!(psiFile instanceof JSFile)) {
                continue;
            }
            final Set<String> componentTemplatePaths = KnockoutTemplatePathResolver.getInstance()
                    .collectTemplatePaths((JSFile) psiFile);

            for (final String templatePath : templatePaths) {
                if (componentTemplatePaths.contains(templatePath)) {
                    results.add(psiFile);
                    break;
                }
            }
        }
    }

    private void addComponentsFromLayoutDeclarations(
            final @NotNull Project project,
            final @NotNull Collection<PsiElement> results,
            final @NotNull Set<String> templatePaths
    ) {
        if (templatePaths.isEmpty()) {
            return;
        }
        for (final KnockoutRegionResolver.LayoutComponentDeclaration declaration
                : KnockoutRegionResolver.getInstance().collectLayoutComponentDeclarations(project)) {
            final String templatePath = KnockoutTemplatePathResolver.getInstance()
                    .normalizeTemplatePath(declaration.getTemplatePath());

            if (templatePath == null
                    || !templatePaths.contains(templatePath)
                    || declaration.getComponentPath() == null
                    || "uiComponent".equals(declaration.getComponentPath())) {
                continue;
            }
            results.addAll(RequireJsPathResolver.getInstance().resolveJsFiles(
                    project,
                    declaration.getComponentPath()
            ));
        }
    }

    private boolean isSupportedFile(final @NotNull PsiFile psiFile) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        return psiFile instanceof JSFile
                || psiFile instanceof XmlFile
                || (virtualFile != null && "html".equals(virtualFile.getExtension()));
    }

    private @NotNull List<PsiElement> prepareFileTargets(final @NotNull List<PsiElement> targets) {
        final List<PsiElement> fileTargets = new ArrayList<>();

        for (final PsiElement target : targets) {
            final PsiFile targetFile = target instanceof PsiFile
                    ? (PsiFile) target
                    : target.getContainingFile();

            if (targetFile != null) {
                fileTargets.add(targetFile);
            }
        }

        return LineMarkerTargetPresentationUtil.prepareTargets(fileTargets);
    }

    private boolean isTemplateDeclaration(final @NotNull UiComponentNavigationData declaration) {
        return UiComponentNavigationData.KIND_TEMPLATE.equals(declaration.getKind())
                || UiComponentNavigationData.KIND_CHILD_TEMPLATE.equals(declaration.getKind())
                || UiComponentNavigationData.KIND_TEMPLATES.equals(declaration.getKind())
                || UiComponentNavigationData.KIND_ELEMENT_TEMPLATE.equals(declaration.getKind());
    }

    private static class ComponentNavigationTargets {
        private final List<PsiElement> targets;
        private final boolean hasTemplateTargets;

        ComponentNavigationTargets(
                final @NotNull List<PsiElement> targets,
                final boolean hasTemplateTargets
        ) {
            this.targets = targets;
            this.hasTemplateTargets = hasTemplateTargets;
        }

        @NotNull List<PsiElement> getTargets() {
            return targets;
        }

        boolean hasTemplateTargets() {
            return hasTemplateTargets;
        }
    }

    private static class TemplateNavigationTargets {
        private final List<PsiElement> targets;
        private final boolean hasComponentTargets;

        TemplateNavigationTargets(
                final @NotNull List<PsiElement> targets,
                final boolean hasComponentTargets
        ) {
            this.targets = targets;
            this.hasComponentTargets = hasComponentTargets;
        }

        @NotNull List<PsiElement> getTargets() {
            return targets;
        }

        boolean hasComponentTargets() {
            return hasComponentTargets;
        }
    }
}
