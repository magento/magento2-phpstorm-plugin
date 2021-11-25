/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;

public class PluginClassGeneratorTest extends BaseGeneratorTestCase {
    private static final String targetClassFnq = "Foo\\Bar\\Service\\SimpleService";
    private static final String targetMethodName = "execute";
    private static final String module = "Foo_Bar";
    private static final String pluginNamespace = "Foo\\Bar\\Plugin";
    private static final String pluginFqn = "Foo\\Bar\\Plugin\\TestPlugin";
    private static final String pluginClassName = "TestPlugin";
    private static final String pluginDir = "Plugin";

    public void testGeneratePluginClassFile()
    {
        PsiFile pluginClassFile;
        addPluginToTargetClass(Plugin.PluginType.before.toString());
        addPluginToTargetClass(Plugin.PluginType.around.toString());
        pluginClassFile = addPluginToTargetClass(Plugin.PluginType.after.toString());

        String filePath = this.getFixturePath(pluginClassName.concat(".php"));
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Plugin",
                pluginClassFile
        );
    }

    private PsiFile addPluginToTargetClass(String pluginType)
    {
        Project project = myFixture.getProject();
        PhpClass targetClass = GetPhpClassByFQN.getInstance(project).execute(targetClassFnq);
        Method targetMethod = targetClass.findMethodByName(targetMethodName);

        PluginFileData pluginClass = new PluginFileData(
                pluginDir,
                pluginClassName,
                pluginType,
                module,
                targetClass,
                targetMethod,
                pluginFqn,
                pluginNamespace
        );
        PluginClassGenerator pluginClassGenerator = new PluginClassGenerator(
                pluginClass,
                project
        );

        return pluginClassGenerator.generate("test");
    }
}
