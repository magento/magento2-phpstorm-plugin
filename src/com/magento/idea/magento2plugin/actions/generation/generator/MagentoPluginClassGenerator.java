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
import com.magento.idea.magento2plugin.actions.generation.MagentoCreateAPluginAction;
import com.magento.idea.magento2plugin.actions.generation.data.MagentoPluginFileData;
import com.magento.idea.magento2plugin.actions.generation.data.MagentoPluginMethodData;
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
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class MagentoPluginClassGenerator {
    private MagentoPluginFileData pluginFileData;
    private Project project;
    private final FillTextBufferWithPluginMethods fillTextBuffer;
    private final CollectInsertedMethods collectInsertedMethods;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;

    public MagentoPluginClassGenerator(@NotNull MagentoPluginFileData pluginFileData, Project project) {
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.fillTextBuffer = FillTextBufferWithPluginMethods.getInstance();
        this.collectInsertedMethods = CollectInsertedMethods.getInstance();
        this.pluginFileData = pluginFileData;
        this.project = project;
    }

    public void generate()
    {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass pluginClass = GetPhpClassByFQN.getInstance(project).execute(pluginFileData.getPluginFqn());
            if (pluginClass == null) {
                pluginClass = createPluginClass();
            }
            if (pluginClass == null) {
                JOptionPane.showMessageDialog(null, "Plugin Class cant be created!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Key<Object> targetClassKey = Key.create(MagentoPluginMethodsGenerator.originalTargetKey);
            Method targetMethod = pluginFileData.getTargetMethod();
            targetMethod.putUserData(targetClassKey, pluginFileData.getTargetClass());
            MagentoPluginMethodsGenerator pluginGenerator = new MagentoPluginMethodsGenerator(pluginClass, targetMethod, targetClassKey);

            MagentoPluginMethodData[] pluginMethodData = pluginGenerator.createPluginMethods(getPluginType());
            if (checkIfMethodExist(pluginClass, pluginMethodData)){
                JOptionPane.showMessageDialog(null, "Plugin method already exist!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Set<CharSequence> insertedMethodsNames = new THashSet();
            PhpClassReferenceResolver resolver = new PhpClassReferenceResolver();
            StringBuffer textBuf = new StringBuffer();

            fillTextBuffer.execute(targetClassKey, insertedMethodsNames, resolver, textBuf, pluginMethodData);

            PsiFile pluginFile = pluginClass.getContainingFile();
            CodeStyleSettings codeStyleSettings = new CodeStyleSettings((PhpFile) pluginFile);
            codeStyleSettings.adjustBeforeWrite();

            int insertPos = getInsertPos(pluginClass);
            PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(pluginClass);

            if (textBuf.length() > 0 && insertPos >= 0) {
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                Document document = psiDocumentManager.getDocument(pluginFile);
                document.insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(pluginFile, insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
            if (!insertedMethodsNames.isEmpty()) {
                List<PsiElement> insertedMethods = collectInsertedMethods.execute(pluginFile, pluginClass.getNameCS(), insertedMethodsNames);
                if (scope != null && insertedMethods != null) {
                    resolver.importReferences(scope, insertedMethods);
                }
            }
            codeStyleSettings.restore();
        });
    }

    private boolean checkIfMethodExist(PhpClass pluginClass, MagentoPluginMethodData[] pluginMethodData) {
        Collection<Method> currentPluginMethods = pluginClass.getMethods();
        for (Method currentPluginMethod: currentPluginMethods) {
            for (MagentoPluginMethodData pluginMethod: pluginMethodData) {
                if (pluginMethod.getMethod().getName().equals(currentPluginMethod.getName())){
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private PhpClass createPluginClass() {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project).getModuleDirectoryByModuleName(getPluginModule());
        String[] pluginDirectories = pluginFileData.getPluginDirectory().split(File.separator);
        for (String pluginDirectory: pluginDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, pluginDirectory);
        }

        Properties attributes = getAttributes();
        PsiFile pluginFile = fileFromTemplateGenerator.generate(Plugin.getInstance(pluginFileData.getPluginClassName()), attributes, parentDirectory, MagentoCreateAPluginAction.ACTION_NAME);
        if (pluginFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) pluginFile);
    }

    private Properties getAttributes() {
        Properties attributes = new Properties();
        this.fillAttributes(attributes);
        return attributes;
    }

    private void fillAttributes(Properties attributes) {
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
