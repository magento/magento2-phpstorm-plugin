/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleReadmeMdData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleReadmeMdFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleReadmeMdGenerator extends FileGenerator {

    private final ModuleReadmeMdData moduleReadmeMdData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Construct generator.
     *
     * @param moduleReadmeMdData ModuleReadmeFileData
     * @param project Project
     */
    public ModuleReadmeMdGenerator(
            final @NotNull ModuleReadmeMdData moduleReadmeMdData,
            final Project project
    ) {
        super(project);
        this.moduleReadmeMdData = moduleReadmeMdData;
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
        final PsiDirectory moduleDir = resolveModuleRoot(moduleReadmeMdData);

        return fileFromTemplateGenerator.generate(
                new ModuleReadmeMdFile(),
                getAttributes(),
                moduleDir == null ? moduleReadmeMdData.getBaseDir() : moduleDir,
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
        attributes.setProperty("PACKAGE", moduleReadmeMdData.getPackageName());
        attributes.setProperty("MODULE_NAME", moduleReadmeMdData.getModuleName());
    }

    private @Nullable PsiDirectory resolveModuleRoot(final @NotNull ModuleReadmeMdData data) {
        final PsiDirectory packageDir = data.getBaseDir().findSubdirectory(data.getPackageName());

        return packageDir == null ? null : packageDir.findSubdirectory(data.getModuleName());
    }
}
