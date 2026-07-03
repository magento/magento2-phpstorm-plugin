/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;

public class AbstractDialogTest extends BasePlatformTestCase {

    public void testOkActionDoesNotWrapHandlerInWriteAction() {
        final TestDialog dialog = new TestDialog();

        dialog.doOKAction();

        assertTrue(dialog.wasOkHandlerCalled());
        assertFalse(dialog.wasWriteAccessAllowedInOkHandler());
    }

    private static final class TestDialog extends AbstractDialog {

        private boolean okHandlerCalled;
        private boolean writeAccessAllowedInOkHandler;

        private TestDialog() {
            super();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            return new JPanel();
        }

        @Override
        protected boolean validateFormFields() {
            return true;
        }

        @Override
        protected void onWriteActionOK() {
            okHandlerCalled = true;
            writeAccessAllowedInOkHandler =
                    ApplicationManager.getApplication().isWriteAccessAllowed();
        }

        private boolean wasOkHandlerCalled() {
            return okHandlerCalled;
        }

        private boolean wasWriteAccessAllowedInOkHandler() {
            return writeAccessAllowedInOkHandler;
        }
    }
}
