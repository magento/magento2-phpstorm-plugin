/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.JsMixinIndex;
import com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsMixinLineMarkerProvider implements LineMarkerProvider {
    private static final String TARGET_TOOLTIP_TEXT = "Navigate to target JS";
    private static final String MIXINS_TOOLTIP_TEXT = "Navigate to JS mixins";
    private static final String TARGET_METHOD_TOOLTIP_TEXT = "Navigate to target method";
    private static final String MIXIN_METHOD_TOOLTIP_TEXT = "Navigate to JS mixin override";
    private static final Pattern WRAPPED_MEMBER_PATTERN = Pattern.compile(
            "\\b([A-Za-z_$][\\w$]*)\\.([A-Za-z_$][\\w$]*)\\s*=\\s*wrapper\\.wrap(?:Super)?\\(\\s*\\1\\.\\2\\b"
    );
    private static final Pattern FUNCTION_WRAPPER_PATTERN = Pattern.compile(
            "\\breturn\\s+wrapper\\.wrap\\(\\s*([A-Za-z_$][\\w$]*)\\b"
    );
    private static final Pattern EXTEND_MIXIN_OBJECT_PATTERN = Pattern.compile(
            "\\breturn\\s+[A-Za-z_$][\\w$]*\\.extend\\(\\s*([A-Za-z_$][\\w$]*)\\s*\\)"
    );
    private static final Pattern WIDGET_MIXIN_OBJECT_PATTERN = Pattern.compile(
            "\\$\\.widget\\([^;]*,\\s*([A-Za-z_$][\\w$]*)\\s*\\)"
    );

    public JsMixinLineMarkerProvider() {
        NavigationInstrumentation.infoOnce(
                "js-mixin-linemarker-instantiated",
                () -> "JS mixin line marker provider instantiated"
        );
    }

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        final PsiFile psiFile = psiElement.getContainingFile();

        if (!(psiFile instanceof JSFile)) {
            return null;
        }
        if (!Settings.isEnabled(psiElement.getProject())) {
            NavigationInstrumentation.infoOnce(
                    "js-mixin-linemarker-disabled-" + psiElement.getProject().getLocationHash(),
                    () -> "JS mixin line markers skipped: Magento support disabled; "
                            + NavigationInstrumentation.describeSettings(psiElement.getProject())
            );
            return null;
        }

        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(psiFile);

        if (!psiElement.equals(anchor)) {
            return null;
        }

        final String requireJsPath = RequireJsPathResolver.getInstance().getRequireJsPath(psiFile);

        if (requireJsPath == null) {
            NavigationInstrumentation.infoOnce(
                    "js-mixin-linemarker-no-path-" + NavigationInstrumentation.describeFile(psiFile),
                    () -> "JS mixin line marker skipped: no Magento requirejs path for "
                            + NavigationInstrumentation.describeFile(psiFile)
            );
            return null;
        }

        final List<PsiElement> targets = collectTargetsForMixin(
                psiElement.getProject(),
                requireJsPath
        );

        if (!targets.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "js-mixin-linemarker-target-created-" + requireJsPath,
                    () -> "JS mixin line marker created: mixin '" + requireJsPath
                            + "' targets=" + targets.size()
            );
            return NavigationGutterIconBuilder
                    .create(JavaScriptFileType.INSTANCE.getIcon())
                    .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(targets))
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(TARGET_TOOLTIP_TEXT)
                    .createLineMarkerInfo(anchor);
        }

        final List<PsiElement> mixins = collectMixinsForTarget(
                psiElement.getProject(),
                requireJsPath
        );

        if (!mixins.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "js-mixin-linemarker-mixins-created-" + requireJsPath,
                    () -> "JS mixin line marker created: target '" + requireJsPath
                            + "' mixins=" + mixins.size()
            );
            return NavigationGutterIconBuilder
                    .create(AllIcons.Nodes.Plugin)
                    .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(mixins))
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(MIXINS_TOOLTIP_TEXT)
                    .createLineMarkerInfo(
                            anchor,
                            LineMarkerTargetPresentationUtil.createPopupNavigationHandler(mixins, MIXINS_TOOLTIP_TEXT)
                    );
        }

        NavigationInstrumentation.infoOnce(
                "js-mixin-linemarker-empty-" + requireJsPath,
                () -> "JS mixin line marker has no related files for '" + requireJsPath + "'"
        );

        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> psiElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (psiElements.isEmpty() || !Settings.isEnabled(psiElements.get(0).getProject())) {
            if (!psiElements.isEmpty()) {
                NavigationInstrumentation.infoOnce(
                        "js-mixin-slow-linemarker-disabled-"
                                + psiElements.get(0).getProject().getLocationHash(),
                        () -> "JS mixin slow line markers skipped: "
                                + NavigationInstrumentation.describeSettings(psiElements.get(0).getProject())
                );
            }
            return;
        }

        final Set<PsiFile> processedFiles = new HashSet<>();

        for (final PsiElement psiElement : psiElements) {
            final PsiFile psiFile = psiElement.getContainingFile();

            if (!(psiFile instanceof JSFile) || processedFiles.contains(psiFile)) {
                continue;
            }
            processedFiles.add(psiFile);

            final String requireJsPath = RequireJsPathResolver.getInstance().getRequireJsPath(psiFile);

            if (requireJsPath == null) {
                NavigationInstrumentation.infoOnce(
                        "js-mixin-slow-linemarker-no-path-" + NavigationInstrumentation.describeFile(psiFile),
                        () -> "JS mixin slow line marker skipped: no Magento requirejs path for "
                                + NavigationInstrumentation.describeFile(psiFile)
                );
                continue;
            }

            addTargetLineMarker(psiElement, requireJsPath, collection);
            addMixinLineMarker(psiElement, requireJsPath, collection);
            addMixinMethodLineMarkers((JSFile) psiFile, requireJsPath, collection);
        }
    }

    private void addTargetLineMarker(
            final @NotNull PsiElement anchorContext,
            final @NotNull String requireJsPath,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> targets = collectTargetsForMixin(
                anchorContext.getProject(),
                requireJsPath
        );

        if (targets.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "js-mixin-target-linemarker-empty-" + requireJsPath,
                    () -> "JS mixin target line marker has no targets for mixin '" + requireJsPath + "'"
            );
            return;
        }

        collection.add(NavigationGutterIconBuilder
                .create(JavaScriptFileType.INSTANCE.getIcon())
                .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(targets))
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(TARGET_TOOLTIP_TEXT)
                .createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(anchorContext.getContainingFile())));
        NavigationInstrumentation.infoOnce(
                "js-mixin-target-linemarker-added-" + requireJsPath,
                () -> "JS mixin target line marker added for '" + requireJsPath
                        + "' targets=" + targets.size()
        );
    }

    private void addMixinLineMarker(
            final @NotNull PsiElement anchorContext,
            final @NotNull String requireJsPath,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> mixins = collectMixinsForTarget(
                anchorContext.getProject(),
                requireJsPath
        );

        if (mixins.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "js-mixin-mixins-linemarker-empty-" + requireJsPath,
                    () -> "JS mixin mixins line marker has no mixins for target '" + requireJsPath + "'"
            );
            return;
        }

        collection.add(NavigationGutterIconBuilder
                .create(AllIcons.Nodes.Plugin)
                .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(mixins))
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(MIXINS_TOOLTIP_TEXT)
                .createLineMarkerInfo(
                        PsiTreeUtil.getDeepestFirst(anchorContext.getContainingFile()),
                        LineMarkerTargetPresentationUtil.createPopupNavigationHandler(mixins, MIXINS_TOOLTIP_TEXT)
                ));
        NavigationInstrumentation.infoOnce(
                "js-mixin-mixins-linemarker-added-" + requireJsPath,
                () -> "JS mixin mixins line marker added for '" + requireJsPath
                        + "' mixins=" + mixins.size()
        );
    }

    private void addMixinMethodLineMarkers(
            final @NotNull JSFile jsFile,
            final @NotNull String requireJsPath,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        addMixinFileMethodLineMarkers(jsFile, requireJsPath, collection);
        addTargetFileMethodLineMarkers(jsFile, requireJsPath, collection);
    }

    private void addMixinFileMethodLineMarkers(
            final @NotNull JSFile mixinFile,
            final @NotNull String mixinPath,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<MixinOverride> overrides = collectDocumentedMixinOverrides(mixinFile);

        if (overrides.isEmpty()) {
            return;
        }
        final List<PsiElement> targetFiles = collectTargetsForMixin(mixinFile.getProject(), mixinPath);

        for (final MixinOverride override : overrides) {
            final List<PsiElement> targetMethods = new ArrayList<>();

            if (override.name == null) {
                targetMethods.addAll(targetFiles);
            } else {
                for (final PsiElement targetFile : targetFiles) {
                    if (targetFile instanceof JSFile) {
                        targetMethods.addAll(findObjectMethodAnchors((JSFile) targetFile, override.name));
                    }
                }
            }
            if (targetMethods.isEmpty()) {
                continue;
            }
            collection.add(NavigationGutterIconBuilder
                    .create(AllIcons.Nodes.Method)
                    .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(targetMethods))
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(TARGET_METHOD_TOOLTIP_TEXT)
                    .createLineMarkerInfo(override.anchor));
        }
    }

    private void addTargetFileMethodLineMarkers(
            final @NotNull JSFile targetFile,
            final @NotNull String targetPath,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> mixinFiles = collectMixinsForTarget(targetFile.getProject(), targetPath);

        if (mixinFiles.isEmpty()) {
            return;
        }
        for (final PsiElement targetMethod : findObjectMethodAnchors(targetFile)) {
            final String methodName = targetMethod.getText();
            final List<PsiElement> mixinOverrides = new ArrayList<>();

            for (final PsiElement mixinFile : mixinFiles) {
                if (!(mixinFile instanceof JSFile)) {
                    continue;
                }
                for (final MixinOverride override : collectDocumentedMixinOverrides((JSFile) mixinFile)) {
                    if (methodName.equals(override.name)) {
                        mixinOverrides.add(override.anchor);
                    }
                }
            }
            if (mixinOverrides.isEmpty()) {
                continue;
            }
            collection.add(NavigationGutterIconBuilder
                    .create(AllIcons.Nodes.Plugin)
                    .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(mixinOverrides))
                    .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                    .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                    .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                    .setTooltipText(MIXIN_METHOD_TOOLTIP_TEXT)
                    .createLineMarkerInfo(
                            targetMethod,
                            LineMarkerTargetPresentationUtil.createPopupNavigationHandler(
                                    mixinOverrides,
                                    MIXIN_METHOD_TOOLTIP_TEXT
                            )
                    ));
        }
        addTargetFileFunctionMixinLineMarker(targetFile, mixinFiles, collection);
    }

    private void addTargetFileFunctionMixinLineMarker(
            final @NotNull JSFile targetFile,
            final @NotNull List<PsiElement> mixinFiles,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> mixinOverrides = new ArrayList<>();

        for (final PsiElement mixinFile : mixinFiles) {
            if (!(mixinFile instanceof JSFile)) {
                continue;
            }
            for (final MixinOverride override : collectDocumentedMixinOverrides((JSFile) mixinFile)) {
                if (override.name == null) {
                    mixinOverrides.add(override.anchor);
                }
            }
        }
        if (mixinOverrides.isEmpty()) {
            return;
        }
        collection.add(NavigationGutterIconBuilder
                .create(AllIcons.Nodes.Plugin)
                .setTargets(LineMarkerTargetPresentationUtil.prepareTargets(mixinOverrides))
                .setNamer(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .setTargetRenderer(LineMarkerTargetPresentationUtil.TARGET_RENDERER)
                .setCellRenderer(LineMarkerTargetPresentationUtil.CELL_RENDERER)
                .setTooltipText(MIXIN_METHOD_TOOLTIP_TEXT)
                .createLineMarkerInfo(
                        PsiTreeUtil.getDeepestFirst(targetFile),
                        LineMarkerTargetPresentationUtil.createPopupNavigationHandler(
                                mixinOverrides,
                                MIXIN_METHOD_TOOLTIP_TEXT
                        )
                ));
    }

    private @NotNull List<MixinOverride> collectDocumentedMixinOverrides(final @NotNull JSFile mixinFile) {
        final List<MixinOverride> result = new ArrayList<>();
        result.addAll(collectWrappedMemberOverrides(mixinFile));
        result.addAll(collectFunctionWrapperOverrides(mixinFile));
        result.addAll(collectMixinObjectMethodOverrides(mixinFile));

        return result;
    }

    private @NotNull List<MixinOverride> collectWrappedMemberOverrides(final @NotNull JSFile mixinFile) {
        final List<MixinOverride> result = new ArrayList<>();
        final Matcher matcher = WRAPPED_MEMBER_PATTERN.matcher(mixinFile.getText());

        while (matcher.find()) {
            final PsiElement anchor = mixinFile.findElementAt(matcher.start(2));

            if (anchor != null) {
                result.add(new MixinOverride(matcher.group(2), anchor));
            }
        }

        return result;
    }

    private @NotNull List<MixinOverride> collectFunctionWrapperOverrides(final @NotNull JSFile mixinFile) {
        final List<MixinOverride> result = new ArrayList<>();
        final Matcher matcher = FUNCTION_WRAPPER_PATTERN.matcher(mixinFile.getText());

        while (matcher.find()) {
            final PsiElement anchor = mixinFile.findElementAt(matcher.start(1));

            if (anchor != null) {
                result.add(new MixinOverride(null, anchor));
            }
        }

        return result;
    }

    private @NotNull List<MixinOverride> collectMixinObjectMethodOverrides(final @NotNull JSFile mixinFile) {
        final Set<String> mixinObjectNames = collectUsedMixinObjectNames(mixinFile.getText());
        final List<MixinOverride> result = new ArrayList<>();

        if (mixinObjectNames.isEmpty()) {
            return result;
        }
        final Collection<JSVariable> variables = PsiTreeUtil.findChildrenOfType(mixinFile, JSVariable.class);

        for (final JSVariable variable : variables) {
            if (!mixinObjectNames.contains(variable.getName())) {
                continue;
            }
            final JSObjectLiteralExpression objectLiteral = PsiTreeUtil.getChildOfType(
                    variable,
                    JSObjectLiteralExpression.class
            );

            if (objectLiteral == null) {
                continue;
            }
            for (final JSProperty property : objectLiteral.getProperties()) {
                if (!isFunctionProperty(property)) {
                    continue;
                }
                final ASTNode nameIdentifier = property.findNameIdentifier();

                if (nameIdentifier != null) {
                    result.add(new MixinOverride(property.getName(), nameIdentifier.getPsi()));
                }
            }
        }

        return result;
    }

    private @NotNull Set<String> collectUsedMixinObjectNames(final @NotNull String text) {
        final Set<String> result = new HashSet<>();
        Matcher matcher = EXTEND_MIXIN_OBJECT_PATTERN.matcher(text);

        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        matcher = WIDGET_MIXIN_OBJECT_PATTERN.matcher(text);

        while (matcher.find()) {
            result.add(matcher.group(1));
        }

        return result;
    }

    private @NotNull List<PsiElement> findObjectMethodAnchors(final @NotNull JSFile jsFile) {
        final List<PsiElement> result = new ArrayList<>();
        final Collection<JSProperty> properties = PsiTreeUtil.findChildrenOfType(jsFile, JSProperty.class);

        for (final JSProperty property : properties) {
            if (!isFunctionProperty(property)) {
                continue;
            }
            final ASTNode nameIdentifier = property.findNameIdentifier();

            if (nameIdentifier != null) {
                result.add(nameIdentifier.getPsi());
            }
        }

        return result;
    }

    private @NotNull List<PsiElement> findObjectMethodAnchors(
            final @NotNull JSFile jsFile,
            final @NotNull String methodName
    ) {
        final List<PsiElement> result = new ArrayList<>();
        final Collection<JSProperty> properties = PsiTreeUtil.findChildrenOfType(jsFile, JSProperty.class);

        for (final JSProperty property : properties) {
            if (!methodName.equals(property.getName())) {
                continue;
            }
            if (!isFunctionProperty(property)) {
                continue;
            }
            final ASTNode nameIdentifier = property.findNameIdentifier();

            if (nameIdentifier != null) {
                result.add(nameIdentifier.getPsi());
            }
        }

        return result;
    }

    private boolean isFunctionProperty(final @NotNull JSProperty property) {
        final JSExpression value = property.getValue();

        return value != null && value.getText().trim().startsWith("function");
    }

    private @NotNull List<PsiElement> collectMixinsForTarget(
            final @NotNull Project project,
            final @NotNull String targetPath
    ) {
        final List<PsiElement> results = new ArrayList<>();
        final Collection<Set<String>> mixinSets = FileBasedIndex.getInstance()
                .getValues(JsMixinIndex.KEY, targetPath, GlobalSearchScope.allScope(project));

        for (final Set<String> mixins : mixinSets) {
            for (final String mixin : mixins) {
                results.addAll(RequireJsPathResolver.getInstance().resolveJsFiles(project, mixin));
            }
        }
        for (final Set<String> mixins : collectMixinDeclarationsFromMagentoVfs(project, targetPath)) {
            for (final String mixin : mixins) {
                results.addAll(RequireJsPathResolver.getInstance().resolveJsFiles(project, mixin));
            }
        }

        return results;
    }

    private @NotNull List<PsiElement> collectTargetsForMixin(
            final @NotNull Project project,
            final @NotNull String mixinPath
    ) {
        final List<PsiElement> results = new ArrayList<>();
        final Collection<String> targetPaths = FileBasedIndex.getInstance()
                .getAllKeys(JsMixinIndex.KEY, project);

        for (final String targetPath : targetPaths) {
            final Collection<Set<String>> mixinSets = FileBasedIndex.getInstance()
                    .getValues(JsMixinIndex.KEY, targetPath, GlobalSearchScope.allScope(project));

            for (final Set<String> mixins : mixinSets) {
                if (mixins.contains(mixinPath)) {
                    results.addAll(RequireJsPathResolver.getInstance().resolveJsFiles(project, targetPath));
                }
            }
        }
        for (final Map.Entry<String, Set<String>> entry : collectMixinDeclarationsFromMagentoVfs(project).entrySet()) {
            if (entry.getValue().contains(mixinPath)) {
                results.addAll(RequireJsPathResolver.getInstance().resolveJsFiles(project, entry.getKey()));
            }
        }

        return results;
    }

    private @NotNull Collection<Set<String>> collectMixinDeclarationsFromMagentoVfs(
            final @NotNull Project project,
            final @NotNull String targetPath
    ) {
        final List<Set<String>> result = new ArrayList<>();
        final Map<String, Set<String>> declarations = collectMixinDeclarationsFromMagentoVfs(project);
        final Set<String> mixins = declarations.get(targetPath);

        if (mixins != null) {
            result.add(mixins);
        }

        return result;
    }

    private @NotNull Map<String, Set<String>> collectMixinDeclarationsFromMagentoVfs(
            final @NotNull Project project
    ) {
        final Map<String, Set<String>> result = new java.util.HashMap<>();
        final PsiManager psiManager = PsiManager.getInstance(project);
        int scannedFiles = 0;

        for (final VirtualFile file : MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "requirejs-config.js".equals(virtualFile.getName())
        )) {
            scannedFiles++;
            final PsiFile psiFile = psiManager.findFile(file);

            if (!(psiFile instanceof JSFile)) {
                continue;
            }
            for (final Map.Entry<String, Set<String>> entry : JsMixinIndex.getMixinMap((JSFile) psiFile).entrySet()) {
                result.computeIfAbsent(entry.getKey(), ignored -> new HashSet<>()).addAll(entry.getValue());
            }
        }
        final int finalScannedFiles = scannedFiles;
        NavigationInstrumentation.infoOnce(
                "js-mixin-vfs-declarations-" + project.getLocationHash(),
                () -> "Magento VFS mixin declaration scan scannedRequireJsConfigs=" + finalScannedFiles
                        + " targets=" + result.size()
        );

        return result;
    }

    private static final class MixinOverride {
        private final @Nullable String name;
        private final PsiElement anchor;

        private MixinOverride(
                final @Nullable String name,
                final @NotNull PsiElement anchor
        ) {
            this.name = name;
            this.anchor = anchor;
        }
    }
}
