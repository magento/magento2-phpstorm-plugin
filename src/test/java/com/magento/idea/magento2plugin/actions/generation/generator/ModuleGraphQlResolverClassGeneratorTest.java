/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.GraphQlResolverFileData;

public class ModuleGraphQlResolverClassGeneratorTest extends BaseGeneratorTestCase {

    public void testGenerateModuleGraphQlResolverClassFile()
    {
        Project project = myFixture.getProject();
        GraphQlResolverFileData graphQlResolverFileData = new GraphQlResolverFileData(
                "Model",
                "TestResolver",
                "Foo_Bar",
                "Foo\\Bar\\Model\\TestResolver",
                "Foo\\Bar\\Model"
        );
        ModuleGraphQlResolverClassGenerator graphQlResolverClassGenerator = new ModuleGraphQlResolverClassGenerator(
                graphQlResolverFileData,
                project
        );
        PsiFile graphQlResolverFile = graphQlResolverClassGenerator.generate("test");

        String filePath = this.getFixturePath("TestResolver.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model",
                graphQlResolverFile
        );
    }
}
