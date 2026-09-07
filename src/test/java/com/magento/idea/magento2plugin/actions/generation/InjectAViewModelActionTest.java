/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;

public class InjectAViewModelActionTest extends BaseProjectTestCase {
    private static final String VENDOR_LAYOUT = "vendor/example/module/view/frontend/layout/default.xml";

    public void testAppCodeLayoutIsEnabled() {
        configureLayout("app/code/Foo/Bar/view/frontend/layout/inject_view_model.xml");
        assertActionEnabled(true);
    }

    public void testUnmarkedVendorLayoutIsDisabled() {
        configureLayout(VENDOR_LAYOUT);
        assertActionEnabled(false);
    }

    public void testMarkedVendorLayoutIsEnabledAndUnmarkingDisablesIt() {
        configureLayout(VENDOR_LAYOUT);
        final Settings settings = Settings.getInstance(getProject());
        final String moduleUrl = myFixture.getTempDirFixture().getFile("vendor/example/module").getUrl();
        settings.addMagentoFolder(moduleUrl);
        assertActionEnabled(true);

        settings.removeMagentoFolder(moduleUrl);
        assertActionEnabled(false);
    }

    public void testMarkedLayoutDirectoryIsEnabled() {
        configureLayout(VENDOR_LAYOUT);
        Settings.getInstance(getProject()).addMagentoFolder(
                myFixture.getFile().getContainingDirectory().getVirtualFile().getUrl()
        );
        assertActionEnabled(true);
    }

    public void testDisabledPluginHidesActionInMarkedDirectory() {
        configureLayout(VENDOR_LAYOUT);
        final Settings settings = Settings.getInstance(getProject());
        settings.addMagentoFolder(myFixture.getFile().getContainingDirectory().getVirtualFile().getUrl());
        settings.pluginEnabled = false;
        assertActionEnabled(false);
    }

    private void configureLayout(final String path) {
        Settings.getInstance(getProject()).myMagentoFolders = new ArrayList<>();
        myFixture.addFileToProject(
                path,
                "<page><body><referenceBlock name=\"con<caret>tent\"/></body></page>"
        );
        myFixture.configureFromTempProjectFile(path);
        Settings.getInstance(getProject()).setMagentoPath(myFixture.getTempDirFixture().getFile("").getPath());
    }

    private void assertActionEnabled(final boolean expected) {
        final InjectAViewModelAction action = new InjectAViewModelAction();
        final DataContext context = SimpleDataContext.builder()
                .add(PlatformDataKeys.PROJECT, getProject())
                .add(PlatformDataKeys.PSI_FILE, myFixture.getFile())
                .add(PlatformDataKeys.CARET, myFixture.getEditor().getCaretModel().getPrimaryCaret())
                .build();
        final AnActionEvent event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, context);
        action.update(event);

        assertEquals(expected, event.getPresentation().isEnabled());
        assertEquals(expected, event.getPresentation().isVisible());
    }
}
