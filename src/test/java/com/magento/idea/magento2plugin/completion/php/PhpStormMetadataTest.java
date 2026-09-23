/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.php;

import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.config.library.PhpLibraryRoot;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Exercises the bundled metadata registered by withPhp.xml using PhpStorm's PHP
 * engine. No metadata is copied into the test project: registration and packaging
 * must work for these tests to pass.
 */
public class PhpStormMetadataTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return Path.of("src/test/testData/completion/php/PhpStormMetadata").toAbsolutePath().toString();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        refreshBundledMetadata();
        myFixture.copyFileToProject("magento.php");
    }

    public void testExistingObjectManagerReturnTypesWithoutHyvaInstalled() {
        for (final String method : List.of("get", "create")) {
            assertReturnType("\\Magento\\Framework\\ObjectManagerInterface",
                    method + "(\\Example\\Collection::class)", "\\Example\\Collection");
        }
    }

    public void testUniversalFactoryAddsRequestedTypeDespiteStaleDocblock() {
        assertReturnType("\\Magento\\Framework\\Validator\\UniversalFactory",
                "create(\\Example\\Collection::class)", "\\Example\\Collection");
    }

    public void testUnitTestObjectManagerReturnType() {
        assertReturnType("\\Magento\\Framework\\TestFramework\\Unit\\Helper\\ObjectManager",
                "getObject(\\Example\\Collection::class)", "\\Example\\Collection");
    }

    public void testMessageFactoryReturnTypesFromConstantsAndStrings() {
        for (final String type : List.of("Error", "Warning", "Notice", "Success")) {
            final String[] otherTypes = List.of("Error", "Warning", "Notice", "Success").stream()
                    .filter(candidate -> !candidate.equals(type))
                    .map(candidate -> "\\Magento\\Framework\\Message\\" + candidate)
                    .toArray(String[]::new);
            assertReturnType("\\Magento\\Framework\\Message\\Factory",
                    "create(\\Magento\\Framework\\Message\\MessageInterface::TYPE_"
                            + type.toUpperCase(Locale.ROOT) + ")",
                    "\\Magento\\Framework\\Message\\" + type, otherTypes);
            assertReturnType("\\Magento\\Framework\\Message\\Factory",
                    "create('" + type.toLowerCase(Locale.ROOT) + "')",
                    "\\Magento\\Framework\\Message\\" + type, otherTypes);
        }
    }

    public void testMessageFactoryArgumentCompletion() {
        assertCompletionContains("\\Magento\\Framework\\Message\\Factory", "create(<caret>)",
                "MessageInterface::TYPE_ERROR", "MessageInterface::TYPE_WARNING",
                "MessageInterface::TYPE_NOTICE", "MessageInterface::TYPE_SUCCESS");
    }

    public void testHyvaViewModelReturnTypeFromClassConstantAndString() {
        addHyva();
        assertReturnType("\\Hyva\\Theme\\Model\\ViewModelRegistry",
                "require(\\Example\\ViewModel::class)", "\\Example\\ViewModel");
        assertReturnType("\\Hyva\\Theme\\Model\\ViewModelRegistry",
                "require('Example\\ViewModel')", "\\Example\\ViewModel");
    }

    public void testHyvaAndMagewireFactoryReturnTypes() {
        addHyva();
        for (final String factory : List.of(
                "\\Hyva\\Checkout\\Model\\CustomConditionFactory",
                "\\Hyva\\Checkout\\Model\\Magewire\\Component\\EvaluationResultFactory",
                "\\Magewirephp\\Magewire\\Model\\Action\\Type\\Factory")) {
            assertReturnType(factory, "create(\\Example\\ViewModel::class)", "\\Example\\ViewModel");
        }
    }

    public void testFormFactoryReturnTypesOnInterfaceAndInheritedMethod() {
        addHyva();
        final Map<String, String> factories = Map.of(
                "'elements'", "\\Hyva\\Checkout\\Model\\Form\\EntityFormFactory",
                "'fields'", "\\Hyva\\Checkout\\Model\\Form\\EntityFormFieldFactory",
                "\\Hyva\\Checkout\\Model\\Form\\EntityField\\EavAttributeFieldFactory::ACCESSOR",
                "\\Hyva\\Checkout\\Model\\Form\\EntityField\\EavAttributeFieldFactory");
        for (final String form : List.of("\\Hyva\\Checkout\\Model\\Form\\EntityFormInterface", "\\Example\\Form")) {
            for (final Map.Entry<String, String> factory : factories.entrySet()) {
                final String[] otherTypes = factories.values().stream()
                        .filter(candidate -> !candidate.equals(factory.getValue()))
                        .toArray(String[]::new);
                assertReturnType(form, "getFactoryFor(" + factory.getKey() + ")", factory.getValue(), otherTypes);
            }
        }
    }

    public void testFormFactoryArgumentCompletion() {
        addHyva();
        assertCompletionContains("\\Example\\Form", "getFactoryFor('<caret>')", "elements", "fields", "eav_fields");
    }

    public void testSqlConditionCompletionAtBothArgumentPositions() {
        final String[] conditions = {"eq", "nlike", "ntoa", "regexp", "seq", "sneq"};
        assertCompletionContains("\\Magento\\Framework\\Api\\FilterBuilder",
                "setConditionType('<caret>')", conditions);
        assertCompletionContains("\\Magento\\Framework\\Api\\SearchCriteriaBuilder",
                "addFilter('sku', 'example', '<caret>')", conditions);
        assertCompletionDoesNotContain("\\Magento\\Framework\\Api\\SearchCriteriaBuilder",
                "addFilter('<caret>', 'example')", "nlike", "ntoa", "regexp", "seq", "sneq");
    }

    public void testPluralScopeCompletionAtScopeArgumentOnly() {
        for (final String method : List.of("getValue", "isSetFlag")) {
            assertCompletionContains("\\Magento\\Framework\\App\\Config\\ScopeConfigInterface",
                    method + "('general/store_information/name', <caret>)",
                    "ScopeInterface::SCOPE_STORES", "ScopeInterface::SCOPE_WEBSITES");
        }
        assertCompletionDoesNotContain("\\Magento\\Framework\\App\\Config\\ScopeConfigInterface",
                "getValue(<caret>)", "ScopeInterface::SCOPE_STORES", "ScopeInterface::SCOPE_WEBSITES");
    }

    public void testSortDirectionCompletion() {
        assertCompletionContains("\\Magento\\Framework\\Api\\SortOrder", "setDirection(<caret>)",
                "SortOrder::SORT_ASC", "SortOrder::SORT_DESC");
    }

    public void testAreaCodeCompletion() {
        for (final String call : List.of("setAreaCode(<caret>)", "emulateAreaCode(<caret>, function () {})")) {
            assertCompletionContains("\\Magento\\Framework\\App\\State", call,
                    "Area::AREA_GLOBAL", "Area::AREA_FRONTEND", "Area::AREA_ADMINHTML", "Area::AREA_DOC",
                    "Area::AREA_CRONTAB", "Area::AREA_WEBAPI_REST", "Area::AREA_WEBAPI_SOAP", "Area::AREA_GRAPHQL");
        }
    }

    public void testDirectoryCodeCompletion() {
        for (final String method : List.of("getDirectoryRead", "getDirectoryWrite")) {
            assertCompletionContains("\\Magento\\Framework\\Filesystem", method + "(<caret>)",
                    "DirectoryList::ROOT", "DirectoryList::MEDIA", "DirectoryList::GENERATED_CODE",
                    "DirectoryList::GENERATED_METADATA");
        }
        assertCompletionDoesNotContain("\\Magento\\Framework\\Filesystem",
                "getDirectoryRead('media', <caret>)", "DirectoryList::MEDIA");
    }

    public void testStepUpdateTypeCompletion() {
        addHyva();
        assertCompletionContains("\\Hyva\\Checkout\\Model\\Checkout\\Step", "getUpdates(<caret>)",
                "Step::UPDATE_TYPE_LAYOUT", "Step::UPDATE_TYPE_DEFAULT", "Step::UPDATE_TYPE_CUSTOM");
    }

    public void testFlashMessageCompletionThroughTraits() {
        addHyva();
        assertCompletionContains("\\Example\\Component", "dispatchMessage(<caret>, 'Message')",
                "FlashMessage::ERROR", "FlashMessage::WARNING", "FlashMessage::NOTICE", "FlashMessage::SUCCESS");
        assertCompletionContains("\\Example\\Message", "asCustomType(<caret>)",
                "FlashMessage::ERROR", "FlashMessage::WARNING", "FlashMessage::NOTICE", "FlashMessage::SUCCESS");
    }

    public void testBatchEvaluationTypeCompletion() {
        addHyva();
        assertCompletionContains("\\Hyva\\Checkout\\Model\\Magewire\\Component\\Evaluation\\Batch",
                "clearByType(<caret>)", "Batch::TYPE", "Redirect::TYPE");
    }

    public void testCustomEvaluationDoesNotSuggestBuiltInProcessors() {
        addHyva();
        // createCustom names a project-defined frontend processor. Built-in
        // results use their dedicated factory methods and have different payloads.
        assertCompletionDoesNotContain("\\Hyva\\Checkout\\Model\\Magewire\\Component\\EvaluationResultFactory",
                "createCustom(<caret>)", "Batch::TYPE", "Redirect::TYPE");
    }

    private void addHyva() {
        myFixture.copyFileToProject("hyva.php");
    }

    private void refreshBundledMetadata() {
        // The sandbox reuses its VFS cache across builds. Refresh the archive so
        // these tests see the newly packaged metadata after a branch switch.
        for (final PhpLibraryRoot library : PhpLibraryRoot.EP_NAME.getExtensionList()) {
            if ("/.phpstorm.meta.php/".equals(library.path)) {
                library.getPathBasedLibraryRoots().forEach(root -> {
                    final VirtualFile archive = JarFileSystem.getInstance().getVirtualFileForJar(root);
                    VfsUtil.markDirtyAndRefresh(false, true, true, archive == null ? root : archive);
                });
            }
        }
        IndexingTestUtil.waitUntilIndexesAreReady(getProject());
    }

    private void assertReturnType(
            final String receiver,
            final String call,
            final String expected,
            final String... unexpected
    ) {
        configureCall(receiver, "<caret>" + call);
        final MethodReference reference = PsiTreeUtil.getParentOfType(
                myFixture.getFile().findElementAt(myFixture.getCaretOffset()), MethodReference.class);
        assertNotNull(reference);
        // PhpStorm combines metadata with declared types (and inferred null for
        // empty fixture bodies), so require the selected class without demanding
        // that the engine discard a method's original PHPDoc or native type.
        final Set<String> types = reference.getType().global(getProject()).getTypes();
        assertTrue(receiver + "::" + call + ": missing " + expected + " in " + types, types.contains(expected));
        for (final String type : unexpected) {
            assertFalse(receiver + "::" + call + ": unexpected " + type + " in " + types, types.contains(type));
        }
    }

    private void assertCompletionContains(final String receiver, final String call, final String... expected) {
        final List<String> completions = completeCall(receiver, call);
        for (final String value : expected) {
            assertTrue(receiver + "::" + call + ": missing " + value + " in " + completions,
                    completions.contains(value));
        }
    }

    private void assertCompletionDoesNotContain(final String receiver, final String call, final String... unexpected) {
        final List<String> completions = completeCall(receiver, call);
        for (final String value : unexpected) {
            assertFalse(receiver + "::" + call + ": unexpected " + value + " in " + completions,
                    completions.contains(value));
        }
    }

    private List<String> completeCall(final String receiver, final String call) {
        configureCall(receiver, call);
        myFixture.completeBasic();
        final List<String> completions = myFixture.getLookupElementStrings();
        return completions == null ? List.of() : completions;
    }

    private void configureCall(final String receiver, final String call) {
        myFixture.configureByText("usage.php",
                "<?php function example(" + receiver + " $subject) { $subject->" + call + "; }");
    }
}
