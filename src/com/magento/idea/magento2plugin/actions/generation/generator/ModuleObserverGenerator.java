/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.ModuleObserverData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleObserverFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class ModuleObserverGenerator extends FileGenerator {

    private final ModuleObserverData moduleObserverData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Construct generator.
     *
     * @param moduleObserverData ModuleObserverData
     * @param project Project
     */
    public ModuleObserverGenerator(
            final @NotNull ModuleObserverData moduleObserverData,
            final Project project
    ) {
        super(project);
        this.moduleObserverData = moduleObserverData;
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
                new ModuleObserverFile(moduleObserverData.getClassName()),
                getAttributes(),
                moduleObserverData.getBaseDir(),
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
        attributes.setProperty("CLASS_NAME", ModuleObserverFile.resolveClassNameFromInput(
                moduleObserverData.getClassName()
        ));
        attributes.setProperty("NAMESPACE", moduleObserverData.getClassFqn());
        attributes.setProperty("EVENT_NAME", moduleObserverData.getEventName());
    }
}
