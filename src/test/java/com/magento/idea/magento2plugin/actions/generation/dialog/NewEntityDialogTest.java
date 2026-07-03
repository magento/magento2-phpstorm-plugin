/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import junit.framework.TestCase;

public class NewEntityDialogTest extends TestCase {

    public void testFormatSnakeCaseLabelSkipsEmptyParts() {
        assertEquals("", NewEntityDialog.formatSnakeCaseLabel(""));
        assertEquals("", NewEntityDialog.formatSnakeCaseLabel("_"));
        assertEquals("Product Entity", NewEntityDialog.formatSnakeCaseLabel("_product__entity_"));
    }
}
