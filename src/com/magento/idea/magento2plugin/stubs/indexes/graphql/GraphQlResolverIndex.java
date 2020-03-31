/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes.graphql;

import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import org.jetbrains.annotations.NotNull;
import java.util.*;

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
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> map = new HashMap<>();

            GraphQLFile graphQLFile = (GraphQLFile) inputData.getPsiFile();
            PsiElement[] children = graphQLFile.getChildren();
            for (PsiElement child : children) {
                if (!(child instanceof GraphQLObjectTypeDefinition) && !(child instanceof GraphQLInterfaceTypeDefinition)) {
                    continue;
                }
                PsiElement[] objectChildren = child.getChildren();
                for (PsiElement objectChild : objectChildren) {
                    if (!(objectChild instanceof GraphQLFieldsDefinition)) {
                        continue;
                    }
                    PsiElement[] fieldsChildren = objectChild.getChildren();
                    for (PsiElement fieldsChild : fieldsChildren) {
                        if (!(fieldsChild instanceof GraphQLFieldDefinition)) {
                            continue;
                        }
                        PsiElement[] fieldChildren = fieldsChild.getChildren();
                        for (PsiElement fieldChild : fieldChildren) {
                            if (!(fieldChild instanceof GraphQLDirective)) {
                                continue;
                            }
                            if (!fieldChild.getText().startsWith("@resolver")) {
                                continue;
                            }

                            PsiElement[] directiveChildren = fieldChild.getChildren();

                            for (PsiElement directiveChild : directiveChildren) {
                                if (!(directiveChild instanceof GraphQLArguments)) {
                                    continue;
                                }

                                PsiElement[] argumentsChildren = directiveChild.getChildren();
                                for (PsiElement argumentsChild : argumentsChildren) {
                                    if (!(argumentsChild instanceof GraphQLArgument)) {
                                        continue;
                                    }

                                    PsiElement argumentStringValue = GraphQlUtil.fetchResolverQuotedStringFromArgument(argumentsChild);
                                    if (argumentStringValue == null) continue;

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
        return virtualFile -> (virtualFile.getFileType() == GraphQLFileType.INSTANCE
                && virtualFile.getName().equals("schema.graphqls"));
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }


    public static List<GraphQLQuotedString> getGraphQLUsages(@NotNull PhpClass phpClass) {
        List<GraphQLQuotedString> quotedStrings = new ArrayList<>();

        String classFqn = phpClass.getFQN();
        Collection<VirtualFile> containingFiles = FileBasedIndex
                .getInstance().getContainingFiles(KEY, classFqn, GlobalSearchScope.allScope(phpClass.getProject()));

        PsiManager psiManager = PsiManager.getInstance(phpClass.getProject());
        for (VirtualFile virtualFile : containingFiles) {
            GraphQLFile file = (GraphQLFile) psiManager.findFile(virtualFile);
            if (file == null) {
                continue;
            }
            PsiElement[] children = file.getChildren();
            findMatchingQuotedString(children, classFqn, quotedStrings);
        }
        return quotedStrings;
    }

    private static PsiElement findMatchingQuotedString(PsiElement[] psiElements, String classFqn, List<GraphQLQuotedString> quotedStrings) {
        for (PsiElement element: psiElements) {
            if (!(element instanceof GraphQLArgument) && element.getChildren().length == 0) {
                continue;
            }
            if (!(element instanceof GraphQLArgument) && element.getChildren().length != 0) {
                findMatchingQuotedString(element.getChildren(), classFqn, quotedStrings);
                continue;
            }

            PsiElement argumentStringValue = GraphQlUtil.fetchResolverQuotedStringFromArgument(element);
            if (argumentStringValue == null) continue;

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
