/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.references;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.Statement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import gnu.trove.THashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhpClassReferenceResolver extends PhpClassReferenceExtractor {

    private final PhpClassReferenceStorage myCandidatesToImportStorage =
            new PhpClassReferenceStorage();

    /**
     * Check if provided FQN is not compound.
     *
     * @param scopeHolder PhpPsiElement
     * @param fqn String
     * @param name String
     *
     * @return boolean
     */
    public static boolean isNonCompoundUseName(
            final @NotNull PhpPsiElement scopeHolder,
            final @NotNull String fqn,
            final @NotNull String name
    ) {
        final String currentNamespaceName = scopeHolder instanceof PhpNamespace
                ? ((PhpNamespace)scopeHolder).getFQN()
                : "";

        return "\\".equals(currentNamespaceName)
                && PhpLangUtil.equalsClassNames(fqn, PhpLangUtil.toFQN(name));
    }

    /**
     * Collect references.
     *
     * @param scopeHolder PhpPsiElement
     * @param movedElements movedElements
     */
    public void importReferences(
            final @NotNull PhpPsiElement scopeHolder,
            final @NotNull List<PsiElement> movedElements
    ) {
        final Map<String, String> referencesToReplace = this.importWithConflictResolve(scopeHolder);

        if (referencesToReplace.isEmpty()) {
            return;
        }
        final PhpClassReferenceResolver.PhpClassReferenceRenamer renamer =
                new PhpClassReferenceResolver.PhpClassReferenceRenamer(
                        scopeHolder.getProject(),
                        referencesToReplace
                );
        renamer.processElements(movedElements);
    }

    @Override
    protected void processReference(
            final @NotNull String name,
            final @NotNull String fqn,
            final @NotNull PsiElement identifier
    ) {
        if (!PhpType.isPrimitiveType(name)) {
            this.myCandidatesToImportStorage.processReference(name, fqn);
        }
    }

    private static @Nullable String alreadyImported(
            final @NotNull PhpPsiElement scopeHolder,
            final @NotNull Map<String, String> aliases,
            final @NotNull String fqn,
            final @NotNull String name
    ) {
        final boolean isSameNamespace = PhpCodeInsightUtil.isSameNamespace(scopeHolder, fqn);

        if (isSameNamespace && PhpLangUtil.equalsClassNames(PhpLangUtil.toShortName(fqn), name)) {
            return name;
        }
        final boolean isNonCompound = isNonCompoundUseName(scopeHolder, fqn, name);

        if (isNonCompound) {
            return name;
        }
        final Iterator<Entry<String, String>> entryIterator = aliases.entrySet().iterator();

        Entry<String, String> alias;

        do {
            if (!entryIterator.hasNext()) {
                return null;
            }

            alias = entryIterator.next();
        } while (!PhpLangUtil.equalsClassNames(alias.getValue(), fqn));

        return alias.getKey();
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ConfusingTernary"})
    private Map<String, String> importWithConflictResolve(
            final @NotNull PhpPsiElement scopeHolder
    ) {
        final Map<String, String> aliases = PhpCodeInsightUtil.getAliasesInScope(scopeHolder);
        final Map<String, String> referencesToReplace = new THashMap<>();
        final boolean autoImport = PhpCodeInsightUtil.isAutoImportEnabled(scopeHolder);

        for (final String name : this.myCandidatesToImportStorage.getNames()) {
            final String originalFqn = this.myCandidatesToImportStorage.getFqnByName(name);
            assert originalFqn != null;

            final String alias = alreadyImported(scopeHolder, aliases, originalFqn, name);

            if (alias != null) {
                if (!PhpLangUtil.equalsClassNames(name, alias)) {
                    referencesToReplace.put(name, alias);
                }
            } else if (!autoImport) {
                referencesToReplace.put(name, originalFqn);
            } else {
                final String importedFqn = aliases.get(name);

                if (importedFqn != null
                        && !PhpLangUtil.equalsClassNames(importedFqn, originalFqn)) {
                    final String originalName = PhpLangUtil.toShortName(originalFqn);
                    final String fqnForOriginalName = aliases.get(originalName);
                    if (fqnForOriginalName != null
                            && !PhpLangUtil.equalsClassNames(fqnForOriginalName, originalFqn)) {
                        referencesToReplace.put(name, originalFqn);
                    } else {
                        referencesToReplace.put(name, originalName);
                        if (fqnForOriginalName == null) {
                            insertUseStatement(scopeHolder, originalName, originalFqn);
                        }
                    }
                } else {
                    insertUseStatement(scopeHolder, name, originalFqn);
                }
            }
        }

        return referencesToReplace;
    }

    private static void insertUseStatement(
            final PhpPsiElement scopeHolder,
            final String name,
            final String originalFqn
    ) {
        final String originalName = PhpLangUtil.toShortName(originalFqn);

        if (PhpLangUtil.equalsClassNames(originalName, name)) {
            PhpAliasImporter.insertUseStatement(originalFqn, scopeHolder);
            return;
        }
        PhpAliasImporter.insertUseStatement(originalFqn, name, scopeHolder);
    }

    private static class PhpClassReferenceRenamer extends PhpClassReferenceExtractor {

        private final Project myProject;
        private final Map<String, String> myRefToRename;

        private PhpClassReferenceRenamer(
                final @NotNull Project project,
                final @NotNull Map<String, String> replaceWithFqn
        ) {
            super();
            this.myProject = project;
            this.myRefToRename = replaceWithFqn;
        }

        @SuppressWarnings("PMD.ConfusingTernary")
        @Override
        protected void processReference(
                final @NotNull String name,
                final @NotNull String reference,
                final @NotNull PsiElement identifier
        ) {
            if (this.myRefToRename.containsKey(name)) {
                final String fqn = this.myRefToRename.get(name);
                PsiElement newReference;
                PsiElement oldReference;

                if (!PhpPsiUtil.isOfType(identifier, PhpTokenTypes.IDENTIFIER)
                        && !(identifier instanceof ClassReference)) {

                    newReference = PhpPsiElementFactory.createPhpDocType(this.myProject, fqn);
                    oldReference = PhpPsiUtil.getParentByCondition(
                            identifier,
                            false,
                            PhpDocType.INSTANCEOF,
                            PhpDocComment.INSTANCEOF
                    );
                } else {
                    newReference = PhpPsiElementFactory.createClassReference(this.myProject, fqn);
                    oldReference = PhpPsiUtil.getParentByCondition(
                            identifier,
                            false,
                            ClassReference.INSTANCEOF,
                            Statement.INSTANCEOF
                    );
                }
                assert oldReference != null;

                final PsiElement added = oldReference.addRange(
                        newReference.getFirstChild(),
                        newReference.getLastChild()
                );
                oldReference.deleteChildRange(
                        oldReference.getFirstChild(),
                        added.getPrevSibling()
                );
            }
        }
    }
}
