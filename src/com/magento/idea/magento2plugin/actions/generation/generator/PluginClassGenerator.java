/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData;
import com.magento.idea.magento2plugin.actions.generation.data.code.PluginMethodData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.PluginMethodsGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.references.PhpClassReferenceResolver;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.actions.generation.util.CollectInsertedMethods;
import com.magento.idea.magento2plugin.actions.generation.util.FillTextBufferWithPluginMethods;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import gnu.trove.THashSet;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.ExcessiveImports"})
public class PluginClassGenerator extends FileGenerator {

    private final PluginFileData pluginFileData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final FillTextBufferWithPluginMethods fillTextBuffer;
    private final CollectInsertedMethods collectInsertedMethods;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;

    /**
     * Plugin class generator.
     *
     * @param pluginFileData PluginFileData
     * @param project Project
     */
    public PluginClassGenerator(
            final @NotNull PluginFileData pluginFileData,
            final Project project
    ) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.fillTextBuffer = new FillTextBufferWithPluginMethods();
        this.collectInsertedMethods = CollectInsertedMethods.getInstance();
        this.pluginFileData = pluginFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Generate plugin.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.ExcessiveMethodLength"})
    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile[] pluginFile = {null};
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass pluginClass = GetPhpClassByFQN.getInstance(project)
                    .execute(pluginFileData.getPluginFqn());
            final String errorTitle = commonBundle.message("common.error");
            if (pluginClass == null) {
                pluginClass = createPluginClass(actionName);
            }
            if (pluginClass == null) {
                String errorMessage;

                if (fileFromTemplateGenerator.getLastExceptionMessage() == null) {
                    errorMessage = validatorBundle.message(
                            "validator.file.cantBeCreated",
                            "Plugin Class"
                    );
                } else {
                    errorMessage = validatorBundle.message(
                            "validator.file.cantBeCreatedWithException",
                            "Plugin Class",
                            fileFromTemplateGenerator.getLastExceptionMessage()
                    );
                }
                ApplicationManager.getApplication().invokeLater(
                        () -> JOptionPane.showMessageDialog(
                                null,
                                errorMessage,
                                errorTitle,
                                JOptionPane.ERROR_MESSAGE
                        )
                );

                return;
            }

            final Key<Object> targetClassKey = Key.create(PluginMethodsGenerator.originalTargetKey);
            final Method targetMethod = pluginFileData.getTargetMethod();
            targetMethod.putUserData(targetClassKey, pluginFileData.getTargetClass());
            final PluginMethodsGenerator pluginGenerator
                    = new PluginMethodsGenerator(
                            pluginClass,
                            targetMethod,
                            targetClassKey
            );

            final PluginMethodData[] pluginMethodData
                    = pluginGenerator.createPluginMethods(getPluginType());
            if (checkIfMethodExist(pluginClass, pluginMethodData)) {
                final String errorMessage =
                        validatorBundle.message(
                                "validator.file.alreadyExists",
                                "Plugin Class"
                        );
                ApplicationManager.getApplication().invokeLater(
                        () -> JOptionPane.showMessageDialog(
                                null,
                                errorMessage,
                                errorTitle,
                                JOptionPane.ERROR_MESSAGE
                        )
                );

                return;
            }

            final Set<CharSequence> insertedMethodsNames = new THashSet();
            final PhpClassReferenceResolver resolver = new PhpClassReferenceResolver();
            final StringBuffer textBuf = new StringBuffer();

            fillTextBuffer.execute(
                    targetClassKey,
                    insertedMethodsNames,
                    resolver,
                    textBuf,
                    pluginMethodData
            );

            pluginFile[0] = pluginClass.getContainingFile();
            final CodeStyleSettings codeStyleSettings
                    = new CodeStyleSettings((PhpFile) pluginFile[0]);
            codeStyleSettings.adjustBeforeWrite();

            final int insertPos = getInsertPos(pluginClass);
            final PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(pluginClass);

            if (textBuf.length() > 0 && insertPos >= 0) {
                final PsiDocumentManager psiDocumentManager
                        = PsiDocumentManager.getInstance(project);
                final Document document = psiDocumentManager.getDocument(pluginFile[0]);
                document.insertString(insertPos, textBuf);
                final int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project)
                        .reformatText(pluginFile[0], insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
            if (!insertedMethodsNames.isEmpty()) {
                final List<PsiElement> insertedMethods
                        = collectInsertedMethods.execute(
                                pluginFile[0],
                                pluginClass.getNameCS(),
                                insertedMethodsNames
                );
                if (scope != null && insertedMethods != null) {
                    resolver.importReferences(scope, insertedMethods);
                }
            }
            codeStyleSettings.restore();
        });

        return pluginFile[0];
    }

    private boolean checkIfMethodExist(
            final PhpClass pluginClass,
            final PluginMethodData... pluginMethodData
    ) {
        final Collection<Method> currentPluginMethods = pluginClass.getMethods();
        for (final Method currentPluginMethod: currentPluginMethods) {
            for (final PluginMethodData pluginMethod: pluginMethodData) {
                if (pluginMethod.getMethod().getName().equals(currentPluginMethod.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private PhpClass createPluginClass(final String actionName) {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(getPluginModule());

        if (parentDirectory == null) {
            return null;
        }
        final String[] pluginDirectories = pluginFileData.getPluginDirectory()
                .split(File.separator);
        for (final String pluginDirectory: pluginDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    pluginDirectory
            );
        }

        final Properties attributes = getAttributes();
        final PsiFile pluginFile = fileFromTemplateGenerator.generate(
                new Plugin(pluginFileData.getPluginClassName()),
                attributes,
                parentDirectory,
                actionName
        );
        if (pluginFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) pluginFile);
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAME", pluginFileData.getPluginClassName());
        attributes.setProperty("NAMESPACE", pluginFileData.getNamespace());
    }

    public Plugin.PluginType getPluginType() {
        return Plugin.getPluginTypeByString(pluginFileData.getPluginType());
    }

    public String getPluginModule() {
        return pluginFileData.getPluginModule();
    }

    /**
     * Get methods insert position.
     *
     * @param pluginClass PhpClass
     *
     * @return int
     */
    private int getInsertPos(final PhpClass pluginClass) {
        int insertPos = -1;

        final LeafPsiElement[] leafElements = PsiTreeUtil.getChildrenOfType(
                pluginClass,
                LeafPsiElement.class
        );

        if (leafElements == null) {
            return insertPos;
        }

        for (final LeafPsiElement leafPsiElement: leafElements) {
            if (!MagentoPhpClass.CLOSING_TAG.equals(leafPsiElement.getText())) {
                continue;
            }
            insertPos = leafPsiElement.getTextOffset();
        }

        return insertPos == -1 ? insertPos : insertPos - 1;
    }
}
