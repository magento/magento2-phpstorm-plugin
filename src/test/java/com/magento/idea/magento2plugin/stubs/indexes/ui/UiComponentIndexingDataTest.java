/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.ui;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ExtensionTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.FileContentImpl;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.JsMixinIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.KnockoutTemplateIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.RequireJsIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.util.magento.js.JsPsiSupportProvider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class UiComponentIndexingDataTest extends BasePlatformTestCase {
    private static final String TEMPLATE = "Foo_Bar/template/child";
    private static final String COMPONENT = "Foo_Bar/js/child";
    private static final String JS = "define({components: {child: {component: '" + COMPONENT
            + "', displayArea: 'content', template: '" + TEMPLATE + "'}}});";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Settings.getInstance(getProject()).pluginEnabled = true;
    }

    public void testJavascriptIndexesShareExtractionAndPreserveValues() {
        assertNotNull("JsPsiSupportProvider should be registered when JS plugin is enabled",
                JsPsiSupportProvider.getInstance());
        final CountingContent content = content("component.js", JS);
        assertIndexValues(content, JS);
        assertEquals("The three indexes should extract declarations only once via JS PSI", 1, content.psiReads);
    }

    public void testJavascriptFallbackWithoutJsPsiProviderAvoidsPsiAndMatchesResults() {
        ExtensionTestUtil.maskExtensions(
                JsPsiSupportProvider.EP_NAME,
                Collections.emptyList(),
                getTestRootDisposable()
        );
        assertNull(JsPsiSupportProvider.getInstance());

        final CountingContent content = content("component-fallback.js", JS);
        assertIndexValues(content, JS);
        assertEquals("Fallback JS parsing should not construct PSI", 0, content.psiReads);
    }

    public void testDynamicJsExpressionsAreIgnoredInBothJsPsiAndFallbackModes() {
        final String dynamicJs = """
                define([], function () {
                    return {
                        defaults: {
                            template: this.template,
                            displayArea: regionName,
                            childTemplate: chooseTemplate()
                        }
                    };
                });
                """;
        final CountingContent psiContent = content("dynamic-psi.js", dynamicJs);
        assertAllIndexesEmpty(psiContent);
        assertEquals(1, psiContent.psiReads);
        assertTrue(new KnockoutTemplateIndex().getIndexer().map(psiContent).isEmpty());
        assertEquals(2, psiContent.psiReads);

        ExtensionTestUtil.maskExtensions(
                JsPsiSupportProvider.EP_NAME,
                Collections.emptyList(),
                getTestRootDisposable()
        );
        final CountingContent fallbackContent = content("dynamic-fallback.js", dynamicJs);
        assertAllIndexesEmpty(fallbackContent);
        assertTrue(new KnockoutTemplateIndex().getIndexer().map(fallbackContent).isEmpty());
        assertEquals(0, fallbackContent.psiReads);
    }

    public void testRequireJsAndKnockoutIndexesMatchBetweenJsPsiAndFallbackModes() {
        final String requireJs = """
                require.config({
                    map: { '*': { 'aliasA': 'Foo_Bar/js/a' } },
                    paths: { 'pathB': 'Foo_Bar/js/b' },
                    config: { mixins: { 'Foo_Bar/js/a': { 'Foo_Bar/js/a-mixin': true } } }
                });
                """;
        final String componentJs = "define({defaults: {template: '" + TEMPLATE + "'}});";

        final CountingContent requirePsi = projectContent("psi/requirejs-config.js", requireJs);
        final CountingContent componentPsi = content("ko-component.js", componentJs);
        final Map<String, String> expectedRequire = new RequireJsIndex().getIndexer().map(requirePsi);
        final Map<String, Set<String>> expectedMixins = new JsMixinIndex().getIndexer().map(requirePsi);
        final Map<String, Set<String>> expectedTemplates = new KnockoutTemplateIndex().getIndexer().map(componentPsi);
        assertEquals("'Foo_Bar/js/a'", expectedRequire.get("aliasA"));
        assertEquals("'Foo_Bar/js/b'", expectedRequire.get("pathB"));
        assertEquals(Set.of("Foo_Bar/js/a-mixin"), expectedMixins.get("Foo_Bar/js/a"));
        assertEquals(Set.of(componentPsi.getFile().getUrl()), expectedTemplates.get(TEMPLATE));

        ExtensionTestUtil.maskExtensions(
                JsPsiSupportProvider.EP_NAME,
                Collections.emptyList(),
                getTestRootDisposable()
        );
        final CountingContent requireFallback = projectContent("fallback/requirejs-config.js", requireJs);
        final CountingContent componentFallback = content("fallback-ko-component.js", componentJs);
        assertEquals(expectedRequire, new RequireJsIndex().getIndexer().map(requireFallback));
        assertEquals(expectedMixins, new JsMixinIndex().getIndexer().map(requireFallback));
        assertEquals(
                Set.of(componentFallback.getFile().getUrl()),
                new KnockoutTemplateIndex().getIndexer().map(componentFallback).get(TEMPLATE)
        );
        assertEquals(0, requireFallback.psiReads);
        assertEquals(0, componentFallback.psiReads);
    }

    public void testXmlIndexesShareExtractionAndPreserveOffsets() {
        final String xml = "<config><item name=\"child\">"
                + "<item name=\"component\">" + COMPONENT + "</item>"
                + "<item name=\"displayArea\">content</item>"
                + "<item name=\"template\">" + TEMPLATE + "</item>"
                + "</item></config>";
        final CountingContent content = content("component.xml", xml);
        assertIndexValues(content, xml);
        assertEquals(1, content.psiReads);
    }

    public void testNewContentForSameFileDoesNotReuseOldDeclarations() {
        final CountingContent before = content("component.js", JS);
        assertTrue(new UiComponentTemplateDeclarationIndex().getIndexer().map(before)
                .containsKey(UiComponentTemplateDeclarationIndex.templatePathKey(TEMPLATE)));

        final String replacement = "Foo_Bar/template/replacement";
        final CountingContent after = new CountingContent(FileContentImpl.createByText(
                before.getFile(), JS.replace(TEMPLATE, replacement), getProject()));
        final Map<String, Set<UiComponentNavigationData>> updated =
                new UiComponentTemplateDeclarationIndex().getIndexer().map(after);
        assertTrue(updated.containsKey(UiComponentTemplateDeclarationIndex.templatePathKey(replacement)));
        assertFalse(updated.containsKey(UiComponentTemplateDeclarationIndex.templatePathKey(TEMPLATE)));
        assertTrue(new UiComponentTemplateDeclarationIndex().getIndexer().map(before)
                .containsKey(UiComponentTemplateDeclarationIndex.templatePathKey(TEMPLATE)));
        assertEquals(1, before.psiReads);
        assertEquals(1, after.psiReads);
    }

    public void testCancellationDoesNotPublishAnEmptyResult() {
        final CountingContent content = content("component.js", JS);
        content.cancelNextRead = true;
        assertThrows(ProcessCanceledException.class,
                () -> new DisplayAreaIndex().getIndexer().map(content));
        assertIndexValues(content, JS);
        assertEquals("Cancelled extraction must be retried once", 2, content.psiReads);
    }

    public void testEmptyContentIsSharedAndDisabledIndexesDoNotExtract() {
        final CountingContent empty = content("empty.js", "define({});");
        Settings.getInstance(getProject()).pluginEnabled = false;
        assertAllIndexesEmpty(empty);
        assertEquals(0, empty.psiReads);
        Settings.getInstance(getProject()).pluginEnabled = true;
        assertAllIndexesEmpty(empty);
        assertEquals(1, empty.psiReads);
    }

    private void assertAllIndexesEmpty(final CountingContent content) {
        assertTrue(new DisplayAreaIndex().getIndexer().map(content).isEmpty());
        assertTrue(new UiComponentTemplateDeclarationIndex().getIndexer().map(content).isEmpty());
        assertTrue(new UiComponentComponentDeclarationIndex().getIndexer().map(content).isEmpty());
    }

    private void assertIndexValues(final CountingContent content, final String text) {
        final Set<UiComponentNavigationData> areas = new DisplayAreaIndex().getIndexer().map(content).get("content");
        assertNotNull(areas);
        assertEquals(1, areas.size());
        assertEquals(text.indexOf("content"), areas.iterator().next().getValueOffset());

        final Set<UiComponentNavigationData> templates = new UiComponentTemplateDeclarationIndex()
                .getIndexer().map(content).get(UiComponentTemplateDeclarationIndex.templatePathKey(TEMPLATE));
        assertNotNull(templates);
        assertEquals(1, templates.size());
        assertEquals(text.indexOf(TEMPLATE), templates.iterator().next().getValueOffset());
        assertEquals("child", templates.iterator().next().getComponentName());
        assertEquals(COMPONENT, templates.iterator().next().getComponentJsPath());

        final Set<UiComponentNavigationData> components = new UiComponentComponentDeclarationIndex()
                .getIndexer().map(content).get(UiComponentComponentDeclarationIndex.componentNameKey("child"));
        assertNotNull(components);
        assertEquals(1, components.size());
        assertEquals(content.getFile().getUrl(), components.iterator().next().getFileUrl());
    }

    private CountingContent content(final String name, final String text) {
        final VirtualFile file = myFixture.configureByText(name, text).getVirtualFile();
        return new CountingContent(FileContentImpl.createByText(file, text, getProject()));
    }

    private CountingContent projectContent(final String relativePath, final String text) {
        final VirtualFile file = myFixture.addFileToProject(relativePath, text).getVirtualFile();
        return new CountingContent(FileContentImpl.createByText(file, text, getProject()));
    }

    private static final class CountingContent extends UserDataHolderBase implements FileContent {
        private final FileContent delegate;
        private int psiReads;
        private boolean cancelNextRead;

        private CountingContent(final FileContent delegate) {
            this.delegate = delegate;
        }

        @Override
        public @NotNull PsiFile getPsiFile() {
            psiReads++;
            if (cancelNextRead) {
                cancelNextRead = false;
                throw new ProcessCanceledException();
            }
            return delegate.getPsiFile();
        }

        @Override
        public byte @NotNull [] getContent() {
            return delegate.getContent();
        }

        @Override
        public @NotNull CharSequence getContentAsText() {
            return delegate.getContentAsText();
        }

        @Override
        public @NotNull FileType getFileType() {
            return delegate.getFileType();
        }

        @Override
        public @NotNull VirtualFile getFile() {
            return delegate.getFile();
        }

        @Override
        public @NotNull String getFileName() {
            return delegate.getFileName();
        }

        @Override
        public @NotNull Project getProject() {
            return delegate.getProject();
        }
    }
}
