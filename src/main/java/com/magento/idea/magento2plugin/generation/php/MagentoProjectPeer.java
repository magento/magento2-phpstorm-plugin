/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.generation.php;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.platform.WebProjectGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.JComponent;

public class MagentoProjectPeer implements ProjectGeneratorPeer<MagentoProjectGeneratorSettings> {
    private final NewModuleForm myForm = new NewModuleForm();

    public MagentoProjectPeer() {
    }

    @NotNull
    public JComponent getComponent() {
        return this.myForm.getContentPane();
    }

    public void buildUI(@NotNull SettingsStep settingsStep) {
        settingsStep.addSettingsComponent(this.myForm.getContentPane());
    }

    @NotNull
    public MagentoProjectGeneratorSettings getSettings() {
        return this.myForm.getSettings();
    }

    @Nullable
    public ValidationInfo validate() {
        return this.myForm.validate();
    }

    public boolean isBackgroundJobRunning() {
        return false;
    }

    @Override
    public void addSettingsListener(@NotNull SettingsListener listener) {
        this.myForm.addSettingsStateListener(listener);
    }

    @Override
    public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener listener) {
    }
}
