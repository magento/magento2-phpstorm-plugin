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
import org.jetbrains.annotations.NotNull;

public class PluginClassGeneratorTest extends BaseGeneratorTestCase {

    private static final String TARGET_CLASS_FQN = "Foo\\Bar\\Service\\SimpleService";
    private static final String TARGET_METHOD_NAME = "execute";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String PLUGIN_NAMESPACE = "Foo\\Bar\\Plugin";
    private static final String PLUGIN_FQN = "Foo\\Bar\\Plugin\\TestPlugin";
    private static final String PLUGIN_CLASS_NAME = "TestPlugin";
    private static final String PLUGIN_DIR = "Plugin";

    /**
     * Test of plugin generation.
     */
    public void testGeneratePluginClassFile() {
        PsiFile pluginClassFile;
        addPluginToTargetClass(Plugin.PluginType.before.toString());
        addPluginToTargetClass(Plugin.PluginType.around.toString());
        pluginClassFile = addPluginToTargetClass(Plugin.PluginType.after.toString());

        final String filePath = this.getFixturePath(PLUGIN_CLASS_NAME.concat(".php"));
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Plugin",
                pluginClassFile
        );
    }

    /**
     * Add plugins for the target class.
     *
     * @param pluginType String
     *
     * @return PsiFile
     */
    private PsiFile addPluginToTargetClass(final @NotNull String pluginType) {
        final Project project = myFixture.getProject();
        final PhpClass targetClass = 
                GetPhpClassByFQN.getInstance(project).execute(TARGET_CLASS_FQN);
        final Method targetMethod = targetClass.findMethodByName(TARGET_METHOD_NAME);

        final PluginFileData pluginClass = new PluginFileData(
                PLUGIN_DIR,
                PLUGIN_CLASS_NAME,
                pluginType,
                MODULE_NAME,
                targetClass,
                targetMethod,
                PLUGIN_FQN,
                PLUGIN_NAMESPACE
        );
        final PluginClassGenerator pluginClassGenerator = new PluginClassGenerator(
                pluginClass,
                project
        );

        return pluginClassGenerator.generate("test");
    }
}
