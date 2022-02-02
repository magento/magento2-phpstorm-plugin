/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.php;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.actions.generation.data.php.WebApiInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.generator.PhpFileGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PreferenceDiXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.WebApiInterfaceFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import com.magento.idea.magento2plugin.util.php.PhpTypeModifierUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    protected void onFileGenerated(final PsiFile generatedFile, final @NotNull String actionName) {
        super.onFileGenerated(generatedFile, actionName);

        if (generatedFile instanceof PhpFile) {
            PhpTypeModifierUtil.addImplementForPhpClass(
                    data.getClassFqn(),
                    file.getClassFqn(),
                    project
            );
            PhpTypeModifierUtil.insertInheritDocCommentForMethods(data.getMethods());

            final PhpClass interfaceClass =
                    GetFirstClassOfFile.getInstance().execute((PhpFile) generatedFile);

            new PreferenceDiXmlGenerator(
                    new PreferenceDiXmFileData(
                            data.getModuleName(),
                            Objects.requireNonNull(interfaceClass).getPresentableFQN(),
                            data.getClassFqn(),
                            Areas.base.toString()
                    ),
                    project
            ).generate(actionName, false);
        }
    }

    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
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

        typesBuilder
                .append("NAMESPACE", file.getNamespace(), false)
                .append("DESCRIPTION", data.getDescription(), false)
                .append("INTERFACE_NAME", data.getName(), false)
                .append("METHODS_DELIMITER", WebApiInterfaceFile.METHODS_DELIMITER, false)
                .append("METHODS", methodsString.toString(), false);

        if (!typeList.isEmpty()) {
            attributes.setProperty(
                    "USES",
                    PhpClassGeneratorUtil.formatUses(typeList)
            );
        }
    }
}
