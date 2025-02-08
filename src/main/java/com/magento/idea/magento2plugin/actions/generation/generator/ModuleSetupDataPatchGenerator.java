/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.ModuleSetupDataPatchData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleSetupDataPatchFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ModuleSetupDataPatchGenerator extends FileGenerator {

    private final ModuleSetupDataPatchData moduleSetupPatchData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Construct generator.
     *
     * @param moduleSetupPatchData ModuleSetupPatchData
     * @param project Project
     */
    public ModuleSetupDataPatchGenerator(
            final @NotNull ModuleSetupDataPatchData moduleSetupPatchData,
            final Project project
    ) {
        super(project);
        this.moduleSetupPatchData = moduleSetupPatchData;
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
    }

    /**
     * Generate file.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        return fileFromTemplateGenerator.generate(
                new ModuleSetupDataPatchFile(moduleSetupPatchData.getClassName()),
                getAttributes(),
                moduleSetupPatchData.getBaseDir(),
                actionName
        );
    }

    /**
     * Fill template properties.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("CLASS_NAME", ModuleSetupDataPatchFile.resolveClassNameFromInput(
                moduleSetupPatchData.getClassName()
        ));
        attributes.setProperty(
                "MODULE_NAME",
                moduleSetupPatchData.getPackageName() + "\\" + moduleSetupPatchData.getModuleName()
        );
    }
}
