/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.lang.Language;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.ValidationRuleExtractorUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ValidationRule;
import com.magento.idea.magento2plugin.magento.files.BlockPhp;

public class PhpGenerationValidationTest extends BasePlatformTestCase {

    @FieldValidation(rule = RuleRegistry.PHP_NAMESPACE_NAME, message = {"namespace"})
    private String namespace;

    public void testReflectivelyLoadedNamespaceRuleValidatesQualifiedNames() throws Exception {
        final FieldValidation annotation = getClass().getDeclaredField("namespace")
                .getAnnotation(FieldValidation.class);
        final ValidationRule rule = ValidationRuleExtractorUtil.extract(annotation);

        assertNotNull(rule);
        assertTrue(rule.check("Vendor\\Module\\Controller"));
        assertFalse(rule.check("Vendor\\Invalid Name"));
        assertFalse(rule.check("Vendor\\123Invalid"));
    }

    public void testClassRuleRejectsPhpKeywordsAndInvalidIdentifiers() {
        final ValidationRule rule = PhpClassRule.getInstance();

        assertTrue(rule.check("ProductRepository"));
        assertFalse(rule.check("class"));
        assertFalse(rule.check("123Product"));
        assertFalse(rule.check("Product Repository"));
    }

    public void testLegacyBlockFileUsesTheInstalledPhpLanguage() {
        final BlockPhp file = BlockPhp.getInstance("ProductList");
        final Language language = file.getLanguage();

        assertSame(PhpLanguage.INSTANCE, language);
        assertEquals("ProductList.php", file.getFileName());
        assertEquals("Magento Regular Class", file.getTemplate());
    }
}
