/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * JetBrains JavaScript PSI-backed implementation of {@link JsPsiSupportProvider} registered in {@code withJs.xml}.
 */
public final class JsPsiSupportProviderImpl implements JsPsiSupportProvider {
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

    @Override
    public boolean isJavaScriptFile(final @NotNull VirtualFile file) {
        return file.getFileType().equals(JavaScriptFileType.INSTANCE);
    }

    @Override
    public @Nullable RequireJsConfig parseRequireJsConfig(final @NotNull PsiFile psiFile) {
        if (!(psiFile instanceof JSFile jsFile)) {
            return null;
        }
        final List<RequireJsConfig.MixinEntry> mixinEntries = new ArrayList<>();
        final List<RequireJsConfig.MapEntry> mapEntries = new ArrayList<>();
        final List<RequireJsConfig.PathEntry> pathEntries = new ArrayList<>();

        for (final JSObjectLiteralExpression rootConfig : findRequireJsConfigRoots(jsFile)) {
            collectMapEntries(rootConfig, mapEntries);
            collectPathEntries(rootConfig, pathEntries);
            collectMixinEntries(rootConfig, mixinEntries);
        }

        return new RequireJsConfig(mapEntries, pathEntries, mixinEntries);
    }

    @Override
    public @Nullable Set<String> collectKnockoutTemplatePaths(final @NotNull PsiFile psiFile) {
        if (!(psiFile instanceof JSFile jsFile)) {
            return null;
        }
        final Set<String> templatePaths = new LinkedHashSet<>();
        final KnockoutTemplatePathResolver resolver = KnockoutTemplatePathResolver.getInstance();

        for (final JSProperty property : PsiTreeUtil.findChildrenOfType(jsFile, JSProperty.class)) {
            final String literalText = getQuotedLiteralText(property.getValue());
            if (literalText == null) {
                continue;
            }
            final PsiElement parent = property.getParent();
            final JSProperty parentProperty = parent == null
                    ? null
                    : PsiTreeUtil.getParentOfType(parent, JSProperty.class);
            if (!resolver.isTemplateProperty(
                    property.getName(),
                    parentProperty == null ? null : parentProperty.getName()
            )) {
                continue;
            }
            final String normalized = resolver.normalizeTemplatePath(literalText);
            if (normalized != null) {
                templatePaths.add(normalized);
            }
        }

        return templatePaths;
    }

    @Override
    public @Nullable List<UiComponentNavigationData> collectUiComponentDeclarations(final @NotNull PsiFile psiFile) {
        if (!(psiFile instanceof JSFile jsFile)) {
            return null;
        }
        final VirtualFile virtualFile = jsFile.getVirtualFile() != null
                ? jsFile.getVirtualFile()
                : jsFile.getViewProvider().getVirtualFile();
        return collectUiDeclarationsFromScope(jsFile, virtualFile);
    }

    @Override
    public @Nullable List<UiComponentNavigationData> collectEmbeddedUiComponentDeclarations(
            final @NotNull PsiElement jsScope,
            final @NotNull VirtualFile virtualFile
    ) {
        return collectUiDeclarationsFromScope(jsScope, virtualFile);
    }

    @Override
    public @Nullable String getDisplayAreaValue(final @NotNull PsiElement element) {
        final JSProperty property = PsiTreeUtil.getParentOfType(element, JSProperty.class);
        if (property == null || !"displayArea".equals(property.getName())) {
            return null;
        }
        final String unquoted = getUnquotedStringLiteral(property.getValue());
        return unquoted == null || unquoted.isBlank() ? null : unquoted;
    }

    @Override
    public @Nullable List<DisplayAreaAnchor> collectDisplayAreaAnchors(final @NotNull PsiFile psiFile) {
        if (!(psiFile instanceof JSFile jsFile)) {
            return null;
        }
        final List<DisplayAreaAnchor> anchors = new ArrayList<>();
        for (final JSProperty property : PsiTreeUtil.findChildrenOfType(jsFile, JSProperty.class)) {
            if (!"displayArea".equals(property.getName())) {
                continue;
            }
            final String displayArea = getUnquotedStringLiteral(property.getValue());
            if (displayArea == null || displayArea.isBlank()) {
                continue;
            }
            final PsiElement nameIdentifier = property.getNameIdentifier();
            final PsiElement anchor = nameIdentifier != null ? nameIdentifier : property;
            anchors.add(new DisplayAreaAnchor(displayArea, anchor.getTextRange().getStartOffset()));
        }
        return anchors;
    }

    private @NotNull List<JSObjectLiteralExpression> findRequireJsConfigRoots(final @NotNull JSFile jsFile) {
        final Set<JSObjectLiteralExpression> roots = new LinkedHashSet<>();

        for (final JSVariable variable : PsiTreeUtil.findChildrenOfType(jsFile, JSVariable.class)) {
            if ("config".equals(variable.getName())
                    && variable.getInitializer() instanceof JSObjectLiteralExpression objectLiteral) {
                roots.add(objectLiteral);
            }
        }
        for (final JSCallExpression call : PsiTreeUtil.findChildrenOfType(jsFile, JSCallExpression.class)) {
            final JSExpression methodExpr = call.getMethodExpression();
            if (methodExpr == null) {
                continue;
            }
            final String callee = methodExpr.getText().replaceAll("\\s+", "");
            if (!"require.config".equals(callee) && !"requirejs.config".equals(callee)) {
                continue;
            }
            final JSArgumentList args = call.getArgumentList();
            if (args == null) {
                continue;
            }
            final JSExpression[] expressions = args.getArguments();
            if (expressions.length > 0 && expressions[0] instanceof JSObjectLiteralExpression objectLiteral) {
                roots.add(objectLiteral);
            }
        }
        if (roots.isEmpty()) {
            for (final JSObjectLiteralExpression candidate
                    : PsiTreeUtil.findChildrenOfType(jsFile, JSObjectLiteralExpression.class)) {
                if (PsiTreeUtil.getParentOfType(candidate, JSProperty.class, JSObjectLiteralExpression.class) == null
                        && (candidate.findProperty("map") != null
                        || candidate.findProperty("paths") != null
                        || candidate.findProperty("config") != null)) {
                    roots.add(candidate);
                    break;
                }
            }
        }

        return new ArrayList<>(roots);
    }

    private void collectMapEntries(
            final @NotNull JSObjectLiteralExpression rootConfig,
            final @NotNull List<RequireJsConfig.MapEntry> results
    ) {
        final JSObjectLiteralExpression mapObj = getObjectValue(rootConfig.findProperty("map"));
        if (mapObj == null) {
            return;
        }
        for (final JSProperty contextProp : mapObj.getProperties()) {
            final String context = contextProp.getName();
            final JSObjectLiteralExpression mappingsObj = getObjectValue(contextProp);
            if (context == null || mappingsObj == null) {
                continue;
            }
            for (final JSProperty mapping : mappingsObj.getProperties()) {
                final String alias = mapping.getName();
                final JSExpression value = mapping.getValue();
                final String literalText = getQuotedLiteralText(value);
                final String target = unquoteAndTrim(literalText);
                if (alias != null && !alias.isBlank() && target != null && value != null) {
                    results.add(new RequireJsConfig.MapEntry(
                            context,
                            alias,
                            target,
                            literalText,
                            getKeyStartOffset(mapping),
                            value.getTextRange().getStartOffset()
                    ));
                }
            }
        }
    }

    private void collectPathEntries(
            final @NotNull JSObjectLiteralExpression rootConfig,
            final @NotNull List<RequireJsConfig.PathEntry> results
    ) {
        final JSObjectLiteralExpression pathsObj = getObjectValue(rootConfig.findProperty("paths"));
        if (pathsObj == null) {
            return;
        }
        for (final JSProperty mapping : pathsObj.getProperties()) {
            final String alias = mapping.getName();
            final JSExpression value = mapping.getValue();
            final String literalText = getQuotedLiteralText(value);
            final String target = unquoteAndTrim(literalText);
            if (alias != null && !alias.isBlank() && target != null && value != null) {
                results.add(new RequireJsConfig.PathEntry(
                        alias,
                        target,
                        literalText,
                        getKeyStartOffset(mapping),
                        value.getTextRange().getStartOffset()
                ));
            }
        }
    }

    private void collectMixinEntries(
            final @NotNull JSObjectLiteralExpression rootConfig,
            final @NotNull List<RequireJsConfig.MixinEntry> results
    ) {
        final JSObjectLiteralExpression configObj = getObjectValue(rootConfig.findProperty("config"));
        if (configObj == null) {
            return;
        }
        final JSObjectLiteralExpression mixinsObj = getObjectValue(configObj.findProperty("mixins"));
        if (mixinsObj == null) {
            return;
        }
        for (final JSProperty targetProp : mixinsObj.getProperties()) {
            final String target = JsProperty.normalizeRequirePath(targetProp.getName());
            final JSObjectLiteralExpression mixinDeclarations = getObjectValue(targetProp);
            if (target == null || mixinDeclarations == null) {
                continue;
            }
            for (final JSProperty mixinProp : mixinDeclarations.getProperties()) {
                final String mixin = JsProperty.normalizeRequirePath(mixinProp.getName());
                if (mixin == null) {
                    continue;
                }
                final JSExpression value = mixinProp.getValue();
                final boolean enabled = value == null || !"false".equals(value.getText().trim());
                final int keyOffset = getKeyStartOffset(mixinProp);
                final int valueOffset = value == null ? keyOffset : value.getTextRange().getStartOffset();
                results.add(new RequireJsConfig.MixinEntry(
                        target,
                        mixin,
                        enabled,
                        keyOffset,
                        valueOffset
                ));
            }
        }
    }

    private static int getKeyStartOffset(final @NotNull JSProperty property) {
        final PsiElement nameIdentifier = property.getNameIdentifier();
        return nameIdentifier != null
                ? nameIdentifier.getTextRange().getStartOffset()
                : property.getTextRange().getStartOffset();
    }

    private @NotNull List<UiComponentNavigationData> collectUiDeclarationsFromScope(
            final @NotNull PsiElement scopeRoot,
            final @NotNull VirtualFile virtualFile
    ) {
        final Set<UiComponentNavigationData> results = new LinkedHashSet<>();
        final String fileUrl = virtualFile.getUrl();
        final String fileComponentPath = UiComponentScopeResolver.getInstance()
                .getRequireJsPathFromFilePath(virtualFile);

        collectJsObjectComponentDeclarations(scopeRoot, fileUrl, fileComponentPath, results);
        collectDynamicJsLayoutChildDeclarations(scopeRoot, fileUrl, fileComponentPath, results);
        collectJsFileDefaultDeclarations(scopeRoot, fileUrl, fileComponentPath, results);

        return new ArrayList<>(results);
    }

    private void collectJsObjectComponentDeclarations(
            final @NotNull PsiElement scopeRoot,
            final @NotNull String fileUrl,
            final @Nullable String fileComponentPath,
            final @NotNull Set<UiComponentNavigationData> results
    ) {
        for (final JSProperty property : PsiTreeUtil.findChildrenOfType(scopeRoot, JSProperty.class)) {
            if (!(property.getValue() instanceof JSObjectLiteralExpression objectLiteral)
                    || !isJsComponentObject(property)) {
                continue;
            }
            final String componentName = buildJsComponentName(property);
            if (componentName == null) {
                continue;
            }
            final String parentComponentName = getParentComponentName(componentName);
            final String componentJsPath = getDirectJsStringProperty(objectLiteral, "component");
            final String parentComponentJsPath = getParentJsComponentPath(property);
            final String effectiveJsPath = componentJsPath == null ? fileComponentPath : componentJsPath;

            results.add(new UiComponentNavigationData(
                    fileUrl,
                    UiComponentNavigationData.KIND_COMPONENT,
                    componentName,
                    componentName,
                    parentComponentName,
                    effectiveJsPath,
                    parentComponentJsPath,
                    null,
                    property.getTextRange().getStartOffset()
            ));
            addJsComponentValueDeclarations(
                    results,
                    objectLiteral,
                    fileUrl,
                    componentName,
                    parentComponentName,
                    effectiveJsPath,
                    parentComponentJsPath
            );
        }
    }

    private void collectDynamicJsLayoutChildDeclarations(
            final @NotNull PsiElement scopeRoot,
            final @NotNull String fileUrl,
            final @Nullable String fileComponentPath,
            final @NotNull Set<UiComponentNavigationData> results
    ) {
        for (final JSObjectLiteralExpression objectLiteral
                : PsiTreeUtil.findChildrenOfType(scopeRoot, JSObjectLiteralExpression.class)) {
            if (!isDynamicJsLayoutChildObject(objectLiteral)) {
                continue;
            }
            final String componentJsPath = getDirectJsStringProperty(objectLiteral, "component");
            final String scopeComponentPath = fileComponentPath != null ? fileComponentPath : componentJsPath;
            final String componentName = buildDynamicJsLayoutComponentName(
                    objectLiteral,
                    scopeComponentPath != null ? scopeComponentPath : "dynamic"
            );

            results.add(new UiComponentNavigationData(
                    fileUrl,
                    UiComponentNavigationData.KIND_COMPONENT,
                    componentName,
                    componentName,
                    fileComponentPath,
                    componentJsPath,
                    fileComponentPath,
                    null,
                    objectLiteral.getTextRange().getStartOffset()
            ));
            addJsComponentValueDeclarations(
                    results,
                    objectLiteral,
                    fileUrl,
                    componentName,
                    fileComponentPath,
                    componentJsPath,
                    fileComponentPath
            );
        }
    }

    private void collectJsFileDefaultDeclarations(
            final @NotNull PsiElement scopeRoot,
            final @NotNull String fileUrl,
            final @Nullable String fileComponentPath,
            final @NotNull Set<UiComponentNavigationData> results
    ) {
        for (final JSProperty property : PsiTreeUtil.findChildrenOfType(scopeRoot, JSProperty.class)) {
            if (isInsideJsComponentObject(property) || isInsideDynamicJsLayoutChildObject(property)) {
                continue;
            }
            final String propertyName = property.getName();
            if ("displayArea".equals(propertyName)) {
                addJsStringDeclaration(results, property, fileUrl, UiComponentNavigationData.KIND_DISPLAY_AREA,
                        fileComponentPath, null, fileComponentPath, null, null);
            } else if ("template".equals(propertyName)) {
                addJsStringDeclaration(results, property, fileUrl, UiComponentNavigationData.KIND_TEMPLATE,
                        fileComponentPath, null, fileComponentPath, null, null);
            } else if ("childTemplate".equals(propertyName)) {
                addJsStringDeclaration(results, property, fileUrl, UiComponentNavigationData.KIND_CHILD_TEMPLATE,
                        fileComponentPath, null, fileComponentPath, null, null);
            } else if ("elementTmpl".equals(propertyName)) {
                addJsStringDeclaration(results, property, fileUrl, UiComponentNavigationData.KIND_ELEMENT_TEMPLATE,
                        fileComponentPath, null, fileComponentPath, null, null);
            } else if ("templates".equals(propertyName)) {
                addJsTemplatesCollectionDeclarations(results, property, fileUrl,
                        fileComponentPath, null, fileComponentPath, null);
            }
        }
    }

    private void addJsComponentValueDeclarations(
            final @NotNull Set<UiComponentNavigationData> results,
            final @NotNull JSObjectLiteralExpression objectLiteral,
            final @NotNull String fileUrl,
            final @NotNull String componentName,
            final @Nullable String parentComponentName,
            final @Nullable String componentJsPath,
            final @Nullable String parentComponentJsPath
    ) {
        addJsStringDeclaration(results, objectLiteral.findProperty("displayArea"), fileUrl,
                UiComponentNavigationData.KIND_DISPLAY_AREA, componentName, parentComponentName,
                componentJsPath, parentComponentJsPath, null);
        addJsStringDeclaration(results, findJsProperty(objectLiteral, "template"), fileUrl,
                UiComponentNavigationData.KIND_TEMPLATE, componentName, parentComponentName,
                componentJsPath, parentComponentJsPath, null);
        addJsStringDeclaration(results, findJsProperty(objectLiteral, "childTemplate"), fileUrl,
                UiComponentNavigationData.KIND_CHILD_TEMPLATE, componentName, parentComponentName,
                componentJsPath, parentComponentJsPath, null);
        addJsStringDeclaration(results, findJsProperty(objectLiteral, "elementTmpl"), fileUrl,
                UiComponentNavigationData.KIND_ELEMENT_TEMPLATE, componentName, parentComponentName,
                componentJsPath, parentComponentJsPath, null);
        addJsTemplatesCollectionDeclarations(results, findJsProperty(objectLiteral, "templates"), fileUrl,
                componentName, parentComponentName, componentJsPath, parentComponentJsPath);
    }

    private void addJsStringDeclaration(
            final @NotNull Set<UiComponentNavigationData> results,
            final @Nullable JSProperty property,
            final @NotNull String fileUrl,
            final @NotNull String kind,
            final @Nullable String componentName,
            final @Nullable String parentComponentName,
            final @Nullable String componentJsPath,
            final @Nullable String parentComponentJsPath,
            final @Nullable String templateKey
    ) {
        if (property == null) {
            return;
        }
        final String value = getUnquotedStringLiteral(property.getValue());
        if (value == null) {
            return;
        }
        final String normalizedValue = UiComponentScopeResolver.getInstance()
                .normalizeValueForKind(kind, value);
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
                property.getValue().getTextRange().getStartOffset() + 1
        ));
    }

    private void addJsTemplatesCollectionDeclarations(
            final @NotNull Set<UiComponentNavigationData> results,
            final @Nullable JSProperty templatesProperty,
            final @NotNull String fileUrl,
            final @Nullable String componentName,
            final @Nullable String parentComponentName,
            final @Nullable String componentJsPath,
            final @Nullable String parentComponentJsPath
    ) {
        final JSObjectLiteralExpression templatesObj = getObjectValue(templatesProperty);
        if (templatesObj == null) {
            return;
        }
        for (final JSProperty templateEntry : templatesObj.getProperties()) {
            addJsStringDeclaration(results, templateEntry, fileUrl, UiComponentNavigationData.KIND_TEMPLATES,
                    componentName, parentComponentName, componentJsPath, parentComponentJsPath,
                    templateEntry.getName());
        }
    }

    private static @Nullable JSProperty findJsProperty(
            final @NotNull JSObjectLiteralExpression objectLiteral,
            final @NotNull String propertyName
    ) {
        final JSProperty directProperty = objectLiteral.findProperty(propertyName);
        if (directProperty != null) {
            return directProperty;
        }
        final JSObjectLiteralExpression configObj = getObjectValue(objectLiteral.findProperty("config"));
        return configObj == null ? null : configObj.findProperty(propertyName);
    }

    private static @Nullable String getDirectJsStringProperty(
            final @NotNull JSObjectLiteralExpression objectLiteral,
            final @NotNull String propertyName
    ) {
        final JSProperty property = objectLiteral.findProperty(propertyName);
        return property == null ? null : getUnquotedStringLiteral(property.getValue());
    }

    private boolean isJsComponentObject(final @NotNull JSProperty property) {
        final String name = property.getName();
        if (name == null || JS_CONTAINER_NAMES.contains(name) || JS_VALUE_PROPERTY_NAMES.contains(name)) {
            return false;
        }
        final JSObjectLiteralExpression objectLiteral = getObjectValue(property);
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

    private boolean isInsideJsComponentObject(final @NotNull JSProperty property) {
        PsiElement current = PsiTreeUtil.getParentOfType(property.getParent(), JSProperty.class);
        while (current instanceof JSProperty jsProp) {
            if (jsProp.getValue() instanceof JSObjectLiteralExpression && isJsComponentObject(jsProp)) {
                return true;
            }
            current = PsiTreeUtil.getParentOfType(jsProp.getParent(), JSProperty.class);
        }
        return false;
    }

    private static boolean isInsideDynamicJsLayoutChildObject(final @NotNull JSProperty property) {
        PsiElement current = PsiTreeUtil.getParentOfType(property, JSObjectLiteralExpression.class, false);
        while (current instanceof JSObjectLiteralExpression obj) {
            if (isDynamicJsLayoutChildObject(obj)) {
                return true;
            }
            current = PsiTreeUtil.getParentOfType(obj.getParent(), JSObjectLiteralExpression.class, false);
        }
        return false;
    }

    private static boolean isDynamicJsLayoutChildObject(final @NotNull JSObjectLiteralExpression objectLiteral) {
        if (objectLiteral.findProperty("displayArea") == null) {
            return false;
        }
        final JSProperty parentProp = objectLiteral.findProperty("parent");
        return parentProp != null
                && parentProp.getValue() != null
                && "this.name".equals(parentProp.getValue().getText().trim());
    }

    private static @NotNull String buildDynamicJsLayoutComponentName(
            final @NotNull JSObjectLiteralExpression objectLiteral,
            final @NotNull String scopeComponentPath
    ) {
        final String name = getDirectJsStringProperty(objectLiteral, "name");
        if (name != null && !name.isBlank()) {
            return scopeComponentPath + "." + name;
        }
        return scopeComponentPath + ".dynamic." + objectLiteral.getTextRange().getStartOffset();
    }

    private static boolean isInsideJsUiComponentContainer(final @NotNull JSProperty property) {
        PsiElement current = PsiTreeUtil.getParentOfType(property.getParent(), JSProperty.class);
        while (current instanceof JSProperty jsProp) {
            final String name = jsProp.getName();
            if ("components".equals(name) || "children".equals(name)) {
                return true;
            }
            current = PsiTreeUtil.getParentOfType(jsProp.getParent(), JSProperty.class);
        }
        return false;
    }

    private static boolean hasStaticJsTemplateDeclaration(final @NotNull JSObjectLiteralExpression objectLiteral) {
        return isStaticJsTemplateDeclaration(findJsProperty(objectLiteral, "template"),
                UiComponentNavigationData.KIND_TEMPLATE)
                || isStaticJsTemplateDeclaration(findJsProperty(objectLiteral, "elementTmpl"),
                UiComponentNavigationData.KIND_ELEMENT_TEMPLATE);
    }

    private static boolean isStaticJsTemplateDeclaration(
            final @Nullable JSProperty property,
            final @NotNull String kind
    ) {
        if (property == null) {
            return false;
        }
        final String rawValue = getUnquotedStringLiteral(property.getValue());
        return UiComponentScopeResolver.getInstance().normalizeValueForKind(kind, rawValue) != null;
    }

    private static @Nullable String buildJsComponentName(final @NotNull JSProperty property) {
        final List<String> segments = new ArrayList<>();
        PsiElement current = property;
        while (current != null) {
            if (current instanceof JSProperty jsProp) {
                final String name = jsProp.getName();
                if (name != null && !JS_CONTAINER_NAMES.contains(name) && !JS_VALUE_PROPERTY_NAMES.contains(name)) {
                    segments.add(name);
                }
            }
            current = PsiTreeUtil.getParentOfType(current.getParent(), JSProperty.class);
        }
        Collections.reverse(segments);
        return segments.isEmpty() ? null : String.join(".", segments);
    }

    private static @Nullable String getParentComponentName(final @NotNull String componentName) {
        final int lastDot = componentName.lastIndexOf('.');
        return lastDot > 0 ? componentName.substring(0, lastDot) : null;
    }

    private static @Nullable String getParentJsComponentPath(final @NotNull JSProperty property) {
        PsiElement current = PsiTreeUtil.getParentOfType(property.getParent(), JSProperty.class);
        while (current instanceof JSProperty jsProp) {
            if (jsProp.getValue() instanceof JSObjectLiteralExpression objectLiteral) {
                final String componentPath = getDirectJsStringProperty(objectLiteral, "component");
                if (componentPath != null) {
                    return componentPath;
                }
            }
            current = PsiTreeUtil.getParentOfType(jsProp.getParent(), JSProperty.class);
        }
        return null;
    }

    private static @Nullable JSObjectLiteralExpression getObjectValue(final @Nullable JSProperty property) {
        if (property == null) {
            return null;
        }
        final JSExpression value = property.getValue();
        return value instanceof JSObjectLiteralExpression objectLiteral ? objectLiteral : null;
    }

    private static @Nullable String getQuotedLiteralText(final @Nullable JSExpression expression) {
        if (!(expression instanceof JSLiteralExpression literal) || !literal.isQuotedLiteral()) {
            return null;
        }
        final String text = literal.getText();
        if (text.length() < 2) {
            return null;
        }
        final char first = text.charAt(0);
        final char last = text.charAt(text.length() - 1);
        if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
            return text;
        }
        return null;
    }

    private static @Nullable String getUnquotedStringLiteral(final @Nullable JSExpression expression) {
        final String text = getQuotedLiteralText(expression);
        return text == null ? null : text.substring(1, text.length() - 1);
    }

    private static @Nullable String unquoteAndTrim(final @Nullable String literalText) {
        if (literalText == null || literalText.length() < 2) {
            return null;
        }
        final String trimmed = literalText.substring(1, literalText.length() - 1).trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
