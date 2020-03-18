package com.magento.idea.magento2plugin.actions.generation.ImportReferences;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.refactoring.importReferences.PhpClassReferenceExtractor;
import gnu.trove.THashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhpClassReferenceStorage extends PhpClassReferenceExtractor {
    private final Map<String, String> myReferences = new THashMap();

    public PhpClassReferenceStorage() {
    }

    protected void processReference(@NotNull String name, @NotNull String fqn, @NotNull PsiElement identifier) {
        this.myReferences.put(name, fqn);
    }

    @Nullable
    public String getFqnByName(@NotNull String name) {
        return this.myReferences.get(name);
    }

    public Set<String> getNames() {
        return this.myReferences.keySet();
    }
}
