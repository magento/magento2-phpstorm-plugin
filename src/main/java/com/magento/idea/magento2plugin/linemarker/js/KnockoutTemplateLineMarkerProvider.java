/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.KnockoutTemplateIndex;
import com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutRegionResolver;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutTemplatePathResolver;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnockoutTemplateLineMarkerProvider implements LineMarkerProvider {
    private static final String TEMPLATE_TOOLTIP_TEXT = "Navigate to Knockout template";
    private static final String COMPONENT_TOOLTIP_TEXT = "Navigate to Knockout component";
    private static final String REGION_COMPONENT_TOOLTIP_TEXT = "Navigate to region components";
    private static final String REGION_CHILD_TEMPLATE_TOOLTIP_TEXT = "Navigate to child Knockout templates";
    private static final String REGION_TEMPLATE_TOOLTIP_TEXT = "Navigate to region templates";

    public KnockoutTemplateLineMarkerProvider() {
        NavigationInstrumentation.infoOnce(
                "ko-template-linemarker-instantiated",
                () -> "Knockout template line marker provider instantiated"
        );
    }

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        final PsiFile psiFile = psiElement.getContainingFile();

        if (!isSupportedFile(psiFile)) {
            return null;
        }
        if (!Settings.isEnabled(psiElement.getProject())) {
            NavigationInstrumentation.infoOnce(
                    "ko-template-linemarker-disabled-" + psiElement.getProject().getLocationHash(),
                    () -> "Knockout template line markers skipped: Magento support disabled; "
                            + NavigationInstrumentation.describeSettings(psiElement.getProject())
            );
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
            if (!psiElements.isEmpty()) {
                NavigationInstrumentation.infoOnce(
                        "ko-template-slow-linemarker-disabled-"
                                + psiElements.get(0).getProject().getLocationHash(),
                        () -> "Knockout template slow line markers skipped: "
                                + NavigationInstrumentation.describeSettings(psiElements.get(0).getProject())
                );
            }
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
        addLayoutDeclarationLineMarkers(psiFile, collection);
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
        collection.add(NavigationGutterIconBuilder
                .create(HtmlFileType.INSTANCE.getIcon())
                .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(targets))
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(REGION_TEMPLATE_TOOLTIP_TEXT)
                .createLineMarkerInfo(anchor));
    }

    private void addGetRegionLineMarkers(
            final @NotNull PsiFile psiFile,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        for (final KnockoutRegionResolver.RegionMatch regionMatch : KnockoutRegionResolver.getInstance()
                .collectGetRegionMatches(psiFile.getText())) {
            final List<PsiElement> targets = KnockoutRegionResolver.getInstance()
                    .resolveDisplayAreaComponentFiles(psiFile.getProject(), regionMatch.getRegionName());
            final List<PsiElement> childTemplates = collectDisplayAreaTemplates(
                    psiFile.getProject(),
                    regionMatch.getRegionName(),
                    targets
            );

            if (targets.isEmpty() && childTemplates.isEmpty()) {
                NavigationInstrumentation.info(
                        "Knockout getRegion marker skipped: no component targets for '"
                                + regionMatch.getRegionName() + "' in "
                                + NavigationInstrumentation.describeFile(psiFile)
                );
                continue;
            }
            final PsiElement anchor = psiFile.findElementAt(regionMatch.getStartOffset());

            if (anchor == null) {
                NavigationInstrumentation.info(
                        "Knockout getRegion marker skipped: no PSI anchor for '"
                                + regionMatch.getRegionName() + "' at offset "
                                + regionMatch.getStartOffset() + " in "
                                + NavigationInstrumentation.describeFile(psiFile)
                );
                continue;
            }

            if (!childTemplates.isEmpty()) {
                collection.add(NavigationGutterIconBuilder
                        .create(HtmlFileType.INSTANCE.getIcon())
                        .setTargets(prepareFileTargets(childTemplates))
                        .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                        .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                        .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                        .setTooltipText(REGION_CHILD_TEMPLATE_TOOLTIP_TEXT)
                        .createLineMarkerInfo(anchor));
                NavigationInstrumentation.info(
                        "Knockout getRegion marker created for '"
                                + regionMatch.getRegionName() + "' in "
                                + NavigationInstrumentation.describeFile(psiFile)
                                + " childTemplateTargets=" + childTemplates.size()
                                + " componentTargets=" + targets.size()
                );
                continue;
            }
            collection.add(NavigationGutterIconBuilder
                    .create(JavaScriptFileType.INSTANCE.getIcon())
                    .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(targets))
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(REGION_COMPONENT_TOOLTIP_TEXT)
                    .createLineMarkerInfo(anchor));
            NavigationInstrumentation.info(
                    "Knockout getRegion marker created for '"
                            + regionMatch.getRegionName() + "' in "
                            + NavigationInstrumentation.describeFile(psiFile)
                            + " componentTargets=" + targets.size()
                            + " childTemplateTargets=0"
            );
        }
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
                NavigationInstrumentation.info(
                        "Knockout displayArea marker skipped: no template targets for '"
                                + displayArea + "' in "
                                + NavigationInstrumentation.describeFile(jsFile)
                );
                continue;
            }
            final ASTNode nameIdentifier = property.findNameIdentifier();
            final PsiElement anchor = nameIdentifier == null ? property : nameIdentifier.getPsi();

            collection.add(NavigationGutterIconBuilder
                    .create(HtmlFileType.INSTANCE.getIcon())
                    .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(targets))
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(REGION_TEMPLATE_TOOLTIP_TEXT)
                    .createLineMarkerInfo(anchor));
            NavigationInstrumentation.info(
                    "Knockout displayArea marker created for '"
                            + displayArea + "' in "
                            + NavigationInstrumentation.describeFile(jsFile)
                            + " templateTargets=" + targets.size()
            );
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
            final List<PsiElement> templates = collectTemplates((JSFile) psiFile);

            if (templates.isEmpty()) {
                NavigationInstrumentation.infoOnce(
                        "ko-template-linemarker-empty-" + NavigationInstrumentation.describeFile(psiFile),
                        () -> "Knockout component line marker has no template targets for "
                                + NavigationInstrumentation.describeFile(psiFile)
                );
                return null;
            }
            NavigationInstrumentation.infoOnce(
                    "ko-template-linemarker-created-" + NavigationInstrumentation.describeFile(psiFile),
                    () -> "Knockout component line marker created for "
                            + NavigationInstrumentation.describeFile(psiFile)
                            + " templates=" + templates.size()
            );

            return NavigationGutterIconBuilder
                    .create(HtmlFileType.INSTANCE.getIcon())
                    .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(templates))
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(TEMPLATE_TOOLTIP_TEXT)
                    .createLineMarkerInfo(anchor);
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null || !"html".equals(virtualFile.getExtension())) {
            return null;
        }
        final List<PsiElement> components = collectComponents(psiFile);

        if (components.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "ko-component-linemarker-empty-" + NavigationInstrumentation.describeFile(psiFile),
                    () -> "Knockout template line marker has no component targets for "
                            + NavigationInstrumentation.describeFile(psiFile)
            );
            return null;
        }
        NavigationInstrumentation.infoOnce(
                "ko-component-linemarker-created-" + NavigationInstrumentation.describeFile(psiFile),
                () -> "Knockout template line marker created for "
                        + NavigationInstrumentation.describeFile(psiFile)
                        + " components=" + components.size()
        );

        return NavigationGutterIconBuilder
                .create(JavaScriptFileType.INSTANCE.getIcon())
                .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(components))
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(COMPONENT_TOOLTIP_TEXT)
                .createLineMarkerInfo(anchor);
    }

    private @NotNull List<PsiElement> collectTemplates(final @NotNull JSFile jsFile) {
        final List<PsiElement> results = new ArrayList<>();

        for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                .collectTemplatePaths(jsFile)) {
            results.addAll(KnockoutTemplatePathResolver.getInstance()
                    .resolveTemplateFiles(jsFile.getProject(), templatePath));
        }
        NavigationInstrumentation.infoOnce(
                "ko-template-linemarker-collected-" + NavigationInstrumentation.describeFile(jsFile),
                () -> "Knockout component templates collected for "
                        + NavigationInstrumentation.describeFile(jsFile)
                        + " targets=" + results.size()
        );

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
        addComponentsFromMagentoVfs(project, results, KnockoutTemplatePathResolver.getInstance()
                .getTemplateRequireJsPaths(psiFile));
        addComponentsFromLayoutDeclarations(project, results, KnockoutTemplatePathResolver.getInstance()
                .getTemplateRequireJsPaths(psiFile));
        NavigationInstrumentation.infoOnce(
                "ko-component-linemarker-collected-" + NavigationInstrumentation.describeFile(psiFile),
                () -> "Knockout template components collected for "
                        + NavigationInstrumentation.describeFile(psiFile)
                        + " targets=" + results.size()
        );

        return results;
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
        int scannedFiles = 0;

        for (final VirtualFile file : MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "js".equals(virtualFile.getExtension())
        )) {
            scannedFiles++;
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
        final int finalScannedFiles = scannedFiles;
        NavigationInstrumentation.infoOnce(
                "ko-component-linemarker-vfs-" + project.getLocationHash() + "-" + templatePaths.hashCode(),
                () -> "Magento VFS Knockout component scan scannedJsFiles=" + finalScannedFiles
                        + " templatePathAliases=" + templatePaths.size()
                        + " totalTargets=" + results.size()
        );
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
                || (virtualFile != null && (
                        "html".equals(virtualFile.getExtension())
                                || "xml".equals(virtualFile.getExtension())
                                || "php".equals(virtualFile.getExtension())
                ));
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
}
