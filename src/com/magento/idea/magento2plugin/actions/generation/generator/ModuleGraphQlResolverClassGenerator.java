/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.GraphQlResolverFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolverPhp;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Properties;

public class ModuleGraphQlResolverClassGenerator extends FileGenerator {
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private GraphQlResolverFileData graphQlResolverFileData;
    private Project project;
    private ValidatorBundle validatorBundle;

    public ModuleGraphQlResolverClassGenerator(@NotNull GraphQlResolverFileData graphQlResolverFileData, Project project) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.graphQlResolverFileData = graphQlResolverFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
    }

    @Override
    public PsiFile generate(String actionName) {
        final PsiFile[] graphQlFile = {null};
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass graphQlResolverClass = GetPhpClassByFQN.getInstance(project).execute(graphQlResolverFileData.getGraphQlResolverClassFqn());

            if (graphQlResolverClass == null) {
                graphQlResolverClass = createGraphQlResolverClass(actionName);
            }

            if (graphQlResolverClass == null) {
                String errorMessage = validatorBundle.message("validator.file.cantBeCreated", "GraphQL Resolver Class");
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Properties attributes = new Properties();
            String methodTemplate = PhpCodeUtil.getCodeTemplate(
                    GraphQlResolverPhp.GRAPHQL_RESOLVER_TEMPLATE_NAME, attributes, project);

            graphQlFile[0] = graphQlResolverClass.getContainingFile();
            CodeStyleSettings codeStyleSettings = new CodeStyleSettings((PhpFile) graphQlFile[0]);
            codeStyleSettings.adjustBeforeWrite();

            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            Document document = psiDocumentManager.getDocument(graphQlFile[0]);
            int insertPos = getInsertPos(graphQlResolverClass);
            document.insertString(insertPos, methodTemplate);
            int endPos = insertPos + methodTemplate.length() + 1;
            CodeStyleManager.getInstance(project).reformatText(graphQlFile[0], insertPos, endPos);
            psiDocumentManager.commitDocument(document);
            codeStyleSettings.restore();
        });
        return graphQlFile[0];
    }

    private int getInsertPos(PhpClass graphQlResolverClass) {
        int insertPos = -1;
        LeafPsiElement[] leafElements = PsiTreeUtil.getChildrenOfType(graphQlResolverClass, LeafPsiElement.class);
        for (LeafPsiElement leafPsiElement : leafElements) {
            if (!leafPsiElement.getText().equals(MagentoPhpClass.CLOSING_TAG)) {
                continue;
            }
            insertPos = leafPsiElement.getTextOffset();
        }
        return insertPos;
    }

    private PhpClass createGraphQlResolverClass(String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(graphQlResolverFileData.getGraphQlResolverModule());
        String[] graphQlResolverDirectories = graphQlResolverFileData.getGraphQlResolverDirectory().split("/");
        for (String graphQlResolverDirectory : graphQlResolverDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, graphQlResolverDirectory);
        }

        Properties attributes = getAttributes();
        PsiFile graphQlResolverFile = fileFromTemplateGenerator.generate(
                GraphQlResolverPhp.getInstance(graphQlResolverFileData.getGraphQlResolverClassName()),
                attributes,
                parentDirectory,
                actionName
        );
        if (graphQlResolverFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) graphQlResolverFile);
    }

    protected void fillAttributes(Properties attributes) {
        String graphQlResolverClassName = graphQlResolverFileData.getGraphQlResolverClassName();
        attributes.setProperty("NAME", graphQlResolverClassName);
        attributes.setProperty("NAMESPACE", graphQlResolverFileData.getNamespace());
    }
}
