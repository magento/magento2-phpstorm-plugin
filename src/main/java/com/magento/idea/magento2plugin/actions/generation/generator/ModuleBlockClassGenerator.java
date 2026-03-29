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
import com.magento.idea.magento2plugin.actions.generation.data.BlockFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.BlockPhp;
import com.magento.idea.magento2plugin.magento.files.PhpPreference;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class ModuleBlockClassGenerator extends FileGenerator {
    private final BlockFileData blockFileData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final boolean interactive;

    /**
     * Constructor.
     *
     * @param blockFileData BlockFileData
     * @param project Project
     */
    public ModuleBlockClassGenerator(
            final @NotNull BlockFileData blockFileData,
            final Project project
    ) {
        this(blockFileData, project, true);
    }

    /**
     * Constructor.
     *
     * @param blockFileData BlockFileData
     * @param project Project
     * @param interactive whether UI error reporting should be shown
     */
    public ModuleBlockClassGenerator(
            final @NotNull BlockFileData blockFileData,
            final Project project,
            final boolean interactive
    ) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.blockFileData = blockFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.interactive = interactive;
    }

    /**
     * Generate a Block File.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final String errorTitle = commonBundle.message("common.error");
        final PhpClass block = GetPhpClassByFQN.getInstance(project).execute(getBlockFqn());

        if (block != null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.alreadyExists",
                    "Block Class"
            );
            showError(errorMessage, errorTitle);

            return null;
        }

        final PhpFile blockFile = createBlockClass(actionName);
        if (blockFile == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.cantBeCreated",
                    "Block Class"
            );
            showError(errorMessage, errorTitle);

            return null;
        }

        return blockFile;
    }

    @NotNull
    private String getBlockFqn() {
        return blockFileData.getNamespace()
                + Package.fqnSeparator
                + blockFileData.getBlockClassName();
    }

    private PhpFile createBlockClass(final String actionName) {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(getBlockModule());

        if (parentDirectory == null) {
            return null;
        }
        final String[] blockDirectories = blockFileData.getBlockDirectory().split(File.separator);

        for (final String blockDirectory: blockDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                parentDirectory,
                blockDirectory
            );
        }

        final Properties attributes = getAttributes();
        final PsiFile blockFile = fileFromTemplateGenerator.generate(
                PhpPreference.getInstance(blockFileData.getBlockClassName()),
                attributes,
                parentDirectory,
                actionName
        );
        if (blockFile == null) {
            return null;
        }
        return (PhpFile) blockFile;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        final String blockClassName = blockFileData.getBlockClassName();
        attributes.setProperty("NAME", blockClassName);
        attributes.setProperty("NAMESPACE", blockFileData.getNamespace());
        if (!BlockPhp.STOREFRONT_BLOCK_NAME.equals(blockClassName)) {
            attributes.setProperty("USE", BlockPhp.STOREFRONT_BLOCK_FQN);
            attributes.setProperty("EXTENDS", BlockPhp.STOREFRONT_BLOCK_NAME);
            return;
        }
        attributes.setProperty("EXTENDS", Package.fqnSeparator + BlockPhp.STOREFRONT_BLOCK_FQN);
    }

    public String getBlockModule() {
        return blockFileData.getBlockModule();
    }

    private void showError(final String errorMessage, final String errorTitle) {
        if (!interactive) {
            return;
        }

        JOptionPane.showMessageDialog(
                null,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE
        );
    }
}
