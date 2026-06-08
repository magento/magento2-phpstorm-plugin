/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.JsMixinIndex;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsMixinLineMarkerProvider implements LineMarkerProvider {
    private static final String TARGET_TOOLTIP_TEXT = "Navigate to target JS";
    private static final String MIXINS_TOOLTIP_TEXT = "Navigate to JS mixins";
    private static final String TARGET_METHOD_TOOLTIP_TEXT = "Navigate to target method";
    private static final String MIXIN_METHOD_TOOLTIP_TEXT = "Navigate to JS mixin override";
    private static final Function<PsiElement, String> TARGET_TOOLTIP_PROVIDER =
            ignored -> getTooltipText(TARGET_TOOLTIP_TEXT);
    private static final Function<PsiElement, String> MIXINS_TOOLTIP_PROVIDER =
            ignored -> getTooltipText(MIXINS_TOOLTIP_TEXT);
    private static final Function<PsiElement, String> TARGET_METHOD_TOOLTIP_PROVIDER =
            ignored -> getTooltipText(TARGET_METHOD_TOOLTIP_TEXT);
    private static final Function<PsiElement, String> MIXIN_METHOD_TOOLTIP_PROVIDER =
            ignored -> getTooltipText(MIXIN_METHOD_TOOLTIP_TEXT);
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

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> psiElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (psiElements.isEmpty() || !Settings.isEnabled(psiElements.get(0).getProject())) {
            return;
        }

        final Set<String> processedFiles = new HashSet<>();

        for (final PsiElement psiElement : psiElements) {
            final PsiFile psiFile = psiElement.getContainingFile();

            if (!(psiFile instanceof JSFile)) {
                continue;
            }
            final String fileKey = getFileKey(psiFile);

            if (!processedFiles.add(fileKey)) {
                continue;
            }

            final String requireJsPath = RequireJsPathResolver.getInstance().getRequireJsPath(psiFile);

            if (requireJsPath == null) {
                continue;
            }

            addTargetLineMarker(psiElement, requireJsPath, psiElements, collection);
            addMixinLineMarker(psiElement, requireJsPath, psiElements, collection);
            addMixinMethodLineMarkers((JSFile) psiFile, requireJsPath, psiElements, collection);
        }
    }

    private void addTargetLineMarker(
            final @NotNull PsiElement anchorContext,
            final @NotNull String requireJsPath,
            final @NotNull List<? extends PsiElement> scopeElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> targets = collectTargetsForMixin(
                anchorContext.getProject(),
                requireJsPath
        );

        final List<PsiElement> preparedTargets = LineMarkerTargetPresentationUtil.prepareTargets(targets);

        if (preparedTargets.isEmpty()) {
            return;
        }

        final PsiElement anchor = getFileLineMarkerAnchor(anchorContext.getContainingFile());

        if (!containsElement(scopeElements, anchor)) {
            return;
        }
        if (hasLineMarker(collection, anchor, TARGET_TOOLTIP_TEXT)) {
            return;
        }

        addLineMarker(
                collection,
                anchor,
                JavaScriptFileType.INSTANCE.getIcon(),
                preparedTargets,
                TARGET_TOOLTIP_TEXT
        );
    }

    private void addMixinLineMarker(
            final @NotNull PsiElement anchorContext,
            final @NotNull String requireJsPath,
            final @NotNull List<? extends PsiElement> scopeElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> mixins = collectMixinsForTarget(
                anchorContext.getProject(),
                requireJsPath
        );

        final List<PsiElement> preparedMixins = LineMarkerTargetPresentationUtil.prepareTargets(mixins);

        if (preparedMixins.isEmpty()) {
            return;
        }

        final PsiElement anchor = getFileLineMarkerAnchor(anchorContext.getContainingFile());

        if (!containsElement(scopeElements, anchor)) {
            return;
        }
        if (hasLineMarker(collection, anchor, MIXINS_TOOLTIP_TEXT)) {
            return;
        }

        addLineMarker(
                collection,
                anchor,
                AllIcons.Nodes.Plugin,
                preparedMixins,
                MIXINS_TOOLTIP_TEXT
        );
    }

    private void addMixinMethodLineMarkers(
            final @NotNull JSFile jsFile,
            final @NotNull String requireJsPath,
            final @NotNull List<? extends PsiElement> scopeElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        addMixinFileMethodLineMarkers(jsFile, requireJsPath, scopeElements, collection);
        addTargetFileMethodLineMarkers(jsFile, requireJsPath, scopeElements, collection);
    }

    private void addMixinFileMethodLineMarkers(
            final @NotNull JSFile mixinFile,
            final @NotNull String mixinPath,
            final @NotNull List<? extends PsiElement> scopeElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<MixinOverride> overrides = collectDocumentedMixinOverrides(mixinFile);

        if (overrides.isEmpty()) {
            return;
        }
        final List<PsiElement> targetFiles = collectTargetsForMixin(mixinFile.getProject(), mixinPath);

        for (final MixinOverride override : overrides) {
            if (!containsElement(scopeElements, override.anchor)) {
                continue;
            }
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
            final List<PsiElement> preparedTargetMethods =
                    LineMarkerTargetPresentationUtil.prepareTargets(targetMethods);

            if (preparedTargetMethods.isEmpty()) {
                continue;
            }
            addLineMarker(
                    collection,
                    override.anchor,
                    AllIcons.Nodes.Method,
                    preparedTargetMethods,
                    TARGET_METHOD_TOOLTIP_TEXT
            );
        }
    }

    private void addTargetFileMethodLineMarkers(
            final @NotNull JSFile targetFile,
            final @NotNull String targetPath,
            final @NotNull List<? extends PsiElement> scopeElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> mixinFiles = collectMixinsForTarget(targetFile.getProject(), targetPath);

        if (mixinFiles.isEmpty()) {
            return;
        }
        for (final PsiElement targetMethod : findObjectMethodAnchors(targetFile)) {
            if (!containsElement(scopeElements, targetMethod)) {
                continue;
            }
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
            final List<PsiElement> preparedMixinOverrides =
                    LineMarkerTargetPresentationUtil.prepareTargets(mixinOverrides);

            if (preparedMixinOverrides.isEmpty()) {
                continue;
            }
            addLineMarker(
                    collection,
                    targetMethod,
                    AllIcons.Nodes.Plugin,
                    preparedMixinOverrides,
                    MIXIN_METHOD_TOOLTIP_TEXT
            );
        }
        addTargetFileFunctionMixinLineMarker(targetFile, mixinFiles, scopeElements, collection);
    }

    private void addTargetFileFunctionMixinLineMarker(
            final @NotNull JSFile targetFile,
            final @NotNull List<PsiElement> mixinFiles,
            final @NotNull List<? extends PsiElement> scopeElements,
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
        final List<PsiElement> preparedMixinOverrides =
                LineMarkerTargetPresentationUtil.prepareTargets(mixinOverrides);

        if (preparedMixinOverrides.isEmpty()) {
            return;
        }
        final PsiElement anchor = getFileLineMarkerAnchor(targetFile);

        if (!containsElement(scopeElements, anchor)) {
            return;
        }
        if (hasLineMarker(collection, anchor, MIXINS_TOOLTIP_TEXT)) {
            return;
        }
        addLineMarker(
                collection,
                anchor,
                AllIcons.Nodes.Plugin,
                preparedMixinOverrides,
                MIXIN_METHOD_TOOLTIP_TEXT
        );
    }

    private void addLineMarker(
            final @NotNull Collection<? super LineMarkerInfo<?>> collection,
            final @NotNull PsiElement anchor,
            final @NotNull Icon icon,
            final @NotNull List<PsiElement> targets,
            final @NotNull String tooltip
    ) {
        if (hasLineMarker(collection, anchor, tooltip)) {
            return;
        }
        final TextRange textRange = anchor.getTextRange();

        collection.add(new LineMarkerInfo<>(
                anchor,
                textRange,
                icon,
                getTooltipProvider(tooltip),
                LineMarkerTargetPresentationUtil.createNavigationHandler(targets, tooltip),
                GutterIconRenderer.Alignment.RIGHT,
                () -> getTooltipText(tooltip)
        ));
    }

    private @NotNull Function<PsiElement, String> getTooltipProvider(final @NotNull String tooltip) {
        return switch (tooltip) {
            case TARGET_TOOLTIP_TEXT -> TARGET_TOOLTIP_PROVIDER;
            case MIXINS_TOOLTIP_TEXT -> MIXINS_TOOLTIP_PROVIDER;
            case TARGET_METHOD_TOOLTIP_TEXT -> TARGET_METHOD_TOOLTIP_PROVIDER;
            case MIXIN_METHOD_TOOLTIP_TEXT -> MIXIN_METHOD_TOOLTIP_PROVIDER;
            default -> ignored -> getTooltipText(tooltip);
        };
    }

    private static @NotNull String getTooltipText(final @NotNull String tooltip) {
        return "<html>" + tooltip + "</html>";
    }

    private boolean hasLineMarker(
            final @NotNull Collection<? super LineMarkerInfo<?>> collection,
            final @NotNull PsiElement anchor,
            final @NotNull String tooltip
    ) {
        for (final Object item : collection) {
            if (!(item instanceof LineMarkerInfo<?> lineMarkerInfo)) {
                continue;
            }
            if (!getTooltipText(tooltip).equals(lineMarkerInfo.getLineMarkerTooltip())) {
                continue;
            }
            if (lineMarkerInfo.startOffset == anchor.getTextRange().getStartOffset()) {
                return true;
            }
        }

        return false;
    }

    private boolean containsElement(
            final @NotNull List<? extends PsiElement> elements,
            final @NotNull PsiElement anchor
    ) {
        final PsiFile anchorFile = anchor.getContainingFile();
        final TextRange anchorRange = anchor.getTextRange();

        for (final PsiElement element : elements) {
            if (element.equals(anchor)) {
                return true;
            }
            if (!anchorRange.equals(element.getTextRange())) {
                continue;
            }
            if (anchorFile != null && anchorFile.equals(element.getContainingFile())) {
                return true;
            }
        }

        return false;
    }

    private @NotNull PsiElement getFileLineMarkerAnchor(final @NotNull PsiFile psiFile) {
        final PsiElement anchor = psiFile.findElementAt(getFirstCodeOffset(psiFile.getText()));

        return anchor == null ? PsiTreeUtil.getDeepestFirst(psiFile) : anchor;
    }

    private int getFirstCodeOffset(final @NotNull String text) {
        int offset = 0;

        while (offset < text.length()) {
            if (Character.isWhitespace(text.charAt(offset))) {
                offset++;
                continue;
            }
            if (text.startsWith("//", offset)) {
                final int endOfLine = text.indexOf('\n', offset + 2);

                if (endOfLine < 0) {
                    return 0;
                }
                offset = endOfLine + 1;
                continue;
            }
            if (text.startsWith("/*", offset)) {
                final int commentEnd = text.indexOf("*/", offset + 2);

                if (commentEnd < 0) {
                    return 0;
                }
                offset = commentEnd + 2;
                continue;
            }

            return offset;
        }

        return 0;
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
                final PsiElement nameIdentifier = property.getNameIdentifier();

                if (nameIdentifier != null) {
                    result.add(new MixinOverride(property.getName(), nameIdentifier));
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
            final PsiElement nameIdentifier = property.getNameIdentifier();

            if (nameIdentifier != null) {
                result.add(nameIdentifier);
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
            final PsiElement nameIdentifier = property.getNameIdentifier();

            if (nameIdentifier != null) {
                result.add(nameIdentifier);
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
        final Set<String> seenFiles = new HashSet<>();
        final Collection<Set<String>> mixinSets = FileBasedIndex.getInstance()
                .getValues(JsMixinIndex.KEY, targetPath, GlobalSearchScope.allScope(project));

        for (final Set<String> mixins : mixinSets) {
            for (final String mixin : mixins) {
                addResolvedJsFiles(project, mixin, seenFiles, results);
            }
        }

        return results;
    }

    private @NotNull List<PsiElement> collectTargetsForMixin(
            final @NotNull Project project,
            final @NotNull String mixinPath
    ) {
        final List<PsiElement> results = new ArrayList<>();
        final Set<String> seenFiles = new HashSet<>();
        final Collection<String> targetPaths = FileBasedIndex.getInstance()
                .getAllKeys(JsMixinIndex.KEY, project);

        for (final String targetPath : targetPaths) {
            final Collection<Set<String>> mixinSets = FileBasedIndex.getInstance()
                    .getValues(JsMixinIndex.KEY, targetPath, GlobalSearchScope.allScope(project));

            for (final Set<String> mixins : mixinSets) {
                if (mixins.contains(mixinPath)) {
                    addResolvedJsFiles(project, targetPath, seenFiles, results);
                }
            }
        }

        return results;
    }

    private void addResolvedJsFiles(
            final @NotNull Project project,
            final @NotNull String requireJsPath,
            final @NotNull Set<String> seenFiles,
            final @NotNull List<PsiElement> results
    ) {
        for (final PsiElement resolvedFile : RequireJsPathResolver.getInstance().resolveJsFiles(project, requireJsPath)) {
            final String key = getFileKey(resolvedFile);

            if (key != null && !seenFiles.add(key)) {
                continue;
            }
            results.add(resolvedFile);
        }
    }

    private @Nullable String getFileKey(final @NotNull PsiElement psiElement) {
        final PsiFile containingFile = psiElement.getContainingFile();

        if (containingFile == null) {
            return null;
        }
        final VirtualFile virtualFile = containingFile.getVirtualFile();

        return virtualFile == null ? null : virtualFile.getUrl();
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
