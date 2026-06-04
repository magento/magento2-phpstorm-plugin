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
