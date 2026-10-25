/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.IndexingTestUtil;
import com.jetbrains.php.lang.inspections.deadcode.PhpUnusedDeclarationInspection;
import com.jetbrains.php.lang.inspections.deadcode.local.PhpUnusedSymbolsInspection;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Regression coverage for constants referenced only from DI configuration (#1587).
 */
public class DiConstantUsageTest extends BaseProjectTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(
                new PhpUnusedDeclarationInspection(), new PhpUnusedSymbolsInspection());
    }

    public void testConstantReferencedOnlyInDiXmlIsUsed() {
        assertDiConstantIsUsed("Testing\\Example\\Model\\Labels::BASE_LABEL");
    }

    public void testFullyQualifiedConstantReferencedOnlyInDiXmlIsUsed() {
        assertDiConstantIsUsed("\\Testing\\Example\\Model\\Labels::BASE_LABEL");
    }

    public void testConstantReferencedInAreaDiArrayItemIsUsed() {
        assertConstantUsage("""
                <argument name="labels" xsi:type="array">
                    <item name="label" xsi:type="const">Testing\\Example\\Model\\Labels::BASE_LABEL</item>
                </argument>
                """, "etc/frontend/di.xml", true);
    }

    public void testStringValueDoesNotCountAsConstantUsage() {
        assertConstantUsage("""
                <argument name="label" xsi:type="string">Testing\\Example\\Model\\Labels::BASE_LABEL</argument>
                """, "etc/di.xml", false);
    }

    public void testOtherConfigurationDoesNotCountAsDiConstantUsage() {
        assertConstantUsage(constantArgument("Testing\\Example\\Model\\Labels::BASE_LABEL"),
                "etc/config.xml", false);
    }

    public void testDisabledPluginDoesNotCountDiConstantUsage() {
        Settings.getInstance(getProject()).pluginEnabled = false;
        assertConstantUsage(constantArgument("Testing\\Example\\Model\\Labels::BASE_LABEL"),
                "etc/di.xml", false);
    }

    private void assertDiConstantIsUsed(final String reference) {
        assertConstantUsage(constantArgument(reference), "etc/di.xml", true);
    }

    private static String constantArgument(final String reference) {
        return "<argument name=\"label\" xsi:type=\"const\">%s</argument>".formatted(reference);
    }

    private void assertConstantUsage(
            final String argument,
            final String configPath,
            final boolean expectedUsed
    ) {
        final PsiFile declarations = myFixture.addFileToProject(
                "src/app/code/Testing/Example/Model/Labels.php",
                """
                <?php
                namespace Testing\\Example\\Model;

                class Labels
                {
                    public const BASE_LABEL = 'label';
                    public const UNUSED_LABEL = 'unused';
                    public const PHP_LABEL = 'php';
                }

                class OtherLabels
                {
                    public const UNUSED_LABEL = 'other';
                }

                echo Labels::PHP_LABEL;
                """
        );
        final PsiFile config = myFixture.addFileToProject(
                "src/app/code/Testing/Example/" + configPath,
                """
                <?xml version="1.0"?>
                <config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:noNamespaceSchemaLocation="urn:magento:framework:ObjectManager/etc/config.xsd">
                    <type name="Testing\\Example\\Model\\Consumer">
                        <arguments>
                            %s
                            <argument name="other" xsi:type="const">Testing\\Example\\Model\\OtherLabels::UNUSED_LABEL</argument>
                        </arguments>
                    </type>
                </config>
                """.formatted(argument)
        );
        IndexingTestUtil.waitUntilIndexesAreReady(getProject());

        ReadAction.runBlocking(() -> {
            final PhpClass phpClass = PsiTreeUtil.findChildOfType(declarations, PhpClass.class);
            assertNotNull(phpClass);
            final Field constant = phpClass.findFieldByName("BASE_LABEL", true);
            assertNotNull(constant);
            final PsiElement xmlReference = config.findElementAt(config.getText().indexOf("::BASE_LABEL") + 2);
            assertNotNull(xmlReference);
            assertTrue("The XML reference must resolve to the constant declaration",
                    Arrays.stream(xmlReference.getReferences()).anyMatch(ref -> ref.isReferenceTo(constant)));
        });

        myFixture.configureFromExistingVirtualFile(declarations.getVirtualFile());
        final List<HighlightInfo> highlighting = myFixture.doHighlighting();
        assertTrue("A reference to another class's constant must not mark this one used: " + highlighting,
                hasUnusedHighlight(highlighting, "UNUSED_LABEL"));
        assertFalse("A constant referenced from PHP must count as used: " + highlighting,
                hasUnusedHighlight(highlighting, "PHP_LABEL"));
        assertEquals("Unexpected unused highlighting for the XML constant reference: " + highlighting,
                !expectedUsed, hasUnusedHighlight(highlighting, "BASE_LABEL"));
    }

    private boolean hasUnusedHighlight(final List<HighlightInfo> highlighting, final String constant) {
        final int offset = myFixture.getFile().getText().indexOf(constant);
        return highlighting.stream().anyMatch(info -> info.getStartOffset() == offset
                && info.getDescription() != null
                && info.getDescription().toLowerCase(Locale.ROOT).contains("unused"));
    }
}
