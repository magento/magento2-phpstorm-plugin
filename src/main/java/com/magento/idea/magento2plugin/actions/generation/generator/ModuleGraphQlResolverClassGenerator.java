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
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolverPhp;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class ModuleGraphQlResolverClassGenerator extends FileGenerator {

    private final GraphQlResolverFileData graphQlResolverFileData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;

    /**
     * Construct generator.
     *
     * @param graphQlResolverFileData GraphQlResolverFileData
     * @param project Project
     */
    public ModuleGraphQlResolverClassGenerator(
            final @NotNull GraphQlResolverFileData graphQlResolverFileData,
            final Project project
    ) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.graphQlResolverFileData = graphQlResolverFileData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    @Override
    public PsiFile generate(final String actionName) {

        final PhpClass[] graphQlResolverClass = {GetPhpClassByFQN.getInstance(project)
                .execute(graphQlResolverFileData.getGraphQlResolverClassFqn())};

        if (graphQlResolverClass[0] == null) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                graphQlResolverClass[0] = createGraphQlResolverClass(actionName);
            });
        }

        if (graphQlResolverClass[0] == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.cantBeCreated",
                    "GraphQL Resolver Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    commonBundle.message("common.error"),
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        final PsiFile[] graphQlFile = {null};
        WriteCommandAction.runWriteCommandAction(project, () -> {
            final Properties attributes = new Properties();
            final String methodTemplate = PhpCodeUtil.getCodeTemplate(
                    GraphQlResolverPhp.GRAPHQL_RESOLVER_TEMPLATE_NAME, attributes, project);

            graphQlFile[0] = graphQlResolverClass[0].getContainingFile();
            final CodeStyleSettings codeStyleSettings = new CodeStyleSettings(
                    (PhpFile) graphQlFile[0]
            );
            codeStyleSettings.adjustBeforeWrite();

            final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            final Document document = psiDocumentManager.getDocument(graphQlFile[0]);
            final int insertPos = getInsertPos(graphQlResolverClass[0]);
            document.insertString(insertPos, methodTemplate);
            final int endPos = insertPos + methodTemplate.length() + 1;
            CodeStyleManager.getInstance(project).reformatText(graphQlFile[0], insertPos, endPos);
            psiDocumentManager.commitDocument(document);
            codeStyleSettings.restore();
        });

        return graphQlFile[0];
    }

    private int getInsertPos(final PhpClass graphQlResolverClass) {
        int insertPos = -1;
        final LeafPsiElement[] leafElements = PsiTreeUtil.getChildrenOfType(
                graphQlResolverClass,
                LeafPsiElement.class
        );
        for (final LeafPsiElement leafPsiElement : leafElements) {
            if (!MagentoPhpClass.CLOSING_TAG.equals(leafPsiElement.getText())) {
                continue;
            }
            insertPos = leafPsiElement.getTextOffset();
        }
        return insertPos;
    }

    private PhpClass createGraphQlResolverClass(final String actionName) {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(graphQlResolverFileData.getGraphQlResolverModule());

        if (parentDirectory == null) {
            return null;
        }
        final String graphQlResolverDirectory = graphQlResolverFileData
                .getGraphQlResolverDirectory();

        if (!graphQlResolverDirectory.isBlank()) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectories(
                    parentDirectory,
                    graphQlResolverDirectory
            );
        }
        final Properties attributes = getAttributes();
        final PsiFile graphQlResolverFile = fileFromTemplateGenerator.generate(
                GraphQlResolverPhp.getInstance(
                        graphQlResolverFileData.getGraphQlResolverClassName()
                ),
                attributes,
                parentDirectory,
                actionName
        );
        if (graphQlResolverFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) graphQlResolverFile);
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        final String graphQlResolverClassName
                = graphQlResolverFileData.getGraphQlResolverClassName();
        attributes.setProperty("NAME", graphQlResolverClassName);
        attributes.setProperty("NAMESPACE", graphQlResolverFileData.getNamespace());
    }
}
