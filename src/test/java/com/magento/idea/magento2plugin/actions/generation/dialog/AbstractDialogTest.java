/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.nio.file.Paths;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;

public class AbstractDialogTest extends BasePlatformTestCase {

    @Override
    public void setUp() throws Exception {
        VfsRootAccess.allowRootAccess(
                getTestRootDisposable(),
                toSystemIndependentPath("."),
                PathManager.getHomePath(),
                PathManager.getConfigPath(),
                PathManager.getSystemPath(),
                PathManager.getPluginsPath()
        );
        super.setUp();
    }

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

    private static String toSystemIndependentPath(final String path) {
        return FileUtil.toSystemIndependentName(
                Paths.get(path).toAbsolutePath().normalize().toString()
        );
    }
}
