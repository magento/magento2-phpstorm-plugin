/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.magento.idea.magento2plugin.util.RegExUtil;
import junit.framework.TestCase;

public class NewEntityDialogTest extends TestCase {

    public void testFormatSnakeCaseLabelSkipsEmptyParts() {
        assertEquals("", NewEntityDialog.formatSnakeCaseLabel(""));
        assertEquals("", NewEntityDialog.formatSnakeCaseLabel("_"));
        assertEquals("Product Entity", NewEntityDialog.formatSnakeCaseLabel("_product__entity_"));
    }

    public void testManagementIdentifierIsUniquePerEntity() {
        final String bookId = NewEntityDialog.formatManagementIdentifier("Foo_Bar", "book");
        final String authorId = NewEntityDialog.formatManagementIdentifier("Foo_Bar", "author");

        assertEquals("Foo_Bar::book_management", bookId);
        assertEquals("Foo_Bar::author_management", authorId);
        assertFalse(bookId.equals(authorId));
    }

    public void testManagementIdentifierIsValidXmlIdentifier() {
        assertTrue(NewEntityDialog.formatManagementIdentifier("Foo_Bar", "product_entity")
                .matches(RegExUtil.Magento.XML_IDENTIFIER));
    }
}
