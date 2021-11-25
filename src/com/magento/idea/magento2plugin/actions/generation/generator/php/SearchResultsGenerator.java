/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.php;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.php.SearchResultsData;
import com.magento.idea.magento2plugin.actions.generation.generator.PhpFileGenerator;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.SearchResultsFile;
import com.magento.idea.magento2plugin.magento.files.SearchResultsInterfaceFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SearchResultsGenerator extends PhpFileGenerator {

    private final SearchResultsData data;

    /**
     * Search results file generator constructor.
     *
     * @param data SearchResultsData
     * @param project Project
     */
    public SearchResultsGenerator(
            final @NotNull SearchResultsData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Search results file generator constructor.
     *
     * @param data SearchResultsData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public SearchResultsGenerator(
            final @NotNull SearchResultsData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new SearchResultsFile(data.getModuleName(), data.getEntityName());
    }

    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final String interfaceClassFqn = new SearchResultsInterfaceFile(
                data.getModuleName(),
                data.getEntityName()
        ).getClassFqn();

        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("ENTITY_NAME", data.getEntityName(), false)
                .append("CLASS_NAME", file.getClassName(), false)
                .append("INTERFACE_NAME", interfaceClassFqn)
                .append(
                        "PARENT_CLASS_NAME",
                        FrameworkLibraryType.SEARCH_RESULT_IMPLEMENTATION.getType()
                );
    }
}
