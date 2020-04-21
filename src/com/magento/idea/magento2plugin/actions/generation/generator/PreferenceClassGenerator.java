/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.PhpPreference;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.util.Properties;

public class PreferenceClassGenerator extends FileGenerator {
    private PreferenceFileData preferenceFileData;
    private Project project;
    private ValidatorBundle validatorBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;

    public PreferenceClassGenerator(@NotNull PreferenceFileData preferenceFileData, Project project) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.preferenceFileData = preferenceFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
    }

    public PsiFile generate(String actionName) {
        PhpClass pluginClass = GetPhpClassByFQN.getInstance(project).execute(preferenceFileData.getPreferenceFqn());

        if (pluginClass == null) {
            pluginClass = createPluginClass(actionName);
        }

        if (pluginClass == null) {
            String errorMessage = validatorBundle.message("validator.file.cantBeCreated", "Preference Class");
            JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);

            return null;
        }

        return pluginClass.getContainingFile();
    }

    private PhpClass createPluginClass(String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project).getModuleDirectoryByModuleName(getPreferenceModule());
        String[] pluginDirectories = preferenceFileData.getPreferenceDirectory().split("/");
        for (String pluginDirectory: pluginDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, pluginDirectory);
        }

        Properties attributes = getAttributes();
        PsiFile pluginFile = fileFromTemplateGenerator.generate(PhpPreference.getInstance(preferenceFileData.getPreferenceClassName()), attributes, parentDirectory, actionName);
        if (pluginFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) pluginFile);
    }

    protected void fillAttributes(Properties attributes) {
        String preferenceClassName = preferenceFileData.getPreferenceClassName();
        attributes.setProperty("NAME", preferenceClassName);
        attributes.setProperty("NAMESPACE", preferenceFileData.getNamespace());
        if (!preferenceFileData.isInheritClass()) {
            return;
        }
        String parentClassName = preferenceFileData.getTargetClass().getName();
        if (!parentClassName.equals(preferenceClassName)) {
            attributes.setProperty("USE", preferenceFileData.getTargetClass().getPresentableFQN());
            attributes.setProperty("EXTENDS", parentClassName);
            return;
        }
        attributes.setProperty("EXTENDS", preferenceFileData.getTargetClass().getFQN());
    }

    public String getPreferenceModule() {
        return preferenceFileData.getPreferenceModule();
    }
}
