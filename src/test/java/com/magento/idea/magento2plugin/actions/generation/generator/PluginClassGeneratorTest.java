/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public void testGeneratePluginsWithNullableStringParameter() {
        assertPluginParameterTypePreserved("?string");
    }

    public void testGeneratePluginsWithScalarUnionParameter() {
        assertPluginParameterTypePreserved("string|int");
    }

    public void testGeneratePluginsWithNullableClassParameter() {
        final PsiFile generated = assertPluginParameterTypePreserved("?SimpleService");
        assertTrue("The nullable class must still be imported",
                generated.getText().contains("use Foo\\Bar\\Service\\SimpleService;"));
    }

    public void testGeneratePluginsWithAssociativeArrayDefault() {
        assertPluginParameterPreserved("array",
                "['first' => ['enabled' => true], 'second' => false, 'filter' => 'a=b']");
    }

    public void testGeneratePluginsWithEqualsInStringDefault() {
        assertPluginParameterPreserved("string", "'left = right'");
    }

    private PsiFile assertPluginParameterTypePreserved(final String parameterType) {
        return assertPluginParameterPreserved(parameterType, null);
    }

    private PsiFile assertPluginParameterPreserved(
            final String parameterType,
            final @Nullable String defaultValue
    ) {
        final String defaultDeclaration = defaultValue == null ? "" : " = " + defaultValue;
        myFixture.addFileToProject("app/code/Foo/Bar/Service/TypedService.php",
                "<?php namespace Foo\\Bar\\Service; class TypedService { "
                        + "public function execute(" + parameterType + " $value"
                        + defaultDeclaration + "): void {} }");
        final String targetFqn = "Foo\\Bar\\Service\\TypedService";
        final PhpClass targetClass = GetPhpClassByFQN.getInstance(getProject()).execute(targetFqn);
        assertNotNull(targetClass);
        final Method targetMethod = targetClass.findMethodByName(TARGET_METHOD_NAME);
        assertNotNull(targetMethod);

        PsiFile generated = null;
        for (final Plugin.PluginType pluginType : Plugin.PluginType.values()) {
            generated = addPluginToTargetClass(pluginType.toString(), targetFqn);
            assertNotNull(generated);
            assertNull("Generated PHP must have no syntax errors",
                    PsiTreeUtil.findChildOfType(generated, PsiErrorElement.class));
            final PhpClass generatedClass = GetFirstClassOfFile.getInstance()
                    .execute((PhpFile) generated);
            assertNotNull(generatedClass);
            final Method generatedMethod = generatedClass.findMethodByName(pluginType + "Execute");
            assertNotNull(generatedMethod);
            final Parameter[] parameters = generatedMethod.getParameters();
            final Parameter value = parameters[parameters.length - 1];
            assertEquals("value", value.getName());
            assertEquals("The parameter type must survive generation and import resolution",
                    targetMethod.getParameters()[0].getDeclaredType().getTypes(),
                    value.getDeclaredType().getTypes());
            if (defaultValue != null) {
                assertNotNull("The parameter default must be preserved", value.getDefaultValue());
                assertTrue("The complete default expression must survive generation and formatting",
                        PsiEquivalenceUtil.areElementsEquivalent(
                                targetMethod.getParameters()[0].getDefaultValue(), value.getDefaultValue()));
            }
        }

        return generated;
    }

    /**
     * Add plugins for the target class.
     *
     * @param pluginType String
     *
     * @return PsiFile
     */
    private PsiFile addPluginToTargetClass(final @NotNull String pluginType) {
        return addPluginToTargetClass(pluginType, TARGET_CLASS_FQN);
    }

    private PsiFile addPluginToTargetClass(
            final @NotNull String pluginType,
            final @NotNull String targetClassFqn
    ) {
        final Project project = myFixture.getProject();
        final PhpClass targetClass = 
                GetPhpClassByFQN.getInstance(project).execute(targetClassFqn);
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
