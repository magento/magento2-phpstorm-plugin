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
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.PhpPreference;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class PreferenceClassGenerator extends FileGenerator {
    private final PreferenceFileData preferenceFileData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;

    /**
     * Constructor.
     */
    public PreferenceClassGenerator(
            @NotNull final PreferenceFileData preferenceFileData,
            final Project project
    ) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.preferenceFileData = preferenceFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }


    @Override
    public PsiFile generate(final String actionName) {
        PhpClass pluginClass = GetPhpClassByFQN.getInstance(project).execute(
                preferenceFileData.getPreferenceFqn()
        );

        if (pluginClass == null) {
            pluginClass = createPluginClass(actionName);
        }

        if (pluginClass == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.cantBeCreated",
                    "Preference Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    commonBundle.message("common.error"),
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        return pluginClass.getContainingFile();
    }

    private PhpClass createPluginClass(final String actionName) {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(getPreferenceModule());

        if (parentDirectory == null) {
            return null;
        }
        final String[] pluginDirectories = preferenceFileData.getPreferenceDirectory()
                .split(File.separator);
        for (final String pluginDirectory: pluginDirectories) {
            parentDirectory = directoryGenerator
                    .findOrCreateSubdirectory(parentDirectory, pluginDirectory);
        }

        final Properties attributes = getAttributes();
        final PsiFile pluginFile = fileFromTemplateGenerator.generate(
                PhpPreference.getInstance(preferenceFileData.getPreferenceClassName()),
                attributes,
                parentDirectory,
                actionName
        );
        if (pluginFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) pluginFile);
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        final String preferenceClassName = preferenceFileData.getPreferenceClassName();
        attributes.setProperty("NAME", preferenceClassName);
        attributes.setProperty("NAMESPACE", preferenceFileData.getNamespace());
        if (preferenceFileData.isInterface()) {
            attributes.setProperty("INTERFACE", "interface");
        }
        if (!preferenceFileData.isInheritClass()) {
            return;
        }
        final String parentClassName = preferenceFileData.getTargetClass().getName();
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
