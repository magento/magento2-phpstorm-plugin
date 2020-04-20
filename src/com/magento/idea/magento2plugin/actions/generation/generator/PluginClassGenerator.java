/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

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
import com.magento.idea.magento2plugin.actions.generation.ImportReferences.PhpClassReferenceResolver;
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData;
import com.magento.idea.magento2plugin.actions.generation.data.code.PluginMethodData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.PluginMethodsGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.actions.generation.util.CollectInsertedMethods;
import com.magento.idea.magento2plugin.actions.generation.util.FillTextBufferWithPluginMethods;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class PluginClassGenerator extends FileGenerator {
    private PluginFileData pluginFileData;
    private Project project;
    private ValidatorBundle validatorBundle;
    private final FillTextBufferWithPluginMethods fillTextBuffer;
    private final CollectInsertedMethods collectInsertedMethods;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;

    public PluginClassGenerator(@NotNull PluginFileData pluginFileData, Project project) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.fillTextBuffer = FillTextBufferWithPluginMethods.getInstance();
        this.collectInsertedMethods = CollectInsertedMethods.getInstance();
        this.pluginFileData = pluginFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
    }

    public PsiFile generate(String actionName)
    {
        final PsiFile[] pluginFile = {null};
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass pluginClass = GetPhpClassByFQN.getInstance(project).execute(pluginFileData.getPluginFqn());
            if (pluginClass == null) {
                pluginClass = createPluginClass(actionName);
            }
            if (pluginClass == null) {
                String errorMessage = validatorBundle.message("validator.file.cantBeCreated", "Plugin Class");
                JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);

                return;
            }

            Key<Object> targetClassKey = Key.create(PluginMethodsGenerator.originalTargetKey);
            Method targetMethod = pluginFileData.getTargetMethod();
            targetMethod.putUserData(targetClassKey, pluginFileData.getTargetClass());
            PluginMethodsGenerator pluginGenerator = new PluginMethodsGenerator(pluginClass, targetMethod, targetClassKey);

            PluginMethodData[] pluginMethodData = pluginGenerator.createPluginMethods(getPluginType());
            if (checkIfMethodExist(pluginClass, pluginMethodData)) {
                String errorMessage = validatorBundle.message("validator.file.alreadyExists", "Plugin Class");
                JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);

                return;
            }

            Set<CharSequence> insertedMethodsNames = new THashSet();
            PhpClassReferenceResolver resolver = new PhpClassReferenceResolver();
            StringBuffer textBuf = new StringBuffer();

            fillTextBuffer.execute(targetClassKey, insertedMethodsNames, resolver, textBuf, pluginMethodData);

            pluginFile[0] = pluginClass.getContainingFile();
            CodeStyleSettings codeStyleSettings = new CodeStyleSettings((PhpFile) pluginFile[0]);
            codeStyleSettings.adjustBeforeWrite();

            int insertPos = getInsertPos(pluginClass);
            PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(pluginClass);

            if (textBuf.length() > 0 && insertPos >= 0) {
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                Document document = psiDocumentManager.getDocument(pluginFile[0]);
                document.insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(pluginFile[0], insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
            if (!insertedMethodsNames.isEmpty()) {
                List<PsiElement> insertedMethods = collectInsertedMethods.execute(pluginFile[0], pluginClass.getNameCS(), insertedMethodsNames);
                if (scope != null && insertedMethods != null) {
                    resolver.importReferences(scope, insertedMethods);
                }
            }
            codeStyleSettings.restore();
        });

        return pluginFile[0];
    }

    private boolean checkIfMethodExist(PhpClass pluginClass, PluginMethodData[] pluginMethodData) {
        Collection<Method> currentPluginMethods = pluginClass.getMethods();
        for (Method currentPluginMethod: currentPluginMethods) {
            for (PluginMethodData pluginMethod: pluginMethodData) {
                if (!pluginMethod.getMethod().getName().equals(currentPluginMethod.getName())){
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private PhpClass createPluginClass(String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project).getModuleDirectoryByModuleName(getPluginModule());
        String[] pluginDirectories = pluginFileData.getPluginDirectory().split("/");
        for (String pluginDirectory: pluginDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, pluginDirectory);
        }

        Properties attributes = getAttributes();
        PsiFile pluginFile = fileFromTemplateGenerator.generate(Plugin.getInstance(pluginFileData.getPluginClassName()), attributes, parentDirectory, actionName);
        if (pluginFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) pluginFile);
    }

    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("NAME", pluginFileData.getPluginClassName());
        attributes.setProperty("NAMESPACE", pluginFileData.getNamespace());
    }

    public Plugin.PluginType getPluginType() {
        return Plugin.getPluginTypeByString(pluginFileData.getPluginType());
    }

    public String getPluginModule() {
        return pluginFileData.getPluginModule();
    }

    private int getInsertPos(PhpClass pluginClass) {
        int insertPos = -1;
        LeafPsiElement[] leafElements =  PsiTreeUtil.getChildrenOfType(pluginClass, LeafPsiElement.class);
        for (LeafPsiElement leafPsiElement: leafElements) {
            if (!leafPsiElement.getText().equals(MagentoPhpClass.CLOSING_TAG)) {
                continue;
            }
            insertPos = leafPsiElement.getTextOffset();
        }
        return insertPos;
    }
}
