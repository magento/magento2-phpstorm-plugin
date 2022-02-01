/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.graphql;

import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLArguments;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldsDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLInterfaceTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLQuotedString;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.ExcessiveImports"})
public class GraphQlResolverIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY
            = ID.create("com.magento.idea.magento2plugin.stubs.indexes.resolver_usages");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.NPathComplexity"})
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new HashMap<>();

            final GraphQLFile graphQLFile = (GraphQLFile) inputData.getPsiFile();
            final PsiElement[] children = graphQLFile.getChildren();
            for (final PsiElement child : children) {
                if (!(child instanceof GraphQLObjectTypeDefinition)
                        && !(child instanceof GraphQLInterfaceTypeDefinition)) {
                    continue;
                }
                final PsiElement[] objectChildren = child.getChildren();
                for (final PsiElement objectChild : objectChildren) {
                    if (!(objectChild instanceof GraphQLFieldsDefinition)) {
                        continue;
                    }
                    final PsiElement[] fieldsChildren = objectChild.getChildren();
                    for (final PsiElement fieldsChild : fieldsChildren) {
                        if (!(fieldsChild instanceof GraphQLFieldDefinition)) {
                            continue;
                        }
                        final PsiElement[] fieldChildren = fieldsChild.getChildren();
                        for (final PsiElement fieldChild : fieldChildren) {
                            if (!(fieldChild instanceof GraphQLDirective)) {
                                continue;
                            }
                            if (!fieldChild.getText().startsWith("@resolver")) {
                                continue;
                            }

                            final PsiElement[] directiveChildren = fieldChild.getChildren();

                            for (final PsiElement directiveChild : directiveChildren) {
                                if (!(directiveChild instanceof GraphQLArguments)) {
                                    continue;
                                }

                                final PsiElement[] argumentsChildren = directiveChild.getChildren();
                                for (final PsiElement argumentsChild : argumentsChildren) {
                                    if (!(argumentsChild instanceof GraphQLArgument)) {
                                        continue;
                                    }

                                    final PsiElement argumentStringValue =
                                            GraphQlUtil.fetchResolverQuotedStringFromArgument(
                                                    argumentsChild
                                            );
                                    if (argumentStringValue == null) {
                                        continue;
                                    }

                                    String resolverFQN = argumentStringValue.getText();
                                    if (resolverFQN == null) {
                                        return null;
                                    }

                                    resolverFQN = GraphQlUtil.resolverStringToPhpFQN(resolverFQN);

                                    map.put(resolverFQN, null);
                                }
                            }
                        }
                    }
                }
            }

            return map;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType() == GraphQLFileType.INSTANCE
                && "schema.graphqls".equals(virtualFile.getName());
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    /**
     * Get GraphQL Usages.
     *
     * @param phpClass PhpClass
     *
     * @return List[GraphQLQuotedString]
     */
    public static List<GraphQLQuotedString> getGraphQLUsages(final @NotNull PhpClass phpClass) {
        final List<GraphQLQuotedString> quotedStrings = new ArrayList<>();

        final String classFqn = phpClass.getFQN();
        final Collection<VirtualFile> containingFiles = FileBasedIndex
                .getInstance().getContainingFiles(
                        KEY,
                        classFqn,
                        GlobalSearchScope.allScope(phpClass.getProject())
                );

        final PsiManager psiManager = PsiManager.getInstance(phpClass.getProject());
        for (final VirtualFile virtualFile : containingFiles) {
            final PsiFile fileCandidate = psiManager.findFile(virtualFile);

            if (!(fileCandidate instanceof GraphQLFile)) {
                continue;
            }
            final GraphQLFile file = (GraphQLFile) fileCandidate;
            final PsiElement[] children = file.getChildren();
            findMatchingQuotedString(children, classFqn, quotedStrings);
        }
        return quotedStrings;
    }

    private static PsiElement findMatchingQuotedString(
            final PsiElement[] psiElements,
            final String classFqn,
            final List<GraphQLQuotedString> quotedStrings
    ) {
        for (final PsiElement element: psiElements) {
            if (!(element instanceof GraphQLArgument) && element.getChildren().length == 0) {
                continue;
            }
            if (!(element instanceof GraphQLArgument) && element.getChildren().length != 0) {
                findMatchingQuotedString(element.getChildren(), classFqn, quotedStrings);
                continue;
            }

            final PsiElement argumentStringValue =
                    GraphQlUtil.fetchResolverQuotedStringFromArgument(element);
            if (argumentStringValue == null) {
                continue;
            }

            String resolverFQN = argumentStringValue.getText();
            if (resolverFQN == null) {
                return null;
            }

            resolverFQN = GraphQlUtil.resolverStringToPhpFQN(resolverFQN);

            if (!resolverFQN.equals(classFqn)) {
                continue;
            }

            quotedStrings.add((GraphQLQuotedString) argumentStringValue.getFirstChild());
        }
        return null;
    }
}
