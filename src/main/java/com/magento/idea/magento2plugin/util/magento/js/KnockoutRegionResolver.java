/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnockoutRegionResolver {
    private static final Pattern GET_REGION_PATTERN = Pattern.compile(
            "getRegion\\s*\\(\\s*(['\\\"])([^'\\\"]+)\\1\\s*\\)"
    );
    private static final Pattern GET_REGION_ENTITY_QUOTE_PATTERN = Pattern.compile(
            "getRegion\\s*\\(\\s*&(?:quot|apos|#39);([^&]+)&(?:quot|apos|#39);\\s*\\)"
    );
    private static final Pattern PHP_LAYOUT_PAIR_PATTERN = Pattern.compile(
            "(['\\\"])(component|displayArea|template)\\1\\s*=>\\s*(['\\\"])([^'\\\"]+)\\3"
    );
    private static final String COMPONENT_KEY = "component";
    private static final String DISPLAY_AREA_KEY = "displayArea";
    private static final String TEMPLATE_KEY = "template";
    private static KnockoutRegionResolver INSTANCE;

    private KnockoutRegionResolver() {
    }

    public static KnockoutRegionResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KnockoutRegionResolver();
        }

        return INSTANCE;
    }

    public @Nullable String getDisplayArea(final @Nullable JSProperty property) {
        if (property == null || !"displayArea".equals(property.getName())) {
            return null;
        }
        final JSExpression value = property.getValue();

        if (value == null || !isQuotedText(value.getText())) {
            return null;
        }

        return unquote(value.getText());
    }

    public @NotNull List<RegionMatch> collectGetRegionMatches(final @NotNull String text) {
        final List<RegionMatch> matches = new ArrayList<>();
        final Matcher matcher = GET_REGION_PATTERN.matcher(text);

        while (matcher.find()) {
            matches.add(new RegionMatch(
                    matcher.group(2),
                    matcher.start(2),
                    matcher.end(2)
            ));
        }
        final Matcher entityQuoteMatcher = GET_REGION_ENTITY_QUOTE_PATTERN.matcher(text);

        while (entityQuoteMatcher.find()) {
            matches.add(new RegionMatch(
                    entityQuoteMatcher.group(1),
                    entityQuoteMatcher.start(1),
                    entityQuoteMatcher.end(1)
            ));
        }

        return matches;
    }

    public @NotNull List<PsiElement> resolveDisplayAreaComponentFiles(
            final @NotNull Project project,
            final @NotNull String displayArea
    ) {
        final Set<PsiElement> results = new LinkedHashSet<>();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(
                JavaScriptFileType.INSTANCE,
                GlobalSearchScope.allScope(project)
        );
        NavigationInstrumentation.infoOnce(
                "ko-region-resolve-display-area-scan-" + displayArea,
                () -> "Resolving displayArea components for '" + displayArea
                        + "' scannedJsFiles=" + files.size()
        );

        for (final VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);

            if (!(psiFile instanceof JSFile) || !containsDisplayArea((JSFile) psiFile, displayArea)) {
                continue;
            }
            results.add(psiFile);
        }
        addDisplayAreaComponentFilesFromMagentoVfs(project, displayArea, results);
        addDisplayAreaComponentFilesFromLayoutXml(project, displayArea, results);
        addDisplayAreaComponentFilesFromLayoutDeclarations(project, displayArea, results);
        NavigationInstrumentation.infoOnce(
                "ko-region-resolve-display-area-result-" + displayArea,
                () -> "Resolved displayArea components for '" + displayArea
                        + "' targets=" + results.size()
        );

        return new ArrayList<>(results);
    }

    public @NotNull List<PsiElement> resolveGetRegionTemplateFiles(
            final @NotNull Project project,
            final @NotNull String regionName
    ) {
        final Set<PsiElement> results = new LinkedHashSet<>();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Collection<VirtualFile> files = FileTypeIndex.getFiles(
                HtmlFileType.INSTANCE,
                GlobalSearchScope.allScope(project)
        );
        NavigationInstrumentation.infoOnce(
                "ko-region-resolve-get-region-scan-" + regionName,
                () -> "Resolving getRegion templates for '" + regionName
                        + "' scannedHtmlFiles=" + files.size()
        );

        for (final VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile == null || !containsGetRegion(psiFile, regionName)) {
                continue;
            }
            results.add(psiFile);
        }
        addGetRegionTemplateFilesFromMagentoVfs(project, regionName, results);
        NavigationInstrumentation.infoOnce(
                "ko-region-resolve-get-region-result-" + regionName,
                () -> "Resolved getRegion templates for '" + regionName
                        + "' targets=" + results.size()
        );

        return new ArrayList<>(results);
    }

    public @NotNull List<PsiElement> resolveDisplayAreaTemplateFiles(
            final @NotNull Project project,
            final @NotNull String displayArea
    ) {
        final Set<PsiElement> results = new LinkedHashSet<>();

        addDisplayAreaTemplateFilesFromLayoutXml(project, displayArea, results);
        addDisplayAreaTemplateFilesFromLayoutDeclarations(project, displayArea, results);
        NavigationInstrumentation.info(
                "Resolved displayArea templates for '" + displayArea + "' targets=" + results.size()
        );

        return new ArrayList<>(results);
    }

    public @NotNull List<LayoutComponentDeclaration> collectLayoutComponentDeclarations(
            final @NotNull Project project
    ) {
        final List<LayoutComponentDeclaration> results = new ArrayList<>();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Set<VirtualFile> files = new LinkedHashSet<>(FileTypeIndex.getFiles(
                XmlFileType.INSTANCE,
                GlobalSearchScope.allScope(project)
        ));
        files.addAll(FilenameIndex.getAllFilesByExt(project, "php"));
        files.addAll(MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "xml".equals(virtualFile.getExtension())
                        || "php".equals(virtualFile.getExtension())
        ));

        for (final VirtualFile file : files) {
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile != null) {
                results.addAll(collectLayoutComponentDeclarations(psiFile));
            }
        }

        return deduplicateDeclarations(results);
    }

    public @NotNull List<LayoutComponentDeclaration> collectLayoutComponentDeclarations(
            final @NotNull PsiFile psiFile
    ) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (psiFile instanceof XmlFile) {
            return collectXmlLayoutComponentDeclarations((XmlFile) psiFile);
        }
        if (virtualFile != null && "php".equals(virtualFile.getExtension())) {
            return collectPhpLayoutComponentDeclarations(psiFile);
        }

        return new ArrayList<>();
    }

    private void addDisplayAreaComponentFilesFromMagentoVfs(
            final @NotNull Project project,
            final @NotNull String displayArea,
            final @NotNull Collection<PsiElement> results
    ) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        int scannedFiles = 0;

        for (final VirtualFile file : MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "js".equals(virtualFile.getExtension())
        )) {
            scannedFiles++;
            final PsiFile psiFile = psiManager.findFile(file);

            if (!(psiFile instanceof JSFile) || !containsDisplayArea((JSFile) psiFile, displayArea)) {
                continue;
            }
            results.add(psiFile);
        }
        final int finalScannedFiles = scannedFiles;
        NavigationInstrumentation.infoOnce(
                "ko-region-resolve-display-area-vfs-" + displayArea,
                () -> "Magento VFS displayArea scan for '" + displayArea
                        + "' scannedJsFiles=" + finalScannedFiles
                        + " totalTargets=" + results.size()
        );
    }

    private void addDisplayAreaComponentFilesFromLayoutDeclarations(
            final @NotNull Project project,
            final @NotNull String displayArea,
            final @NotNull Collection<PsiElement> results
    ) {
        for (final LayoutComponentDeclaration declaration : collectLayoutComponentDeclarations(project)) {
            if (!displayArea.equals(declaration.getDisplayArea())
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

    private void addDisplayAreaTemplateFilesFromLayoutDeclarations(
            final @NotNull Project project,
            final @NotNull String displayArea,
            final @NotNull Collection<PsiElement> results
    ) {
        for (final LayoutComponentDeclaration declaration : collectLayoutComponentDeclarations(project)) {
            if (!displayArea.equals(declaration.getDisplayArea()) || declaration.getTemplatePath() == null) {
                continue;
            }
            results.addAll(KnockoutTemplatePathResolver.getInstance().resolveTemplateFiles(
                    project,
                    declaration.getTemplatePath()
            ));
        }
    }

    private void addDisplayAreaComponentFilesFromLayoutXml(
            final @NotNull Project project,
            final @NotNull String displayArea,
            final @NotNull Collection<PsiElement> results
    ) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Set<VirtualFile> xmlFiles = new LinkedHashSet<>(FileTypeIndex.getFiles(
                XmlFileType.INSTANCE,
                GlobalSearchScope.allScope(project)
        ));
        xmlFiles.addAll(MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "xml".equals(virtualFile.getExtension())
        ));
        int scannedFiles = 0;

        for (final VirtualFile file : xmlFiles) {
            scannedFiles++;
            final PsiFile psiFile = psiManager.findFile(file);

            if (!(psiFile instanceof XmlFile) || !psiFile.getText().contains(displayArea)) {
                continue;
            }
            addLayoutXmlDisplayAreaComponentFiles((XmlFile) psiFile, displayArea, results);
        }
        final int finalScannedFiles = scannedFiles;
        NavigationInstrumentation.infoOnce(
                "ko-region-resolve-display-area-xml-" + displayArea,
                () -> "Magento layout XML displayArea scan for '" + displayArea
                        + "' scannedXmlFiles=" + finalScannedFiles
                        + " totalTargets=" + results.size()
        );
    }

    private void addDisplayAreaTemplateFilesFromLayoutXml(
            final @NotNull Project project,
            final @NotNull String displayArea,
            final @NotNull Collection<PsiElement> results
    ) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Set<VirtualFile> xmlFiles = new LinkedHashSet<>(FileTypeIndex.getFiles(
                XmlFileType.INSTANCE,
                GlobalSearchScope.allScope(project)
        ));
        xmlFiles.addAll(MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "xml".equals(virtualFile.getExtension())
        ));
        int scannedFiles = 0;

        for (final VirtualFile file : xmlFiles) {
            scannedFiles++;
            final PsiFile psiFile = psiManager.findFile(file);

            if (!(psiFile instanceof XmlFile) || !psiFile.getText().contains(displayArea)) {
                continue;
            }
            addLayoutXmlDisplayAreaTemplateFiles((XmlFile) psiFile, displayArea, results);
        }
        final int finalScannedFiles = scannedFiles;
        NavigationInstrumentation.info(
                "Magento layout XML displayArea template scan for '" + displayArea
                        + "' scannedXmlFiles=" + finalScannedFiles
                        + " totalTargets=" + results.size()
        );
    }

    private void addGetRegionTemplateFilesFromMagentoVfs(
            final @NotNull Project project,
            final @NotNull String regionName,
            final @NotNull Collection<PsiElement> results
    ) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        int scannedFiles = 0;

        for (final VirtualFile file : MagentoVfsUtil.findMagentoFiles(
                project,
                virtualFile -> "html".equals(virtualFile.getExtension())
        )) {
            scannedFiles++;
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile == null || !containsGetRegion(psiFile, regionName)) {
                continue;
            }
            results.add(psiFile);
        }
        final int finalScannedFiles = scannedFiles;
        NavigationInstrumentation.infoOnce(
                "ko-region-resolve-get-region-vfs-" + regionName,
                () -> "Magento VFS getRegion scan for '" + regionName
                        + "' scannedHtmlFiles=" + finalScannedFiles
                        + " totalTargets=" + results.size()
        );
    }

    private boolean containsDisplayArea(
            final @NotNull JSFile jsFile,
            final @NotNull String displayArea
    ) {
        final Collection<JSProperty> properties = PsiTreeUtil.findChildrenOfType(
                jsFile,
                JSProperty.class
        );

        for (final JSProperty property : properties) {
            if (displayArea.equals(getDisplayArea(property))) {
                return true;
            }
        }

        return false;
    }

    private void addLayoutXmlDisplayAreaComponentFiles(
            final @NotNull XmlFile xmlFile,
            final @NotNull String displayArea,
            final @NotNull Collection<PsiElement> results
    ) {
        final Collection<XmlTag> tags = PsiTreeUtil.findChildrenOfType(xmlFile, XmlTag.class);

        for (final XmlTag tag : tags) {
            final String componentPath = getLayoutXmlComponentPath(tag, displayArea);

            if (componentPath == null) {
                continue;
            }
            results.addAll(RequireJsPathResolver.getInstance().resolveJsFiles(
                    xmlFile.getProject(),
                    componentPath
            ));
        }
    }

    private void addLayoutXmlDisplayAreaTemplateFiles(
            final @NotNull XmlFile xmlFile,
            final @NotNull String displayArea,
            final @NotNull Collection<PsiElement> results
    ) {
        final Collection<XmlTag> tags = PsiTreeUtil.findChildrenOfType(xmlFile, XmlTag.class);

        for (final XmlTag tag : tags) {
            if (!isDisplayAreaTag(tag, displayArea)) {
                continue;
            }
            final XmlTag componentTag = getComponentNodeTag(tag);

            if (componentTag == null) {
                continue;
            }
            final String xmlTemplatePath = getLayoutXmlTemplatePath(componentTag);

            if (xmlTemplatePath != null) {
                results.addAll(KnockoutTemplatePathResolver.getInstance().resolveTemplateFiles(
                        xmlFile.getProject(),
                        xmlTemplatePath
                ));
            }
            final String componentPath = getLayoutXmlComponentPath(tag, displayArea);

            if (componentPath == null || "uiComponent".equals(componentPath)) {
                continue;
            }
            for (final PsiElement component : RequireJsPathResolver.getInstance().resolveJsFiles(
                    xmlFile.getProject(),
                    componentPath
            )) {
                if (component instanceof JSFile) {
                    for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                            .collectTemplatePaths((JSFile) component)) {
                        results.addAll(KnockoutTemplatePathResolver.getInstance().resolveTemplateFiles(
                                xmlFile.getProject(),
                                templatePath
                        ));
                    }
                }
            }
        }
    }

    private @NotNull List<LayoutComponentDeclaration> collectXmlLayoutComponentDeclarations(
            final @NotNull XmlFile xmlFile
    ) {
        final List<LayoutComponentDeclaration> results = new ArrayList<>();
        final Collection<XmlTag> tags = PsiTreeUtil.findChildrenOfType(xmlFile, XmlTag.class);

        for (final XmlTag tag : tags) {
            final String componentPath = getXmlValue(tag, COMPONENT_KEY);
            final String displayArea = getXmlValue(tag, DISPLAY_AREA_KEY);
            final String templatePath = getLayoutXmlTemplatePath(tag);

            if (componentPath == null && displayArea == null && templatePath == null) {
                continue;
            }
            results.add(new LayoutComponentDeclaration(
                    xmlFile,
                    componentPath,
                    templatePath,
                    displayArea,
                    getXmlValueOffset(tag, TEMPLATE_KEY),
                    getXmlValueOffset(tag, DISPLAY_AREA_KEY)
            ));
        }

        return deduplicateDeclarations(results);
    }

    private @NotNull List<LayoutComponentDeclaration> collectPhpLayoutComponentDeclarations(
            final @NotNull PsiFile psiFile
    ) {
        final List<LayoutComponentDeclaration> results = new ArrayList<>();
        final String text = psiFile.getText();
        final Matcher matcher = PHP_LAYOUT_PAIR_PATTERN.matcher(text);
        final Set<String> processedRanges = new LinkedHashSet<>();

        while (matcher.find()) {
            final ContainerRange range = findPhpArrayContainerRange(text, matcher.start());

            if (range == null || !processedRanges.add(range.getStart() + ":" + range.getEnd())) {
                continue;
            }
            final LayoutComponentDeclaration declaration = createPhpLayoutComponentDeclaration(
                    psiFile,
                    text.substring(range.getStart(), range.getEnd()),
                    range.getStart()
            );

            if (declaration != null) {
                results.add(declaration);
            }
        }

        return deduplicateDeclarations(results);
    }

    private @Nullable LayoutComponentDeclaration createPhpLayoutComponentDeclaration(
            final @NotNull PsiFile psiFile,
            final @NotNull String containerText,
            final int containerStartOffset
    ) {
        String componentPath = null;
        String displayArea = null;
        String templatePath = null;
        int displayAreaOffset = -1;
        int templateOffset = -1;
        final Matcher matcher = PHP_LAYOUT_PAIR_PATTERN.matcher(containerText);

        while (matcher.find()) {
            final String key = matcher.group(2);
            final String value = matcher.group(4);
            final int valueOffset = containerStartOffset + matcher.start(4);

            if (COMPONENT_KEY.equals(key)) {
                componentPath = value;
            } else if (DISPLAY_AREA_KEY.equals(key)) {
                displayArea = value;
                displayAreaOffset = valueOffset;
            } else if (TEMPLATE_KEY.equals(key)) {
                templatePath = value;
                templateOffset = valueOffset;
            }
        }

        if (componentPath == null && displayArea == null && templatePath == null) {
            return null;
        }

        return new LayoutComponentDeclaration(
                psiFile,
                componentPath,
                templatePath,
                displayArea,
                templateOffset,
                displayAreaOffset
        );
    }

    private @Nullable ContainerRange findPhpArrayContainerRange(
            final @NotNull String text,
            final int offset
    ) {
        final int start = findPhpArrayContainerStart(text, offset);

        if (start < 0) {
            return null;
        }
        final int end = findPhpArrayContainerEnd(text, start);

        if (end <= start) {
            return null;
        }

        return new ContainerRange(start, end);
    }

    private int findPhpArrayContainerStart(final @NotNull String text, final int offset) {
        int depth = 0;

        for (int i = Math.min(offset, text.length() - 1); i >= 0; i--) {
            final char currentChar = text.charAt(i);

            if (currentChar == ')' || currentChar == ']') {
                depth++;
            } else if (currentChar == '(' || currentChar == '[') {
                if (depth == 0) {
                    return i;
                }
                depth--;
            }
        }

        return -1;
    }

    private int findPhpArrayContainerEnd(final @NotNull String text, final int start) {
        int depth = 0;

        for (int i = start; i < text.length(); i++) {
            final char currentChar = text.charAt(i);

            if (currentChar == '(' || currentChar == '[') {
                depth++;
            } else if (currentChar == ')' || currentChar == ']') {
                depth--;
                if (depth == 0) {
                    return i + 1;
                }
            }
        }

        return text.length();
    }

    private @NotNull List<LayoutComponentDeclaration> deduplicateDeclarations(
            final @NotNull List<LayoutComponentDeclaration> declarations
    ) {
        final List<LayoutComponentDeclaration> results = new ArrayList<>();
        final Set<String> keys = new LinkedHashSet<>();

        for (final LayoutComponentDeclaration declaration : declarations) {
            final String key = declaration.getSourceFile().getVirtualFile() + "|"
                    + declaration.getComponentPath() + "|"
                    + declaration.getTemplatePath() + "|"
                    + declaration.getDisplayArea() + "|"
                    + declaration.getTemplateOffset() + "|"
                    + declaration.getDisplayAreaOffset();

            if (keys.add(key)) {
                results.add(declaration);
            }
        }

        return results;
    }

    private @Nullable String getLayoutXmlComponentPath(
            final @NotNull XmlTag tag,
            final @NotNull String displayArea
    ) {
        if (displayArea.equals(tag.getAttributeValue("displayArea"))) {
            return tag.getAttributeValue("component");
        }

        if (!isNamedItem(tag, "displayArea") || !displayArea.equals(getTagValue(tag))) {
            return null;
        }
        final XmlTag parent = tag.getParentTag();

        if (parent == null) {
            return null;
        }

        return getDirectChildItemValue(parent, "component");
    }

    private @Nullable String getXmlValue(
            final @NotNull XmlTag tag,
            final @NotNull String key
    ) {
        final String attributeValue = tag.getAttributeValue(key);

        return attributeValue != null ? attributeValue : getDirectChildItemValue(tag, key);
    }

    private int getXmlValueOffset(
            final @NotNull XmlTag tag,
            final @NotNull String key
    ) {
        if (tag.getAttribute(key) != null && tag.getAttribute(key).getValueElement() != null) {
            return tag.getAttribute(key).getValueElement().getTextRange().getStartOffset() + 1;
        }
        final XmlTag child = getDirectChildItem(tag, key);

        if (child == null || getTagValue(child) == null) {
            return -1;
        }
        final String valueText = child.getValue().getText();
        final String trimmedValue = child.getValue().getTrimmedText();
        final int valueStartOffset = child.getValue().getTextRange().getStartOffset();
        final int trimOffset = valueText.indexOf(trimmedValue);

        return valueStartOffset + Math.max(trimOffset, 0);
    }

    private boolean isDisplayAreaTag(final @NotNull XmlTag tag, final @NotNull String displayArea) {
        return displayArea.equals(tag.getAttributeValue("displayArea"))
                || (isNamedItem(tag, "displayArea") && displayArea.equals(getTagValue(tag)));
    }

    private @Nullable XmlTag getComponentNodeTag(final @NotNull XmlTag displayAreaTag) {
        if (displayAreaTag.getAttributeValue("displayArea") != null) {
            return displayAreaTag;
        }

        return displayAreaTag.getParentTag();
    }

    private @Nullable String getLayoutXmlTemplatePath(final @NotNull XmlTag componentTag) {
        final String directTemplate = getDirectChildItemValue(componentTag, "template");

        if (directTemplate != null) {
            return directTemplate;
        }
        final XmlTag configTag = getDirectChildItem(componentTag, "config");

        return configTag == null ? null : getDirectChildItemValue(configTag, "template");
    }

    private @Nullable XmlTag getDirectChildItem(
            final @NotNull XmlTag parent,
            final @NotNull String itemName
    ) {
        for (final XmlTag child : parent.getSubTags()) {
            if (isNamedItem(child, itemName)) {
                return child;
            }
        }

        return null;
    }

    private @Nullable String getDirectChildItemValue(
            final @NotNull XmlTag parent,
            final @NotNull String itemName
    ) {
        for (final XmlTag child : parent.getSubTags()) {
            if (isNamedItem(child, itemName)) {
                return getTagValue(child);
            }
        }

        return null;
    }

    private boolean isNamedItem(final @NotNull XmlTag tag, final @NotNull String itemName) {
        return "item".equals(tag.getName()) && itemName.equals(tag.getAttributeValue("name"));
    }

    private @Nullable String getTagValue(final @NotNull XmlTag tag) {
        final String value = tag.getValue().getTrimmedText();

        return value.isBlank() ? null : value;
    }

    private boolean containsGetRegion(
            final @NotNull PsiFile psiFile,
            final @NotNull String regionName
    ) {
        for (final RegionMatch regionMatch : collectGetRegionMatches(psiFile.getText())) {
            if (regionName.equals(regionMatch.getRegionName())) {
                return true;
            }
        }

        return false;
    }

    private boolean isQuotedText(final @NotNull String text) {
        final String trimmed = text.trim();

        return (trimmed.startsWith("'") && trimmed.endsWith("'"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\""));
    }

    private @Nullable String unquote(final @Nullable String rawValue) {
        if (rawValue == null) {
            return null;
        }
        final String value = rawValue.trim();

        if (value.length() < 2) {
            return null;
        }

        return value.substring(1, value.length() - 1);
    }

    private static class ContainerRange {
        private final int start;
        private final int end;

        ContainerRange(final int start, final int end) {
            this.start = start;
            this.end = end;
        }

        int getStart() {
            return start;
        }

        int getEnd() {
            return end;
        }
    }

    public static class LayoutComponentDeclaration {
        private final PsiFile sourceFile;
        private final String componentPath;
        private final String templatePath;
        private final String displayArea;
        private final int templateOffset;
        private final int displayAreaOffset;

        LayoutComponentDeclaration(
                final @NotNull PsiFile sourceFile,
                final @Nullable String componentPath,
                final @Nullable String templatePath,
                final @Nullable String displayArea,
                final int templateOffset,
                final int displayAreaOffset
        ) {
            this.sourceFile = sourceFile;
            this.componentPath = componentPath;
            this.templatePath = templatePath;
            this.displayArea = displayArea;
            this.templateOffset = templateOffset;
            this.displayAreaOffset = displayAreaOffset;
        }

        public @NotNull PsiFile getSourceFile() {
            return sourceFile;
        }

        public @Nullable String getComponentPath() {
            return componentPath;
        }

        public @Nullable String getTemplatePath() {
            return templatePath;
        }

        public @Nullable String getDisplayArea() {
            return displayArea;
        }

        public int getTemplateOffset() {
            return templateOffset;
        }

        public int getDisplayAreaOffset() {
            return displayAreaOffset;
        }
    }

    public static class RegionMatch {
        private final String regionName;
        private final int startOffset;
        private final int endOffset;

        RegionMatch(
                final @NotNull String regionName,
                final int startOffset,
                final int endOffset
        ) {
            this.regionName = regionName;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public @NotNull String getRegionName() {
            return regionName;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }
    }
}
