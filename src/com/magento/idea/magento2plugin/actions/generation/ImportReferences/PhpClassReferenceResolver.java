package com.magento.idea.magento2plugin.actions.generation.ImportReferences;

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
    private final PhpClassReferenceStorage myCandidatesToImportStorage = new PhpClassReferenceStorage();

    public PhpClassReferenceResolver() {
    }

    public void processElement(@NotNull PsiElement element) {
        super.processElement(element);
    }

    protected void processReference(@NotNull String name, @NotNull String fqn, @NotNull PsiElement identifier) {
        if (!PhpType.isPrimitiveType(name)) {
            this.myCandidatesToImportStorage.processReference(name, fqn, identifier);
        }
    }

    @Nullable
    private static String alreadyImported(@NotNull PhpPsiElement scopeHolder, @NotNull Map<String, String> aliases, @NotNull String fqn, @NotNull String name) {
        boolean isSameNamespace = PhpCodeInsightUtil.isSameNamespace(scopeHolder, fqn);
        if (isSameNamespace && PhpLangUtil.equalsClassNames(PhpLangUtil.toShortName(fqn), name)) {
            return name;
        }
        boolean isNonCompound = isNonCompoundUseName(scopeHolder, fqn, name);
        if (isNonCompound) {
            return name;
        }
        Iterator entryIterator = aliases.entrySet().iterator();

        Entry alias;
        do {
            if (!entryIterator.hasNext()) {
                return null;
            }

            alias = (Entry) entryIterator.next();
        } while (!PhpLangUtil.equalsClassNames((CharSequence) alias.getValue(), fqn));

        return (String) alias.getKey();
    }

    public static boolean isNonCompoundUseName(@NotNull PhpPsiElement scopeHolder, @NotNull String fqn, @NotNull String name) {
        String currentNamespaceName = scopeHolder instanceof PhpNamespace ? ((PhpNamespace)scopeHolder).getFQN() : "";
        return "\\".equals(currentNamespaceName) && PhpLangUtil.equalsClassNames(fqn, PhpLangUtil.toFQN(name));
    }

    public void importReferences(@NotNull PhpPsiElement scopeHolder, @NotNull List<PsiElement> movedElements) {
        Map<String, String> referencesToReplace = this.importWithConflictResolve(scopeHolder);
        if (referencesToReplace.isEmpty()) {
            return;
        }
        PhpClassReferenceResolver.PhpClassReferenceRenamer renamer = new PhpClassReferenceResolver.PhpClassReferenceRenamer(scopeHolder.getProject(), referencesToReplace);
        renamer.processElements(movedElements);
    }

    private Map<String, String> importWithConflictResolve(PhpPsiElement scopeHolder) {
        Map<String, String> aliases = PhpCodeInsightUtil.getAliasesInScope(scopeHolder);
        Map<String, String> referencesToReplace = new THashMap();
        boolean autoImport = PhpCodeInsightUtil.isAutoImportEnabled(scopeHolder);

        for (String name : this.myCandidatesToImportStorage.getNames()) {
            String originalFqn = this.myCandidatesToImportStorage.getFqnByName(name);
            assert originalFqn != null;

            String alias = alreadyImported(scopeHolder, aliases, originalFqn, name);
            if (alias != null) {
                if (!PhpLangUtil.equalsClassNames(name, alias)) {
                    referencesToReplace.put(name, alias);
                }
            } else if (!autoImport) {
                referencesToReplace.put(name, originalFqn);
            } else {
                String importedFqn = aliases.get(name);
                if (!PhpLangUtil.equalsClassNames(importedFqn, originalFqn)) {
                    if (importedFqn != null) {
                        String originalName = PhpLangUtil.toShortName(originalFqn);
                        String fqnForOriginalName = aliases.get(originalName);
                        if (fqnForOriginalName != null && !PhpLangUtil.equalsClassNames(fqnForOriginalName, originalFqn)) {
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
        }

        return referencesToReplace;
    }

    private static void insertUseStatement(PhpPsiElement scopeHolder, String name, String originalFqn) {
        String originalName = PhpLangUtil.toShortName(originalFqn);
        if (PhpLangUtil.equalsClassNames(originalName, name)) {
            PhpAliasImporter.insertUseStatement(originalFqn, scopeHolder);
            return;
        }
        PhpAliasImporter.insertUseStatement(originalFqn, name, scopeHolder);
    }

    private static class PhpClassReferenceRenamer extends PhpClassReferenceExtractor {
        private final Project myProject;
        private final Map<String, String> myRefToRename;

        private PhpClassReferenceRenamer(Project project, @NotNull Map<String, String> replaceWithFqn) {
            super();
            this.myProject = project;
            this.myRefToRename = replaceWithFqn;
        }

        protected void processReference(@NotNull String name, @NotNull String reference, @NotNull PsiElement identifier) {
            if (this.myRefToRename.containsKey(name)) {
                String fqn = this.myRefToRename.get(name);
                Object newReference;
                PsiElement oldReference;
                if (!PhpPsiUtil.isOfType(identifier, PhpTokenTypes.IDENTIFIER) && !(identifier instanceof ClassReference)) {
                    newReference = PhpPsiElementFactory.createPhpDocType(this.myProject, fqn);
                    oldReference = PhpPsiUtil.getParentByCondition(identifier, false, PhpDocType.INSTANCEOF, PhpDocComment.INSTANCEOF);
                } else {
                    newReference = PhpPsiElementFactory.createClassReference(this.myProject, fqn);
                    oldReference = PhpPsiUtil.getParentByCondition(identifier, false, ClassReference.INSTANCEOF, Statement.INSTANCEOF);
                }

                assert oldReference != null;

                PsiElement added = oldReference.addRange(((PsiElement)newReference).getFirstChild(), ((PsiElement)newReference).getLastChild());
                oldReference.deleteChildRange(oldReference.getFirstChild(), added.getPrevSibling());
            }
        }
    }
}

