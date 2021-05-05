/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.php;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.magento.idea.magento2plugin.actions.generation.data.php.WebApiInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.generator.PhpFileGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.WebApiInterfaceFile;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import com.magento.idea.magento2plugin.util.php.PhpTypeModifierUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class WebApiInterfaceGenerator extends PhpFileGenerator {

    private final WebApiInterfaceData data;

    /**
     * Web API Interface generator constructor.
     *
     * @param data WebApiInterfaceData
     * @param project Project
     */
    public WebApiInterfaceGenerator(
            final @NotNull WebApiInterfaceData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Web API Interface generator constructor.
     *
     * @param data WebApiInterfaceData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public WebApiInterfaceGenerator(
            final @NotNull WebApiInterfaceData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new WebApiInterfaceFile(data.getModuleName(), data.getName());
    }

    @Override
    protected void onFileGenerated(final PsiFile generatedFile) {
        super.onFileGenerated(generatedFile);

        if (generatedFile != null) {
            PhpTypeModifierUtil.addImplementForPhpClass(
                    data.getClassFqn(),
                    file.getClassFqn(),
                    project
            );
            PhpTypeModifierUtil.insertInheritDocCommentForMethods(data.getMethods());
        }
    }

    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();
        final StringBuilder methodsString = new StringBuilder();
        final List<String> typeList = new ArrayList<>();

        for (final Method method : data.getMethods()) {
            if (methodsString.length() != 0) {
                methodsString.append(WebApiInterfaceFile.METHODS_DELIMITER);
            }
            methodsString.append(
                    PhpTypeMetadataParserUtil.getMethodDefinitionForInterface(
                            method,
                            WebApiInterfaceFile.DEFAULT_METHOD_DESCRIPTION
                    )
            );
            final List<String> methodTypes =
                    PhpTypeMetadataParserUtil.getMethodDefinitionPhpTypes(method);

            for (final String methodType : methodTypes) {
                if (!typeList.contains(methodType)) {
                    typeList.add(methodType);
                }
            }
        }

        phpClassTypesBuilder
                .appendProperty("NAMESPACE", file.getNamespace())
                .appendProperty("DESCRIPTION", data.getDescription())
                .appendProperty("INTERFACE_NAME", data.getName())
                .appendProperty("METHODS_DELIMITER", WebApiInterfaceFile.METHODS_DELIMITER)
                .appendProperty("METHODS", methodsString.toString())
                .mergeProperties(attributes);

        if (!typeList.isEmpty()) {
            attributes.setProperty(
                    "USES",
                    PhpClassGeneratorUtil.formatUses(typeList)
            );
        }
    }
}
