/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.php;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.actions.generation.data.php.SearchResultsData;
import com.magento.idea.magento2plugin.actions.generation.generator.PhpFileGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PreferenceDiXmlGenerator;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.SearchResultsFile;
import com.magento.idea.magento2plugin.magento.files.SearchResultsInterfaceFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SearchResultsInterfaceGenerator extends PhpFileGenerator {

    private final SearchResultsData data;
    private PsiFile preferenceFile;

    /**
     * Search results interface file generator constructor.
     *
     * @param data SearchResultsData
     * @param project Project
     */
    public SearchResultsInterfaceGenerator(
            final @NotNull SearchResultsData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Search results interface file generator constructor.
     *
     * @param data SearchResultsData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public SearchResultsInterfaceGenerator(
            final @NotNull SearchResultsData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    /**
     * Get generated/edited preference file.
     *
     * @return PsiFile
     */
    public PsiFile getPreferenceFile() {
        return preferenceFile;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new SearchResultsInterfaceFile(data.getModuleName(), data.getEntityName());
    }

    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("CLASS_NAME", file.getClassName(), false)
                .append("DTO_TYPE", "\\" + data.getDtoType(), false)
                .append("PARENT_INTERFACE", FrameworkLibraryType.SEARCH_RESULT.getType());
    }

    @Override
    protected void onFileGenerated(final PsiFile generatedFile, final @NotNull String actionName) {
        super.onFileGenerated(generatedFile, actionName);

        if (generatedFile instanceof PhpFile) {
            final SearchResultsFile implementationFile =
                    new SearchResultsFile(data.getModuleName(), data.getEntityName());

            preferenceFile = new PreferenceDiXmlGenerator(
                    new PreferenceDiXmFileData(
                            data.getModuleName(),
                            file.getClassFqn(),
                            implementationFile.getClassFqn(),
                            Areas.base.toString()
                    ),
                    project
            ).generate(actionName, false);
        }
    }
}
