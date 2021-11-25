/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.graphqls.fix;

import com.google.common.base.Strings;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.magento.idea.magento2plugin.actions.generation.NewGraphQlResolverAction;
import com.magento.idea.magento2plugin.actions.generation.data.GraphQlResolverFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleGraphQlResolverClassGenerator;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CreateResolverClassQuickFix implements LocalQuickFix {
    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
        return new InspectionBundle().message(
                "inspection.graphql.schema.resolver.fix.family"
        );
    }

    @Override
    public void applyFix(
            @NotNull final Project project,
            @NotNull final ProblemDescriptor descriptor
    ) {
        final String resolverFqn = StringUtil.unquoteString(descriptor.getPsiElement().getText())
                .replace("\\\\", "\\");
        final List<String> fqnPartsList
                = new ArrayList<>(Arrays.asList(resolverFqn.split("\\\\")));

        fqnPartsList.removeIf(Strings::isNullOrEmpty);

        final int endIndex = fqnPartsList.size() - 1;
        final String moduleName = String.join("_", fqnPartsList.subList(0, 2));
        final String resolverName = fqnPartsList.get(endIndex);
        final String directory = fqnPartsList.get(2);
        final String namespace = String.join("\\", fqnPartsList.subList(0, endIndex));

        final GraphQlResolverFileData graphQlResolverFileData = new GraphQlResolverFileData(
                directory,
                resolverName,
                moduleName,
                resolverFqn,
                namespace
        );

        final ModuleGraphQlResolverClassGenerator generator
                = new ModuleGraphQlResolverClassGenerator(graphQlResolverFileData, project);

        generator.generate(NewGraphQlResolverAction.ACTION_NAME, true);
    }
}
